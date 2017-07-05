package com.example.zack.hydrationreminder;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import static com.example.zack.hydrationreminder.R.id.map;

public class WaterPoints extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 88;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_points);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        // go back to Visualizer activity on clicking back button
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


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
        ArrayList<LatLng> latlngs = new ArrayList<>();
        MarkerOptions options = new MarkerOptions();
        latlngs.add(new LatLng(1.2921, 36.8219));
        latlngs.add(new LatLng(1.3101, 36.8125));
        latlngs.add(new LatLng(1.2804, 36.8163));
        latlngs.add(new LatLng(1.1800, 36.9349));


        mMap = googleMap;
        setupPermissions();

        LatLng pos = new LatLng(1.3101, 36.8125);



        for (LatLng point : latlngs) {
            options.position(point);
            options.title("Water Point");
            options.snippet("GLUG GLUG");
            googleMap.addMarker(options);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
    }

    // App permissions for Map
    private void setupPermissions(){
        //If we don't have permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // And if we are on Android M or later
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                // Ask Nicely for Permissions
                String [] permissionsWeNeed = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                requestPermissions(permissionsWeNeed, MY_PERMISSION_ACCESS_FINE_LOCATION);
            } else {
                // Permissions were granted
                mMap.setMyLocationEnabled(true);

            }
        } else {
            // Permissions were granted
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The permission was granted! Start up the visualizer!


                } else {
                    Toast.makeText(this, "Permission for Maps not granted. Application can't run.", Toast.LENGTH_LONG).show();
                    finish();
                    // The permission was denied, so we can show a message why we can't run the app
                    // and then close the app.
                }
            }
            // Other permissions could go down here

        }
    }


}
