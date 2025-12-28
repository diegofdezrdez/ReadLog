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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LibroActivity extends AppCompatActivity {

    private EditText etNombre, etAutor, etNotas, etPaginaActual, etPaginasTotales;
    private CheckBox cbLeido;
    private LinearLayout containerPagActual; // Nuevo vínculo
    private Button btnGuardar, btnVolver, btnEliminar;
    private TextView tvTitulo;
    private int libroId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro);

        // --- VINCULACIÓN ---
        etNombre = findViewById(R.id.libro_titulo);
        etAutor = findViewById(R.id.libro_autor);
        etNotas = findViewById(R.id.libro_descripcion);
        cbLeido = findViewById(R.id.cbLeido);
        etPaginaActual = findViewById(R.id.etPaginaActual);
        etPaginasTotales = findViewById(R.id.etPaginasTotales);

        // Ahora vinculamos el contenedor específico de la página actual
        containerPagActual = findViewById(R.id.containerPagActual);

        btnGuardar = findViewById(R.id.libro_guardar);
        btnVolver = findViewById(R.id.btnVolver);
        btnEliminar = findViewById(R.id.btnEliminar);

        tvTitulo = findViewById(R.id.tvTituloHeader);

        // --- LÓGICA VISUAL ---
        // Si está leído, ocultamos "Página Actual" pero dejamos "Páginas Totales"
        cbLeido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    containerPagActual.setVisibility(View.GONE);
                } else {
                    containerPagActual.setVisibility(View.VISIBLE);
                }
            }
        });

        // --- MODO EDICIÓN ---
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            libroId = extras.getInt("id");
            etNombre.setText(extras.getString("titulo"));
            etAutor.setText(extras.getString("autor"));
            etNotas.setText(extras.getString("notas"));

            int leido = extras.getInt("leido", 0);
            int pagActual = extras.getInt("pag_actual", 0);
            int pagTotales = extras.getInt("pag_totales", 0);

            // IMPORTANTE: Primero cargamos los datos y luego marcamos el CheckBox
            // para que el Listener actúe sobre la visibilidad correctamente.

            // Cargamos siempre las totales
            etPaginasTotales.setText(String.valueOf(pagTotales));

            // Cargamos la actual solo si procede (aunque se ocultará si activamos leido)
            etPaginaActual.setText(String.valueOf(pagActual));

            // Esto disparará el Listener y ajustará la visibilidad de containerPagActual
            cbLeido.setChecked(leido == 1);

            if(tvTitulo != null) tvTitulo.setText("Editar Libro");
            btnGuardar.setText("ACTUALIZAR");
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

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmarBorrado();
            }
        });
    }

    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar Libro")
                .setMessage("¿Estás seguro de que quieres borrar este libro? No podrás recuperarlo.")
                .setPositiveButton("SÍ, BORRAR", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(LibroActivity.this);
                        admin.eliminarLibro(libroId);
                        Toast.makeText(LibroActivity.this, "Libro eliminado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("CANCELAR", null)
                .show();
    }

    private void guardarCambios() {
        String titulo = etNombre.getText().toString();
        String autor = etAutor.getText().toString();
        String notas = etNotas.getText().toString();

        boolean isLeido = cbLeido.isChecked();
        int valorLeido = isLeido ? 1 : 0;

        int pagActual = 0;
        int pagTotales = 0;

        // 1. Siempre leemos las páginas totales (si el usuario puso algo)
        String pagTotalesStr = etPaginasTotales.getText().toString();
        if (!pagTotalesStr.isEmpty()) {
            pagTotales = Integer.parseInt(pagTotalesStr);
        }

        // 2. Leemos la página actual solo si NO es leído
        if (!isLeido) {
            String pagActualStr = etPaginaActual.getText().toString();
            if (!pagActualStr.isEmpty()) {
                pagActual = Integer.parseInt(pagActualStr);
            }
        } else {
            // (Opcional) Si es leído, asignamos la actual a totales por coherencia interna,
            // aunque visualmente en la lista saldrá "LEÍDO".
            pagActual = pagTotales;
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
        registro.put("notas", notas);
        registro.put("leido", valorLeido);
        registro.put("pagina_actual", pagActual);
        registro.put("paginas_totales", pagTotales);

        if (libroId == -1) {
            db.insert("libros", null, registro);
            Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show();
        } else {
            admin.actualizarLibro(libroId, titulo, autor, notas, valorLeido, pagActual, pagTotales);
            Toast.makeText(this, "Libro actualizado", Toast.LENGTH_SHORT).show();
        }

        db.close();
        finish();
    }
}