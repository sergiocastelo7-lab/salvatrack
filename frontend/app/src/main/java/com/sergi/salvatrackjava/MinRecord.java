package com.sergi.salvatrackjava;

public class MinRecord {

    private String categoria;
    private String genero;
    private String origen;  // "RFESS" o "FESSGA"
    private String prueba;
    private String tiempo;  // formato "MM:SS:cc"

    public MinRecord() {}

    public String getCategoria() { return categoria; }
    public String getGenero()    { return genero; }
    public String getOrigen()    { return origen; }
    public String getPrueba()    { return prueba; }
    public String getTiempo()    { return tiempo; }

    public String getTiempoDisplay() {
        if (tiempo == null) return "-";
        int lastColon = tiempo.lastIndexOf(':');
        if (lastColon >= 0) {
            return tiempo.substring(0, lastColon) + "." + tiempo.substring(lastColon + 1);
        }
        return tiempo;
    }

    public long getTiempoMs() {
        return Marca.tiempoStringAMs(tiempo);
    }
}