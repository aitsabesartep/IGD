package com.example.ssb.android_controler;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private String ip = "192.168.1.13";
    private String port = "1111";
    private double precisio = 1;
    private SensorManager sManager;
    private Connexio s;
    private float current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                ip = data.getExtras().getString("ip");
                port = data.getExtras().getString("port");
                try {
                    precisio = Double.parseDouble(data.getExtras().getString("pre"));
                }catch (Exception IO){
                    precisio = 1;
                }
                if (!ip.equals("null")){

                    //Connecció
                    s = new Connexio(ip, Integer.parseInt(port),this);
                    s.start();
                    iniciGyro();
                }
            }
        }
    }

    private void iniciGyro(){
        //Comensar a procesar
        current = 0;
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this, sManager.getDefaultSensor(android.hardware.Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            Intent i = new Intent(MainActivity.this, Preferencis.class);
            i.putExtra("ip",ip);
            i.putExtra("port",port);
            i.putExtra("pre",precisio);
            this.startActivityForResult(i, 1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }
        float aux = event.values[1];
        int aux1 = (int) ( aux * 1000);
        float fi = ((float) aux1/ (float) 1000);

        float dif = Math.abs(current-fi);

        TextView tv1 = (TextView) this.findViewById(R.id.pantalla1);
        tv1.setText("Orientation Y (Pitch) :" + Float.toString(dif));

        if (dif < precisio){
            return;
        }

        current = fi;

        TextView tv = (TextView) this.findViewById(R.id.pantalla);
        tv.setText("Orientation Y (Pitch) :" + Float.toString(current));

        try {
            s.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //synchronized (s.getLock()){
            s.getLlista().add(current);
            //s.getLock().notifyAll();
        //}
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}