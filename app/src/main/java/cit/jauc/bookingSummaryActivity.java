package cit.jauc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cit.jauc.lib.HttpHandler;
import cit.jauc.model.Booking;
import cit.jauc.model.Location;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;


import com.mapbox.api.directions.v5.models.DirectionsRoute;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;
import com.mapbox.services.android.navigation.v5.navigation.NavigationRoute;



public class bookingSummaryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Intent receiveIntent;

    private String destinationAddress;
    private String originAddress;

    private Button btnCancel;
    private Button btnConfirm;

    private Location originLocation;
    private Double originLat;
    private Double originLon;

    private Location destinationLocation;
    private Double destinationLat;
    private Double destinationLon;

    private Booking userBooking;

    private FirebaseAuth mAuth;
    private String currentUser;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private DirectionsRoute currentRoute;
    private NavigationMapRoute navigationMapRoute;


    private static final String TAG = "bookingSummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        btnCancel = findViewById(R.id.bt_cancel);
        btnConfirm = findViewById(R.id.bt_confirm);

        // Get the putExtra data from the destinationActivity
        receiveIntent = getIntent();
        destinationAddress = receiveIntent.getStringExtra("destinationAddress");
        destinationLat = receiveIntent.getDoubleExtra("destinationLat",0);
        destinationLon = receiveIntent.getDoubleExtra("destinationLon", 0);
        originAddress = receiveIntent.getStringExtra("originAddress");
        originLat = receiveIntent.getDoubleExtra("originLat", 0);
        originLon = receiveIntent.getDoubleExtra("originLon", 0);

        originLocation = new Location();
        originLocation.setLat(originLat);
        originLocation.setLon(originLon);

        destinationLocation = new Location();
        destinationLocation.setLat(destinationLat);
        destinationLocation.setLon(destinationLon);

        userBooking = new Booking();
        userBooking.setUserId(currentUser);
        userBooking.setOrigin(originLocation);
        userBooking.setDestination(destinationLocation);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_LONG).show();
                Intent backToMainCancelIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(backToMainCancelIntent);
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //POST TO DATABASE
                new BookingToDatabase().execute();
            }
        });
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        // Add the origin marker
        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(originLat, originLon))
                .title("Origin")
                .snippet(originAddress)
        );

        // Add the destination marker
        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(destinationLat, destinationLon))
                .title("Destination")
                .snippet(destinationAddress)
        );
        getRoute(Point.fromLngLat(originLon, originLat), Point.fromLngLat(destinationLon, destinationLat));

        LatLng originLatLng = new LatLng(originLat, originLon);
        LatLng destinationLatLang = new LatLng(destinationLat, destinationLon);

        // Set the bounds of the camera with the origin and destination point
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(originLatLng)
                .include(destinationLatLang)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 50));

    }

    private void getRoute(Point origin, Point destination) {
        NavigationRoute.builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .origin(origin)
                .destination(destination)
                .build()
                .getRoute(new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                        Log.d(TAG, "Response code: " + response.code());
                        if (response.body() == null) {
                            Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                            return;
                        } else if (response.body().routes().size() < 1) {
                            Log.e(TAG, "No routes found");
                            return;
                        }
                        currentRoute = response.body().routes().get(0);

                        // Draw the route on the map
                        if (navigationMapRoute != null) {
                            navigationMapRoute.removeRoute();
                        } else {
                            navigationMapRoute = new NavigationMapRoute(null, mapView, mapboxMap, R.style.NavigationMapRoute);
                        }
                        navigationMapRoute.addRoute(currentRoute);
                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                        Log.e(TAG, "Error: " + t.getMessage());
                    }
                });
    }

    private class BookingToDatabase extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            JSONObject query = new JSONObject();
            JSONObject originQuery = new JSONObject();
            JSONObject destinationQuery = new JSONObject();

            Date currentTime = Calendar.getInstance().getTime();
            DateFormat df = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss Z", Locale.ENGLISH);
            String strDate = df.format(currentTime);

            try {
                query.put("date", strDate);
                query.put("userId", userBooking.getUserId());
                query.put("userName", mAuth.getCurrentUser().getDisplayName());
                originQuery.put("lat", userBooking.getOrigin().getLat());
                originQuery.put("lon", userBooking.getOrigin().getLon());
                originQuery.put("address", originAddress);
                destinationQuery.put("lat", userBooking.getDestination().getLat());
                destinationQuery.put("lon", userBooking.getDestination().getLon());
                destinationQuery.put("address", destinationAddress);
                query.put("destination", destinationQuery);
                query.put("origin", originQuery);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultAsyncTask = new HttpHandler().makeHttpPostRequest(query.toString(), Constants.BOOKINGSURL + ".json", TAG);
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }
            if (resultAsyncTask.length() > 0) {
                return resultAsyncTask;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(bookingSummaryActivity.this, "Booking request sent successfully",
                    Toast.LENGTH_SHORT).show();

            Intent backToMainConfirmIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(backToMainConfirmIntent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(getBaseContext(), destinationActivity.class);
        startActivity(backIntent);
    }

    @Override
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
