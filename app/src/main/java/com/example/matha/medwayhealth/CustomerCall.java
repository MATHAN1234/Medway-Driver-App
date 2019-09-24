package com.example.matha.medwayhealth;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.matha.medwayhealth.Common.Common;
import com.example.matha.medwayhealth.Model.FCMResponse;
import com.example.matha.medwayhealth.Model.Notification;
import com.example.matha.medwayhealth.Model.Sender;
import com.example.matha.medwayhealth.Model.Token;
import com.example.matha.medwayhealth.Remote.IFCMService;
import com.example.matha.medwayhealth.Remote.IGoogleAPI;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {
    TextView textTime,textDistance, textAddress;
    MediaPlayer mediaPlayer;
    Button accept,cancel;

    IGoogleAPI mServices;
    IFCMService mFCMService;
    String CustomerId;

    double lat,lng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mServices= Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        textAddress = (TextView)findViewById(R.id.TextAddress);
        textDistance = (TextView)findViewById(R.id.TextDistance);
        textTime = (TextView)findViewById(R.id.TextTime);

        accept = (Button) findViewById(R.id.accept);
        cancel = (Button) findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(CustomerId))
                {
                    cancelBooking(CustomerId);
                }
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!TextUtils.isEmpty(CustomerId))
                {
                    AcceptBooking(CustomerId);
                }


            }
        });

        mediaPlayer = MediaPlayer.create(this,R.raw.uberbeep);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();


        if(getIntent()!=null)
        {

            lat=getIntent().getDoubleExtra("lat",-1.0);
            lng=getIntent().getDoubleExtra("lng",-1.0);
            CustomerId = getIntent().getStringExtra("customer");
            getDirection(lat,lng);
        }




    }

    private void AcceptBooking(String customerId) {
        Intent intent = new Intent(CustomerCall.this,DriverTracking.class);
        intent.putExtra("lat",lat);
        intent.putExtra("lng",lng);
        intent.putExtra("CustomerId",CustomerId);
        Token token = new Token(CustomerId);
        Notification notification= new Notification("Accept",
                "Ambulance Driver has Accept your request");
        Sender sender = new Sender(token.getToken(), notification);
        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                if(response.body().success==1)
                {
                    Toast.makeText(CustomerCall.this, "Accepted", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {
            }
        });
        startActivity(intent);
        finish();
    }

    private void cancelBooking(String customerId) {

        Token token = new Token(CustomerId);
        Notification notification= new Notification("Cancel", "Driver has cancelled your request");
        Sender sender = new Sender(token.getToken(),notification);

        mFCMService.sendMessage(sender).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {

                if(response.body().success==1)
                {

                    Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });

    }

    private void getDirection(double lat,double lng) {
        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastLocation.getLatitude()+","+Common.mLastLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("MEDWAY",requestApi);
            mServices.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray routes = jsonObject.getJSONArray("routes");
                        JSONObject object = routes.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legsobject = legs.getJSONObject(0);
                        //Distance
                        JSONObject distance = legsobject.getJSONObject("distance");
                        textDistance.setText(distance.getString("text"));
                        //Time
                        JSONObject Time = legsobject.getJSONObject("duration");
                        textTime.setText(Time.getString("text"));
                        //Address
                        String address = legsobject.getString("end_address");
                        textAddress.setText(address);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable throwable) {
                    Toast.makeText(CustomerCall.this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }


    }

