package com.example.ssb.igd_bt;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class PreferenceScreen extends AppCompatActivity {

    public TextView ip;
    public TextView port;
    public TextView frec;
    public int buf;
    public TableLayout tl;
    public Semaphore sem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_screen);
        initComonents();
    }

    private void initComonents() {
        final Button button = (Button) findViewById(R.id.guardarBtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                conectar_bt();
            }
        });

        CheckBox repeatChkBx = ( CheckBox ) findViewById( R.id.check_media );
        repeatChkBx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.table_media).setVisibility(View.VISIBLE);
                    TextView tx = (TextView) findViewById(R.id.tam);
                    buf = Integer.parseInt(tx.getText().toString());
                } else {
                    findViewById(R.id.table_media).setVisibility(View.INVISIBLE);
                    buf = 1;
                }

            }
        });

        ip = (TextView) findViewById(R.id.ip_txt);
        port = (TextView) findViewById(R.id.puerto_txt);
        tl = (TableLayout) findViewById(R.id.config);
        frec = (TextView) findViewById(R.id.frec);
    }

    private void conectar_bt(){
    }

    private void alertOk(){
        new AlertDialog.Builder(this)
                .setTitle("Conectado")
                .setMessage("Connexión correcta")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.mipmap.ic_ok_dialog)
                .show();
    }
    private void alertBad(){
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Problema al conectar con el socket")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.mipmap.ic_err_dialog)
                .show();
    }
}
