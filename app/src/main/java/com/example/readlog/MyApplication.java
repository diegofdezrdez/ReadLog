package com.example.readlog;

import android.app.Application;
import com.google.android.material.color.DynamicColors;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Habilita los colores dinámicos de Material You si están disponibles
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
