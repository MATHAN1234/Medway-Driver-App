package com.example.matha.medwayhealth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Contactus extends AppCompatActivity {
    Button sbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactus);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        sbtn = (Button) findViewById(R.id.mail);
        sbtn.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                Intent intent = new Intent(Contactus.this, Contactthroughmail.class);
                startActivity(intent);
            }
        });
    }
}
