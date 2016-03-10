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
    private float buffery[];
    private float bufferx[];
    private int pos;

    public Connect(String d, int po, double pre, Context con, TextView s, int tamany){
        dir = d;
        port = po;
        precisio = pre;
        c = con;
        current = 0;
        tv = s;

        pos = 0;
        buffery = new float[tamany];
        Arrays.fill(buffery, 0);
        bufferx = new float[tamany];
        Arrays.fill(bufferx, 0);

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

                buffery[pos] = event.values[1];
                bufferx[pos] = event.values[2];
                pos++;
                float mediay = 0;
                float mediax = 0;
                if (pos == 9){
                    pos = 0;
                }
                for (int i = 0; i < buffery.length; i++){
                    mediay = mediay + buffery[i];
                    mediax = mediax + bufferx[i];
                }

                mediax = mediax/bufferx.length;
                mediax = (int) (mediax * (10 * 1));
                mediax = mediax/(10*1);
                mediay = mediay/buffery.length;
                mediay = (int) (mediay * (10 * 1));
                mediay = mediay/(10 * 1);



                tv.setText("Valor[Y]: "+ mediay +
                "\nValor[X]: "+ mediax);

                //Comprovar precisio
                /*float aux = event.values[1];
                int aux1 = (int) ( aux * 1000);
                float fi = ((float) aux1/ (float) 1000);
                float dif = Math.abs(current-fi);
                if (dif < precisio){
                    return;
                }
                last = current;*/

                try {
                    send.writeFloat(mediay);
                } catch (Exception e) {
                    //System.exit(0);
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }
}
