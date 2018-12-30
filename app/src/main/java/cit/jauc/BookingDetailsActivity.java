package cit.jauc;

import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

import cit.jauc.lib.CoordsConverter;
import cit.jauc.lib.HttpFirebaseHandler;
import cit.jauc.model.Booking;
import cit.jauc.model.Rating;

public class BookingDetailsActivity extends AppCompatActivity {

    private static final String TAG = "BookingDetailsActivity";
    Booking booking;
    TextView txDate;
    TextView txFrom;
    TextView txTo;
    TextView txCar;
    String username;
    Button btnRatingAngry;
    Button btnRatingHappy;
    String rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        booking = (Booking) getIntent().getSerializableExtra("Booking");
        username = getIntent().getStringExtra("User");

        txDate = findViewById(R.id.txTripDetailsDate);
        txFrom = findViewById(R.id.txTripDetailsFrom);
        txTo = findViewById(R.id.txTripDetailsTo);
        txCar = findViewById(R.id.txTripDetailsCar);
        btnRatingHappy = findViewById(R.id.btnRatingHappy);
        btnRatingAngry = findViewById(R.id.btnRatingAngry);


        txFrom.setText("Origin: " + CoordsConverter.getLocationfromCoords(booking.getOrigin().getLon(), booking.getOrigin().getLat()));
        txTo.setText("Destination: " + CoordsConverter.getLocationfromCoords(booking.getDestination().getLon(), booking.getDestination().getLat()));
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        txDate.setText(df.format(booking.getBookingDate()));
        if (booking.getCar() != null) {
            txCar.setText("Trip on a " + booking.getCar().getName() + " with plate " + booking.getCar().getPlate());
        }

        //        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        new GetRatingFromBooking().execute(booking.getId());

        btnRatingAngry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRatingHappy.setEnabled(false);
                rating = Constants.SADEMOJI;
                new PostRatingToBooking().execute(rating);
            }
        });

        btnRatingHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnRatingAngry.setEnabled(false);
                rating = Constants.HAPPYEMOJI;
                new PostRatingToBooking().execute(rating);
            }
        });

        btnRatingAngry.setEnabled(false);
        btnRatingHappy.setEnabled(false);

    }

    private class PostRatingToBooking extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            JSONObject query = new JSONObject();
            try {
                query.put("carID", (booking.getCar() != null) ? booking.getCar().getId() : null);
                query.put("rating", params[0]);
                query.put("userId", username);
                query.put("bookingId", booking.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultAsyncTask = new HttpFirebaseHandler().makeHttpPostRequest(query.toString(), Constants.RATINGSURL + ".json", TAG);
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

            Toast.makeText(BookingDetailsActivity.this, "Rating saved.",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    private class GetRatingFromBooking extends AsyncTask<String, Integer, Rating> {

        String jsonResponse = "";
        HttpURLConnection connection = null;
        InputStream is = null;

        @Override
        protected Rating doInBackground(String... bookingId) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = new HttpFirebaseHandler().makeHttpGetRequest(Constants.RATINGSURL + ".json", TAG);
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return convertJsonToRating(resultAsyncTask, bookingId[0]);
            }
            return null;
        }

        private Rating convertJsonToRating(String resultAsyncTask, String bookingId) {
            Rating result = new Rating();

            try {
                JSONObject data = new JSONObject(resultAsyncTask);
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (data.get(key) instanceof JSONObject) {
                        JSONObject element = (JSONObject) data.get(key);
                        String id = (element.has("bookingId")) ? element.getString("bookingId") : "";
                        if (id.equalsIgnoreCase(bookingId)) {
                            result.setBookingID(element.getString("bookingId"));
                            result.setRating(element.getString("rating"));
                            result.setUserID(element.getString("userId"));

                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Rating rating) {
            super.onPostExecute(rating);

            if (rating != null && rating.getBookingID() != null) {
                btnRatingAngry.setEnabled(false);
                btnRatingHappy.setEnabled(false);
                if (rating.getRating().equalsIgnoreCase(Constants.HAPPYEMOJI)) {
                    // btnRatingAngry.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY);
                    btnRatingHappy.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY);
                    //btnRatingHappy.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    btnRatingAngry.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_light_disabled));
                }
                if (rating.getRating().equalsIgnoreCase(Constants.SADEMOJI)) {
                    // btnRatingHappy.getBackground().setColorFilter(0xFFFF0000, PorterDuff.Mode.MULTIPLY); // RED
                    btnRatingAngry.getBackground().setColorFilter(0xFF00FF00, PorterDuff.Mode.MULTIPLY); // GREEN
                    //btnRatingAngry.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                    btnRatingHappy.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_light_disabled));
                }
            } else {
                btnRatingAngry.setEnabled(true);
                btnRatingHappy.setEnabled(true);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

}
