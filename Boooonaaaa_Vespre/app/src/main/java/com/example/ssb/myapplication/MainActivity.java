package com.example.ssb.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements SensorEventListener {


    SensorManager smanager;
    float[] gData = new float[3];           // Gravity or accelerometer
    float[] mData = new float[3];           // Magnetometer
    float[] orientation = new float[3];
    float[] Rmat = new float[9];            //Matriu de rotacio
    float[] R2 = new float[9];              //Matriu rotacio
    float[] Imat = new float[9];
    boolean haveGrav = false;
    boolean haveAccel = false;
    boolean haveMag = false;
    TextView tv;

    float[] bufferx;
    float[] buffery;
    float[] bufferz;
    int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bufferx = new float[20];
        Arrays.fill(bufferx, 0);
        buffery = new float[20];
        Arrays.fill(buffery, 0);
        bufferz = new float[20];
        Arrays.fill(bufferz, 0);
        pos = 0;

        tv = (TextView) findViewById(R.id.text);
        smanager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
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

            //Obtenim radiants i les passam a graus
            double x180pi = 180.0 / Math.PI;
            bufferx[pos] = (float) (orientation[2] * x180pi);
            buffery[pos] = (float) (orientation[1] * x180pi);
            bufferz[pos] = (float) (orientation[0] * x180pi);
            pos++;
            if (pos == 20){
                pos = 0;
            }
            float mediax = 0;
            float mediay = 0;
            float mediaz = 0;
            for (int i = 0; i < bufferx.length; i++){
                mediax = mediax + bufferx[i];
                mediay = mediay + buffery[i];
                mediaz = mediaz + bufferz[i];
            }

            mediax = mediax/bufferx.length;
            mediay = mediay/buffery.length;
            mediaz = mediaz/bufferz.length;

            tv.setText("Y: " + mediay+
            "\n\nX: "+mediax+
            "\n\nZ: "+mediaz);

            //Log.d(TAG, "mh: " + (int)(orientation[0]*DEG));
            //Log.d(TAG, "pitch: " + (int) (orientation[1]*DEG));
            //Log.d(TAG, "roll: " + (int)(orientation[2]*DEG));
            //Log.d(TAG, "yaw: " + (int)(orientation[0]*DEG));
            //Log.d(TAG, "inclination: " + (int)(incl*DEG));
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
