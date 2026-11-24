package com.itq.seguridadavanzadamovilapp;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Administrador de claves criptográficas aisladas en el hardware (TEE / HSM) mediante Android KeyStore System.
 */
public class KeyStoreManager {

    private static final String TAG = "KEYSTORE_MGR";
    private static final String KEYSTORE_PROVIDER = "AndroidKeyStore";
    private static final String ALIAS_LLAVE_MAESTRA = "LlaveMaestraHardwareItq";
    private static final String TRANSFORMACION_AES_GCM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    private static KeyStoreManager instancia;

    private KeyStoreManager() {
        inicializarKeyStore();
    }

    public static synchronized KeyStoreManager getInstancia() {
        if (instancia == null) {
            instancia = new KeyStoreManager();
        }
        return instancia;
    }

    private void inicializarKeyStore() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            if (!keyStore.containsAlias(ALIAS_LLAVE_MAESTRA)) {
                KeyGenerator keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER);

                KeyGenParameterSpec keySpec = new KeyGenParameterSpec.Builder(
                        ALIAS_LLAVE_MAESTRA,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build();

                keyGenerator.init(keySpec);
                keyGenerator.generateKey();
                Log.d(TAG, "Nueva llave AES-256 generada exitosamente en Android KeyStore (TEE/HSM).");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando Android KeyStore", e);
        }
    }

    public String cifrarTextoConHardware(String textoPlano) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(ALIAS_LLAVE_MAESTRA, null);
            Cipher cipher = Cipher.getInstance(TRANSFORMACION_AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] textoCifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            byte[] combinado = new byte[iv.length + textoCifrado.length];
            System.arraycopy(iv, 0, combinado, 0, iv.length);
            System.arraycopy(textoCifrado, 0, combinado, iv.length, textoCifrado.length);

            return Base64.encodeToString(combinado, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Error al cifrar datos con KeyStore", e);
            return null;
        }
    }

    @SuppressWarnings("unused")
    public String descifrarTextoConHardware(String textoCifradoBase64) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER);
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(ALIAS_LLAVE_MAESTRA, null);
            byte[] combinado = Base64.decode(textoCifradoBase64, Base64.DEFAULT);

            int ivSize = 12;
            byte[] iv = new byte[ivSize];
            byte[] datosCifrados = new byte[combinado.length - ivSize];

            System.arraycopy(combinado, 0, iv, 0, ivSize);
            System.arraycopy(combinado, ivSize, datosCifrados, 0, datosCifrados.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMACION_AES_GCM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            byte[] textoPlanoBytes = cipher.doFinal(datosCifrados);
            return new String(textoPlanoBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Error al descifrar datos con KeyStore", e);
            return null;
        }
    }
}
