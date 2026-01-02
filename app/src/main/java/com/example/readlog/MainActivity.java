package com.example.readlog;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private RecyclerView recyclerViewLibros;
    private LibroAdapter libroAdapter;
    private EditText etBusqueda;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- VINCULACIÓN ---
        recyclerViewLibros = findViewById(R.id.recyclerViewLibros);
        etBusqueda = findViewById(R.id.etBusqueda);
        topAppBar = findViewById(R.id.topAppBar);

        // Configuración RecyclerView
        recyclerViewLibros.setLayoutManager(new LinearLayoutManager(this));
        libroAdapter = new LibroAdapter(new ArrayList<>(), this, new LibroAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Libro libro) {
                Intent intent = new Intent(MainActivity.this, LibroActivity.class);
                intent.putExtra("id", libro.getId());
                intent.putExtra("titulo", libro.getTitulo());
                intent.putExtra("autor", libro.getAutor());
                intent.putExtra("notas", libro.getNotas());
                intent.putExtra("leido", libro.getLeido());
                intent.putExtra("pag_actual", libro.getPagActual());
                intent.putExtra("pag_totales", libro.getPagTotales());
                intent.putExtra("favorito", libro.getFavorito());
                intent.putExtra("estado", libro.getEstado());
                startActivity(intent);
            }
        });
        recyclerViewLibros.setAdapter(libroAdapter);


        // --- LISTENERS ---
        // Barra de herramientas superior
        topAppBar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
        });

        topAppBar.setOnMenuItemClickListener(new MaterialToolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.btnCalendario) {
                    startActivity(new Intent(MainActivity.this, CalendarioActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.btnEstadisticas) {
                    startActivity(new Intent(MainActivity.this, EstadisticasActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Botón flotante
        ExtendedFloatingActionButton btnNuevo = findViewById(R.id.nuevo);
        btnNuevo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, LibroActivity.class));
            Toast.makeText(MainActivity.this, R.string.title_add_book, Toast.LENGTH_SHORT).show();
        });

        // Buscador
        etBusqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarLibros(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(etBusqueda != null) etBusqueda.setText("");
        cargarLibros("");
    }

    private void cargarLibros(String busqueda) {
        List<Libro> listaLibros = new ArrayList<>();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        String orderBy = " ORDER BY favorito DESC, CASE WHEN estado = 'en_progreso' THEN 1 WHEN estado = 'pendiente' THEN 2 ELSE 3 END, id DESC";
        String campos = "id, titulo, autor, notas, leido, pagina_actual, paginas_totales, favorito, estado";

        String query;
        Cursor fila;
        
        if (busqueda.isEmpty()) {
            query = "SELECT " + campos + " FROM libros" + orderBy;
            fila = db.rawQuery(query, null);
        } else {
            query = "SELECT " + campos + " FROM libros WHERE titulo LIKE ? OR autor LIKE ?" + orderBy;
            String[] args = new String[]{"%" + busqueda + "%", "%" + busqueda + "%"};
            fila = db.rawQuery(query, args);
        }

        if (fila.moveToFirst()) {
            do {
                int id = fila.getInt(0);
                String titulo = fila.getString(1);
                String autor = fila.getString(2);
                String notas = fila.getString(3);
                int leido = fila.getInt(4);
                int pagActual = fila.getInt(5);
                int pagTotales = fila.getInt(6);
                int favorito = fila.getInt(7);
                String estado = fila.getString(8);

                listaLibros.add(new Libro(id, titulo, autor, notas, leido, pagActual, pagTotales, favorito, estado));
            } while (fila.moveToNext());
        }

        fila.close();
        db.close();

        libroAdapter.setLibros(listaLibros);

        if (listaLibros.isEmpty() && !busqueda.isEmpty()) {
            Toast.makeText(this, R.string.no_books_found, Toast.LENGTH_SHORT).show();
        }
    }
}