package cit.jauc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cit.jauc.adapter.BookingHistoryAdapter;
import cit.jauc.model.Booking;

public class BookingHistoryActivity extends AppCompatActivity {

    private List<Booking> bookingList;
    private static final String TAG = "BookingHistoryActivity";
    ArrayAdapter<Booking> bookingArrayAdapter;
    ListView listView;
    Context activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = this;

        listView = findViewById(R.id.lv_booking_list);
        String username = getIntent().getStringExtra("User");

/*        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        new GetBookingList().execute(username);
    }


    private class GetBookingList extends AsyncTask<String, Integer, List<Booking>> {

        String jsonResponse = "";
        HttpURLConnection connection = null;
        InputStream is = null;

        @Override
        protected List<Booking> doInBackground(String... user) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = makeHttpGetRequest();
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return convertJsonToBookings(resultAsyncTask, user[0]);
            }
            return null;
        }

        private List<Booking> convertJsonToBookings(String resultAsyncTask, String user) {
            List<Booking> result = new ArrayList<>();
            try {
                JSONObject data = new JSONObject(resultAsyncTask);
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (data.get(key) instanceof JSONObject) {
                        JSONObject element = (JSONObject) data.get(key);
                        String userId = element.getString("userId");
                        if (userId.equalsIgnoreCase(user)) { //TODO get user id
                            Booking booking = new Booking();
                            booking.setUserId(userId);
                            String carId = (element.has("carId")) ? element.getString("carId") : null;
                            booking.setCarId(carId);
                            String invoiceId = (element.has("invoiceID")) ? element.getString("invoiceId") : null;
                            booking.setInvoice(invoiceId);

                            JSONObject origin = element.has("origin") ? element.getJSONObject("origin") : null;
                            long originLon = origin.getLong("lon");
                            long originLat = origin.getLong("lat");
                            booking.setOrigin(originLon, originLat);

                            JSONObject destination = element.has("destination") ? element.getJSONObject("destination") : null;
                            long destinationLon = destination.getLong("lon");
                            long destinationLat = destination.getLong("lat");
                            booking.setDestination(destinationLon, destinationLat);

                            String dateStr = element.has("destination") ? element.getString("date") : null;
                            if (dateStr != null) {
                                DateFormat format = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss Z", Locale.ENGLISH);
                                Date date = format.parse(dateStr);
                                booking.setBookingDate(date);
                            }

                            result.add(booking);
                        }


                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return result;
        }

        private String makeHttpGetRequest() throws IOException {
            try {
                URL url = new URL(String.format("https://jauc-ae38e.firebaseio.com/bookings.json"));
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
        protected void onPostExecute(List<Booking> bookings) {
            super.onPostExecute(bookings);

            bookingList = bookings;
            BookingHistoryAdapter adapter = new BookingHistoryAdapter(activity, bookings);
            listView.setAdapter(adapter);
            //listView.setAdapter(bookingArrayAdapter);

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

}


