package com.example.cb_densimeter;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Guardar extends AppCompatActivity {

    Button btnGuardarEx, btnGuardarNuevo, btnBack2, btnDelete;
    Spinner spinnerPerfil;
    EditText editRenE, editEvE, editPerfil, editRenN, editEvN;

    Bundle datos;
    ArrayList<String> spinnerList;
    ArrayList<Perfiles> lista;
    SQLHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardar);

        btnGuardarEx = findViewById(R.id.btnGuardarEx);
        btnGuardarNuevo = findViewById(R.id.btnGuardarNuevo);
        btnBack2 = findViewById(R.id.btnBack2);
        btnDelete = findViewById(R.id.btnDelete);
        spinnerPerfil = findViewById(R.id.spinnerPerfil);
        editRenE = findViewById(R.id.editRenE);
        editEvE = findViewById(R.id.editEvE);
        editPerfil = findViewById(R.id.editPerfil);
        editRenN = findViewById(R.id.editRenN);
        editEvN = findViewById(R.id.editEvN);

        datos = getIntent().getExtras();
        float rendimiento = datos.getFloat("rendimiento");
        float evaporacion = datos.getFloat("evaporacion");


        editRenE.setText(String.valueOf(rendimiento));
        editRenN.setText(String.valueOf(rendimiento));
        editEvE.setText(String.valueOf(evaporacion));
        editEvN.setText(String.valueOf(evaporacion));

        helper = new SQLHelper(this, "maltas_sqlite", null, 1);

        consultarPerfiles();
        rellenarSpinner();

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,spinnerList);
        spinnerPerfil.setAdapter(arrayAdapter);




        btnBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnGuardarEx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarExistente();

            }
        });

        btnGuardarNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertarNuevo();

            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Borrar perfil");
                builder.setMessage("¿Seguro que quieres borrar el perfil?");

                builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        borrarPerfil();


                    }
                });
                builder.setNegativeButton("Cancelar", null);
                AlertDialog dialog = builder.create();
                dialog.show();





            }
        });


    }

 /*
 Crea un AlertDialog para la confirmación del borrado de un perfil
  */
    private void dialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Borrar perfil");
        builder.setMessage("¿Seguro que quieres borrar el perfil?");

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                borrarPerfil();


            }
        });
        builder.setNegativeButton("Cancelar", null);
        AlertDialog dialog = builder.create();
        dialog.show();

    }
/*
rellena el spiner de perfiles
 */
    private void rellenarSpinner() {
        spinnerList = new ArrayList<String>();
        spinnerList.add("Seleccione");
        for (int i = 0; i < lista.size(); i++) {
            spinnerList.add(lista.get(i).getPerfilNombre());
        }

    }
/*
rellena el arraylist lista con los datos de la tabla perfiles
 */
    private void consultarPerfiles() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Perfiles perfil = null;
        lista = new ArrayList<Perfiles>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utilidades.TABLA_PERFILES, null);

        while (cursor.moveToNext()) {
            perfil = new Perfiles();
            perfil.setPerfilNombre(cursor.getString(1));
            perfil.setRendimiento(cursor.getFloat(2));
            perfil.setEvaporacion(cursor.getFloat(3));

            lista.add(perfil);

        }
        db.close();
    }
/*
inserta una fila con los datos de un nuevo perfil en la tabla perfiles
 */
    private void insertarNuevo() {
        SQLiteDatabase db = helper.getWritableDatabase();

        if(!editPerfil.getText().toString().equals("")) {
            ContentValues values = new ContentValues();
            values.put(Utilidades.CAMPO_NOMBRE_PERFIL, editPerfil.getText().toString());
            values.put(Utilidades.CAMPO_RENDIMIENTO, Float.valueOf(editRenN.getText().toString()));
            values.put(Utilidades.CAMPO_EVAPORACION, Float.valueOf(editEvN.getText().toString()));
            db.insert(Utilidades.TABLA_PERFILES, null, values);


            Toast.makeText(this, "Se cargaron los datos en el " + editPerfil.getText().toString(), Toast.LENGTH_LONG).show();
            finish();
        } else {
            Toast.makeText(this, "Debe introducir el nombre del perfil", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }
/*
actualiza la fila de la tabla perfiles con datos nuevos (haciendo la media con los datos anteriores)
 */
    private void insertarExistente() {

        SQLiteDatabase db = helper.getWritableDatabase();
        SQLiteDatabase db1 = helper.getReadableDatabase();

        if(spinnerPerfil.getSelectedItem().toString() != "Seleccione") {
            Cursor cursor = db1.rawQuery("SELECT * FROM " + Utilidades.TABLA_PERFILES + " WHERE " + Utilidades.CAMPO_NOMBRE_PERFIL+ " = '" + spinnerPerfil.getSelectedItem().toString() +"'", null);

            cursor.moveToFirst();

            float renTemp = cursor.getFloat(2);
            float evTemp = cursor.getFloat(3);

            renTemp = (renTemp + Float.valueOf(editRenE.getText().toString()))/2;
            evTemp = (evTemp + Float.valueOf(editEvE.getText().toString()))/2;

            ContentValues values = new ContentValues();
            values.put(Utilidades.CAMPO_RENDIMIENTO, renTemp);
            values.put(Utilidades.CAMPO_EVAPORACION,evTemp);

            db.update(Utilidades.TABLA_PERFILES, values, Utilidades.CAMPO_NOMBRE_PERFIL + " = '" + spinnerPerfil.getSelectedItem().toString() +"'", null);

            Toast.makeText(this, "Se cargaron los datos en el " + spinnerPerfil.getSelectedItem().toString() +". Valores actuales: Rendimiento: "+ renTemp+ ", Evaporacion: " +evTemp, Toast.LENGTH_LONG).show();

            finish();
        } else {
        Toast.makeText(this, "Debe seleccionar un perfil", Toast.LENGTH_SHORT).show();
    }
        db.close();
        db1.close();

    }
/*
elimina la fila de un perfil de la tabla perfiles
 */
    private void borrarPerfil() {

        if (spinnerPerfil.getSelectedItem().toString() != "Seleccione") {
            SQLiteDatabase db = helper.getWritableDatabase();
            db.execSQL("delete from " + Utilidades.TABLA_PERFILES + " where " + Utilidades.CAMPO_NOMBRE_PERFIL + " = '" + spinnerPerfil.getSelectedItem().toString() + "'");
            Toast.makeText(this, "Perfil eliminado, puede crear un nuevo perfil", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Debe seleccionar un perfil", Toast.LENGTH_SHORT).show();
        }
    }


}