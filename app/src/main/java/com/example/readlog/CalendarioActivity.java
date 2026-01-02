package com.example.readlog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.elevation.SurfaceColors;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CalendarioActivity extends BaseActivity {

    private GridLayout gridCalendario;
    private TextView tvMesAno;
    private ImageButton btnAnterior, btnSiguiente;
    private MaterialToolbar topAppBar;
    private Button btnVolver;
    private Calendar calendarioActual;
    private Set<String> diasConActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);
        
        // Aplicar color de superficie para status bar (Corregido)
        int colorSurface = SurfaceColors.SURFACE_2.getColor(this);
        getWindow().setStatusBarColor(colorSurface);

        gridCalendario = findViewById(R.id.gridCalendario);
        tvMesAno = findViewById(R.id.tvMesAno);
        btnAnterior = findViewById(R.id.btnMesAnterior);
        btnSiguiente = findViewById(R.id.btnMesSiguiente);
        topAppBar = findViewById(R.id.topAppBar);
        btnVolver = findViewById(R.id.btnVolverCalendario);

        calendarioActual = Calendar.getInstance();
        diasConActividad = new HashSet<>();

        cargarDiasConActividad();
        actualizarCalendario();

        btnAnterior.setOnClickListener(v -> {
            calendarioActual.add(Calendar.MONTH, -1);
            actualizarCalendario();
        });

        btnSiguiente.setOnClickListener(v -> {
            calendarioActual.add(Calendar.MONTH, 1);
            actualizarCalendario();
        });

        // Eliminar el icono de navegación (X)
        topAppBar.setNavigationIcon(null);
        
        // Listener para el botón volver
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarDiasConActividad() {
        diasConActividad.clear();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        String query = "SELECT fecha_actualizacion FROM libros WHERE fecha_actualizacion IS NOT NULL AND fecha_actualizacion > 0";
        Cursor cursor = db.rawQuery(query, null);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        if (cursor.moveToFirst()) {
            do {
                long timestamp = cursor.getLong(0);
                Date date = new Date(timestamp);
                String fechaStr = sdf.format(date);
                diasConActividad.add(fechaStr);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    private void actualizarCalendario() {
        gridCalendario.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String mesAno = sdf.format(calendarioActual.getTime());
        // Capitalizar primera letra
        mesAno = mesAno.substring(0, 1).toUpperCase() + mesAno.substring(1);
        tvMesAno.setText(mesAno);

        Calendar cal = (Calendar) calendarioActual.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=domingo
        int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        String[] diasSemana = {
            getString(R.string.day_sun), getString(R.string.day_mon), getString(R.string.day_tue),
            getString(R.string.day_wed), getString(R.string.day_thu), getString(R.string.day_fri),
            getString(R.string.day_sat)
        };
        for (String dia : diasSemana) {
            TextView tv = new TextView(this);
            tv.setText(dia);
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_LabelSmall);
            // Usar atributo de color para tema claro/oscuro
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            
            // Usar parámetros de diseño para el grid
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            tv.setLayoutParams(params);
            
            gridCalendario.addView(tv);
        }

        SimpleDateFormat sdfDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < primerDiaSemana; i++) {
            TextView tv = new TextView(this);
            tv.setText("");
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            tv.setLayoutParams(params);
            
            gridCalendario.addView(tv);
        }

        for (int dia = 1; dia <= diasEnMes; dia++) {
            cal.set(Calendar.DAY_OF_MONTH, dia);
            String fechaStr = sdfDia.format(cal.getTime());

            TextView tv = new TextView(this);
            tv.setText(String.valueOf(dia));
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT; // O un tamaño fijo para que sean cuadrados
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8); // Margen entre celdas
            tv.setLayoutParams(params);
            
            // Padding interno para el círculo de fondo
            int padding = 20; // Ajustar según densidad
            tv.setPadding(padding, padding, padding, padding);

            if (diasConActividad.contains(fechaStr)) {
                // Usar drawable circular para el fondo
                tv.setBackgroundResource(R.drawable.circle_background_primary);
                // Texto blanco o color que contraste
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setBackground(null);
                // Color de texto normal según tema
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                tv.setTypeface(null, Typeface.NORMAL);
            }

            gridCalendario.addView(tv);
        }
    }
}