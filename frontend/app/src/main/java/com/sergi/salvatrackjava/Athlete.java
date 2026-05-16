package com.sergi.salvatrackjava;

import java.io.Serializable;
import java.util.List;

public class Athlete implements Serializable {

    private String nombre;
    private long anoNacimiento;
    private String club;
    private String categoria;
    private String genero;
    private List<Marca> marcas;

    public Athlete() {}

    // Getters
    public String getNombre()        { return nombre; }
    public long getAnoNacimiento()   { return anoNacimiento; }
    public String getClub()          { return club; }
    public String getCategoria()     { return categoria; }
    public String getGenero()        { return genero; }
    public List<Marca> getMarcas()   { return marcas; }

    // Setters (necesarios para el parser manual)
    public void setNombre(String v)          { nombre = v; }
    public void setAnoNacimiento(long v)     { anoNacimiento = v; }
    public void setClub(String v)            { club = v; }
    public void setCategoria(String v)       { categoria = v; }
    public void setGenero(String v)          { genero = v; }
    public void setMarcas(List<Marca> v)     { marcas = v; }

    /** Devuelve las dos primeras iniciales del nombre (ej: "LARA LÓPEZ" → "LL") */
    public String getIniciales() {
        if (nombre == null || nombre.isEmpty()) return "?";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length >= 2) {
            return String.valueOf(partes[0].charAt(0)) + partes[1].charAt(0);
        }
        return String.valueOf(partes[0].charAt(0));
    }
}