package cit.jauc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;

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
import cit.jauc.model.StripeCustomer;
import cit.jauc.model.User;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    Button button;
    Button btnOpenBookingHistory;
    Button btnOpenUserProfile;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences sharedpreferences;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedpreferences = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        button = findViewById(R.id.logout_test);
        btnOpenBookingHistory = findViewById(R.id.btn_booking_history);
        btnOpenUserProfile = findViewById(R.id.btn_user_profile);
        mAuth = FirebaseAuth.getInstance();

        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                sendToLogin();
            }
        });

        btnOpenUserProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), UserProfileActivity.class);
                i.putExtra("User", mAuth.getCurrentUser().getUid());
                startActivity(i);
            }
        });

        btnOpenBookingHistory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), BookingHistoryActivity.class);
                i.putExtra("User", mAuth.getCurrentUser().getUid());
                startActivity(i);
            }
        });

        mAuthListener = new AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                }
            }
        };

        if(sharedpreferences.getString("userId", "").equalsIgnoreCase("")) {
            new GetUserDetails().execute(mAuth.getCurrentUser().getUid());
        }
    }

    private void sendToLogin() { //funtion
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(getBaseContext(), gso);
        mGoogleSignInClient.signOut().addOnCompleteListener(MainActivity.this,
                new OnCompleteListener<Void>() {  //signout Google
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent setupIntent = new Intent(getBaseContext(), LoginActivity.class);
                        Toast.makeText(getBaseContext(), "Logged Out", Toast.LENGTH_LONG)
                                .show(); //if u want to show some text
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                        finish();
                    }
                });
    }

    private class GetUserDetails extends AsyncTask<String, Integer, User> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Loading user details...");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected User doInBackground(String... user) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = new HttpHandler().makeHttpGetRequest(Constants.USERURL + "/" + user[0] + ".json", TAG);
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return convertJsonToUser(resultAsyncTask, user[0]);
            }
            return null;
        }

        private User convertJsonToUser(String resultAsyncTask, String user) {
            User result = new User();
            try {
                JSONObject data = new JSONObject(resultAsyncTask);
                        String userId = data.getString("key");
                        if (userId.equalsIgnoreCase(user)) {
                            if (data.has("customerToken")) {
                                result = new StripeCustomer();
                                ((StripeCustomer) result).setCustomerToken(data.getString("customerToken"));
                            }
                            result.setKey(userId);
                            result.setPhotoUrl(data.getString("photoUrl"));
                            result.setEmail(data.getString("email"));
                            result.setDisplayName(data.getString("displayName"));
                        }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);

            SharedPreferences.Editor editor = sharedpreferences.edit();
            if (user instanceof StripeCustomer) {
                String cusToken = ((StripeCustomer) user).getCustomerToken();
                editor.putString("stripeCustomer", cusToken);
            }
            editor.putString("userId", user.getKey());
            editor.putString("userEmail", user.getEmail());
            editor.putString("userDisplayName", user.getDisplayName());
            editor.putString("userPhotoUrl", user.getPhotoUrl());
            editor.commit();
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
