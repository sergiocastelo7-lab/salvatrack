package com.sergi.salvatrackjava;

import java.io.Serializable;

public class Marca implements Serializable {

    private String prueba;
    private String tiempo;
    private String piscina;
    private String crono;

    public Marca() {}

    // Getters
    public String getPrueba()  { return prueba; }
    public String getTiempo()  { return tiempo; }
    public String getPiscina() { return piscina; }
    public String getCrono()   { return crono; }

    // Setters (para el parser manual)
    public void setPrueba(String v)  { prueba = v; }
    public void setTiempo(String v)  { tiempo = v; }
    public void setPiscina(String v) { piscina = v; }
    public void setCrono(String v)   { crono = v; }

    /** "01:05:43" → "01:05.43" */
    public String getTiempoDisplay() {
        if (tiempo == null) return "--:--.--";
        int lastColon = tiempo.lastIndexOf(':');
        if (lastColon >= 0) {
            return tiempo.substring(0, lastColon) + "." + tiempo.substring(lastColon + 1);
        }
        return tiempo;
    }

    /** "01:05:43" → milisegundos */
    public long getTiempoMs() {
        return tiempoStringAMs(tiempo);
    }

    public static long tiempoStringAMs(String t) {
        if (t == null) return Long.MAX_VALUE;
        String[] p = t.split(":");
        if (p.length != 3) return Long.MAX_VALUE;
        try {
            long min = Long.parseLong(p[0].trim());
            long sec = Long.parseLong(p[1].trim());
            long cs  = Long.parseLong(p[2].trim());
            return min * 60000 + sec * 1000 + cs * 10;
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }
}