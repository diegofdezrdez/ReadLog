package com.example.readlog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.android.material.snackbar.Snackbar;

public class ConfiguracionActivity extends BaseActivity {

    private RadioGroup rgTema;
    private RadioButton rbTemaClaro, rbTemaOscuro, rbTemaAuto;
    private Spinner spTamanoLetra;
    private Spinner spIdioma;
    private Button btnGuardar, btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        // Vincular vistas
        rgTema = findViewById(R.id.rgTema);
        rbTemaClaro = findViewById(R.id.rbTemaClaro);
        rbTemaOscuro = findViewById(R.id.rbTemaOscuro);
        rbTemaAuto = findViewById(R.id.rbTemaAuto);
        spTamanoLetra = findViewById(R.id.spTamanoLetra);
        spIdioma = findViewById(R.id.spIdioma);
        btnGuardar = findViewById(R.id.btnGuardarConfig);
        btnVolver = findViewById(R.id.btnVolverConfig);

        // Configurar spinner de tamaño de letra
        String[] tamanos = {getString(R.string.config_font_small), getString(R.string.config_font_normal), getString(R.string.config_font_large)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, tamanos);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTamanoLetra.setAdapter(adapter);

        // Configurar spinner de idioma
        String[] idiomas = {getString(R.string.config_language_spanish), getString(R.string.config_language_english)};
        ArrayAdapter<String> adapterIdioma = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, idiomas);
        adapterIdioma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spIdioma.setAdapter(adapterIdioma);

        // Cargar configuración actual
        cargarConfiguracionActual();

        // Botón guardar
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarConfiguracion();
            }
        });

        // Botón volver
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void cargarConfiguracionActual() {
        // Cargar tema
        int tema = prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (tema == AppCompatDelegate.MODE_NIGHT_NO) {
            rbTemaClaro.setChecked(true);
        } else if (tema == AppCompatDelegate.MODE_NIGHT_YES) {
            rbTemaOscuro.setChecked(true);
        } else {
            rbTemaAuto.setChecked(true);
        }

        // Cargar tamaño de letra
        int tamanoLetra = prefs.getInt("tamano_letra", 1); // 0=pequeño, 1=normal, 2=grande
        spTamanoLetra.setSelection(tamanoLetra);

        // Cargar idioma
        String idioma = prefs.getString("idioma", "es"); // 0=español, 1=inglés
        spIdioma.setSelection(idioma.equals("en") ? 1 : 0);
    }

    private void guardarConfiguracion() {
        SharedPreferences.Editor editor = prefs.edit();

        // Guardar valores anteriores para comparar
        String idiomaAnterior = prefs.getString("idioma", "es");

        // Guardar tema
        int temaSeleccionado;
        if (rbTemaClaro.isChecked()) {
            temaSeleccionado = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (rbTemaOscuro.isChecked()) {
            temaSeleccionado = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            temaSeleccionado = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        editor.putInt("tema", temaSeleccionado);

        // Guardar tamaño de letra
        int tamanoLetra = spTamanoLetra.getSelectedItemPosition();
        editor.putInt("tamano_letra", tamanoLetra);

        // Guardar idioma
        String idiomaNuevo = spIdioma.getSelectedItemPosition() == 0 ? "es" : "en";
        editor.putString("idioma", idiomaNuevo);

        editor.apply();

        // Aplicar cambios inmediatamente
        int temaSeleccionado2 = prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(temaSeleccionado2);
        
        // Mostrar Snackbar de confirmación
        Snackbar.make(findViewById(android.R.id.content), R.string.config_saved, Snackbar.LENGTH_LONG).show();
        
        // Si cambió el idioma, reiniciar toda la aplicación
        if (!idiomaAnterior.equals(idiomaNuevo)) {
            findViewById(android.R.id.content).postDelayed(new Runnable() {
                @Override
                public void run() {
                    reiniciarAplicacion();
                }
            }, 1000);
        } else {
            // Solo recrear esta actividad si no cambió el idioma
            findViewById(android.R.id.content).postDelayed(new Runnable() {
                @Override
                public void run() {
                    recreate();
                }
            }, 1000);
        }
    }

    private void reiniciarAplicacion() {
        android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        // Forzar recreación de la actividad con el nuevo idioma
        Runtime.getRuntime().exit(0);
    }

    public static float obtenerTamanoLetra(SharedPreferences prefs) {
        int tamano = prefs.getInt("tamano_letra", 1);
        switch (tamano) {
            case 0: return 0.85f;  // Pequeño
            case 2: return 1.15f;  // Grande
            default: return 1.0f;  // Normal
        }
    }
}
