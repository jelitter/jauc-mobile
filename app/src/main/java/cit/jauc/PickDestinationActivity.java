package cit.jauc;

import android.content.Context;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.light.Position;


import cit.jauc.model.PulseMarkerView;



public class PickDestinationActivity extends AppCompatActivity {

    private static final String TAG = "LocationPickerActivity";
    private static final int REQUEST_PERMISSIONS = 101;

    private MapView mapView;
    private MapboxMap mapboxMap;

    private ImageView dropPinView;
    private Marker addressPin;
    private ImageButton clearDisplayViewButton;
    private MarkerView userMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_destination);

        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        ImageView dropPinView = new ImageView(this);
        dropPinView.setImageResource(R.drawable.ic_droppin_24dp);




        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap map) {

                mapboxMap = map;


                // User Marker
//                mapboxMap.getMarkerViewManager().addMarkerViewAdapter(new PulseMarkerViewAdapter(PickDestinationActivity.this));

                // Create drop pin using custom image


                // drop pin in center of screen
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                float density = getResources().getDisplayMetrics().density;
                params.bottomMargin = (int) (12 * density);
                dropPinView.setLayoutParams(params);
                mapView.addView(dropPinView);

                // Make drop pin invisible
//                dropPinView.setVisibility(View.INVISIBLE);

                // Get LatLng of selected location
                LatLng position = mapboxMap.getProjection().fromScreenLocation(new PointF(dropPinView.getLeft() + (dropPinView.getWidth() / 2), dropPinView.getBottom()));

                // Remove previous address pin (if exists)
                if (addressPin != null) {
                    if (mapboxMap != null && addressPin != null) {
                        mapboxMap.removeMarker(addressPin);
                    }
                }

                addressPin = mapboxMap.addMarker(new MarkerViewOptions().title("Loading address…").position(position));
                mapboxMap.selectMarker(addressPin);

                //Create Geocoding client
                MapboxGeocoding client = new MapboxGeocoding.Builder()
                        .setAccessToken(getString(R.string.mapbox_access_token))
                        .setCoordinates(Position.fromCoordinates(position.getLongitude(), position.getLatitude()))
                        .setType(GeocodingCriteria.TYPE_ADDRESS)
                        .build();

            }
        });

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
