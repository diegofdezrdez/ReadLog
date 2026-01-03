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
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

public class LibroActivity extends BaseActivity {

    private EditText etNombre, etAutor, etNotas, etPaginaActual, etPaginasTotales;
    private CheckBox cbLeido, cbFavorito;
    private LinearLayout containerPagActual, containerPagTotales;
    private Button btnGuardar, btnEliminar;
    private MaterialToolbar topAppBar;
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
        etPaginaActual = findViewById(R.id.etPaginaActual);
        etPaginasTotales = findViewById(R.id.etPaginasTotales);
        containerPagActual = findViewById(R.id.containerPagActual);
        containerPagTotales = findViewById(R.id.containerPagTotales);

        btnGuardar = findViewById(R.id.libro_guardar);
        btnEliminar = findViewById(R.id.btnEliminar);
        topAppBar = findViewById(R.id.topAppBar);

        // --- LÓGICA VISUAL ---
        // Sincronizar checkbox "Leído" con visibilidad
        cbLeido.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    actualizarVisibilidadPaginas(false);
                } else {
                    actualizarVisibilidadPaginas(true);
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
            int pagActual = extras.getInt("pag_actual", 0);
            int pagTotales = extras.getInt("pag_totales", 0);

            etPaginasTotales.setText(String.valueOf(pagTotales));
            etPaginaActual.setText(String.valueOf(pagActual));
            
            cbFavorito.setChecked(favorito == 1);
            cbLeido.setChecked(leido == 1);
            
            if (leido == 1) {
                actualizarVisibilidadPaginas(false);
            } else {
                actualizarVisibilidadPaginas(true);
            }

            if(topAppBar != null) topAppBar.setTitle(R.string.title_edit_book);
            btnGuardar.setText(R.string.btn_update);
            btnEliminar.setVisibility(View.VISIBLE);
        } else {
            // Modo nuevo libro
            actualizarVisibilidadPaginas(true);
        }

        // --- BOTONES ---
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCambios();
            }
        });

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
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

        // LÓGICA PARA DEFINIR EL ESTADO
        String estado;
        if (pagActual == 0) {
            estado = "pendiente";
        } else if (pagActual < pagTotales) {
            estado = "en_progreso";
        } else {
            estado = "leido";
            // Si el usuario llega al final, marcamos leído automáticamente
            valorLeido = 1;
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