package com.example.cb_densimeter;

public class Perfiles {
    public String perfilNombre;
    public float rendimiento, evaporacion;

    public Perfiles(String perfilNombre, int rendimiento, int evaporacion) {
        this.perfilNombre = perfilNombre;
        this.rendimiento = rendimiento;
        this.evaporacion = evaporacion;
    }

    public Perfiles() {

    }

    public String getPerfilNombre() {
        return perfilNombre;
    }

    public void setPerfilNombre(String perfilNombre) {
        this.perfilNombre = perfilNombre;
    }

    public float getRendimiento() {
        return rendimiento;
    }

    public void setRendimiento(float rendimiento) {
        this.rendimiento = rendimiento;
    }

    public float getEvaporacion() {
        return evaporacion;
    }

    public void setEvaporacion(float evaporacion) {
        this.evaporacion = evaporacion;
    }
}
