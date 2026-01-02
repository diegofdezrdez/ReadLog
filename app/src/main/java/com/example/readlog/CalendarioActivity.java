package com.example.readlog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.appbar.MaterialToolbar;

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
    private Calendar calendarioActual;
    private Set<String> diasConActividad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        gridCalendario = findViewById(R.id.gridCalendario);
        tvMesAno = findViewById(R.id.tvMesAno);
        btnAnterior = findViewById(R.id.btnMesAnterior);
        btnSiguiente = findViewById(R.id.btnMesSiguiente);
        topAppBar = findViewById(R.id.topAppBar);

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

        // Listener para el botón de cerrar/volver de la barra de herramientas
        topAppBar.setNavigationOnClickListener(v -> finish());
        
        // El botón "Volver" de abajo ha sido eliminado de la lógica,
        // ya que ahora la navegación se gestiona desde la barra superior.
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
        tvMesAno.setText(sdf.format(calendarioActual.getTime()));

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
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            tv.setPadding(8, 8, 8, 8);
            gridCalendario.addView(tv);
        }

        SimpleDateFormat sdfDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < primerDiaSemana; i++) {
            TextView tv = new TextView(this);
            tv.setText("");
            gridCalendario.addView(tv);
        }

        for (int dia = 1; dia <= diasEnMes; dia++) {
            cal.set(Calendar.DAY_OF_MONTH, dia);
            String fechaStr = sdfDia.format(cal.getTime());

            TextView tv = new TextView(this);
            tv.setText(String.valueOf(dia));
            tv.setGravity(Gravity.CENTER);
            tv.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            tv.setPadding(16, 16, 16, 16);

            if (diasConActividad.contains(fechaStr)) {
                tv.setBackgroundColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary40));
                tv.setTextColor(ContextCompat.getColor(this, com.google.android.material.R.color.material_dynamic_primary90));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }

            gridCalendario.addView(tv);
        }
    }
}
