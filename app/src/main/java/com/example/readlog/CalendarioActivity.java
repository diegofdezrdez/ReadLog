package com.example.readlog;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.elevation.SurfaceColors;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CalendarioActivity extends BaseActivity {

    private GridLayout gridCalendario;
    private TextView tvMesAno;
    private MaterialButton btnAnterior, btnSiguiente;
    private MaterialToolbar topAppBar;
    private Calendar calendarioActual;
    private Set<String> diasConActividad;
    private BookRepository bookRepository;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private AdminSQLiteOpenHelper databaseHelper;

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
        databaseHelper = new AdminSQLiteOpenHelper(this);
        bookRepository = new BookRepository(this);
        mAuth = FirebaseAuth.getInstance();

        setupAuthStateListener();

        btnAnterior.setOnClickListener(v -> {
            calendarioActual.add(Calendar.MONTH, -1);
            actualizarCalendario();
        });

        btnSiguiente.setOnClickListener(v -> {
            calendarioActual.add(Calendar.MONTH, 1);
            actualizarCalendario();
        });

        topAppBar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupAuthStateListener() {
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                loadBookDates(user);
            } else {
                loadDatesFromSQLite();
            }
        };
    }

    private void loadBookDates(FirebaseUser user) {
        bookRepository.getBooks(user).observe(this, libros -> {
            diasConActividad.clear();
            if (libros == null) return;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            for (Libro libro : libros) {
                if ("leido".equals(libro.getEstado()) && libro.getFechaActualizacion() > 0) {
                    Date date = new Date(libro.getFechaActualizacion());
                    diasConActividad.add(sdf.format(date));
                }
            }
            actualizarCalendario();
        });
    }

    private void loadDatesFromSQLite() {
        diasConActividad.clear();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        String query = "SELECT fecha_actualizacion FROM libros WHERE fecha_actualizacion IS NOT NULL AND fecha_actualizacion > 0 AND estado = 'leido'";
        Cursor cursor = db.rawQuery(query, null);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        if (cursor.moveToFirst()) {
            do {
                long timestamp = cursor.getLong(0);
                Date date = new Date(timestamp);
                diasConActividad.add(sdf.format(date));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        actualizarCalendario();
    }

    private void actualizarCalendario() {
        gridCalendario.removeAllViews();

        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        String mesAno = sdf.format(calendarioActual.getTime());
        mesAno = mesAno.substring(0, 1).toUpperCase() + mesAno.substring(1);
        tvMesAno.setText(mesAno);

        Calendar cal = (Calendar) calendarioActual.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int primerDiaSemana = cal.get(Calendar.DAY_OF_WEEK) - 1;
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
            TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_LabelSmall);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
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
            FrameLayout container = new FrameLayout(this);
            GridLayout.LayoutParams containerParams = new GridLayout.LayoutParams();
            containerParams.width = 0;
            containerParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
            containerParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            containerParams.setMargins(4, 4, 4, 4);
            container.setLayoutParams(containerParams);

            TextView tv = new TextView(this);
            tv.setText(String.valueOf(dia));
            tv.setGravity(Gravity.CENTER);
            TextViewCompat.setTextAppearance(tv, com.google.android.material.R.style.TextAppearance_Material3_BodyLarge);
            int size = (int) (40 * getResources().getDisplayMetrics().density);
            FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(size, size);
            tvParams.gravity = Gravity.CENTER;
            tv.setLayoutParams(tvParams);

            if (diasConActividad.contains(fechaStr)) {
                tv.setBackgroundResource(R.drawable.circle_background_primary);
                tv.setTextColor(ContextCompat.getColor(this, R.color.white));
                tv.setTypeface(null, Typeface.BOLD);
            } else {
                tv.setBackground(null);
                tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                tv.setTypeface(null, Typeface.NORMAL);
            }

            container.addView(tv);
            gridCalendario.addView(container);
        }
    }
}
