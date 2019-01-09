package cit.jauc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cit.jauc.lib.HttpHandler;
import cit.jauc.model.SupportMessage;

public class SupportRequestActivity extends AppCompatActivity {

    SupportMessage message;
    Button btnSend, btnCancel;
    TextView tvBody;
    String userId, email, displayName, photoUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_request);

        btnSend = findViewById(R.id.sendTicket);
        btnCancel = findViewById(R.id.cancelTicket);
        tvBody = findViewById(R.id.body);

        Bundle extras = getIntent().getExtras();
        userId = extras.getString("userId");
        email = extras.getString("email");
        displayName = extras.getString("displayName");
        photoUrl = extras.getString("photoUrl");


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentSupport = new Intent(getBaseContext(), SupportHistoryActivity.class);
                tvBody.setText("");
                intentSupport.putExtra("User", userId);
                startActivity(intentSupport);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = new SupportMessage();

                message.setBody(tvBody.getText().toString());
                message.setDate(new Date());
                message.setRead(false);
                message.setUserId(userId);
                message.setEmail(email);
                message.setUserName(displayName);
                message.setPhotoUrl(photoUrl);

//                tvBody.setText(message.toString());
                new SupportToDatabase().execute();
            }
        });


    }
    private class SupportToDatabase extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            JSONObject query = new JSONObject();

            Date currentTime = Calendar.getInstance().getTime();
            DateFormat df = new SimpleDateFormat("EEE MMM d yyyy HH:mm:ss Z", Locale.ENGLISH);
            String strDate = df.format(currentTime);

            try {
                query.put("date", strDate);
                query.put("userId", message.getUserId());
                query.put("userName", message.getUserName());
                query.put("email", message.getEmail());
                query.put("photoUrl", message.getPhotoUrl());
                query.put("body", message.getBody());


            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultAsyncTask = new HttpHandler().makeHttpPostRequest(query.toString(), Constants.SUPPORTURL + ".json", "support");
            } catch (IOException e) {
                Log.w("support", "closingInputStream:failure", e);
            }
            if (resultAsyncTask.length() > 0) {
                return resultAsyncTask;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(SupportRequestActivity.this, "Support request sent successfully",
                    Toast.LENGTH_SHORT).show();

            Intent backIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(backIntent);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(backIntent);
    }
}
