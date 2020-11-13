 package com.example.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                    Location lastKnownLocation = (Location) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                    if(lastKnownLocation != null) {
                        centerOnMapLocation(lastKnownLocation, "Your Location");
                    }
                }

            }
        }
    }

    public void centerOnMapLocation (Location location, String title) {

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,12));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {   //used when some location is wrong pressed
            @Override
            public void onMapLongClick(LatLng latLng) {


                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                String address = "";

                try {

                    List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

                    if(addressList != null && addressList.size() > 0) {

                        if(addressList.get(0).getThoroughfare() != null) {
                            if(addressList.get(0).getSubThoroughfare() != null) {
                                address += addressList.get(0).getSubThoroughfare() + " ";
                            }

                            address += addressList.get(0).getThoroughfare() + " ";
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(address.equals("")) {

                    SimpleDateFormat form = new SimpleDateFormat("dd/MM/yyyy\nHH:mm:ss");
                    Date currentTime = Calendar.getInstance().getTime();

                    address += form.format(new Date());
                }

                mMap.addMarker(new MarkerOptions().position(latLng).title(address));

                MainActivity.listOfPlaces.add(address);

                MainActivity.locations.add(latLng);

                MainActivity.arrayAdapter.notifyDataSetChanged();

                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.memorableplaces",Context.MODE_PRIVATE);

                try{

                    ArrayList<String> latitudes = new ArrayList<>();
                    ArrayList<String> longitudes = new ArrayList<>();

                    for(int i = 0; i < MainActivity.locations.size(); i++) {
                        latitudes.add(Double.toString(MainActivity.locations.get(i).latitude));
                        longitudes.add(Double.toString(MainActivity.locations.get(i).longitude));
                    }

                    sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.listOfPlaces)).apply();
                    sharedPreferences.edit().putString("latitude",ObjectSerializer.serialize(latitudes)).apply();
                    sharedPreferences.edit().putString("longitude",ObjectSerializer.serialize(longitudes)).apply();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Toast.makeText(MapsActivity.this, "Location Saved!", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = getIntent();
        int placeNumber = intent.getIntExtra("placeNumber",0);

        if(placeNumber == 0) {

            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                        centerOnMapLocation(location,"Your Location");
                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);

            } else {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10,locationListener);

                Location lastKnownLocation = (Location) locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                if(lastKnownLocation != null) {
                    centerOnMapLocation(lastKnownLocation, "Your Location");
                }
            }

        } else {

            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("placeNumber",0)).longitude);

            centerOnMapLocation(placeLocation,MainActivity.listOfPlaces.get(intent.getIntExtra("placeNumber",0)));

        }

    }
}