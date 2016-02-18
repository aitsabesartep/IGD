package com.example.ssb.android_controler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;

/**
 * Created by ssb on 18/2/16.
 */


public class Connect extends AsyncTask<Void, Integer, Boolean> {

    private String dir;
    private int port;
    private double precisio;
    private Socket socket;
    private float current;
    private DataOutputStream send;
    private SensorManager mSensor;
    private Sensor sSensor;
    private SensorEventListener mListener;
    private Context c;

    public Connect(String d, int po, double pre, Context con){
        dir = d;
        port = po;
        precisio = pre;
        c = con;
        current = 0;
        startConect();
        initBuf();
        startSensor();
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
            socket.connect(new InetSocketAddress(dir,port),1000);
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
                float aux = event.values[1];
                int aux1 = (int) ( aux * 1000);
                float fi = ((float) aux1/ (float) 1000);
                float dif = Math.abs(current-fi);
                if (dif < precisio){
                    return;
                }
                current = fi;
                try {
                    send.writeFloat(current);
                } catch (IOException e) {
                    System.exit(0);
                }

            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }
}
