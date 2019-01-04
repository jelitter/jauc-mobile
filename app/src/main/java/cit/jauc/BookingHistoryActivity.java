package cit.jauc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cit.jauc.adapter.BookingHistoryAdapter;
import cit.jauc.lib.HttpHandler;
import cit.jauc.model.Booking;
import cit.jauc.model.Car;
import cit.jauc.model.Invoice;

public class BookingHistoryActivity extends AppCompatActivity {

    private List<Booking> bookingList;
    private static final String TAG = "BookingHistoryActivity";
    ArrayAdapter<Booking> bookingArrayAdapter;
    ListView listView;
    Context activity;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        activity = this;

        listView = findViewById(R.id.lv_booking_list);
        username = getIntent().getStringExtra("User");

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
        ProgressDialog progressDialog = new ProgressDialog(BookingHistoryActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading trip history...");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected List<Booking> doInBackground(String... user) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = new HttpHandler().makeHttpGetRequest(Constants.BOOKINGSURL + ".json", TAG);
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
                            booking.setId(key);
                            booking.setUserId(userId);
                            String carId = (element.has("carId")) ? element.getString("carId") : null;
                            booking.setCarId(carId);
                            if (carId != null) {
                                try {
                                    String carResult = new HttpHandler().makeHttpGetRequest(Constants.CARSURL + "/" + carId + ".json", TAG);
                                    JSONObject carJson = new JSONObject(carResult);
                                    Car car = new Car();
                                    car.setId(carId);
                                    car.setName(carJson.getString("name"));
                                    car.setPlate(carJson.getString("plate"));
                                    booking.setCar(car);
                                } catch (IOException e) {
                                    Log.w(TAG, "unableToGetCarResult:failure", e);
                                }
                            }

                            String invoiceId = (element.has("invoiceId")) ? element.getString("invoiceId") : null;
                            booking.setInvoice(invoiceId);
                            if (invoiceId != null) {
                                try {
                                    String invoiceResult = new HttpHandler().makeHttpGetRequest(Constants.INVOICESURL + "/" + invoiceId + ".json", TAG);
                                    JSONObject invoiceJson = new JSONObject(invoiceResult);
                                    Invoice invoice = new Invoice();
                                    invoice.setId(invoiceId);
                                    invoice.setPaid(invoiceJson.has("paid") && invoiceJson.getBoolean("paid"));
                                    invoice.setPrice((invoiceJson.has("price") ? invoiceJson.getDouble("price") : -1));
                                    invoice.setDescription((invoiceJson.has("description")) ? invoiceJson.getString("description") : "");
                                    booking.setInvoice(invoice);
                                } catch (IOException e) {
                                    Log.w(TAG, "unableToGetInvoiceResult:failure", e);
                                }
                            }

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

        @Override
        protected void onPostExecute(List<Booking> bookings) {
            super.onPostExecute(bookings);

            bookingList = bookings;
            BookingHistoryAdapter adapter = new BookingHistoryAdapter(activity, bookings);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent i = new Intent(view.getContext(), BookingDetailsActivity.class);
                    i.putExtra("Booking", bookingList.get(position));
                    i.putExtra("User", username);
                    view.getContext().startActivity(i);
                }
            });
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

}


