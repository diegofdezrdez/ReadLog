package com.example.readlog;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.lifecycle.Observer;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class EstadisticasActivity extends BaseActivity {

    private TextView tvTotal, tvLeidos, tvPendientes, tvPaginas;
    private BookRepository bookRepository;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private AdminSQLiteOpenHelper databaseHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        tvTotal = findViewById(R.id.tvTotalLibros);
        tvLeidos = findViewById(R.id.tvTotalLeidos);
        tvPendientes = findViewById(R.id.tvTotalPendientes);
        tvPaginas = findViewById(R.id.tvPaginasLeidas);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);

        databaseHelper = new AdminSQLiteOpenHelper(this);
        bookRepository = new BookRepository(this);
        mAuth = FirebaseAuth.getInstance();

        setupAuthStateListener();

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupAuthStateListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                loadBookStats(user);
            } else {
                loadStatsFromSQLite();
            }
        };
    }

    private void loadBookStats(FirebaseUser user) {
        bookRepository.getBooks(user).observe(this, new Observer<List<Libro>>() {
            @Override
            public void onChanged(List<Libro> libros) {
                if (libros == null) return;
                int totalLibros = libros.size();
                int totalLeidos = 0;
                int totalPaginas = 0;

                for (Libro libro : libros) {
                    if ("leido".equals(libro.getEstado())) {
                        totalLeidos++;
                    }
                    totalPaginas += libro.getPagActual();
                }
                updateStatsUI(totalLibros, totalLeidos, totalPaginas);
            }
        });
    }

    private void loadStatsFromSQLite() {
        int[] stats = databaseHelper.obtenerEstadisticas();
        updateStatsUI(stats[0], stats[1], stats[2]);
    }

    private void updateStatsUI(int totalLibros, int totalLeidos, int totalPaginas) {
        int pendientes = totalLibros - totalLeidos;
        tvTotal.setText(String.valueOf(totalLibros));
        tvLeidos.setText(String.valueOf(totalLeidos));
        tvPendientes.setText(String.valueOf(pendientes));
        tvPaginas.setText(String.valueOf(totalPaginas));
    }
}