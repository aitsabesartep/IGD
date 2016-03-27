package com.example.ssb.igd_bt;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;
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

    private static int pos;
    double x180pi = 180.0 / Math.PI;

    public static Semaphore sem = new Semaphore(1, true);
    public static Semaphore sem1 = new Semaphore(1, true);
    public static Semaphore sem2 = new Semaphore(1, true);
    public static int left = 0;
    public static int right = 0;
    public static float[] bufferx;
    public static float[] buffery;
    public static float[] bufferz;

    public static boolean init = false;
    public static int tam = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //init
        smanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, PreferenceScreen.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (!init) return;
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
            if (pos == 1) {
                pos = 0;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static void init(){
        //init
        bufferx = new float[tam];
        Arrays.fill(bufferx, 0);
        buffery = new float[tam];
        Arrays.fill(buffery, 0);
        bufferz = new float[tam];
        Arrays.fill(bufferz, 0);
        pos = 0;
    }
}
