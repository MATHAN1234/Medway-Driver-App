package com.example.matha.medwayhealth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.matha.medwayhealth.Common.Common;

public class profile extends AppCompatActivity {

    TextView Name,email,phone_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Name = (TextView)findViewById(R.id.nametext);
        email   = (TextView)findViewById(R.id.emailtext);
        phone_no = (TextView)findViewById(R.id.phonetext);

        Name.setText(Common.currentUser.getName());
        email.setText(Common.currentUser.getEmail());
        phone_no.setText(Common.currentUser.getPhone());
    }
}
