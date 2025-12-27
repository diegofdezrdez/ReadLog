package com.example.readlog;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.readlog.AdminSQLiteOpenHelper;

public class MainActivity extends AppCompatActivity {

    LinearLayout contenedorLibros;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contenedorLibros = findViewById(R.id.contenedorLibros);

        Button btnNuevo = findViewById(R.id.nuevo); // O R.id.button2 si no lo cambiaste
        btnNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LibroActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLibros();
    }

    private void cargarLibros() {
        contenedorLibros.removeAllViews();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        Cursor fila = db.rawQuery("select id, titulo, autor, descripcion, leido, pagina_actual from libros", null);

        if (fila.moveToFirst()) {
            do {
                // Recuperar datos
                int id = fila.getInt(0);
                String titulo = fila.getString(1);
                String autor = fila.getString(2);
                String desc = fila.getString(3);
                int leido = fila.getInt(4);
                int paginas = fila.getInt(5);

                // Inflar vista
                View bloqueLibro = getLayoutInflater().inflate(R.layout.item_libro, null);

                // Vincular textos
                TextView tvTitulo = bloqueLibro.findViewById(R.id.tvTituloLibro);
                TextView tvAutor = bloqueLibro.findViewById(R.id.tvAutorLibro);
                TextView tvEstado = bloqueLibro.findViewById(R.id.tvEstadoLibro); // <--- NUEVO

                tvTitulo.setText(titulo);
                tvAutor.setText(autor);

                // --- LÓGICA DEL ESTADO ---
                if (leido == 1) {
                    tvEstado.setText("LEÍDO");
                    tvEstado.setTextColor(Color.parseColor("#2E7D32")); // Verde oscuro
                    tvEstado.setBackgroundColor(Color.parseColor("#C8E6C9")); // Verde claro fondo
                } else {
                    if (paginas > 0) {
                        tvEstado.setText("Pág. " + paginas);
                        tvEstado.setTextColor(Color.BLACK);
                        tvEstado.setBackgroundColor(Color.parseColor("#E0E0E0")); // Gris normal
                    } else {
                        tvEstado.setText("Sin empezar");
                        tvEstado.setTextColor(Color.GRAY);
                        tvEstado.setBackgroundColor(Color.parseColor("#F5F5F5")); // Gris muy clarito
                    }
                }

                // Clic para editar
                bloqueLibro.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, LibroActivity.class);
                        intent.putExtra("id", id);
                        intent.putExtra("titulo", titulo);
                        intent.putExtra("autor", autor);
                        intent.putExtra("desc", desc);
                        intent.putExtra("leido", leido);
                        intent.putExtra("paginas", paginas);
                        startActivity(intent);
                    }
                });

                contenedorLibros.addView(bloqueLibro);

            } while (fila.moveToNext());

        } else {
            // Mensaje si está vacío
            TextView tvAviso = new TextView(this);
            tvAviso.setText("No tienes guardado ningún libro");
            tvAviso.setTextSize(18);
            tvAviso.setGravity(android.view.Gravity.CENTER);
            tvAviso.setTextColor(Color.GRAY);
            tvAviso.setPadding(0, 50, 0, 0);
            contenedorLibros.addView(tvAviso);
        }

        db.close();
    }
}