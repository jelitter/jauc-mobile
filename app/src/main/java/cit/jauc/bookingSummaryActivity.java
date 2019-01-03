package cit.jauc;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import cit.jauc.lib.HttpFirebaseHandler;
import cit.jauc.model.Booking;
import cit.jauc.model.Location;

public class bookingSummaryActivity extends AppCompatActivity {
    private Intent receiveIntent;

    private TextView tvDestinationAddress;
    private String destinationAddress;
    private String originAddress;
    private TextView tvOriginAddress;

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

    private static final String TAG = "bookingSummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        btnCancel = findViewById(R.id.bt_cancel);
        btnConfirm = findViewById(R.id.bt_confirm);


        tvDestinationAddress = findViewById(R.id.tv_destination);
        tvOriginAddress = findViewById(R.id.tv_origin);

        receiveIntent = getIntent();
        destinationAddress = receiveIntent.getStringExtra("destinationAddress");
        destinationLat = receiveIntent.getDoubleExtra("destinationLat",0);
        destinationLon = receiveIntent.getDoubleExtra("destinationLon", 0);
        originAddress = receiveIntent.getStringExtra("originAddress");
        originLat = receiveIntent.getDoubleExtra("originLat", 0);
        originLon = receiveIntent.getDoubleExtra("originLon", 0);

        tvDestinationAddress.setText(destinationAddress);
        tvOriginAddress.setText(originAddress);

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

    private class BookingToDatabase extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            JSONObject query = new JSONObject();
            JSONObject originQuery = new JSONObject();
            JSONObject destinationQuery = new JSONObject();

            Date currentTime = Calendar.getInstance().getTime();

            try {
                query.put("date", currentTime);
                query.put("userId", userBooking.getUserId());
                query.put("userName", mAuth.getCurrentUser().getDisplayName());
                originQuery.put("lat", userBooking.getOrigin().getLat());
                originQuery.put("lon", userBooking.getOrigin().getLon());
                destinationQuery.put("lat", userBooking.getDestination().getLat());
                destinationQuery.put("lon", userBooking.getDestination().getLon());
                query.put("destination", destinationQuery);
                query.put("origin", originQuery);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultAsyncTask = new HttpFirebaseHandler().makeHttpPostRequest(query.toString(), Constants.BOOKINGSURL + ".json", TAG);
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
}
