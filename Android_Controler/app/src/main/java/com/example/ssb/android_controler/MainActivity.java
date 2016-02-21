package com.example.ssb.android_controler;

import android.app.Activity;
import android.content.Context;
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

public class MainActivity extends AppCompatActivity{

    private String ip = "192.168.1.133";
    private String port = "7874";
    private double precisio = 5;
    private Context c;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        c = this;
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
                    new Thread(){
                        @Override
                        public void run() {
                            new Connect(ip,Integer.parseInt(port),precisio,c).execute();
                        }
                    }.start();
                }
            }
        }
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
            this.startActivityForResult(i, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}