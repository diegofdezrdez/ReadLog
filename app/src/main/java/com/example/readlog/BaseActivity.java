package com.example.readlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {

    protected SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cargar y aplicar configuración ANTES de super.onCreate
        prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        aplicarTema();
        
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(aplicarTamanoLetra(newBase));
    }

    private void aplicarTema() {
        int tema = prefs.getInt("tema", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(tema);
    }

    private Context aplicarTamanoLetra(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE);
        float escala = ConfiguracionActivity.obtenerTamanoLetra(prefs);
        String idioma = prefs.getString("idioma", "es");
        
        // Configurar idioma
        java.util.Locale locale = new java.util.Locale(idioma);
        java.util.Locale.setDefault(locale);
        
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        configuration.fontScale = escala;
        
        return context.createConfigurationContext(configuration);
    }
}
