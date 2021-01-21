package com.example.cb_densimeter;

public class MaltasReceta {

    public String maltaNombre;
    public int maltaPD;
    public float maltaPeso;

    public MaltasReceta() {

    }

    public MaltasReceta(String maltaNombre, int maltaPD, float maltaPeso) {
        this.maltaNombre = maltaNombre;
        this.maltaPD = maltaPD;
        this.maltaPeso = maltaPeso;
    }

    public String getMaltaNombre() {
        return maltaNombre;
    }

    public void setMaltaNombre(String maltaNombre) {
        this.maltaNombre = maltaNombre;
    }

    public int getMaltaPD() {
        return maltaPD;
    }

    public void setMaltaPD(int maltaPD) {
        this.maltaPD = maltaPD;
    }

    public float getMaltaPeso() {
        return maltaPeso;
    }

    public void setMaltaPeso(float maltaPeso) {
        this.maltaPeso = maltaPeso;
    }
}
