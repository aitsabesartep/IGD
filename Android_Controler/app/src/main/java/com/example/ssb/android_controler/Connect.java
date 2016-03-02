package com.example.ssb.android_controler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ssb on 18/2/16.
 */


public class Connect extends AsyncTask<Void, Integer, Boolean> {

    private String dir;
    private int port;
    private double precisio;
    private Socket socket;
    private int current;
    private DataOutputStream send;
    private SensorManager mSensor;
    private Sensor sSensor;
    private SensorEventListener mListener;
    private Context c;
    private int last = 0;
    private TextView tv;
    private float buffer[];
    private int pos;

    public Connect(String d, int po, double pre, Context con, TextView s, int tamany){
        dir = d;
        port = po;
        precisio = pre;
        c = con;
        current = 0;
        tv = s;

        pos = 0;
        buffer = new float[tamany];
        Arrays.fill(buffer, 0);

        startConect();
        initBuf();
        startSensor();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }

    @Override
    protected void onPreExecute() {
        startSensor();
    }

    @Override
    protected void onPostExecute(Boolean result) {
    }

    @Override
    protected void onCancelled() {
    }

    private void startConect(){

        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(dir, port), 1000);
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Error socket.connect");
            //Avisar
        }
    }

    private void initBuf(){
        try {
            send = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error iniciar buffer");
        }
    }

    private void startSensor(){
        mSensor = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        sSensor = mSensor.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION);
        mSensor.registerListener(mListener, sSensor, SensorManager.SENSOR_DELAY_GAME);
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
                    return;
                }

                buffer[pos] = event.values[1];
                pos++;
                float media = 0;
                if (pos == 9){
                    pos = 0;
                }
                for (int i = 0; i < buffer.length; i++){
                    media = media + buffer[i];
                }
                media = media/buffer.length;

                tv.setText("Valor[Y]: "+ media +
                "\nValor[X]: "+event.values[2] +
                "\nValor[Z]: "+event.values[0]);


                //Comprovar precisio
                float aux = event.values[1];
                int aux1 = (int) ( aux * 1000);
                float fi = ((float) aux1/ (float) 1000);
                float dif = Math.abs(current-fi);
                if (dif < precisio){
                    return;
                }

                if (fi > 4) {
                    current = 37;
                } else if (fi < -4) {
                    current = 39;
                } else if (fi < 4 && fi > -4) {
                    current = 0;
                }

                if (current == last) return;

                last = current;
                try {
                    send.writeInt(current);
                } catch (Exception e) {
                    //System.exit(0);
                }

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }
}
