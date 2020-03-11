package com.example.google_maps_challenge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


//AppCompatActivity
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        GoogleMap.OnMarkerClickListener{

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 2000;
    private long FASTEST_INTERVAL = 5000;
    private LocationManager locationManager;
    private LatLng latLngC;
    private boolean isPemission;

    private SearchView svLocation;
    private ImageButton btnAddMarker;
    private ImageButton btnAddAim;
    private TextView tvDescription;

    private ArrayList<MarkerOptions> markerOptions;
    private ArrayList<Marker> markers;

    private Marker currentMarker;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        markerOptions = new ArrayList<>();
        markers = new ArrayList<>();

        if(requestSinglePermission()){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            checkLocation();

            svLocation = findViewById(R.id.svLocation);
            btnAddMarker = findViewById(R.id.btnAddMarker);
            btnAddAim = findViewById(R.id.btnAddAim);
            tvDescription = findViewById(R.id.tvDescription);

            svLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {

                    String location = svLocation.getQuery().toString();
                    List<Address> addressesList = null;

                    if (location != null || !location.equals("")) {
                        Geocoder geocoder = new Geocoder(MapsActivity.this);
                        try {
                            addressesList = geocoder.getFromLocationName(location, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Address address = addressesList.get(0);

                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        MarkerOptions marker = new MarkerOptions().position(latLng).title(location);
                        Marker m = mMap.addMarker(marker);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

                        if(!markerOptions.contains(marker)){
                            markerOptions.add(marker);
                            markers.add(m);
                        }
                    }

                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            btnAddMarker.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {

                    String location = svLocation.getQuery().toString();
                    List<Address> addressesList = null;

                    if(location != null || !location.equals("")) {
                        Geocoder geocoder = new Geocoder(MapsActivity.this);
                        try {
                            addressesList = geocoder.getFromLocationName(location, 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Address address = addressesList.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                        MarkerOptions marker = new MarkerOptions().position(latLng).title(location);
                        Marker m =  mMap.addMarker(marker);
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

                        if(!markerOptions.contains(marker)){
                            markerOptions.add(marker);
                            markers.add(m);

                        }
                    }
                }
            }));

            btnAddAim.setOnClickListener((new View.OnClickListener() {
                public void onClick(View v) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngC, 14F));
               }
            }));


        }
    }

    private boolean checkLocation() {
        if(!isLocationEnabled()){
            showAlert();
        }
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable location")
                .setMessage("Your location is set to 'Off'. \n Pleace enable location to use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(i);
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    private boolean requestSinglePermission() {
        Dexter.withActivity(this).withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        isPemission = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                            isPemission = false;
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
        return isPemission;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);

        if(mMap != null){
            mMap.clear();

            for(int i = 0; i < markerOptions.size() ; i++){
                mMap.addMarker(markerOptions.get(i));
            }

            if(latLngC!= null ) {

                MarkerOptions marker = new MarkerOptions().position(latLngC).title("Marker in current location");
                marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.nav));
                currentMarker = mMap.addMarker(marker);


                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngC, 14F));

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED){
            return;
        }
        startLocationUpdates();
        mLocation =LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }else{
            //Toast.makeText(this,"Location not detected",Toast.LENGTH_SHORT).show();
        }

    }

    public void startLocationUpdates(){

    mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(UPDATE_INTERVAL)
            .setFastestInterval(FASTEST_INTERVAL);

    if(ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
    ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        return;
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;

        checkClosest();
        latLngC = new LatLng(location.getLatitude(), location.getLongitude());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void checkClosest(){
        double distanciaMasPequena = 0;
        String texto = "prueba";

        if (markers.size() > 0) {

            for (int i = 0; i < markers.size(); i++) {

                Marker m1 = markers.get(i);
                Marker m2 = currentMarker;
                double distancia = Math.sqrt(Math.pow(m1.getPosition().latitude - m2.getPosition().latitude, 2)
                        + Math.pow(m1.getPosition().longitude - m2.getPosition().longitude, 2));
                distancia *= (111.12 * 1000);
                if (i == 0) {
                    distanciaMasPequena = distancia;
                    texto = m1.getTitle();
                } else {
                    if (distancia < distanciaMasPequena) {

                        distanciaMasPequena = distancia;
                        texto = m1.getTitle();

                    }
                }
            }
            int d = (int) distanciaMasPequena;
            if (d < 60) {

                tvDescription.setText("Usted se encuentra en " + texto);


            } else {

                tvDescription.setText("El lugar más cercano es " + texto);
            }
        }
    }

    public int getDistanceBetweenLocations(Marker m1){

        Marker m2 = currentMarker;
        double distancia = Math.sqrt(Math.pow(m1.getPosition().latitude - m2.getPosition().latitude, 2)
                + Math.pow(m1.getPosition().longitude - m2.getPosition().longitude, 2));
        distancia *= (111.12 * 1000);

        int metros =(int) distancia;

        return metros;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if(!marker.equals(currentMarker)) {
            int metros = getDistanceBetweenLocations(marker);
            marker.setSnippet("Usted se encuentra a " + metros + " metros de distancia");
        }else{

            try {
                Location current = currentLocation;
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation( current.getLatitude(), current.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address address = list.get(0);

                    currentMarker.setSnippet(address.getAddressLine(0));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
