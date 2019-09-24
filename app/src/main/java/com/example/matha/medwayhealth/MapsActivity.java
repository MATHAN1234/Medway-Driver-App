package com.example.matha.medwayhealth;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.example.matha.medwayhealth.Common.Common;
import com.example.matha.medwayhealth.Model.Token;
import com.example.matha.medwayhealth.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.github.glomadrian.materialanimatedswitch.MaterialAnimatedSwitch;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class MapsActivity extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;

    FusedLocationProviderClient fusedLocationProviderClient;
    com.google.android.gms.location.LocationCallback locationCallback;
    //play services
    private static final int MY_PERMISSION_REQUEST_CODE = 7000;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;



    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference drivers;
    GeoFire geofire;

    Marker mCurrent;


    MaterialAnimatedSwitch loc_switch;
    //car animation
    private List<LatLng> polyLineList;
    private Marker CarMarker;
    private float v;
    private double lat,lng;
    private Handler handler;
    private LatLng startPosition,endPostition,currentPosition;
    private int index,next;

    //: Extra
    private Button btnGo;


    private PlaceAutocompleteFragment places;
    AutocompleteFilter typefilter;

    private String destination;
    private PolylineOptions polylineOptions,blackPolylineOptions;
    private Polyline blackPolyline, greyPolyline;

    private IGoogleAPI mServices;




    DatabaseReference onlineRef, currentUserRef;

    Runnable drawPathRunnable= new Runnable() {
        @Override
        public void run() {
            if(index<polyLineList.size()-1)
            {
                index++;
                next =  index+1;
            }
            if(index<polyLineList.size()-1)
            {
                startPosition= polyLineList.get(index);
                endPostition= polyLineList.get(next);
            }

            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0,1);
            valueAnimator.setDuration(3000);
            valueAnimator.setInterpolator( new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    v= valueAnimator.getAnimatedFraction();
                    lng=v*endPostition.longitude+(1-v)*startPosition.longitude;
                    lat=v*endPostition.latitude+(1-v)*startPosition.latitude;
                    LatLng newPos= new LatLng(lat,lng);

                    CarMarker.setPosition(newPos);
                    CarMarker.setAnchor(0.5f,0.5f);
                    CarMarker.setRotation(getBearing(startPosition,newPos));
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(newPos)
                                    .zoom(15.5f)
                                    .build()

                    ));

                }
            });

            valueAnimator.start();
            handler.postDelayed(this,3000);

        }
    };

    private float getBearing(LatLng startPostion, LatLng endPosition) {
        double lat = Math.abs(startPostion.latitude-endPosition.latitude);
        double lng = Math.abs(startPostion.longitude-endPosition.longitude);

        if(startPostion.latitude<endPosition.latitude&& startPostion.longitude<endPosition.longitude)
        {
            return (float)(Math.toDegrees(Math.atan(lng/lat)));
        }
        else if(startPostion.latitude>=endPosition.latitude && startPostion.longitude<endPosition.longitude)
        {
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+90);
        }
        else if(startPostion.latitude>=endPosition.latitude && startPostion.longitude>=endPosition.longitude)
        {
            return (float)(Math.toDegrees(Math.atan(lng/lat))+180);
        }
        else if(startPostion.latitude<endPosition.latitude && startPostion.longitude>=endPosition.longitude)
        {
            return (float)((90-Math.toDegrees(Math.atan(lng/lat)))+270);

        }
        return -1;

    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        onlineRef=FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.drivers).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //remove value from driver table when driver is disconnected
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        loc_switch = (MaterialAnimatedSwitch) view.findViewById(R.id.switch_button);

        loc_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(boolean isOnline) {
                if (isOnline) {

                    FirebaseDatabase.getInstance().goOnline(); //set connected when switch is on
                    if (ActivityCompat.checkSelfPermission(MapsActivity.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(MapsActivity.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    buildLocationCallBack();
                    buildLocationRequest();
                    fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());
                    displayLocation();
                    Snackbar.make(view,"You are Online", Snackbar.LENGTH_SHORT).show();
                } else {

                    FirebaseDatabase.getInstance().goOffline();
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    mCurrent.remove();
                    mMap.clear();
                    if(handler != null) {
                        handler.removeCallbacks(drawPathRunnable);
                    }
                    Snackbar.make(view,"You are Offline", Snackbar.LENGTH_SHORT).show();

                }


            }

        });

        polyLineList = new ArrayList<>();

        //Places APIs
      //  typefilter = new  AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).setTypeFilter(3).build();
        places = (PlaceAutocompleteFragment) getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        places.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if(loc_switch.isChecked())
                {
                    destination = place.getAddress().toString();
                    destination = destination.replace(" ","+");// replace space with + for fetch data
                    getDirection();
                }
                else {
                    Toast.makeText(MapsActivity.this.getContext(), "Please make your self online", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Status status) {
                Toast.makeText(MapsActivity.this.getContext(), ""+status.toString(), Toast.LENGTH_SHORT).show();
            }
        });








        //:: Extra

        //Geo fire
        drivers = FirebaseDatabase.getInstance().getReference(Common.drivers);
        geofire = new GeoFire(drivers);

        setUpLocation();
        mServices= Common.getGoogleAPI();


        updateFirebaseToken();
    }

    private void updateFirebaseToken() {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens= db.getReference(Common.token_tbl);

        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.content_navigation,container,false);
        SupportMapFragment fragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        fragment.getMapAsync(this);



        ///init view

        return view;


    }

    private void getDirection() {
        currentPosition = new LatLng(Common.mLastLocation.getLatitude(),Common.mLastLocation.getLongitude());

        String requestApi = null;
        try{
            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+currentPosition.latitude+","+currentPosition.longitude+"&"+
                    "destination="+destination+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);

            Log.d("MEDWAY",requestApi);

            mServices.getPath(requestApi).enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");

                        for(int i=0;i<jsonArray.length();i++)
                        {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);
                        }

                        //Adjust bound
                        LatLngBounds.Builder builder=  new LatLngBounds.Builder();
                        for(LatLng latLng:polyLineList)
                            builder.include(latLng);
                        LatLngBounds bounds = builder.build();
                        CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds,2);
                        mMap.animateCamera(mCameraUpdate);

                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.GRAY);
                        polylineOptions.width(5);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.endCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polyLineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);


                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.endCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);

                        mMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size()-1))
                                .title("pickup Location"));

                        //Animation
                        ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0,100);
                        polyLineAnimator.setDuration(2000);
                        polyLineAnimator.setInterpolator(new LinearInterpolator());
                        polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                List<LatLng> points= greyPolyline.getPoints();
                                int percentValue= (int)valueAnimator.getAnimatedValue();
                                int size = points.size();
                                int newPoints= (int)(size*(percentValue/100.0f));
                                List<LatLng> p= points.subList(0,newPoints);
                                blackPolyline.setPoints(p);
                            }
                        });
                        polyLineAnimator.start();
                        CarMarker = mMap.addMarker(new MarkerOptions().position(currentPosition)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.amb)));

                        handler = new Handler();
                        index=-1;
                        next=1;
                        handler.postDelayed(drawPathRunnable,3000);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {
                    Toast.makeText(MapsActivity.this.getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();

        }
    }

    private  List decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
    //press ctrl+O for runtime permission function override


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    buildLocationCallBack();
                    buildLocationRequest();
                    if(loc_switch.isChecked())

                        displayLocation();
                }

        }
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request runtime permission
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }
        else{

            buildLocationRequest();
            buildLocationCallBack();
            if(loc_switch.isChecked())
            {
                displayLocation();
            }
        }

    }

    private void buildLocationCallBack() {
        locationCallback = new com.google.android.gms.location.LocationCallback(){

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                for(Location location:locationResult.getLocations())
                {
                    Common.mLastLocation = location;
                }
                displayLocation();
            }
        };
    }

    private void buildLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);


    }


    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return; }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Common.mLastLocation = location;
                        if (Common.mLastLocation!= null) {
                            if (loc_switch.isChecked()) {
                                final double latitude = Common.mLastLocation.getLatitude();
                                final double longitude = Common.mLastLocation.getLongitude();
                                LatLng center = new LatLng(latitude,longitude);
                                LatLng northside = SphericalUtil.computeOffset(center,150000,0);
                                LatLng southside = SphericalUtil.computeOffset(center,150000,180);
                                LatLngBounds bounds = LatLngBounds.builder().include(northside).include(southside).build();
                                //Update to firebase
                                geofire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {
                                        //Add marker
                                        if (mCurrent != null)
                                            mCurrent.remove();//remove already marker
                                        mCurrent = mMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude, longitude))
                                                .title("Driver"));
                                        //move camera to this position
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
                                }
                                });
                            }
                        } else
                        { Log.d("ERROR", "cannot display Location ");
                        }
                    }
                }); }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap= googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        //: Update Location
        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            return;
        }
        buildLocationRequest();
        buildLocationCallBack();
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest,locationCallback,Looper.myLooper());

    }



}
