package com.example.cb_densimeter;

public class Utilidades {

    public static final String TABLA_MALTAS = "maltas_local";
    public static final String CAMPO_ID = "id";
    public static final String CAMPO_NOMBRE = "nombre";
    public static final String CAMPO_PD = "pd";
    public static final String TABLA_PERFILES = "perfiles";
    public static final String CAMPO_NOMBRE_PERFIL = "nombre_perfil";
    public static final String CAMPO_RENDIMIENTO = "rendimiento";
    public static final String CAMPO_EVAPORACION = "evaporacion";
    public static final String MALTAS_CREAR = "CREATE TABLE " + TABLA_MALTAS + " ("+CAMPO_ID+" INTEGER PRIMARY KEY, "+CAMPO_NOMBRE+" TEXT, "+CAMPO_PD+" INTEGER)";
    public static final String PERFILES_CREAR = "CREATE TABLE " + TABLA_PERFILES + " ("+CAMPO_ID+" INTEGER PRIMARY KEY, "+CAMPO_NOMBRE_PERFIL+" TEXT, "+CAMPO_RENDIMIENTO+" real, "+CAMPO_EVAPORACION+" real)";
    public static final String DROP_TABLA = "DROP TABLE IF EXISTS maltas_local";
    public static final String DB_NAME = "maltas_sqlite";
    public static final int DB_VERSION = 5;




}
