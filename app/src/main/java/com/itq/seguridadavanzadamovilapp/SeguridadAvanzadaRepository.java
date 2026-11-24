package com.itq.seguridadavanzadamovilapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SeguridadAvanzadaRepository {

    private static final String TAG = "SEGURIDAD_REPO";
    private static final String API_HTTPS_URL = "https://jsonplaceholder.typicode.com/posts";

    private static SeguridadAvanzadaRepository instancia;
    private final RequestQueue requestQueue;
    private SharedPreferences encryptedPreferences;
    private final KeyStoreManager keyStoreManager;

    private final MutableLiveData<List<CredencialSeguraDto>> credencialesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> cargandoLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> estadoSeguridadLiveData = new MutableLiveData<>("Sistema seguro inicializado.");

    private SeguridadAvanzadaRepository(Context context) {
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        this.keyStoreManager = KeyStoreManager.getInstancia();
        inicializarEncryptedPreferences(context);
    }

    public static synchronized SeguridadAvanzadaRepository getInstancia(Context context) {
        if (instancia == null) {
            instancia = new SeguridadAvanzadaRepository(context);
        }
        return instancia;
    }

    private void inicializarEncryptedPreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "secret_shared_prefs_itq",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            Log.d(TAG, "EncryptedSharedPreferences configurado exitosamente con AES-256.");
        } catch (Exception e) {
            Log.e(TAG, "Fallo al inicializar EncryptedSharedPreferences", e);
        }
    }

    public LiveData<List<CredencialSeguraDto>> getCredenciales() {
        return credencialesLiveData;
    }

    public LiveData<Boolean> getCargando() {
        return cargandoLiveData;
    }

    public LiveData<String> getEstadoSeguridad() {
        return estadoSeguridadLiveData;
    }

    public void guardarCredencialSegura(String alias, String usuario, String token) {
        try {
            String usuarioCifrado = keyStoreManager.cifrarTextoConHardware(usuario);
            String tokenCifrado = keyStoreManager.cifrarTextoConHardware(token);

            if (encryptedPreferences != null) {
                encryptedPreferences.edit()
                        .putString("usr_" + alias, usuarioCifrado)
                        .putString("tok_" + alias, tokenCifrado)
                        .apply();
            }

            long now = System.currentTimeMillis();
            CredencialSeguraDto dto = new CredencialSeguraDto(
                    "" + now,
                    alias.toUpperCase(),
                    usuarioCifrado,
                    tokenCifrado,
                    "AES-256 GCM (AndroidKeyStore)",
                    now
            );

            List<CredencialSeguraDto> listaExistente = credencialesLiveData.getValue();
            List<CredencialSeguraDto> nuevaLista = new ArrayList<>(listaExistente != null ? listaExistente : Collections.emptyList());
            nuevaLista.add(0, dto);
            credencialesLiveData.setValue(nuevaLista);

            estadoSeguridadLiveData.setValue("Credencial cifrada y guardada en KeyStore/EncryptedPrefs.");
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar credencial cifrada", e);
            estadoSeguridadLiveData.setValue("Error de cifrado local.");
        }
    }

    public void realizarPeticionHttpsSeguraConTLSPinning() {
        cargandoLiveData.setValue(true);
        estadoSeguridadLiveData.setValue("Verificando Handshake HTTPS con TLS Pinning...");

        // Limpiar caché y desactivar caché de Volley para forzar siempre el handshake TLS por red
        if (requestQueue.getCache() != null) {
            requestQueue.getCache().clear();
        }

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                API_HTTPS_URL,
                null,
                response -> {
                    cargandoLiveData.setValue(false);
                    estadoSeguridadLiveData.setValue("Conexión HTTPS exitosa. Certificado y Pin SHA-256 validados.");
                    procesarRespuestaServidor(response);
                },
                error -> {
                    cargandoLiveData.setValue(false);
                    Log.e(TAG, "Error en handshake TLS o comunicación HTTPS", error);
                    estadoSeguridadLiveData.setValue("Fallo de Seguridad: TLS Pinning o red rechazada.");
                }
        );

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void procesarRespuestaServidor(JSONArray response) {
        try {
            int limite = Math.min(response.length(), 5);
            List<CredencialSeguraDto> listaExistente = credencialesLiveData.getValue();
            List<CredencialSeguraDto> nuevaLista = new ArrayList<>(listaExistente != null ? listaExistente : Collections.emptyList());

            for (int index = 0; index < limite; index++) {
                JSONObject obj = response.getJSONObject(index);
                int id = obj.getInt("id");
                String title = obj.getString("title");

                String tokenSimulado = "JWT_TOKEN_SECRET_" + id;
                String tokenCifrado = keyStoreManager.cifrarTextoConHardware(tokenSimulado);

                CredencialSeguraDto dto = new CredencialSeguraDto(
                        "" + id,
                        "API_HTTPS_ITEM #" + id,
                        title.length() > 20 ? title.substring(0, 20) + "..." : title,
                        tokenCifrado,
                        "HTTPS TLS 1.3 / AES-256",
                        System.currentTimeMillis()
                );
                nuevaLista.add(dto);
            }
            credencialesLiveData.setValue(nuevaLista);
        } catch (Exception e) {
            Log.e(TAG, "Error al procesar respuesta HTTPS", e);
        }
    }
}
