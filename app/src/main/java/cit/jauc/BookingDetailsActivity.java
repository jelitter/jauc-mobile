package cit.jauc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import cit.jauc.lib.CoordsConverter;
import cit.jauc.model.Booking;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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

        btnRatingAngry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating = Constants.SADEMOJI;
                new PostRatingToBooking().execute(rating);
            }
        });

        btnRatingHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rating = Constants.HAPPYEMOJI;
                new PostRatingToBooking().execute(rating);
            }
        });

    }

    private class PostRatingToBooking extends AsyncTask<String, Integer, String> {

        String jsonResponse = "";
        HttpURLConnection connection = null;

        InputStream is = null;

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            String charset = "UTF-8";

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
                resultAsyncTask = makeHttpPostRequest(query.toString(), Constants.RATINGSURL + ".json");
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return resultAsyncTask;
            }
            return null;
        }


        private String makeHttpPostRequest(String query, String requestUrl) throws IOException {

            try {
                URL url = new URL(String.format(requestUrl));
                connection = (HttpsURLConnection) url.openConnection();
                connection.setReadTimeout(15000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();
                //connection.setDoOutput(true); DO NOT DO THIS

                OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
                wr.write(query);
                wr.flush();
                wr.close();

                if (connection.getResponseCode() == 200) {
                    InputStream is = connection.getInputStream();
                    jsonResponse = readFromStream(is);
                }


            } catch (IOException e) {
                Log.w(TAG, "readingBookings:failure", e);

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (is != null) {
                    is.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream is) throws IOException {
            StringBuilder output = new StringBuilder();
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
                String line = br.readLine();
                while (line != null) {
                    output.append(line);
                    line = br.readLine();
                }
            }
            return output.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
