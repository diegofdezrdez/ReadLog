package com.example.readlog;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog; // Necesario para la ventanita de confirmación
import androidx.appcompat.app.AppCompatActivity;

public class LibroActivity extends AppCompatActivity {

    private EditText etNombre, etAutor, etDescripcion, etPaginaActual;
    private CheckBox cbLeido;
    private LinearLayout layoutPaginas;
    private Button btnGuardar, btnVolver, btnEliminar; // <--- Nuevo botón
    private TextView tvTitulo;
    private int libroId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro);

        // --- VINCULACIÓN ---
        etNombre = findViewById(R.id.libro_titulo);
        etAutor = findViewById(R.id.libro_autor);
        etDescripcion = findViewById(R.id.libro_descripcion);
        cbLeido = findViewById(R.id.cbLeido);
        etPaginaActual = findViewById(R.id.etPaginaActual);
        layoutPaginas = findViewById(R.id.layoutPaginas);

        btnGuardar = findViewById(R.id.libro_guardar);
        btnVolver = findViewById(R.id.btnVolver);
        btnEliminar = findViewById(R.id.btnEliminar); // <--- Vinculamos el botón rojo

        tvTitulo = findViewById(R.id.tvTituloHeader);

        // --- LÓGICA VISUAL (Páginas) ---
        cbLeido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    layoutPaginas.setVisibility(View.GONE);
                } else {
                    layoutPaginas.setVisibility(View.VISIBLE);
                }
            }
        });

        // --- MODO EDICIÓN ---
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            libroId = extras.getInt("id");
            etNombre.setText(extras.getString("titulo"));
            etAutor.setText(extras.getString("autor"));
            etDescripcion.setText(extras.getString("desc"));

            int leido = extras.getInt("leido", 0);
            int paginas = extras.getInt("paginas", 0);

            cbLeido.setChecked(leido == 1);
            if (leido == 0) {
                etPaginaActual.setText(String.valueOf(paginas));
            }

            if(tvTitulo != null) tvTitulo.setText("Editar Libro");
            btnGuardar.setText("ACTUALIZAR");

            // ¡IMPORTANTE! Como estamos editando, hacemos visible el botón de borrar
            btnEliminar.setVisibility(View.VISIBLE);
        }

        // --- BOTONES ---
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambios();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // --- BOTÓN ELIMINAR CON CONFIRMACIÓN ---
        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarBorrado();
            }
        });
    }

    // Método para mostrar la ventanita de "¿Estás seguro?"
    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Libro")
                .setMessage("¿Estás seguro de que quieres borrar este libro? No podrás recuperarlo.")
                .setPositiveButton("SÍ, BORRAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Si dice que SÍ, borramos
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(LibroActivity.this);
                        admin.eliminarLibro(libroId);

                        Toast.makeText(LibroActivity.this, "Libro eliminado", Toast.LENGTH_SHORT).show();
                        finish(); // Cerramos la pantalla
                    }
                })
                .setNegativeButton("CANCELAR", null) // Si dice que NO, no hacemos nada
                .show();
    }

    private void guardarCambios() {
        String titulo = etNombre.getText().toString();
        String autor = etAutor.getText().toString();
        String descripcion = etDescripcion.getText().toString();

        boolean isLeido = cbLeido.isChecked();
        int valorLeido = isLeido ? 1 : 0;

        int paginaActual = 0;
        if (!isLeido) {
            String paginaStr = etPaginaActual.getText().toString();
            if (!paginaStr.isEmpty()) {
                paginaActual = Integer.parseInt(paginaStr);
            }
        }

        if (titulo.isEmpty() || autor.isEmpty()) {
            Toast.makeText(this, "Rellena título y autor", Toast.LENGTH_SHORT).show();
            return;
        }

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getWritableDatabase();

        ContentValues registro = new ContentValues();
        registro.put("titulo", titulo);
        registro.put("autor", autor);
        registro.put("descripcion", descripcion);
        registro.put("leido", valorLeido);
        registro.put("pagina_actual", paginaActual);

        if (libroId == -1) {
            db.insert("libros", null, registro);
            Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show();
        } else {
            admin.actualizarLibro(libroId, titulo, autor, descripcion, valorLeido, paginaActual);
            Toast.makeText(this, "Libro actualizado", Toast.LENGTH_SHORT).show();
        }

        db.close();
        finish();
    }
}