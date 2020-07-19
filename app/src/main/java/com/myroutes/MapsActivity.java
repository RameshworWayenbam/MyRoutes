package com.myroutes;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MapsActivity";
    int LOCATION_REQUEST_CODE = 1001;

    private String userName;

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;

    Marker userLocationMarker;
    Circle userLocationAccuracyCircle;

    PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
    Polyline polyline;

    private DatabaseReference myRef;
    long maxid=0;

    LocationCallback locationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null){
                return;
            };
            for(Location location: locationResult.getLocations()){
                Log.d(TAG,"onLocationResult: "+ location.toString());
            }
            if(mMap != null){
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, getPackageManager().PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, getPackageManager().PERMISSION_GRANTED);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        //Toast.makeText(getApplicationContext(),"User Name....." + userName, Toast.LENGTH_LONG).show();

        //re-setting the data on login
        try {
            myRef = FirebaseDatabase.getInstance().getReference(userName);
            myRef.removeValue();
        }catch (Exception e){
            e.printStackTrace();
        }

        startThread();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//      mMap.setMapType(googleMap.MAP_TYPE_SATELLITE);

//        // Add a marker in Sydney and move the camera
//      LatLng sydney = new LatLng(-34, 151);
//      mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//      mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //enableUserLocation();

//      mMap.clear();
//      LatLng latLng = new LatLng(12.87417163, 77.66795397);
//      MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Home").snippet("Wonder of the world").icon(BitmapDescriptorFactory.fromResource(R.drawable.hom_img));
//      mMap.addMarker(markerOptions);
//      CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 16);
//      mMap.animateCamera(cameraUpdate);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.
                PERMISSION_GRANTED)
        {
            //getLastLocation();
            checkSettingsAndStartLocationUpdates();
        } else {
            askLocationPermission();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }
    private void checkSettingsAndStartLocationUpdates(){
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);

        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //setting of devices are satisfied and we can start the location updates
                startLocationUpdates();
            }
        });

        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    ResolvableApiException apiException = (ResolvableApiException) e;
                    try{
                        apiException.startResolutionForResult(MapsActivity.this, 1001);
                    }catch (IntentSender.SendIntentException ex){
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
    private void setUserLocationMarker(Location location){

        LatLng latLng =  new LatLng(location.getLatitude(), location.getLongitude());

        if(userLocationMarker == null){
            // create new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school_bus));
            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            userLocationMarker = mMap.addMarker(markerOptions);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            polylineOptions.add(latLng);
            polyline = mMap.addPolyline(polylineOptions);

            homeMarker(latLng);
            insertData(latLng);
        } else {
            // use previousely created marker
            userLocationMarker.setPosition(latLng);
            userLocationMarker.setRotation(location.getBearing());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
            polylineOptions.add(latLng);
            polyline = mMap.addPolyline(polylineOptions);
            insertData(latLng);
        }

        if(userLocationAccuracyCircle == null){
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(latLng);
            circleOptions.strokeWidth(4);
            circleOptions.strokeColor(Color.argb(255,255,0,0));
            circleOptions.fillColor(Color.argb(32, 255,0, 0));
            circleOptions.radius(location.getAccuracy());
            userLocationAccuracyCircle = mMap.addCircle(circleOptions);
        }else{
            userLocationAccuracyCircle.setCenter(latLng);
            userLocationAccuracyCircle.setRadius(location.getAccuracy());
        }
    }
    private void enableUserLocation(){
        mMap.setMyLocationEnabled(true);
    }

    private void homeMarker(LatLng latLng){
        mMap.addMarker(new MarkerOptions().position(latLng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.hom_img))
                .title("Home")
                .snippet("Home Sweet Home"));
    }

    private void insertData(LatLng latLng){
        String uname = userName;
        myRef = FirebaseDatabase.getInstance().getReference(uname);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                    maxid = (dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        maxid = maxid+1;
        myRef.child(String.valueOf(maxid)).setValue(latLng);
    }

    private void startLocationUpdates(){
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void getLastLocation(){
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    //we have location
                    Log.d(TAG, "on Suceess" + location.toString());
                    Log.d(TAG, "on Suceess" + location.getLatitude());
                    Log.d(TAG, "on Suceess" + location.getLongitude());
                }else{
                    Log.d(TAG, "on Success : Location was null");
                }

            }
        });

        locationTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "onFailure: "+ e.getLocalizedMessage());
            }
        });
    }
    private void askLocationPermission(){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.
                PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))
            {
                Log.d(TAG,"askLocationPermission: you sould show the alert message");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST_CODE);
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //permission granted
                //getLastLocation();
                checkSettingsAndStartLocationUpdates();
            } else {
                //permission not granted
            }
        }
    }

    // Service codes
    private Button buttonStartThread;
    private Handler mainHandler = new Handler();
    private volatile boolean stopThread = false;

    public void startThread() {
        stopThread = false;
        ExampleRunnable runnable = new ExampleRunnable(100);
        new Thread(runnable).start();
    }
    public void stopThread() {
        stopThread = true;
    }
    class ExampleThread extends Thread {
        int seconds;
        ExampleThread(int seconds) {
            this.seconds = seconds;
        }
        @Override
        public void run() {
            for (int i = 0; i < seconds; i++) {
                Log.d(TAG, "startThread: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class ExampleRunnable implements Runnable {
        int seconds;
        ExampleRunnable(int seconds) {
            this.seconds = seconds;
        }
        @Override
        public void run() {
            int lt;
            for (int i = 1; i <= seconds; i++) {
                lt = i;
                if (stopThread)
                    return;
                if (true) {
                    final int finalLt = lt;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //buttonStartThread.setText("50%");
                            //buttonStartThread.setText(finalLt +"%");
                            startLocationUpdates();
                            getLastLocation();
                        }
                    });
                }
                Log.d(TAG, "startThread: " + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
