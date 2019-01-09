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
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cit.jauc.adapter.BookingHistoryAdapter;
import cit.jauc.adapter.SupportHistoryAdapter;
import cit.jauc.lib.HttpHandler;
import cit.jauc.model.Booking;
import cit.jauc.model.SupportMessage;


public class SupportHistoryActivity extends AppCompatActivity {

    private static final String TAG = "SupportHistoryActivity";
    Button supportButton;
    ArrayAdapter<SupportMessage> supportMessageArrayAdapter;
    ListView listView;
    Context activity;
    String username;
    private List<SupportMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_history);


        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        activity = this;

        listView = findViewById(R.id.lv_support_list);
        username = getIntent().getStringExtra("User");

        supportButton = findViewById(R.id.btnSendTicket);
//        supportButton.setText(username);
    }

    @Override
    protected void onStart() {
        super.onStart();
        new GetMessageList().execute(username);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new GetMessageList().execute(username);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new GetMessageList().execute(username);
    }

    private class GetMessageList extends AsyncTask<String, Integer, List<SupportMessage>> {

        ProgressDialog progressDialog = new ProgressDialog(SupportHistoryActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading Support Messages...");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(List<SupportMessage> messages) {
            super.onPostExecute(messages);

            messageList = messages;
            SupportHistoryAdapter adapter = new SupportHistoryAdapter(activity, messages);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Intent i = new Intent(view.getContext(), BookingDetailsActivity.class);
//                    i.putExtra("Message", messageList.get(position));
//                    i.putExtra("User", username);
//                    view.getContext().startActivity(i);
                }
            });
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected List<SupportMessage> doInBackground(String... user) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = new HttpHandler().makeHttpGetRequest(Constants.SUPPORTURL + ".json", TAG);
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return convertJsonToMessages(resultAsyncTask, user[0]);
            }
            return null;
        }

        private List<SupportMessage> convertJsonToMessages(String resultAsyncTask, String user) {
            List<SupportMessage> result = new ArrayList<>();


            try {
                JSONObject data = new JSONObject(resultAsyncTask);
                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    if (data.get(key) instanceof JSONObject) {
                        JSONObject element = (JSONObject) data.get(key);
                        String userId = element.getString("userId");
                        if (userId.equalsIgnoreCase(user)) {
                            SupportMessage message = new SupportMessage();
                            message.setId(key);
                            message.setUserId(userId);

                            String userName = element.getString("userName");
                            message.setUserName(userName);

                            String body = (element.has("body")) ? element.getString("body") : null;
                            message.setBody(body);

                            String email = (element.has("email")) ? element.getString("email") : null;
                            message.setEmail(email);


                            result.add(message);
                        }
                    }
                }

            } 
            catch (JSONException e) {
                e.printStackTrace();
            }
//            catch (ParseException e) {
//                e.printStackTrace();
//            }

            return result;
        }
    }

}
