package com.example.cb_densimeter;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class AjustarActivity extends AppCompatActivity {
    Button btnAddR, btnCalcularReceta, btnBack3;
    Spinner spinnerPerfil1;
    TextView textViewRendimiento, textViewEvaporacion, textAgua, textMaltasPeso;
    EditText editAguaInicialR, editMostoR, editTiempoCoccionR, editDensidadR;
    LinearLayout layoutListR;
    ArrayList<String> spinnerList;
    ArrayList<EntidadMaltas> lista;
    ArrayList<Perfiles> listaPerfiles;
    SQLHelper helper;
    ArrayList<String> maltasList;
    ArrayList<MaltasReceta> listaReceta, maltasFinal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajustar);

        btnAddR = findViewById(R.id.btnAddR);
        btnCalcularReceta = findViewById(R.id.btnCalcularReceta);
        btnBack3 = findViewById(R.id.btnBack3);
        spinnerPerfil1 = findViewById(R.id.spinnerPerfil1);
        textViewRendimiento = findViewById(R.id.textViewRendimiento);
        textViewEvaporacion = findViewById(R.id.textViewEvaporacion);
        textAgua = findViewById(R.id.textAgua);
        textMaltasPeso = findViewById(R.id.textMaltasPeso);
        editAguaInicialR = findViewById(R.id.editAguaInicialR);
        editMostoR = findViewById(R.id.editMostoR);
        editTiempoCoccionR = findViewById(R.id.editTiempoCoccionR);
        editDensidadR = findViewById(R.id.editDensidadR);
        layoutListR = findViewById(R.id.layoutListR);

        listaReceta = new ArrayList<MaltasReceta>();
        maltasFinal = new ArrayList<MaltasReceta>();

        helper = new SQLHelper(this, "maltas_sqlite", null, 1);
        consultarPerfiles();
        consultarMaltas();
        rellenarSpinner();
        rellenarSpinnerMaltas();

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.spinner_item, spinnerList);

        // Specify the layout to use when the list of choices appears
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerPerfil1.setAdapter(arrayAdapter);

        insertarValores();
        /*
        es un listener para el spinner de los perfiles
         */
        spinnerPerfil1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                insertarValores();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAddR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addView();


            }
        });

        btnCalcularReceta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarLeer();
                calcularAgua();



            }
        });

        btnBack3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();


            }
        });

    }
/*
Este metodo calcula el peso de las maltas que se deberian usar en una receta objetivo
 */
    public void calcularMaltas() {
        maltasFinal.clear();
        MaltasReceta maltaFinal = null;
        float pesoT = 0;
        for (int i = 0; i < listaReceta.size(); i++) {

            pesoT = pesoT + listaReceta.get(i).getMaltaPeso();
        }

        float fd = Float.parseFloat(editDensidadR.getText().toString()) - 1000;
        float pd = Float.parseFloat(editMostoR.getText().toString())*fd;
        float ren = Float.parseFloat(textViewRendimiento.getText().toString());

        for (int i = 0; i < listaReceta.size(); i++) {
            maltaFinal = new MaltasReceta();
            float a = (listaReceta.get(i).getMaltaPeso() / pesoT); //proporcion de la malta en cuestion sobre el total peso
            float b = ((a * pd) / (listaReceta.get(i).getMaltaPD() - 1000)); //puntos de densidad correspondientes a una malta sobre el total objetivo
            float c = b/ren;                //puntos de densidad divididos entre el rendimiento del equipo
            int d = ((int) c)*100;          //peso en gramos de la malta ajustado

            maltaFinal.setMaltaNombre(listaReceta.get(i).getMaltaNombre());
            maltaFinal.setMaltaPeso(d);

            maltasFinal.add(maltaFinal);


        }

    }
/*
este metodo ajusta la cantidad de agua inicial segun los datos de evaporacion del perfil y los pesos ajustados de las maltas
 */
    public float calcularAgua() {
        float agua = 0;
        if(!editAguaInicialR.getText().toString().equals("")||
                !editMostoR.getText().toString().equals("")||
                !editTiempoCoccionR.getText().toString().equals("")||
                !editDensidadR.getText().toString().equals("")||
                !textViewEvaporacion.getText().toString().equals("")||
                !textViewRendimiento.getText().toString().equals("")) {

            calcularMaltas();
            float pesoT = 0;
            for (int i = 0; i < maltasFinal.size(); i++) {

                pesoT = pesoT + maltasFinal.get(i).getMaltaPeso();
            }
            pesoT = pesoT/1000;

            float aguaInicial = Float.parseFloat(editAguaInicialR.getText().toString());
            float aguaFinal = Float.parseFloat(editMostoR.getText().toString());
            float tiempo = Float.parseFloat(editTiempoCoccionR.getText().toString()) / 60;
            float evaporacion = Float.parseFloat(textViewEvaporacion.getText().toString());

            agua = aguaFinal + pesoT + (evaporacion*tiempo);


            textAgua.setText(String.valueOf(agua));

            textMaltasPeso.setText("");
            for (int i=0; i<maltasFinal.size(); i++) {
                textMaltasPeso.append(maltasFinal.get(i).getMaltaNombre()+" "+maltasFinal.get(i).getMaltaPeso()+"\n");
            }

            return agua;
        } else {
            Toast.makeText(this, "Debe introducir todos los datos", Toast.LENGTH_SHORT).show();
        }

        return agua;
    }
/*
rellena el spinner de los perfiles con los datos de listaPerfiles
 */
    private void rellenarSpinner() {
        spinnerList = new ArrayList<String>();
        spinnerList.add("Seleccione");
        for (int i = 0; i < listaPerfiles.size(); i++) {
            spinnerList.add(listaPerfiles.get(i).getPerfilNombre());
        }
    }

    /*
    rellena el arraylist listaPerfiles con los datos de de la tabla perfiles
     */

    private void consultarPerfiles() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Perfiles perfil = null;
        listaPerfiles = new ArrayList<Perfiles>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utilidades.TABLA_PERFILES, null);

        while (cursor.moveToNext()) {
            perfil = new Perfiles();
            perfil.setPerfilNombre(cursor.getString(1));
            perfil.setRendimiento(cursor.getFloat(2));
            perfil.setEvaporacion(cursor.getFloat(3));

            listaPerfiles.add(perfil);

        }
        cursor.close();
        db.close();
    }

    /*
    inserta los valores de rendimiento y evaporacion de la tabla perfiles en los textView correspondientes
     */

    private void insertarValores() {

        SQLiteDatabase db1 = helper.getReadableDatabase();

        if(!spinnerPerfil1.getSelectedItem().toString().equals("Seleccione")) {
            Cursor cursor = db1.rawQuery("SELECT * FROM " + Utilidades.TABLA_PERFILES + " WHERE " + Utilidades.CAMPO_NOMBRE_PERFIL+ " = '" + spinnerPerfil1.getSelectedItem().toString() +"'", null);

            cursor.moveToFirst();

            String ren = String.valueOf(cursor.getFloat(2));
            String ev = String.valueOf(cursor.getFloat(3));

            textViewRendimiento.setText(ren);
            textViewEvaporacion.setText(ev);
            cursor.close();

        } else {
            textViewRendimiento.setText("");
            textViewEvaporacion.setText("");
        }

        db1.close();

    }
    /*
    Este metodo "infla" el layout con los views del layout maltas. Para que se agregue una nueva view con cada pulsacion del boton "+malta"
     */
    private void addView() {


        final View maltasView = getLayoutInflater().inflate(R.layout.maltas,null,false);

        EditText editPD = (EditText)maltasView.findViewById(R.id.editPD);
        EditText editPeso = (EditText)maltasView.findViewById(R.id.editPeso);
        Spinner spinnerMaltas = (Spinner)maltasView.findViewById(R.id.spinnerMaltas);
        ImageView imageClear = (ImageView)maltasView.findViewById(R.id.imageClear);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,maltasList);
        spinnerMaltas.setAdapter(arrayAdapter);

        imageClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(maltasView);

            }
        });

        layoutListR.addView(maltasView);

        spinnerMaltas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int pos = position;
                editPD.setText(lista.get(position).getPd());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });

    }

    private void removeView(View view){

        layoutListR.removeView(view);

    }
    /*
    Este metodo rellena el Spiner con los nombres de las maltas del Arraylist maltaslist
     */
    private void rellenarSpinnerMaltas() {
        maltasList = new ArrayList<String>();

        for (int i = 0; i < lista.size(); i++) {
            maltasList.add(lista.get(i).getNombre());
        }

    }
    /*
    Este metodo hace la consulta a bd local a la tabla de maltas y con estos datos rellena el arraylist lista
    */
    private void consultarMaltas() {
        SQLiteDatabase db = helper.getReadableDatabase();
        EntidadMaltas malta = null;
        lista = new ArrayList<EntidadMaltas>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + Utilidades.TABLA_MALTAS, null);

        while (cursor.moveToNext()) {
            malta = new EntidadMaltas();
            malta.setId(cursor.getInt(0));
            malta.setNombre(cursor.getString(1));
            malta.setPd(cursor.getString(2));

            lista.add(malta);

        }
        db.close();

    }
    /*
    comprueba que todos los campos esten correctamente rellenados y rellena la Arraylist listaReceta con los datos de las maltas introducidas
     */
    private boolean comprobarLeer() {
        listaReceta.clear();
        boolean resultado = true;

        for (int i = 0; i < layoutListR.getChildCount(); i++) {
            View maltasView = layoutListR.getChildAt(i);
            EditText editPD = (EditText)maltasView.findViewById(R.id.editPD);
            EditText editPeso = (EditText)maltasView.findViewById(R.id.editPeso);
            Spinner spinnerMaltas = (Spinner)maltasView.findViewById(R.id.spinnerMaltas);

            MaltasReceta mReceta = new MaltasReceta();

            if(!editPD.getText().toString().equals("")) {
                mReceta.setMaltaPD(Integer.valueOf(editPD.getText().toString()));
            } else { resultado = false; break;}

            if(!editPeso.getText().toString().equals("")) {
                mReceta.setMaltaPeso(Integer.valueOf(editPeso.getText().toString()));
            } else { resultado = false; break;}

            mReceta.setMaltaNombre(maltasList.get(spinnerMaltas.getSelectedItemPosition()));
            listaReceta.add(mReceta);


        }

        if (listaReceta.size() == 0) {
            resultado = false;
            Toast.makeText(this, "Debe introducir los datos de las maltas", Toast.LENGTH_SHORT).show();
        } else if (!resultado) {
            Toast.makeText(this, "Debe introducir todos los datos correctamente", Toast.LENGTH_SHORT).show();
        }
        return resultado;
    }
}