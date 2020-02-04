package com.ssg.conversormonetario;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ActivityMensaje extends AppCompatActivity {

    static final int PICK_CONTACT_REQUEST = 1;

    Toolbar toolbar;
    FloatingActionButton fab;
    String numeroTelefonico = "";
    String precioDolar = "";

    TextView txtMensaje;
    TextView txtContacto;
    Button btnEnviar;

    private String no_ha_seleccionado_ningún_contacto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensaje);
        precioDolar = getIntent().getStringExtra("precio");

        inicializarComponentes();
        verificarPermisos();
    }

    private void verificarPermisos() {
        int permissionCheck = ContextCompat.checkSelfPermission(
                this, Manifest.permission.SEND_SMS);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            Log.i("Mensaje", "No se tiene permiso para enviar SMS.");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 225);
        } else {
            Log.i("Mensaje", "Se tiene permiso para enviar SMS!");
        }
    }

    public void inicializarComponentes() {
        txtMensaje = findViewById(R.id.txtMensaje);
        txtMensaje.setText("El precio del dolar para hoy es de " + precioDolar);
        txtContacto = findViewById(R.id.txtContacto);

        btnEnviar = findViewById(R.id.btnEnviar);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionarContacto();
            }
        });


        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarMensaje();
            }
        });
    }

    public void seleccionarContacto() {
        Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts"));
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            Uri uri = data.getData();

            Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null);
            if (cursor.moveToFirst()) {
                int nombre = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int numero = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                numeroTelefonico = cursor.getString(numero);

                txtContacto.setText(cursor.getString(nombre));
            } else {
                txtContacto.setText("No ha seleccionado ningún contacto");
                numeroTelefonico = "";
            }
        }
    }

    public void enviarMensaje() {
        if (numeroTelefonico.equals("")) {
            Toast.makeText(getApplicationContext(), "Seleccione un contacto", Toast.LENGTH_LONG).show();
        } else {
            try {
                SmsManager sms = SmsManager.getDefault();

                String mensaje = txtMensaje.getText().toString().trim();

                sms.sendTextMessage(
                        numeroTelefonico,
                        null,
                        mensaje,
                        null,
                        null);

                Toast.makeText(
                        getApplicationContext(),
                        "Enviando mensaje...",
                        Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Mensaje no enviado", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }

    public void enviarWhatsApp(View v) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, txtMensaje.getText().toString().trim());
        sendIntent.setType("text/plain");
        sendIntent.setPackage("com.whatsapp");

        startActivity(sendIntent);
    }

    public void enviarGeneral(View v) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, txtMensaje.getText().toString().trim());
        startActivity(Intent.createChooser(intent, "Compartir"));
    }


}
