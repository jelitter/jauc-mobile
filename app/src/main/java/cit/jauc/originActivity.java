package cit.jauc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.places.picker.PlacePicker;
import com.mapbox.mapboxsdk.plugins.places.picker.model.PlacePickerOptions;

import java.util.List;

public class originActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        PermissionsListener {

    private static final int REQUEST_CODE = 5678;
    private static final String TAG = "originActivity";


    private MapView mapView;
    private Point originPoint;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private Button goToPickerActivityButton;
    private TextView selectedLocationTextView;
    private String originAddress;
    private cit.jauc.model.Location userLocation;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowTitleEnabled(false);


        userLocation = new cit.jauc.model.Location();
        retrieveLocation(userLocation);

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_destination);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        goToPickerActivity();

    }


    private void goToPickerActivity() {

        startActivityForResult(
                new PlacePicker.IntentBuilder()
                        .accessToken(getString(R.string.mapbox_access_token))
                        .placeOptions(PlacePickerOptions.builder()
                                .statingCameraPosition(new CameraPosition.Builder()
                                        .target(new LatLng(userLocation.getLat(), userLocation.getLon())).zoom(16).build())
                                .build())
                        .build(this), REQUEST_CODE);
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        selectedLocationTextView = findViewById(R.id.selected_location_info_textview);

        if (resultCode == RESULT_CANCELED) {
            // Show the button and set the OnClickListener()
            goToPickerActivityButton = findViewById(R.id.go_to_picker_button);
            goToPickerActivityButton.setVisibility(View.VISIBLE);
            goToPickerActivityButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    goToPickerActivity();
                }

            });
        } else if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Retrieve the information from the selected location's CarmenFeature
            CarmenFeature carmenFeature = PlacePicker.getPlace(data);

            // Set the TextView text to the entire CarmenFeature.
            selectedLocationTextView.setText(String.format(
                    getString(R.string.selected_place_info), carmenFeature.toJson()));

            //Get a Point object from carmenFeature
            originPoint = carmenFeature.center();
            originAddress = carmenFeature.placeName();

            // Create intent and put extra information
            Intent destinationActivityIntent = new Intent(this, destinationActivity.class);
            destinationActivityIntent.putExtra("originAddress", originAddress);
            destinationActivityIntent.putExtra("originLat", originPoint.latitude());
            destinationActivityIntent.putExtra("originLon", originPoint.longitude());
            startActivity(destinationActivityIntent);
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        originActivity.this.mapboxMap = mapboxMap;
        enableLocationComponent();
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

    public void retrieveLocation(cit.jauc.model.Location oLocation){
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

        @SuppressLint("MissingPermission")
        android.location.Location lastAndroidLocation = locationEngine.getLastLocation();
        oLocation.setLat(lastAndroidLocation.getLatitude());
        oLocation.setLon(lastAndroidLocation.getLongitude());
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

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(backIntent);
    }
}
