package com.example.readlog;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class BaseActivity extends AppCompatActivity {

    protected SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Cargar y aplicar configuración ANTES de super.onCreate
        prefs = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        aplicarTema();

        // Habilita el renderizado Edge-to-Edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Lógica para ajustar el color de los iconos de la barra de estado
        handleStatusBarIconsColor();
        
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Vuelve a aplicar el color de los iconos si el tema (día/noche) cambia
        handleStatusBarIconsColor();
    }

    private void handleStatusBarIconsColor() {
        Window window = getWindow();
        WindowInsetsControllerCompat insetsController = new WindowInsetsControllerCompat(window, window.getDecorView());

        // Comprueba si el tema actual es oscuro o claro
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // El tema es oscuro, los iconos deben ser claros
            insetsController.setAppearanceLightStatusBars(false);
        } else {
            // El tema es claro, los iconos deben ser oscuros
            insetsController.setAppearanceLightStatusBars(true);
        }
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
