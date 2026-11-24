package com.itq.seguridadavanzadamovilapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialización de componentes gráficos
        TextView tvEstadoSeguridad = findViewById(R.id.tvEstadoSeguridad);
        ProgressBar pbCargando = findViewById(R.id.pbCargandoSeguridad);
        Button btnProbarHttps = findViewById(R.id.btnProbarHttps);
        RecyclerView rvCredenciales = findViewById(R.id.rvCredencialesSeguras);

        // Configuración de RecyclerView
        rvCredenciales.setLayoutManager(new LinearLayoutManager(this));
        SeguridadAdapter adapter = new SeguridadAdapter();
        rvCredenciales.setAdapter(adapter);

        // Inicialización de ViewModel (Jetpack Lifecycle)
        SeguridadAvanzadaViewModel viewModel = new ViewModelProvider(this).get(SeguridadAvanzadaViewModel.class);

        // Observadores reactivos a LiveData
        viewModel.getCredenciales().observe(this, adapter::setCredenciales);

        viewModel.getCargando().observe(this, cargando -> {
            pbCargando.setVisibility(cargando ? View.VISIBLE : View.GONE);
            btnProbarHttps.setEnabled(!cargando);
        });

        viewModel.getEstadoSeguridad().observe(this, tvEstadoSeguridad::setText);

        // Simulación inicial de cifrado de credenciales de usuario
        viewModel.registrarCredencial("API_GATEWAY_SESSION", "usuario_alumno_itq", "jwt_token_super_secreto_2026");

        // Acción del botón para probar comunicación HTTPS con TLS Pinning
        btnProbarHttps.setOnClickListener(v -> viewModel.conectarHttpsConTLSPinning());
    }
}
