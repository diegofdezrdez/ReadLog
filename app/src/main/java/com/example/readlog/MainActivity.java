package com.example.readlog;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Importante
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    LinearLayout contenedorLibros;
    EditText etBusqueda;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contenedorLibros = findViewById(R.id.contenedorLibros);
        etBusqueda = findViewById(R.id.etBusqueda);

        // --- BOTÓN ESTADÍSTICAS (NUEVO) ---
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

        // LOGICA DE ORDENACIÓN: 1. Leido ASC, 2. (pagina_actual > 0) DESC, 3. id DESC
        String orderBy = " ORDER BY leido ASC, (pagina_actual > 0) DESC, id DESC";
        String campos = "id, titulo, autor, notas, leido, pagina_actual, paginas_totales";

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

                View bloqueLibro = getLayoutInflater().inflate(R.layout.item_libro, null);

                TextView tvTitulo = bloqueLibro.findViewById(R.id.tvTituloLibro);
                TextView tvAutor = bloqueLibro.findViewById(R.id.tvAutorLibro);
                TextView tvEstado = bloqueLibro.findViewById(R.id.tvEstadoLibro);

                tvTitulo.setText(titulo);
                tvAutor.setText(autor);

                if (leido == 1) {
                    tvEstado.setText("LEÍDO");
                    tvEstado.setTextColor(Color.parseColor("#2E7D32"));
                    tvEstado.setBackgroundColor(Color.parseColor("#C8E6C9"));
                } else {
                    if (pagActual > 0) {
                        int porcentaje = 0;
                        if (pagTotales > 0) {
                            porcentaje = (pagActual * 100) / pagTotales;
                        }
                        tvEstado.setText("Pág. " + pagActual + " (" + porcentaje + "%)");
                        tvEstado.setTextColor(Color.BLACK);
                        tvEstado.setBackgroundColor(Color.parseColor("#E0E0E0"));
                    } else {
                        tvEstado.setText("Pendiente");
                        tvEstado.setTextColor(Color.GRAY);
                        tvEstado.setBackgroundColor(Color.parseColor("#F5F5F5"));
                    }
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
                        startActivity(intent);
                    }
                });

                contenedorLibros.addView(bloqueLibro);

            } while (fila.moveToNext());

        } else {
            TextView tvAviso = new TextView(this);
            tvAviso.setText("No se encontraron libros.");
            tvAviso.setTextSize(16);
            tvAviso.setGravity(android.view.Gravity.CENTER);
            tvAviso.setTextColor(Color.GRAY);
            tvAviso.setPadding(0, 50, 0, 0);
            contenedorLibros.addView(tvAviso);
        }

        db.close();
    }
}