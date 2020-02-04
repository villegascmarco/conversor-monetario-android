package com.ssg.conversormonetario;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ActivityMain extends AppCompatActivity {
    TextView txtCantidad;
    TextView txtPrecioDolar;
    TextView txtTotal;

    ImageButton btnRefrescar;
    ImageButton btnCompartir;

    Button btnConvertirPesos;
    Button btnConvertirDolares;

    Dialog dlgConsultarPrecioDolar;
    private double cantidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Si la version del api de android es mayor o igual a 23 (Android 6.0)
         * hay que pedir permiso de esta froma, además de agregarlo al Manifest
         */
        //si la version del api de android es mayor o igual a 23 (Android 6.0)
        //Hay que pedir permiso de esta forma, además de agregarlo al Manifest
        if (Build.VERSION.SDK_INT >= 23) {
            //Pedimos permiso para que la app pueda usar internet
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        } else {
            inicializarComponentes();

        }
    }


    public void cambiarActivity(View v) {
        if (!txtPrecioDolar.getText().toString().equals("")) {
            Intent intent = new Intent(this, ActivityMensaje.class);
            intent.putExtra("precio", txtPrecioDolar.getText().toString());
            startActivity(intent);
        }else{
            Toast.makeText(
                    getApplicationContext(),
                    "Precio no válido",
                    Toast.LENGTH_LONG).show();
        }

    }

    public void inicializarComponentes() {
        txtCantidad = findViewById(R.id.txtCantidad);
        txtPrecioDolar = findViewById(R.id.txtPrecioDolar);
        txtTotal = findViewById(R.id.txtTotal);
        btnConvertirDolares = findViewById(R.id.btnConvertirDolares);
        btnConvertirPesos = findViewById(R.id.btnConvertirPesos);
        btnRefrescar = findViewById(R.id.btnRefrescar);
        btnCompartir = findViewById(R.id.btnCompartir);

        txtTotal.setEnabled(false);
        txtPrecioDolar.setEnabled(false);

        Drawable d = new ColorDrawable(Color.BLACK);
        dlgConsultarPrecioDolar = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        d.setAlpha(130);
        dlgConsultarPrecioDolar.getWindow().setBackgroundDrawable(d);

    }

    /**
     * Este método se invoca al momento que el usuario da permiso a la app para hacer uso de internet
     *
     * @param
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        if (grantResults != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i("info", "Permiso concedido");

            /**
             * Una vez que el usuario de los permisos necesarios, inicializamos los componentes
             */
            inicializarComponentes();
        } else {
            System.exit(0);
        }
    }


    public void consultarPrecioDolar(View v) {
        //Instanciamos una nueva petición HTTP a través de Volley:
        RequestQueue rq = Volley.newRequestQueue(this);

        //La URL del servicio de divisas tomando como base el dólar:
        String url = "https://api.exchangeratesapi.io/latest?base=USD";

        //Generamos un nuevo objeto Response.Listener<String> para indicar que haremos cuando
        //tengamos una respuesta correcta:
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            //Aquí indicamos que haremos con la respuesta de la petición HTTP.
            @Override
            public void onResponse(String response) {
                //Generamos un objeto JSON Genérico:
                JsonParser jp = new JsonParser();
                JsonObject jso = (JsonObject) jp.parse(response);

                //Leemos la propiedad "MXN" como un valor tipo float:
                float precioDolar = jso.get("rates").getAsJsonObject()
                        .get("MXN").getAsFloat();

                //Establecemos el precio del dólar en la caja de texto:
                txtPrecioDolar.setText("" + precioDolar);
                dlgConsultarPrecioDolar.hide();
            }
        };

        //Generamos un nuevo objeto Response.ErrorListener para indicar que haremos
        //cuando ocurra un error con nuestra petición:
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dlgConsultarPrecioDolar.hide();
                Toast.makeText(getBaseContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        //Generamos una nueva petición Volley:
        StringRequest sr = new StringRequest(Request.Method.GET,
                url,
                responseListener, errorListener);

        dlgConsultarPrecioDolar.show();
        //Agregamos la petición a la cola de peticiones de Volley
        //para que se ejecute:
        rq.add(sr);
    }

    public void realizarCalculoAPeso(View v) {
        if (!txtPrecioDolar.getText().toString().equals("") &&
                !txtCantidad.getText().toString().equals("")) {
            double precio = Double.parseDouble(txtPrecioDolar.getText().toString());
            double cantidad = Double.parseDouble(txtCantidad.getText().toString());

            double total = cantidad * precio;
            txtTotal.setText("" + total);
        }
    }

    public void realizarCalculoADolar(View v) {

        if (!txtPrecioDolar.getText().toString().equals("") && !txtCantidad.getText().toString().equals("")) {
            double precio = Double.parseDouble(txtPrecioDolar.getText().toString());
            double cantidad = Double.parseDouble(txtCantidad.getText().toString());

            double total = cantidad / precio;

            txtTotal.setText("" + total);
        }
    }

}
