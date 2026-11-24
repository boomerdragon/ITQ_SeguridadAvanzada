package com.itq.seguridadavanzadamovilapp;

import androidx.annotation.Keep;

/**
 * Modelo DTO que representa una credencial o token seguro.
 * Anotado con @Keep para evitar que R8 / ProGuard modifique los nombres de los atributos durante la ofuscación.
 */
@Keep
@SuppressWarnings("unused")
public class CredencialSeguraDto {

    private String id;
    private String aliasServicio;
    private String usuarioCifrado;
    private String tokenCifrado;
    private String algoritmoUsado;
    private long timestamp;

    public CredencialSeguraDto() {
    }

    public CredencialSeguraDto(String id, String aliasServicio, String usuarioCifrado, String tokenCifrado, String algoritmoUsado, long timestamp) {
        this.id = id;
        this.aliasServicio = aliasServicio;
        this.usuarioCifrado = usuarioCifrado;
        this.tokenCifrado = tokenCifrado;
        this.algoritmoUsado = algoritmoUsado;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAliasServicio() {
        return aliasServicio;
    }

    public void setAliasServicio(String aliasServicio) {
        this.aliasServicio = aliasServicio;
    }

    public String getUsuarioCifrado() {
        return usuarioCifrado;
    }

    public void setUsuarioCifrado(String usuarioCifrado) {
        this.usuarioCifrado = usuarioCifrado;
    }

    public String getTokenCifrado() {
        return tokenCifrado;
    }

    public void setTokenCifrado(String tokenCifrado) {
        this.tokenCifrado = tokenCifrado;
    }

    public String getAlgoritmoUsado() {
        return algoritmoUsado;
    }

    public void setAlgoritmoUsado(String algoritmoUsado) {
        this.algoritmoUsado = algoritmoUsado;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}