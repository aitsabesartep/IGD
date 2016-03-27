package com.example.ssb.igd_final;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox repeatChkBx = ( CheckBox ) findViewById( R.id.check_media );
        repeatChkBx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    findViewById(R.id.table_media).setVisibility(View.VISIBLE);
                } else {
                    findViewById(R.id.table_media).setVisibility(View.INVISIBLE);
                    TextView tx = (TextView) findViewById(R.id.tam_txt);
                    tx.setText("1");
                }

            }
        });

        final Button button = (Button) findViewById(R.id.guardarBtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView tx;
                Intent intent = new Intent(MainActivity.this, Proces.class);
                tx = (TextView) findViewById(R.id.ip_txt);
                intent.putExtra("ip",tx.getText().toString());
                tx = (TextView) findViewById(R.id.puerto_txt);
                intent.putExtra("port",tx.getText().toString());
                tx = (TextView) findViewById(R.id.frec_txt);
                intent.putExtra("frecuencia",tx.getText().toString());
                tx = (TextView) findViewById(R.id.tam_txt);
                intent.putExtra("buffer",tx.getText().toString());
                startActivity(intent);
            }
        });
    }
}
