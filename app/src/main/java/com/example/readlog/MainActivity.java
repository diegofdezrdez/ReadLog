package com.example.readlog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends BaseActivity {

    LinearLayout contenedorLibros;
    EditText etBusqueda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contenedorLibros = findViewById(R.id.contenedorLibros);
        etBusqueda = findViewById(R.id.etBusqueda);

        // --- BOTÓN CONFIGURACIÓN ---
        ImageButton btnConfig = findViewById(R.id.btnConfiguracion);
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
                startActivity(intent);
            }
        });

        // --- BOTÓN CALENDARIO ---
        ImageButton btnCalendario = findViewById(R.id.btnCalendario);
        btnCalendario.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CalendarioActivity.class);
                startActivity(intent);
            }
        });

        // --- BOTÓN ESTADÍSTICAS ---
        ImageButton btnStats = findViewById(R.id.btnEstadisticas);
        btnStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EstadisticasActivity.class);
                startActivity(intent);
            }
        });

        // Configurar botón Nuevo
        Button btnNuevo = findViewById(R.id.nuevo);
        btnNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LibroActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this, R.string.title_add_book, Toast.LENGTH_SHORT).show();
            }
        });

        // --- EL ESPÍA DEL TECLADO ---
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
        contenedorLibros.removeAllViews();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        // LOGICA DE ORDENACIÓN: 1. Favorito DESC, 2. Estado (pendiente, en_progreso, leido), 3. id DESC
        String orderBy = " ORDER BY favorito DESC, CASE WHEN estado = 'en_progreso' THEN 1 WHEN estado = 'pendiente' THEN 2 ELSE 3 END, id DESC";
        String campos = "id, titulo, autor, notas, leido, pagina_actual, paginas_totales, favorito, estado";

        String query;
        if (busqueda.isEmpty()) {
            query = "SELECT " + campos + " FROM libros" + orderBy;
        } else {
            query = "SELECT " + campos + " FROM libros WHERE titulo LIKE '%" + busqueda + "%' OR autor LIKE '%" + busqueda + "%'" + orderBy;
        }

        Cursor fila = db.rawQuery(query, null);

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

                View bloqueLibro = getLayoutInflater().inflate(R.layout.item_libro, null);

                TextView tvTitulo = bloqueLibro.findViewById(R.id.tvTituloLibro);
                TextView tvAutor = bloqueLibro.findViewById(R.id.tvAutorLibro);
                TextView tvEstado = bloqueLibro.findViewById(R.id.tvEstadoLibro);

                // Añadir estrella si es favorito
                String tituloConEstrella = favorito == 1 ? "⭐ " + titulo : titulo;
                tvTitulo.setText(tituloConEstrella);
                tvAutor.setText(autor);

                // Mostrar estado según el campo estado
                if (estado != null && estado.equals("leido")) {
                    tvEstado.setText(R.string.status_read);
                    tvEstado.setTextColor(getResources().getColor(R.color.stats_read_text, null));
                    tvEstado.setBackgroundColor(getResources().getColor(R.color.stats_read_bg, null));
                } else if (estado != null && estado.equals("en_progreso")) {
                    int porcentaje = 0;
                    if (pagTotales > 0 && pagActual > 0) {
                        porcentaje = (pagActual * 100) / pagTotales;
                    }
                    tvEstado.setText(getString(R.string.status_in_progress, porcentaje));
                    tvEstado.setTextColor(getResources().getColor(R.color.stats_pages_text, null));
                    tvEstado.setBackgroundColor(getResources().getColor(R.color.stats_pages_bg, null));
                } else {
                    tvEstado.setText(R.string.status_pending);
                    tvEstado.setTextColor(getResources().getColor(R.color.stats_pending_text, null));
                    tvEstado.setBackgroundColor(getResources().getColor(R.color.stats_pending_bg, null));
                }

                bloqueLibro.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, LibroActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("titulo", titulo);
                        intent.putExtra("autor", autor);
                        intent.putExtra("notas", notas);
                        intent.putExtra("leido", leido);
                        intent.putExtra("pag_actual", pagActual);
                        intent.putExtra("pag_totales", pagTotales);
                        intent.putExtra("favorito", favorito);
                        intent.putExtra("estado", estado);
                        startActivity(intent);
                    }
                });

                contenedorLibros.addView(bloqueLibro);

            } while (fila.moveToNext());

        } else {
            TextView tvAviso = new TextView(this);
            tvAviso.setText(R.string.no_books_found);
            tvAviso.setTextSize(16);
            tvAviso.setGravity(android.view.Gravity.CENTER);
            tvAviso.setTextColor(getResources().getColor(R.color.text_secondary, null));
            tvAviso.setPadding(0, 50, 0, 0);
            contenedorLibros.addView(tvAviso);
            
            // Toast informativo
            if (!busqueda.isEmpty()) {
                Toast.makeText(this, R.string.no_books_found, Toast.LENGTH_SHORT).show();
            }
        }

        db.close();
    }
}