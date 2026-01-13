package com.example.readlog;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirestoreManager {

    private static final String TAG = "FirestoreManager";
    private static final String USERS_COLLECTION = "users";
    private static final String BOOKS_COLLECTION = "libros";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    private final MutableLiveData<List<Libro>> booksLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    // CAMBIO: LiveData para las estadísticas generadas desde Firestore
    private final MutableLiveData<int[]> statsLiveData = new MutableLiveData<>();

    public FirestoreManager() {
        db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        auth = FirebaseAuth.getInstance();
    }

    public LiveData<List<Libro>> getBooksLiveData() {
        return booksLiveData;
    }

    // CAMBIO: Getter para el LiveData de estadísticas
    public LiveData<int[]> getStatsLiveData() {
        return statsLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public void getBooksFromCloud() {
        isLoadingLiveData.setValue(true);
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection(USERS_COLLECTION).document(user.getUid()).collection(BOOKS_COLLECTION)
                .addSnapshotListener((value, e) -> {
                    isLoadingLiveData.setValue(false);
                    if (e != null) {
                        errorLiveData.setValue("Error al cargar libros: " + e.getMessage());
                        Log.w(TAG, "Error al cargar libros.", e);
                        return;
                    }

                    List<Libro> books = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Libro book = doc.toObject(Libro.class);
                            book.setId(doc.getId());
                            books.add(book);
                        }
                    }
                    booksLiveData.setValue(books);

                    // CAMBIO: Calcular y emitir las estadísticas cada vez que la lista de libros cambia
                    statsLiveData.setValue(calculateStatsFromList(books));
                });
        } else {
            isLoadingLiveData.setValue(false);
            errorLiveData.setValue("Usuario no autenticado.");
        }
    }

    // CAMBIO: Nuevo método para calcular estadísticas desde una lista en memoria
    private int[] calculateStatsFromList(List<Libro> libros) {
        if (libros == null) {
            return new int[]{0, 0, 0};
        }

        int totalLibros = libros.size();
        int totalLeidos = 0;
        int totalPaginasLeidas = 0;

        for (Libro libro : libros) {
            if (libro.getLeido() == 1) {
                totalLeidos++;
                totalPaginasLeidas += libro.getPagTotales();
            } else {
                totalPaginasLeidas += libro.getPagActual();
            }
        }
        return new int[]{totalLibros, totalLeidos, totalPaginasLeidas};
    }
    
    public void saveBookToCloud(Libro libro) {
        isLoadingLiveData.setValue(true);
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection(USERS_COLLECTION).document(user.getUid()).collection(BOOKS_COLLECTION).document(libro.getId())
                .set(libro)
                .addOnSuccessListener(aVoid -> {
                    isLoadingLiveData.setValue(false);
                    Log.d(TAG, "Libro guardado con ID: " + libro.getId());
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorLiveData.setValue("Error al guardar el libro: " + e.getMessage());
                    Log.w(TAG, "Error al guardar el libro", e);
                });
        } else {
            isLoadingLiveData.setValue(false);
            errorLiveData.setValue("Usuario no autenticado.");
        }
    }

    public void deleteBookFromCloud(String bookId) {
        isLoadingLiveData.setValue(true);
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            db.collection(USERS_COLLECTION).document(user.getUid()).collection(BOOKS_COLLECTION).document(bookId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isLoadingLiveData.setValue(false);
                    Log.d(TAG, "Libro eliminado con ID: " + bookId);
                })
                .addOnFailureListener(e -> {
                    isLoadingLiveData.setValue(false);
                    errorLiveData.setValue("Error al eliminar el libro: " + e.getMessage());
                    Log.w(TAG, "Error al eliminar el libro", e);
                });
        } else {
            isLoadingLiveData.setValue(false);
            errorLiveData.setValue("Usuario no autenticado.");
        }
    }
}
