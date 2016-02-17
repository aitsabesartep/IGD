package com.example.ssb.android_controler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by ssb on 17/2/16.
 */
public class Connexio extends Thread implements SensorEventListener {

    private String dir;
    private int port;
    private Socket socket;
    private List<Float> llista;
    private Object lock;
    private SensorManager sManager;
    private Context c;

    public Connexio(String d, int p, Context con){
        dir = d;
        port = p;
        llista = new ArrayList<>();
        lock = new Object();
        c = con;
    }

    @Override
    public void run() {

        sManager = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        sManager.registerListener(this, sManager.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);


        super.run();
        socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(dir,port),50);
        } catch (IOException e) {
            //Missatge error connexió.
            try {
                socket.close();
                this.join();
            } catch (InterruptedException e1) {
            } catch (IOException e1) {
            }
        }

        proces();
    }

    private void proces(){
        OutputStream out = null;
        DataOutputStream send = null;
        try {
            out = socket.getOutputStream();
            send = new DataOutputStream(out);
        } catch (IOException e) {
            System.out.println("Error buffer");
        }
        while (true){

            synchronized (lock) {
                while (llista.isEmpty()) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        System.out.println("Error lock");
                    }
                }
            }
            try {
                send.writeFloat(llista.remove(0));
            } catch (IOException e) {
                System.out.println("Error enviar");
            }
        }
    }

    public List getLlista(){
        return llista;
    }

    public Object getLock(){
        return lock;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
