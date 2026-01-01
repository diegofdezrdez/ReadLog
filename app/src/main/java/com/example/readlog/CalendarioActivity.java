package com.example.readlog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class CalendarioActivity extends BaseActivity {

    private GridLayout gridCalendario;
    private TextView tvMesAno;
    private Button btnAnterior, btnSiguiente, btnVolver;
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
        btnVolver = findViewById(R.id.btnVolverCalendario);

        calendarioActual = Calendar.getInstance();
        diasConActividad = new HashSet<>();

        cargarDiasConActividad();
        actualizarCalendario();

        btnAnterior.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarioActual.add(Calendar.MONTH, -1);
                actualizarCalendario();
            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarioActual.add(Calendar.MONTH, 1);
                actualizarCalendario();
            }
        });

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarDiasConActividad() {
        diasConActividad.clear();
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        SQLiteDatabase db = admin.getReadableDatabase();

        // Obtener fechas de actualización de libros
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

        // Mostrar mes y año
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        tvMesAno.setText(sdf.format(calendarioActual.getTime()));

        // Obtener primer día del mes
        Calendar cal = (Calendar) calendarioActual.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0=domingo
        int diasEnMes = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Encabezados de días de la semana
        String[] diasSemana = {
            getString(R.string.day_sun), getString(R.string.day_mon), getString(R.string.day_tue),
            getString(R.string.day_wed), getString(R.string.day_thu), getString(R.string.day_fri),
            getString(R.string.day_sat)
        };
        for (String dia : diasSemana) {
            TextView tv = new TextView(this);
            tv.setText(dia);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(14);
            tv.setTextColor(Color.GRAY);
            tv.setPadding(8, 8, 8, 8);
            gridCalendario.addView(tv);
        }

        SimpleDateFormat sdfDia = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Espacios vacíos antes del primer día
        for (int i = 0; i < primerDiaSemana; i++) {
            TextView tv = new TextView(this);
            tv.setText("");
            gridCalendario.addView(tv);
        }

        // Días del mes
        for (int dia = 1; dia <= diasEnMes; dia++) {
            cal.set(Calendar.DAY_OF_MONTH, dia);
            String fechaStr = sdfDia.format(cal.getTime());

            TextView tv = new TextView(this);
            tv.setText(String.valueOf(dia));
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            tv.setPadding(16, 16, 16, 16);

            // Verificar si hay actividad ese día
            if (diasConActividad.contains(fechaStr)) {
                tv.setBackgroundColor(Color.parseColor("#4CAF50"));
                tv.setTextColor(Color.WHITE);
                tv.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                tv.setTextColor(Color.BLACK);
            }

            gridCalendario.addView(tv);
        }
    }
}
