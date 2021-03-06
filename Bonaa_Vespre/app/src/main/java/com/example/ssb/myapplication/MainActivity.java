package com.example.ssb.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    private SensorManager smanager;
    private float[] gData = new float[3];           // Gravity or accelerometer
    private float[] mData = new float[3];           // Magnetometer
    private float[] orientation = new float[3];
    private float[] Rmat = new float[9];            //Matriu de rotacio
    private float[] R2 = new float[9];              //Matriu rotacio
    private float[] Imat = new float[9];
    private boolean haveGrav = false;
    private boolean haveAccel = false;
    private boolean haveMag = false;
    private TextView tv;
    private int left = 0;
    private int right = 0;

    private float[] bufferx;
    private float[] buffery;
    private float[] bufferz;
    private int pos;
    double x180pi = 180.0 / Math.PI;

    private final Semaphore sem = new Semaphore(1, true);
    private final Semaphore sem1 = new Semaphore(1, true);
    private final Semaphore sem2 = new Semaphore(1, true);

    private Button be;
    private Button bd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bufferx = new float[3];
        Arrays.fill(bufferx, 0);
        buffery = new float[3];
        Arrays.fill(buffery, 0);
        bufferz = new float[3];
        Arrays.fill(bufferz, 0);
        pos = 0;

        be = (Button) findViewById(R.id.be);
        be.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sem1.acquire();
                    left = 1;
                    sem1.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        bd = (Button) findViewById(R.id.bd);
        bd.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sem2.acquire();
                    right = 1;
                    sem2.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        smanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        new Thread(){
            public void run(){
                Connexio c = new Connexio();
                c.execute();
            }
        }.start();
    }

    @Override
    protected void onResume() {

        super.onResume();
        // Register our listeners
        Sensor gsensor = smanager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor asensor = smanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor msensor = smanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        smanager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
        smanager.registerListener(this, asensor, SensorManager.SENSOR_DELAY_GAME);
        smanager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] data;
        switch( event.sensor.getType() ) {

            case Sensor.TYPE_GRAVITY:
                gData[0] = event.values[0];
                gData[1] = event.values[1];
                gData[2] = event.values[2];
                haveGrav = true;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                if (haveGrav) break;    // don't need it, we have better
                gData[0] = event.values[0];
                gData[1] = event.values[1];
                gData[2] = event.values[2];
                haveAccel = true;
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mData[0] = event.values[0];
                mData[1] = event.values[1];
                mData[2] = event.values[2];
                haveMag = true;
                break;
            default:
                return;
        }

        if ((haveGrav || haveAccel) && haveMag) {
            SensorManager.getRotationMatrix(Rmat, Imat, gData, mData);
            SensorManager.remapCoordinateSystem(Rmat,
                    SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, R2);
            // Orientation isn't as useful as a rotation matrix, but
            // we'll show it here anyway.
            SensorManager.getOrientation(R2, orientation);
            float incl = SensorManager.getInclination(Imat);
            try {
                sem.acquire();
                //Obtenim radiants i les passam a graus
                bufferx[pos] = (float) (orientation[2] * x180pi);
                buffery[pos] = (float) (orientation[1] * x180pi);
                bufferz[pos] = (float) (orientation[0] * x180pi);
                sem.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pos++;
            if (pos == 3) {
                pos = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class Connexio extends AsyncTask<Void, Void, Void>{

        private Socket sc;
        private DataOutputStream send;

        public Connexio(){
            sc = new Socket();
        }

        @Override
        protected Void doInBackground(Void... params) {


            try {
                sc.connect(new InetSocketAddress("192.168.43.93",7874));
                send = new DataOutputStream(sc.getOutputStream());
                System.out.println("\n\n\n Conectat!!!!!!!!!!! \n\n\n");
            } catch (IOException e) {
                System.out.println("Error conectar socket");
                sc = null;
            }

            while (!this.isCancelled()){
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                float mediax = 0;
                float mediay = 0;
                float mediaz = 0;
                int le = 0;
                int ri = 0;

                try {
                    sem.acquire();
                    for (int i = 0; i < bufferx.length; i++) {
                            mediax = mediax + bufferx[i];
                            mediay = mediay + buffery[i];
                            mediaz = mediaz + bufferz[i];
                    }
                    sem.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    sem1.acquire();
                    le = left;
                    left = 0;
                    sem1.release();
                    sem2.acquire();
                    right=0;
                    ri = right;
                    sem2.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mediax = mediax/bufferx.length;
                mediay = mediay/buffery.length;
                mediaz = mediaz/bufferz.length;

                try {
                    send.write((mediax + "/" + mediay + "/" + mediaz + "/" + le + "/" + ri + "#\n").getBytes());
                    System.out.println(mediax + "/" + mediay + "/" + mediaz + "/" + le + "/" + ri+ "#\n");
                } catch (IOException e) {
                }
            }
            return null;
        }

    }
}
