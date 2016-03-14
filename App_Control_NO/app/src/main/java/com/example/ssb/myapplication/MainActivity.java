package com.example.ssb.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.widget.TextView;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int pos = 0;
    private float [] bufferx = new float[20];
    private float [] buffery = new float[20];
    private float [] bufferz = new float[20];


    private SensorManager mSensorManager = null;

    // Velocitat angular GYROSCOPI
    private float[] gyro = new float[3];

    //Matriu de rotacio de ses dades de GYRO
    private float[] gyroMatrix = new float[9];

    // Angles d'orientacio de sa matriu des GYRO
    private float[] gyroOrientation = new float[3];

    // Vector camp magnetica
    private float[] magnet = new float[3];

    // Vector acelerometro
    private float[] accel = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];

    // Angles d'orientacio a partir de accel y magnet
    private float[] accMagOrientation = new float[3];

    // Orientacio final
    private float[] fusedOrientation = new float[3];

    public static final float FILTER_COEFFICIENT = 0.98f;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init buffer 0
        Arrays.fill(bufferx, 0);
        Arrays.fill(buffery, 0);
        Arrays.fill(bufferz, 0);

        new Thread(){
            @Override
            public void run() {
                Tasca t = new Tasca();
                t.execute();
            }
        }.start();

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        //Inicialitzam gyroMatrix com a matriu identitat
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

        // get sensorManager and initialise sensor listeners
        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initListeners();
    }

    public void initListeners(){
        //Init sensor que registrara ACELEROMETER
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        //Init sensor GYROSCOPE
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        //Init sensor camp magnetic
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void onSensorChanged(SensorEvent event) {

        //Cada cop que el sensor detecta una variacio,
        // mira quinha estat el sensor que ha varitat

        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                //Primer copiam les noves dades de l'accelerometre
                //(source, a partir de 0, desti, desde 0, length 3)
                //Despres calculam la nova orientacio
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                // process gyro data
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                //Copiam les dades del magnometer dins magnet
                //(source, a partir de 0, desti, desde 0, length 3)
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }


    public static final float EPSILON = 0.000000001f;

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;

    public void gyroFunction(SensorEvent event) {
        // don't start until first accelerometer/magnetometer orientation has been acquired
        if (accMagOrientation == null)
            return;

        // initialisation of the gyroscope based rotation matrix
        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // copy the new gyro values into the gyro array
        // convert the raw gyro data into a rotation vector
        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        // measurement done, save current time for next interval
        timestamp = event.timestamp;

        // convert rotation vector into rotation matrix
        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        // apply the new rotation interval on the gyroscope based rotation matrix
        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        // get the gyroscope based orientation from the rotation matrix
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private class Tasca extends AsyncTask {

        private Socket s;
        private DataOutputStream send;

        @Override
        protected void onPreExecute() {
            try {
                s = new Socket();
                s.connect(new InetSocketAddress("192.168.12.205",7874),1000);
                send = new DataOutputStream(s.getOutputStream());
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        @Override
        protected Object doInBackground(Object[] params) {
            while (!this.isCancelled()) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
                fusedOrientation[0] =
                        FILTER_COEFFICIENT * gyroOrientation[0]
                                + oneMinusCoeff * accMagOrientation[0];

                fusedOrientation[1] =
                        FILTER_COEFFICIENT * gyroOrientation[1]
                                + oneMinusCoeff * accMagOrientation[1];

                fusedOrientation[2] =
                        FILTER_COEFFICIENT * gyroOrientation[2]
                                + oneMinusCoeff * accMagOrientation[2];

                // overwrite gyro matrix and orientation with fused orientation
                // to comensate gyro drift
                gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
                System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

                bufferx[pos] = fusedOrientation[0];
                buffery[pos] = fusedOrientation[1];
                bufferz[pos] = fusedOrientation[2];
                pos++;
                if (pos == 20) {
                    pos = 0;
                }

                float mediax = 0;
                float mediay = 0;
                float mediaz = 0;

                for (int i = 0; i < bufferx.length; i++) {
                    mediax = mediax + bufferx[i];
                    mediay = mediay + buffery[i];
                    mediaz = mediaz + bufferz[i];
                }

                mediax = mediax / bufferx.length;
                mediay = mediay / buffery.length;
                mediaz = mediaz / bufferz.length;


                try {
                    String s= "Valor[Y]: "+mediay+" Valor[X]: "+mediax+" Valor[Z]"+mediaz+"\n";
                    send.write(s.getBytes());
                } catch (Exception e) {
                    System.out.println(fusedOrientation[0]);
                    System.out.println(fusedOrientation[0]);
                    System.out.println(fusedOrientation[0]);
                }
            }
            return 1;
        }

        @Override
        protected void onProgressUpdate(Object[] params) {
        }

        @Override
        protected void onCancelled () {
        }
    }
}