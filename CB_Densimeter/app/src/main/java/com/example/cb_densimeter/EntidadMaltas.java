package com.example.cb_densimeter;

public class EntidadMaltas {

    private Integer id;
    private String nombre;
    private String pd;
    private float peso;

    public EntidadMaltas(Integer id, String nombre, String pd, Float peso) {
        this.id = id;
        this.nombre = nombre;
        this.pd = pd;
        this.peso = peso;
    }
    public EntidadMaltas() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPd() {
        return pd;
    }

    public void setPd(String pd) {
        this.pd = pd;
    }

    public Float getPeso() {
        return peso;
    }

    public void setPeso(float peso) {
        this.peso = peso;
    }
}
