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
    private Context c;
    private TextView tv;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

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
        SensorManager sm = (SensorManager) c.getSystemService(c.SENSOR_SERVICE);
        ImprovedOrientationSensor1Provider s = new ImprovedOrientationSensor1Provider(sm);
    }
}
