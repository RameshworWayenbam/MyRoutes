package com.myroutes;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MapsActivityClient extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final String TAG = "MapsActivityClient";

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    Marker userLocationMarker;
    Marker oldMarker;
    boolean homeMarker = true;
    boolean updateFlag = true;

    PolylineOptions polylineOptions = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
    Polyline polyline;

    private String userName;

    private DatabaseReference myRef;
    private String valueDatabase;
    private String refinedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_client);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, getPackageManager().PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, getPackageManager().PERMISSION_GRANTED);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");

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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        String uname= userName;
        myRef = FirebaseDatabase.getInstance().getReference(uname);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(updateFlag){
                    for(DataSnapshot ds: dataSnapshot.getChildren()){
                        String latstr = ds.child("latitude").getValue().toString();
                        String lonstr = ds.child("longitude").getValue().toString();
                        double lat = Double.parseDouble(latstr);
                        double lon = Double.parseDouble(lonstr);
                        LatLng latLng = new LatLng(lat, lon);
                        setPolyline(latLng);
                        setCurrentMarker(latLng);
                        if(homeMarker){
                            setHomeMarker(latLng);
                            homeMarker=false;
                        }
                    }
                    updateFlag = false;
                } else {
                    Query myQuery = myRef.orderByKey().limitToLast(1);
                    myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            Log.d(TAG,"else of AddValueListerner:");
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                String latstr = ds.child("latitude").getValue().toString();
                                String lonstr = ds.child("longitude").getValue().toString();
                                double lat = Double.parseDouble(latstr);
                                double lon = Double.parseDouble(lonstr);
                                LatLng latLng = new LatLng(lat, lon);
                                setPolyline(latLng);
                                setCurrentMarker(latLng);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPolyline(LatLng latLng){
        polylineOptions.add(latLng);
        polyline = mMap.addPolyline(polylineOptions);
    }

    private void setHomeMarker(LatLng latLng){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.hom_img));
        //markerOptions.rotation(location.getBearing());
        markerOptions.anchor((float) 0.5, (float) 0.5);
        userLocationMarker = mMap.addMarker(markerOptions);
    }

    private void setCurrentMarker(LatLng latLng){
        if(oldMarker != null){
            oldMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.school_bus_client));
        //markerOptions.rotation(location.getBearing());
        markerOptions.anchor((float) 0.5, (float) 0.5);
        userLocationMarker = mMap.addMarker(markerOptions);
        oldMarker = userLocationMarker;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
    }
    private void enableUserLocation(){
        mMap.setMyLocationEnabled(true);
    }
}