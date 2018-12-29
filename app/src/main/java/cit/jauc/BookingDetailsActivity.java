package cit.jauc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cit.jauc.lib.CoordsConverter;
import cit.jauc.model.Booking;
import cit.jauc.model.Rating;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.Locale;

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
                URL url = new URL(requestUrl);
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

    private class GetRatingFromBooking extends AsyncTask<String, Integer, Rating> {

        String jsonResponse = "";
        HttpURLConnection connection = null;
        InputStream is = null;

        @Override
        protected Rating doInBackground(String... bookingId) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = makeHttpGetRequest(Constants.RATINGSURL + ".json");
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
                        String id = element.getString("bookingId");
                        if (id.equalsIgnoreCase(bookingId)) { //TODO get user id
                            result.setBookingID(data.getString("bookingId"));
                            if (data.has("rating")) {
                                result.setRating(data.getString("rating"));
                            }
                            if (data.has("userId")) {
                                result.setUserID(data.getString("userId"));
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        private String makeHttpGetRequest(String requestUrl) throws IOException {
            try {
                URL url = new URL(requestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.connect();

                if (connection.getResponseCode() == 200) {
                    InputStream is = connection.getInputStream();
                    jsonResponse = readFromStream(is);
                }
            } catch (IOException e) {
                Log.w(TAG, "readingReview:failure", e);
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
        protected void onPostExecute(Rating rating) {
            super.onPostExecute(rating);

            if (rating != null && rating.getBookingID() != null) {
                btnRatingAngry.setEnabled(false);
                btnRatingHappy.setEnabled(false);
                if (rating.getRating().equalsIgnoreCase(Constants.HAPPYEMOJI)) {
                    btnRatingHappy.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    btnRatingAngry.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_light_disabled));
                }
                if (rating.getRating().equalsIgnoreCase(Constants.SADEMOJI)) {
                    btnRatingAngry.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
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
