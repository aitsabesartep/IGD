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
    private Socket socket;
    private DataOutputStream send;
    private SensorManager mSensor;
    private Sensor sSensor;
    private SensorEventListener mListener;
    private Context c;
    private TextView tv;
    private float buffery[];
    private float bufferx[];
    private float bufferz[];
    private int pos;

    public Connect(String d, int po, double pre, Context con, TextView s){
        dir = d;
        port = po;
        c = con;
        tv = s;

        //startConect();
        //initBuf();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        startSensor();
        return true;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
    }

    @Override
    protected void onPreExecute() {
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
        System.out.println("Start sensor");
        mSensor = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        sSensor = mSensor.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION);
        mSensor.registerListener(mListener, sSensor, SensorManager.SENSOR_DELAY_GAME);
        mListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
                    return;
                }


                pos++;
                float mediay = 0;
                float mediax = 0;
                float mediaz = 0;

                mediay = event.values[1];
                mediax = event.values[2];

                tv.setText("Valor[Y]: "+ mediay);

                /*
                try {
                    send.writeFloat(mediay);
                } catch (Exception e) {
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }
}
