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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LibroActivity extends BaseActivity {

    private EditText etNombre, etAutor, etNotas, etPaginaActual, etPaginasTotales;
    private CheckBox cbLeido, cbFavorito;
    private RadioGroup rgEstado;
    private RadioButton rbPendiente, rbEnProgreso, rbLeido;
    private LinearLayout containerPagActual;
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
        cbFavorito = findViewById(R.id.cbFavorito);
        rgEstado = findViewById(R.id.rgEstado);
        rbPendiente = findViewById(R.id.rbPendiente);
        rbEnProgreso = findViewById(R.id.rbEnProgreso);
        rbLeido = findViewById(R.id.rbLeido);
        etPaginaActual = findViewById(R.id.etPaginaActual);
        etPaginasTotales = findViewById(R.id.etPaginasTotales);
        containerPagActual = findViewById(R.id.containerPagActual);

        btnGuardar = findViewById(R.id.libro_guardar);
        btnVolver = findViewById(R.id.btnVolver);
        btnEliminar = findViewById(R.id.btnEliminar);
        tvTitulo = findViewById(R.id.tvTituloHeader);

        // --- LÓGICA VISUAL ---
        // Sincronizar checkbox "Leído" con RadioButton "Leído"
        cbLeido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    containerPagActual.setVisibility(View.GONE);
                    rbLeido.setChecked(true);
                } else {
                    containerPagActual.setVisibility(View.VISIBLE);
                }
            }
        });

        // Sincronizar RadioGroup con checkbox "Leído"
        rgEstado.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbLeido) {
                    cbLeido.setChecked(true);
                    containerPagActual.setVisibility(View.GONE);
                } else {
                    cbLeido.setChecked(false);
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
            int favorito = extras.getInt("favorito", 0);
            String estado = extras.getString("estado", "pendiente");
            int pagActual = extras.getInt("pag_actual", 0);
            int pagTotales = extras.getInt("pag_totales", 0);

            etPaginasTotales.setText(String.valueOf(pagTotales));
            etPaginaActual.setText(String.valueOf(pagActual));
            
            cbFavorito.setChecked(favorito == 1);
            cbLeido.setChecked(leido == 1);
            
            // Seleccionar estado
            if (estado.equals("leido")) {
                rbLeido.setChecked(true);
            } else if (estado.equals("en_progreso")) {
                rbEnProgreso.setChecked(true);
            } else {
                rbPendiente.setChecked(true);
            }

            if(tvTitulo != null) tvTitulo.setText("Editar Libro");
            btnGuardar.setText("ACTUALIZAR");
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            // Modo nuevo libro - seleccionar pendiente por defecto
            rbPendiente.setChecked(true);
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
        int valorFavorito = cbFavorito.isChecked() ? 1 : 0;

        // Determinar estado
        String estado = "pendiente";
        if (rbLeido.isChecked()) {
            estado = "leido";
        } else if (rbEnProgreso.isChecked()) {
            estado = "en_progreso";
        }

        int pagActual = 0;
        int pagTotales = 0;

        String pagTotalesStr = etPaginasTotales.getText().toString();
        if (!pagTotalesStr.isEmpty()) {
            pagTotales = Integer.parseInt(pagTotalesStr);
        }

        if (!isLeido) {
            String pagActualStr = etPaginaActual.getText().toString();
            if (!pagActualStr.isEmpty()) {
                pagActual = Integer.parseInt(pagActualStr);
            }
        } else {
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
        registro.put("favorito", valorFavorito);
        registro.put("estado", estado);
        registro.put("fecha_actualizacion", System.currentTimeMillis());

        if (libroId == -1) {
            db.insert("libros", null, registro);
            Toast.makeText(this, "Libro guardado", Toast.LENGTH_SHORT).show();
        } else {
            admin.actualizarLibro(libroId, titulo, autor, notas, valorLeido, pagActual, pagTotales, valorFavorito, estado);
            Toast.makeText(this, "Libro actualizado", Toast.LENGTH_SHORT).show();
        }

        db.close();
        finish();
    }
}