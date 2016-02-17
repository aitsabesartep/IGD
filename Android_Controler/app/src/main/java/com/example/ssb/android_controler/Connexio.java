package com.example.ssb.android_controler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by ssb on 17/2/16.
 */
public class Connexio extends Thread {

    private String dir;
    private int port;
    private double precisio;
    private Socket socket;
    private SensorManager mSensor;
    private Sensor sSensor;
    private SensorEventListener mListener;
    private HandlerThread mHandlerThread;
    private Context c;
    private float current;
    private DataOutputStream send;

    public Connexio(String d, int p, Context con, double pre){
        dir = d;
        port = p;
        c = con;
        precisio = pre;
        current = 0;
    }

    @Override
    public void run() {
        super.run();
        startRegister();
    }

    private void startRegister(){
        startConect();
        initBuf();
        mHandlerThread = new HandlerThread("Sender");
        mHandlerThread.start();
        Handler handler = new Handler(mHandlerThread.getLooper());

        mSensor = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        sSensor = mSensor.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION);
        //sManager.registerListener(c, sManager.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
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
                    e.printStackTrace();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mSensor.registerListener(mListener, sSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void startConect(){
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(dir,port),50);
        } catch (IOException e) {
            //Missatge error connexió.
            try {
                socket.close();
            } catch (IOException e1) {
            }
        }
    }

    private void initBuf(){
        try {
            send = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error buffer");
        }
    }
}
