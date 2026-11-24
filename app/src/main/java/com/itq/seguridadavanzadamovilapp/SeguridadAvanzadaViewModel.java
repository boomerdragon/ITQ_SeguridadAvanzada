package com.itq.seguridadavanzadamovilapp;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class SeguridadAvanzadaViewModel extends AndroidViewModel {

    private final SeguridadAvanzadaRepository repository;

    public SeguridadAvanzadaViewModel(@NonNull Application application) {
        super(application);
        this.repository = SeguridadAvanzadaRepository.getInstancia(application);
    }

    public LiveData<List<CredencialSeguraDto>> getCredenciales() {
        return repository.getCredenciales();
    }

    public LiveData<Boolean> getCargando() {
        return repository.getCargando();
    }

    public LiveData<String> getEstadoSeguridad() {
        return repository.getEstadoSeguridad();
    }

    public void registrarCredencial(String alias, String usuario, String token) {
        repository.guardarCredencialSegura(alias, usuario, token);
    }

    public void conectarHttpsConTLSPinning() {
        repository.realizarPeticionHttpsSeguraConTLSPinning();
    }
}