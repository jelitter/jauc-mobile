package cit.jauc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;

import cit.jauc.model.Booking;

public class BookingHistoryActivity extends AppCompatActivity {

    private List<Booking> bookingList;
    private static final String TAG = "BookingHistoryActivity";
    private DatabaseReference firebaseRef;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

         database = FirebaseDatabase.getInstance();
         firebaseRef = database.getReference("bookings");

/*        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                // A new comment has been added, add it to the displayed list
                Comment comment = dataSnapshot.getValue(Comment.class);

                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so displayed the changed comment.
                Comment newComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

                // A comment has changed, use the key to determine if we are displaying this
                // comment and if so remove it.
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

                // A comment has changed position, use the key to determine if we are
                // displaying this comment and if so move it.
                Comment movedComment = dataSnapshot.getValue(Comment.class);
                String commentKey = dataSnapshot.getKey();

                // ...
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                Toast.makeText(getBaseContext(), "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };
        firebaseRef.addChildEventListener(childEventListener);


        ArrayAdapter<Booking> bookingArrayAdapter = new ArrayAdapter<Booking>(this, android.R.layout.simple_list_item_1, bookingList);
        ListView listView = findViewById(R.id.lv_booking_list);
        //listView.setAdapter(bookingArrayAdapter);

        new GetBookingList().execute("user");
    }

    private class GetBookingList extends AsyncTask<String, Integer, List<Booking>>{

        String jsonResponse = "";
        HttpURLConnection connection = null;
        InputStream is = null;

        @Override
        protected List<Booking> doInBackground(String... user) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = makeHttpGetRequest();
            } catch (IOException e){
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return convertJsonToBookings(resultAsyncTask);
            }
            return null;
        }

        private List<Booking> convertJsonToBookings(String resultAsyncTask) {
            try {
                JSONObject data = new JSONObject(HashMap<String, Booking>);
                Log.v("Json data", data.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String makeHttpGetRequest() throws IOException {
            try{
                URL url = new URL(String.format("https://jauc-ae38e.firebaseio.com/bookings.json"));
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.connect();

                if(connection.getResponseCode()== 200) {
                    InputStream is = connection.getInputStream();
                    jsonResponse = readFromStream(is);
                }
            }
            catch (IOException e) {
                Log.w(TAG, "readingBookings:failure", e);
            }
            finally {
                if(connection !=null) {
                    connection.disconnect();
                }
                if(is != null) {
                    is.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream is) throws IOException {
            StringBuilder output = new StringBuilder();
            if(is != null) {
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                BufferedReader br = new BufferedReader(isr);
                String line = br.readLine();
                while(line !=null) {
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

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

}


