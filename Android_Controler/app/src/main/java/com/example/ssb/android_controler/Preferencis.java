package com.example.ssb.android_controler;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Preferencis extends Activity {

    private TextView dir;
    private TextView port;
    private TextView p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferencis);

        dir = (TextView) this.findViewById(R.id.dir);
        port = (TextView) this.findViewById(R.id.port);
        p = (TextView) this.findViewById(R.id.pre);
        dir.setText(getIntent().getExtras().getString("ip"));
        port.setText(getIntent().getExtras().getString("port"));
        p.setText(String.valueOf(getIntent().getExtras().getDouble("precisio")));


        Button b = (Button) this.findViewById(R.id.a);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //MainActivity.ip = dir.getText().toString();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("ip", dir.getText().toString());
                resultIntent.putExtra("port", port.getText().toString());
                resultIntent.putExtra("pre", p.getText().toString());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        Button c = (Button) this.findViewById(R.id.c);
        c.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_CANCELED, getIntent());
                finish();
            }
        });
    }
}
