package com.example.readlog;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class LibroActivity extends BaseActivity {

    private EditText etNombre, etAutor, etNotas, etPaginaActual, etPaginasTotales;
    private CheckBox cbLeido, cbFavorito;
    private LinearLayout containerPagActual, containerPagTotales;
    private Button btnGuardar, btnEliminar;
    private MaterialToolbar topAppBar;
    private TextInputLayout layoutFechaLectura;
    private TextInputEditText etFechaLectura;

    private BookRepository bookRepository;
    private String libroId = null;
    private boolean isEditMode = false;
    private long fechaSeleccionada = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_libro);

        bookRepository = new BookRepository(this);

        bindViews();
        setupListeners();

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getString("id") != null) {
            isEditMode = true;
            libroId = extras.getString("id");
            populateFields(extras);
        } else {
            isEditMode = false;
            actualizarVisibilidadPaginas(true);
            layoutFechaLectura.setVisibility(View.GONE);
        }
    }

    private void guardarCambios() {
        String titulo = etNombre.getText().toString();
        String autor = etAutor.getText().toString();

        if (titulo.isEmpty() || autor.isEmpty()) {
            Toast.makeText(this, R.string.msg_fill_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String idParaGuardar = (libroId == null) ? UUID.randomUUID().toString() : libroId;
        String notas = etNotas.getText().toString();
        boolean isLeido = cbLeido.isChecked();
        int pagTotales = 0;
        String pagTotalesStr = etPaginasTotales.getText().toString();
        if (!pagTotalesStr.isEmpty()) pagTotales = Integer.parseInt(pagTotalesStr);
        if (pagTotales <= 0) {
            etPaginasTotales.setError("Debe ser mayor a 0");
            Toast.makeText(this, "El número de páginas total debe ser mayor que 0", Toast.LENGTH_SHORT).show();
            return;
        }
        int pagActual = 0;
        if (!isLeido) {
            String pagActualStr = etPaginaActual.getText().toString();
            if (!pagActualStr.isEmpty()) pagActual = Integer.parseInt(pagActualStr);
        } else {
            pagActual = pagTotales;
        }
        if (pagActual > pagTotales) {
            etPaginaActual.setError("No puede ser mayor que " + pagTotales);
            Toast.makeText(this, "La página actual no puede ser mayor que el total", Toast.LENGTH_SHORT).show();
            return;
        }
        String estado = (pagActual == 0) ? "pendiente" : (pagActual < pagTotales) ? "en_progreso" : "leido";

        long fechaDeFinalizacion = 0;
        if (isLeido) {
            // Si ya teníamos una fecha (modo edición), se respeta. Si no (o es libro nuevo), se pone la actual.
            fechaDeFinalizacion = (fechaSeleccionada != 0) ? fechaSeleccionada : System.currentTimeMillis();
        }

        Libro libro = new Libro(
                idParaGuardar,
                titulo,
                autor,
                notas,
                isLeido ? 1 : 0,
                pagActual,
                pagTotales,
                cbFavorito.isChecked() ? 1 : 0,
                estado,
                fechaDeFinalizacion
        );

        bookRepository.saveBook(libro);

        Snackbar.make(findViewById(android.R.id.content),
            (libroId == null) ? R.string.msg_book_saved : R.string.msg_book_updated,
            Snackbar.LENGTH_LONG).show();

        finish();
    }

    private void confirmarBorrado() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_yes_delete, (dialog, which) -> {
                    if (libroId != null) {
                        bookRepository.deleteBook(libroId);
                        Snackbar.make(findViewById(android.R.id.content), R.string.msg_book_deleted, Snackbar.LENGTH_LONG).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
    }

    private void bindViews() {
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
        layoutFechaLectura = findViewById(R.id.layoutFechaLectura);
        etFechaLectura = findViewById(R.id.etFechaLectura);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> guardarCambios());
        topAppBar.setNavigationOnClickListener(v -> finish());
        btnEliminar.setOnClickListener(v -> confirmarBorrado());
        setupDatePicker();

        cbLeido.setOnCheckedChangeListener((buttonView, isChecked) -> {
            actualizarVisibilidadPaginas(!isChecked);

            // La lógica de visibilidad de la fecha solo se aplica en modo CREACIÓN.
            if (isEditMode) return;

            if (isChecked) {
                layoutFechaLectura.setAlpha(0f);
                layoutFechaLectura.setVisibility(View.VISIBLE);
                layoutFechaLectura.animate().alpha(1f).setDuration(300).start();
            } else {
                layoutFechaLectura.animate().alpha(0f).setDuration(300).withEndAction(() -> {
                    layoutFechaLectura.setVisibility(View.GONE);
                    etFechaLectura.setText("");
                    fechaSeleccionada = 0;
                }).start();
            }
        });
    }

    private void setupDatePicker() {
        etFechaLectura.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Seleccionar fecha")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                fechaSeleccionada = selection;
                TimeZone timeZoneUTC = TimeZone.getDefault();
                long offset = timeZoneUTC.getOffset(selection);
                Date date = new Date(selection + offset);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etFechaLectura.setText(sdf.format(date));
            });

            datePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        });
    }

    private void populateFields(Bundle extras) {
        etNombre.setText(extras.getString("titulo"));
        etAutor.setText(extras.getString("autor"));
        etNotas.setText(extras.getString("notas"));
        etPaginasTotales.setText(String.valueOf(extras.getInt("pag_totales", 0)));
        etPaginaActual.setText(String.valueOf(extras.getInt("pag_actual", 0)));
        cbFavorito.setChecked(extras.getInt("favorito", 0) == 1);

        boolean isLeido = extras.getInt("leido", 0) == 1;
        cbLeido.setChecked(isLeido);
        actualizarVisibilidadPaginas(!isLeido);

        // **CAMBIO CLAVE**: Guardamos el valor de la fecha pero nos aseguramos
        // de que el campo nunca sea visible en modo edición.
        if (isLeido) {
            fechaSeleccionada = extras.getLong("fecha_actualizacion", 0);
        }
        layoutFechaLectura.setVisibility(View.GONE);

        if(topAppBar != null) topAppBar.setTitle(R.string.title_edit_book);
        btnGuardar.setText(R.string.btn_update);
        btnEliminar.setVisibility(View.VISIBLE);
    }

    private void actualizarVisibilidadPaginas(boolean mostrarPaginaActual) {
        containerPagActual.setVisibility(mostrarPaginaActual ? View.VISIBLE : View.GONE);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) containerPagTotales.getLayoutParams();
        float density = getResources().getDisplayMetrics().density;
        params.setMarginStart(mostrarPaginaActual ? (int) (8 * density) : 0);
        containerPagTotales.setLayoutParams(params);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}
