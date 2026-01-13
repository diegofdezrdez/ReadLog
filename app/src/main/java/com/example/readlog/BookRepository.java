package com.example.readlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.List;

public class BookRepository {

    private static final String TAG = "AUTH_DEBUG";
    private static final String PREFS_NAME = "com.example.readlog.SYNC_PREFS";
    private static final String KEY_INITIAL_SYNC_COMPLETED = "initial_sync_completed";

    private final AdminSQLiteOpenHelper localDb;
    private final FirestoreManager remoteDb;
    private final FirebaseAuth auth;
    private final SharedPreferences syncPreferences;

    public BookRepository(Context context) {
        this.localDb = new AdminSQLiteOpenHelper(context);
        this.remoteDb = new FirestoreManager();
        this.auth = FirebaseAuth.getInstance();
        this.syncPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public LiveData<List<Libro>> getBooks(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "getBooks -> Modo NUBE para usuario: " + user.getUid());
            remoteDb.getBooksFromCloud();
            return remoteDb.getBooksLiveData();
        } else {
            Log.d(TAG, "getBooks -> Modo LOCAL");
            MutableLiveData<List<Libro>> localBooksLiveData = new MutableLiveData<>();
            localBooksLiveData.setValue(localDb.getAllBooks());
            return localBooksLiveData;
        }
    }

    // CAMBIO: Nuevo método para obtener estadísticas de forma híbrida
    public LiveData<int[]> getStatistics(FirebaseUser user) {
        if (user != null) {
            Log.d(TAG, "getStatistics -> Modo NUBE");
            // Devuelve el LiveData que FirestoreManager ya está actualizando en tiempo real
            return remoteDb.getStatsLiveData();
        } else {
            Log.d(TAG, "getStatistics -> Modo LOCAL");
            // Para el modo local, calculamos las estadísticas una sola vez y las devolvemos en un LiveData
            MutableLiveData<int[]> localStats = new MutableLiveData<>();
            localStats.setValue(localDb.obtenerEstadisticas());
            return localStats;
        }
    }

    public void saveBook(Libro libro) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            remoteDb.saveBookToCloud(libro);
        } else {
            localDb.actualizarLibro(libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getNotas(), libro.getLeido(), libro.getPagActual(), libro.getPagTotales(), libro.getFavorito(), libro.getEstado());
        }
    }

    public void deleteBook(String bookId) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            remoteDb.deleteBookFromCloud(bookId);
        } else {
            localDb.eliminarLibro(bookId);
        }
    }

    public void performInitialMigration() {
        FirebaseUser user = auth.getCurrentUser();
        boolean isSyncCompleted = syncPreferences.getBoolean(KEY_INITIAL_SYNC_COMPLETED + "_" + (user != null ? user.getUid() : ""), false);

        if (user == null || isSyncCompleted) {
            return;
        }

        List<Libro> localBooks = localDb.getAllBooks();
        if (localBooks.isEmpty()) {
            setSyncCompleted(user.getUid());
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        for (Libro libro : localBooks) {
            batch.set(db.collection("users").document(user.getUid()).collection("libros").document(libro.getId()), libro);
        }

        batch.commit().addOnSuccessListener(aVoid -> {
            setSyncCompleted(user.getUid());
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error durante la migración a Firestore", e);
        });
    }

    private void setSyncCompleted(String userId) {
        syncPreferences.edit().putBoolean(KEY_INITIAL_SYNC_COMPLETED + "_" + userId, true).apply();
    }
}
