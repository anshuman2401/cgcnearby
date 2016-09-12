package com.anshuman.cgcnearby;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BottomSheetBehavior bottomSheetBehavior;
    final private static int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    LocationManager locationManager;
    Location location;
    String provider = locationManager.GPS_PROVIDER;
    Marker marker;
    private ArrayList<LocationModel> locationList;
    final private static String get_locations = "http://www.anshumankaushik.in/cgcnearby/getlocations.php";
    ImageView moreInfoImageVew;
    LatLng latlnginfo;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Initialising locationList which will contains all places
        locationList = new ArrayList<LocationModel>();

        //button that will give more information about any marker
        moreInfoImageVew = (ImageView) findViewById(R.id.moreInfoImageView);

        //Bottom sheet on which info will be displayed
        View bottomSheet = findViewById(R.id.bottom_sheet);

        //as name tells it is a location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Bottom sheet behavior is checking for bottomsheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        //starting height for bottom sheet
        bottomSheetBehavior.setPeekHeight(0);

        //Starting state of bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setPeekHeight(0);
                    //if bottom sheet is swipped down then make its height to 0
                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });

        ImageView currentImageView = (ImageView) findViewById(R.id.currentImageView);

        currentImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                new find().run();
            }
        });

        //Getting and saving all location list
        addLocationstoList();

        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 100, 1, locationListener);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in default location and move the camera
        LatLng sydney = new LatLng(-27, 12);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Getting Location..."));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                permissionManager();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //set visible to more info button
                moreInfoImageVew.setVisibility(View.VISIBLE);

                //getting marker postion
                latlnginfo = marker.getPosition();

                moreInfoImageVew.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Showing bottom sheet when more info button is clicked
                        BottomSheetDialogFragment bottomSheetDialogFragment = new BottomSheet();
                        bottomSheetDialogFragment.show(getSupportFragmentManager(), bottomSheetDialogFragment.getTag());

                        //sending location of marker to fragment
                        Bundle bundle = new Bundle();
                        bundle.putString("latitude", String.valueOf(latlnginfo.latitude));
                        bundle.putString("longitude", String.valueOf(latlnginfo.longitude));
                        bottomSheetDialogFragment.setArguments(bundle);
                    }
                });

                return false;
            }
        });
    }

    public void jumpToLocation() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //getting location from network provider
         locationListener = new MyLocationListener();

        location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);

        //if location is available go to method onLocationChanged
        if (location != null) {

            locationListener.onLocationChanged(location);

        } else {

            //requesting location from Network provider
            locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 400, 1, locationListener);

            //if location is not available
            Toast.makeText(MapsActivity.this, "Waiting for location...", Toast.LENGTH_SHORT).show();

        }
    }

    public void addLocationstoList() {

        //Showing progress dialog
        final ProgressDialog dialog = ProgressDialog.show(MapsActivity.this, "", "Searching for Places...", false, false);
        final StringRequest request = new StringRequest(Request.Method.POST, get_locations, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                dialog.dismiss();

                try {

                    locationList.clear();
                    //Making array
                    JSONArray array = new JSONArray(response);

                    for (int i = 0; i < array.length(); i++) {

                        //getting one by one location details and adding to location list
                        JSONObject object = array.getJSONObject(i);

                        LocationModel locationModel = new LocationModel();

                        locationModel.setName(object.getString("name"));

                        locationModel.setLatitude(object.getString("latitude"));

                        locationModel.setLongitude(object.getString("longitude"));

                        locationList.add(locationModel);

                    }

                    //After getting location, show location
                    showLocations();

                } catch (JSONException e) {

                    e.printStackTrace();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                dialog.dismiss();

                Toast.makeText(MapsActivity.this, "Something went wrong! Please try again later.", Toast.LENGTH_SHORT).show();

            }
        });

        //sending request
        MySingleton.getInstance(getApplicationContext()).addRequsetQueue(request);
    }

    public void showLocations() {

        for (int i = 0; i < locationList.size(); i++) {

            //getting location one by one from locationList
            LocationModel locationModel = locationList.get(i);

            LatLng yourLocation = new LatLng(Double.parseDouble(locationModel.getLatitude()), Double.parseDouble(locationModel.getLongitude()));

            //Adding marker respect to gotted locations
            mMap.addMarker(new MarkerOptions().position(yourLocation).title(locationModel.getName()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        }

    }

    //called when app resumes
    @Override
    protected void onResume() {
        super.onResume();

        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //requesting location from gps
        locationManager.requestLocationUpdates(provider, 400, 1, locationListener);
    }

    //called when app minimized
    @Override
    protected void onPause() {
        super.onPause();

        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        //Stop reciving updates
        locationManager.removeUpdates(locationListener);
    }

    public void permissionManager() {

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);


            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MapsActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MapsActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

            }
        } else {

            //if permission is previously granted
            jumpToLocation();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }

                    //If permission is granted
                    jumpToLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getApplicationContext(), "Location Permission Required", Toast.LENGTH_SHORT).show();
                    permissionManager();

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    class MyLocationListener implements LocationListener {


        @Override
        public void onLocationChanged(Location location) {

            //set marker to present location
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
            marker.setPosition(latlng);
            marker.setTitle("Your Location");

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    public class find extends Thread {

        public find() {

            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) {
            }

            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) {
            }

            // don't start listeners if no provider is enabled
            if (!gps_enabled && !network_enabled) {
                Toast.makeText(getApplicationContext(), "Sorry, location is not determined. Please enable location providers", Toast.LENGTH_LONG).show();

            }

        }

        @Override
        public void run() {

            locationListener = new MyLocationListener();

            if (gps_enabled) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 1, locationListener);
            }
            if (network_enabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 500, 1, locationListener);
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.list_view:

                LatLng latLng = marker.getPosition();

                SharedPreferences sharedPreferences = getSharedPreferences("currentLocation", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("latitude", String.valueOf(latLng.latitude));
                editor.putString("longitude", String.valueOf(latLng.longitude));
                editor.commit();

                startActivity(new Intent(MapsActivity.this, LocationsInListView.class));

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

