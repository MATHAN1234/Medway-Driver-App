package com.example.matha.medwayhealth;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.matha.medwayhealth.Common.Common;
import com.example.matha.medwayhealth.Model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Registration extends AppCompatActivity implements View.OnClickListener{

    EditText reg_phone,reg_password,reg_Name,reg_email;
    private FirebaseAuth mAuth;
    private FirebaseDatabase db;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        setContentView(R.layout.activity_registration);
        reg_Name = (EditText)findViewById(R.id.reg_name);
        reg_email = (EditText)findViewById(R.id.reg_email);
        reg_phone = (EditText)findViewById(R.id.reg_phone_text);
        reg_password= (EditText)findViewById(R.id.reg_password_text);
        mAuth= FirebaseAuth.getInstance();
        db= FirebaseDatabase.getInstance();
        users=db.getReference(Common.user_drivers);
        findViewById(R.id.btn_signUp).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId())
        {
            case R.id.btn_signUp:
                registerUser();
                break;
        }
    }

    private void registerUser() {

        final String email= reg_email.getText().toString().trim();
        final String name= reg_Name.getText().toString().trim();
        final String phone= reg_phone.getText().toString().trim();
        final String password= reg_password.getText().toString().trim();

        if(TextUtils.isEmpty(phone))
        {
            Toast.makeText(Registration.this, "Enter Phone number", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(Registration.this, "Enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(name))
        {
            Toast.makeText(Registration.this, "Enter Name", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(Registration.this, "Enter password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email,password.toString().trim()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                User userAdd = new User();
                userAdd.setPhone(phone);
                userAdd.setPassword(password);
                userAdd.setEmail(email);
                userAdd.setName(name);
                users.child(mAuth.getInstance().getCurrentUser().getUid()).setValue(userAdd).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Registration.this, "Sign up Successful", Toast.LENGTH_SHORT).show();

                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Registration.this, "Not Successful"+ e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Registration.this, "Not Successful"+ e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
