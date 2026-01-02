package com.example.readlog;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

public class LibroActivity extends BaseActivity {

    private EditText etNombre, etAutor, etNotas, etPaginaActual, etPaginasTotales;
    private CheckBox cbLeido, cbFavorito;
    private RadioGroup rgEstado;
    private RadioButton rbPendiente, rbEnProgreso, rbLeido;
    private LinearLayout containerPagActual, containerPagTotales;
    private Button btnGuardar, btnVolver, btnEliminar;
    private MaterialToolbar topAppBar;
    private int libroId = -1;
    // Variable para evitar bucles infinitos entre listeners
    private boolean isUpdatingProgrammatically = false;

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
        containerPagTotales = findViewById(R.id.containerPagTotales);

        btnGuardar = findViewById(R.id.libro_guardar);
        btnVolver = findViewById(R.id.btnVolver);
        btnEliminar = findViewById(R.id.btnEliminar);
        topAppBar = findViewById(R.id.topAppBar);

        // --- LÓGICA VISUAL ---
        // Sincronizar checkbox "Leído" con RadioButton "Leído"
        cbLeido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isUpdatingProgrammatically) return;
                
                isUpdatingProgrammatically = true;
                if (isChecked) {
                    actualizarVisibilidadPaginas(false);
                    rbLeido.setChecked(true);
                } else {
                    actualizarVisibilidadPaginas(true);
                    // Si se desmarca leído, volver a pendiente si estaba en leído
                    if (rbLeido.isChecked()) {
                        rbPendiente.setChecked(true);
                    }
                }
                isUpdatingProgrammatically = false;
            }
        });

        // Sincronizar RadioGroup con checkbox "Leído"
        rgEstado.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (isUpdatingProgrammatically) return;
                
                isUpdatingProgrammatically = true;
                if (checkedId == R.id.rbLeido) {
                    cbLeido.setChecked(true);
                    actualizarVisibilidadPaginas(false);
                } else {
                    cbLeido.setChecked(false);
                    actualizarVisibilidadPaginas(true);
                }
                isUpdatingProgrammatically = false;
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
            
            // Usamos la bandera para inicializar sin disparar lógica circular
            isUpdatingProgrammatically = true;
            cbLeido.setChecked(leido == 1);
            
            // Seleccionar estado
            if (estado.equals("leido")) {
                rbLeido.setChecked(true);
                actualizarVisibilidadPaginas(false);
            } else if (estado.equals("en_progreso")) {
                rbEnProgreso.setChecked(true);
                actualizarVisibilidadPaginas(true);
            } else {
                rbPendiente.setChecked(true);
                actualizarVisibilidadPaginas(true);
            }
            isUpdatingProgrammatically = false;

            if(topAppBar != null) topAppBar.setTitle(R.string.title_edit_book);
            btnGuardar.setText(R.string.btn_update);
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            // Modo nuevo libro - seleccionar pendiente por defecto
            rbPendiente.setChecked(true);
            actualizarVisibilidadPaginas(true);
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

    private void actualizarVisibilidadPaginas(boolean mostrarPaginaActual) {
        if (mostrarPaginaActual) {
            containerPagActual.setVisibility(View.VISIBLE);
            
            // Restaurar márgenes originales (8dp en start)
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) containerPagTotales.getLayoutParams();
            float density = getResources().getDisplayMetrics().density;
            params.setMarginStart((int)(8 * density));
            containerPagTotales.setLayoutParams(params);
        } else {
            containerPagActual.setVisibility(View.GONE);
            
            // Quitar margen start para que ocupe todo el ancho correctamente
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) containerPagTotales.getLayoutParams();
            params.setMarginStart(0);
            containerPagTotales.setLayoutParams(params);
        }
    }
    
    // --- LÓGICA PARA OCULTAR TECLADO ---
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_yes_delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(LibroActivity.this);
                        admin.eliminarLibro(libroId);
                        Snackbar.make(findViewById(android.R.id.content), R.string.msg_book_deleted, Snackbar.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
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

        // --- VALIDACIONES DE INTEGRIDAD DE DATOS ---
        
        // 1. Validar campos obligatorios
        if (titulo.isEmpty() || autor.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validar que las páginas totales sean mayor a 0
        if (pagTotales <= 0){
            etPaginasTotales.setError("Debe ser mayor a 0");
            Toast.makeText(this, "El número de páginas total debe ser mayor que 0", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validar que la página actual no sea mayor que las totales (solo si no es 0)
        if (pagActual > pagTotales) {
            etPaginaActual.setError("No puede ser mayor que " + pagTotales);
            Toast.makeText(this, "La página actual no puede ser mayor que el total", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. LÓGICA AUTOMÁTICA: Si pagActual == pagTotales, marcar como LEÍDO automáticamente
        if (pagActual == pagTotales && !isLeido) {
            isLeido = true;
            valorLeido = 1;
            estado = "leido";
            // Nota: No actualizamos la UI (checkboxes) porque estamos saliendo de la actividad
        }

        // 5. LÓGICA AUTOMÁTICA: Si pagActual > 0, marcar como EN PROGRESO automáticamente
        if (pagActual > 0 && !isLeido){
            estado = "en_progreso";
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
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_book_saved, Snackbar.LENGTH_LONG)
                    .setAction(R.string.btn_back, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }).show();
        } else {
            admin.actualizarLibro(libroId, titulo, autor, notas, valorLeido, pagActual, pagTotales, valorFavorito, estado);
            Snackbar.make(findViewById(android.R.id.content), R.string.msg_book_updated, Snackbar.LENGTH_LONG).show();
        }

        db.close();
        finish();
    }
}