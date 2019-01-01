package cit.jauc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;


import java.util.List;

import cit.jauc.model.Location;


public class originActivity extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private static final String TAG = "originActivity";

    private PermissionsManager permissionsManager;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Button btnBookHere;
    private Location originLocation;
    private String username;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox Access Token
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        originLocation = new Location();


        setContentView(R.layout.activity_origin);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        username = getIntent().getStringExtra("User");

        btnBookHere = (Button) findViewById(R.id.btn_goHere);
        btnBookHere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentDestination = new Intent(getBaseContext(), destinationActivity.class);
                intentDestination.putExtra("user", username);
                intentDestination.putExtra("originLat", originLocation.getLat());
                intentDestination.putExtra("originLon", originLocation.getLon());
                startActivity(intentDestination);
            }
        });

    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        originActivity.this.mapboxMap = mapboxMap;
        enableLocationComponent();
        retrieveLocation(originLocation);
    }

    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent() {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            LocationComponentOptions options = LocationComponentOptions.builder(this)
                    .trackingGesturesManagement(true)
                    .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
                    .build();

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(this, options);

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);

        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    public Location retrieveLocation(Location originLocation){
        originLocation = new Location();

        LocationEngine locationEngine = new LocationEngineProvider(originActivity.this).obtainLocationEngineBy(LocationEngine.Type.ANDROID);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.addLocationEngineListener(new LocationEngineListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onConnected() {
                locationEngine.requestLocationUpdates();
            }

            @Override
            public void onLocationChanged(android.location.Location location) {
                locationEngine.removeLocationEngineListener(this);
            }
        });

        @SuppressLint("MissingPermission") android.location.Location lastAndroidLocation = locationEngine.getLastLocation();
        originLocation.setLat((long) lastAndroidLocation.getLatitude());
        originLocation.setLon((long) lastAndroidLocation.getLongitude());

        return originLocation;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocationComponent();
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }


    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
