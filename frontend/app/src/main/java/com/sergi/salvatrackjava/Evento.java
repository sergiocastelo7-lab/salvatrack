package com.sergi.salvatrackjava;

public class Evento implements java.io.Serializable {

    private String evento;
    private String fecha_inicio;
    private String fecha_fin;
    private String horario;
    private String origen;
    private String sede;

    // Constructor vacío obligatorio para Firestore
    public Evento() {}

    public String getEvento()       { return evento; }
    public String getFecha_inicio() { return fecha_inicio; }
    public String getFecha_fin()    { return fecha_fin; }
    public String getHorario()      { return horario; }
    public String getOrigen()       { return origen; }
    public String getSede()         { return sede; }
}
