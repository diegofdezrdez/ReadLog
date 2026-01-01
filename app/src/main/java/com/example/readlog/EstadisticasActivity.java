package com.example.readlog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class EstadisticasActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        TextView tvTotal = findViewById(R.id.tvTotalLibros);
        TextView tvLeidos = findViewById(R.id.tvTotalLeidos);
        TextView tvPendientes = findViewById(R.id.tvTotalPendientes);
        TextView tvPaginas = findViewById(R.id.tvPaginasLeidas);
        Button btnVolver = findViewById(R.id.btnCerrarStats);

        // Llamamos al helper para obtener los datos
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this);
        int[] stats = admin.obtenerEstadisticas();

        int totalLibros = stats[0];
        int totalLeidos = stats[1];
        int totalPaginas = stats[2];
        int pendientes = totalLibros - totalLeidos;

        // Ponemos los textos
        tvTotal.setText(String.valueOf(totalLibros));
        tvLeidos.setText(String.valueOf(totalLeidos));
        tvPendientes.setText(String.valueOf(pendientes));
        tvPaginas.setText(String.valueOf(totalPaginas));

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}