package com.example.cb_densimeter;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class CalcularActivity extends AppCompatActivity {
    ArrayList<String> maltasList;
    ArrayList<EntidadMaltas> lista, listaRemoto;
    ArrayList<MaltasReceta> listaReceta;
    LinearLayout layoutList;
    Button btnAdd, btnBack1, btnCalcular1, btnActualizar, btnGuardar;
    SQLHelper helper;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcular);

        btnBack1 = findViewById(R.id.btnBack1);
        btnAdd = findViewById(R.id.btnAdd);
        layoutList = findViewById(R.id.layoutList);
        btnCalcular1 = findViewById(R.id.btnCalcular1);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnGuardar = findViewById(R.id.btnGuardar);
        listaRemoto = new ArrayList<EntidadMaltas>();
        listaReceta = new ArrayList<MaltasReceta>();



        helper = new SQLHelper(this, "maltas_sqlite", null, 1);
        rellenarTabla();
        consultarMaltas();
        rellenarSpinner();



        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                borrarTabla();
                new Actualizar().execute();
                insertarDatos();
                rellenarSpinner();

            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addView();
                rellenarSpinner();

            }
        });


        btnBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnCalcular1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                comprobarLeer();
                TextView textRendimiento = (TextView) findViewById(R.id.textRendimiento);
                calcularRendimiento();
                textRendimiento.setText(String.valueOf(calcularRendimiento()));

                TextView textEvaporacion = (TextView) findViewById(R.id.textEvaporacion);
                calcularEvaporacion();
                textEvaporacion.setText(String.valueOf(calcularEvaporacion()));



            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (v.getContext(), Guardar.class);
                intent.putExtra("rendimiento", calcularRendimiento());
                intent.putExtra("evaporacion", calcularEvaporacion());
                startActivityForResult(intent, 0);
            }
        });
    }

/*
Este es el metodo para el calculo de la evaporacion segun los datos recogidos de los EditText.
Para el calculo se considera que de media 1 kg de malta absorbe 1 litro de agua.
Por eso el peso total que viene en gramos se divide entre 1000 para obtener kg o litros a restar del agua inical.
El peso total de las maltas se obtiene recorriendo el arraylist listaReceta que es rellenada con los datos recogidos del formulario en el metodo comprobarLeer().
 */

    public float calcularEvaporacion() {
        EditText editAguaInicial = (EditText)findViewById(R.id.editAguaInicial);
        EditText editMosto = (EditText)findViewById(R.id.editMosto);
        EditText editTiempoCoccion = (EditText)findViewById(R.id.editTiempoCoccion);
        float evaporacion = 0;
        float pesoT = 0;
        for (int i = 0; i < listaReceta.size(); i++) {

            pesoT = pesoT + listaReceta.get(i).getMaltaPeso();
        }
        pesoT = pesoT/1000;

        if(!editAguaInicial.getText().toString().equals("")||!editMosto.getText().toString().equals("")||!editTiempoCoccion.getText().toString().equals("")) {
            float aguaInicial = Integer.valueOf(editAguaInicial.getText().toString());
            float aguaFinal = Integer.valueOf(editMosto.getText().toString());
            float tiempo = Integer.valueOf(editTiempoCoccion.getText().toString()) / 60;
            evaporacion = (aguaInicial - aguaFinal - pesoT) / tiempo;
            return evaporacion;
        } else {
            Toast.makeText(this, "Debe introducir todos los datos", Toast.LENGTH_SHORT).show();
        }

        return evaporacion;

    }
/*
Este metodo comprueba que campos del formulario no esten vacios y siguiendo la formula calcula el rendimiento del equipo.
 */
    public float calcularRendimiento() {
        EditText editAguaInicial = (EditText)findViewById(R.id.editAguaInicial);
        EditText editMosto = (EditText)findViewById(R.id.editMosto);
        EditText editTiempoCoccion = (EditText)findViewById(R.id.editTiempoCoccion);
        EditText editDensidad = (EditText)findViewById(R.id.editDensidad);
        float r = 0;

        if(!editAguaInicial.getText().toString().equals("")||!editMosto.getText().toString().equals("")||!editTiempoCoccion.getText().toString().equals("")||!editDensidad.getText().toString().equals("")) {

            float fd = Integer.valueOf(editDensidad.getText().toString()); // densidad inicial
            float vol = Integer.valueOf(editMosto.getText().toString());    //litros de mosto
            float pd = (fd - 1000) * vol;                                   // puntos de densidad
            float pesoT = 0;                                                // peso total de las maltas que suma al recorrer el Arraylist listaReceta.
            for (int i = 0; i < listaReceta.size(); i++) {

                pesoT = pesoT + listaReceta.get(i).getMaltaPeso();
            }

            for (int i = 0; i < listaReceta.size(); i++) {
                float a = (listaReceta.get(i).getMaltaPeso() / pesoT);          //para facilidad de lectura la formula (rendimiento(i)=puntos de densidad/extracto potencial/peso malta*100)se divide 4 pasos
                float b = ((a * pd) / (listaReceta.get(i).getMaltaPD() - 1000));
                float c = (b / (listaReceta.get(i).getMaltaPeso())) * 100;
                r = r + c / listaReceta.size();     //el bucle recorre todas las maltas utulizadas y calcula el rendimiento para cada una de ellas. Al dividir entre el tamaño del Arraylist y sumar obtenemos la media de rendimiento (r)

            }

        } else {
            Toast.makeText(this, "Debe introducir todos los datos", Toast.LENGTH_SHORT).show();

        }

        return r;
    }
/*
comprueba que todos los campos esten correctamente rellenados y rellena la Arraylist listaReceta con los datos de las maltas introducidas
 */
    private boolean comprobarLeer() {
        listaReceta.clear();
        boolean resultado = true;

        for (int i = 0; i < layoutList.getChildCount(); i++) {
            View maltasView = layoutList.getChildAt(i);
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

/*
Este metodo rellena el Spiner con los nombres de las maltas del Arraylist maltaslist
 */
    private void rellenarSpinner() {
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

/*
este metodo vacia la tabla maltas
 */

    }
    private void borrarTabla() {
        SQLHelper helper = new SQLHelper(this, "maltas_sqlite", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(Utilidades.TABLA_MALTAS,null,null);

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
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,R.layout.spinner_item,maltasList);

        // Specify the layout to use when the list of choices appears
//        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        spinnerMaltas.setAdapter(arrayAdapter);

        imageClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeView(maltasView);

            }
        });

        layoutList.addView(maltasView);

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

        layoutList.removeView(view);

    }
/*
Este metodo rellena la tabla local maltas con los datos del ArrayList listaRemoto
 */
    public void insertarDatos() {
        SQLHelper helper = new SQLHelper(this, "maltas_sqlite", null, 1);
        SQLiteDatabase db = helper.getWritableDatabase();


        ContentValues values = new ContentValues();
        for (int i = 0; i < listaRemoto.size(); i++) {


        values.put(Utilidades.CAMPO_NOMBRE, listaRemoto.get(i).getNombre());
        values.put(Utilidades.CAMPO_PD, listaRemoto.get(i).getPd());
        db.insert(Utilidades.TABLA_MALTAS, null, values);


        }
        db.close();
        Toast.makeText(this, "Se cargaron los datos", Toast.LENGTH_SHORT).show();
    }
/*
Este metodo descarga los datos de una tabla maltas remota conectandose al webservice.php
 */
    private String descargar() {
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://maltas.atspace.cc/webservice.php");
        String resultado = "";
        HttpResponse response;
        try {
            response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream instream = entity.getContent();
            resultado = covertStreamToString(instream);
        
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        
        }
        return resultado;


    }
/*
Este metodo complementa al anterior convirtiendo en String los datos recibidos del webService
 */
    private String covertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            finally {
                is.close();
            }
            return sb.toString();
        } else {return "";}
    }
/*
filtra los datos recibidos del webservice en formto JSON y con ellos rellena el ArrayList listaRemoto
 */
    private boolean filtrar() {
        listaRemoto.clear();
        String data = descargar();
        if(!data.equalsIgnoreCase("")) {
            JSONObject json;
            try {
                json = new JSONObject(data);
                JSONArray jsonArray = json.optJSONArray("maltas");
                for (int i=0; i < jsonArray.length(); i++) {
                    EntidadMaltas maltas = new EntidadMaltas();
                    JSONObject jsonArrayChild = jsonArray.getJSONObject(i);
                    maltas.setNombre(jsonArrayChild.optString("nombre"));
                    maltas.setPd(jsonArrayChild.optString("pd"));
                    listaRemoto.add(maltas);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
    /*
    se crea una clase que hereda del AsyncTask para realizar la conexion anterior en un hilo independiente.
     */
    class Actualizar extends AsyncTask<String,String,String> {
        @Override
        protected String doInBackground(String... params) {
            descargar();
            filtrar();

            return null;
        }
    }

    /*
    Este metodo comprueba si la tabla maltas está vacia y la rellena con los datos por defecto
     */
    public void rellenarTabla() {
        SQLHelper helper = new SQLHelper(this, "maltas_sqlite", null, 1);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + Utilidades.TABLA_MALTAS, null);
    if(!cursor.moveToNext()) {
        ContentValues values = new ContentValues();
        SQLiteDatabase db1 = helper.getWritableDatabase();

        values.put(Utilidades.CAMPO_NOMBRE, "Otra");
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Lager");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Pale");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Biscuit");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Viena");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Munich");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Brown");
        values.put(Utilidades.CAMPO_PD, 1028);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Crystal");
        values.put(Utilidades.CAMPO_PD, 1029);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Special B");
        values.put(Utilidades.CAMPO_PD, 1028);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Chocolate");
        values.put(Utilidades.CAMPO_PD, 1025);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Cebada Tostada");
        values.put(Utilidades.CAMPO_PD, 1025);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta Black");
        values.put(Utilidades.CAMPO_PD, 1025);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta de Trigo");
        values.put(Utilidades.CAMPO_PD, 1033);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Malta de Centeno");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Copos de avena");
        values.put(Utilidades.CAMPO_PD, 1028);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Copos de maiz");
        values.put(Utilidades.CAMPO_PD, 1033);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Copos de cebada");
        values.put(Utilidades.CAMPO_PD, 1024);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Copos de trigo");
        values.put(Utilidades.CAMPO_PD, 1030);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Copos de arroz");
        values.put(Utilidades.CAMPO_PD, 1032);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

        values.put(Utilidades.CAMPO_NOMBRE, "Azucar blanco");
        values.put(Utilidades.CAMPO_PD, 1038);
        db1.insert(Utilidades.TABLA_MALTAS, null, values);

    }

        db.close();


    }


}