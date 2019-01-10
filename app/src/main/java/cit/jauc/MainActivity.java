package cit.jauc;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import cit.jauc.adapter.JAUCFirebaseMessageService;

import com.stripe.android.Stripe;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cit.jauc.lib.HttpHandler;
import cit.jauc.model.StripeCustomer;
import cit.jauc.model.User;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    Button button;
    Button btnOpenBookingHistory;
    Button btnBook;
    Button btnOpenUserProfile;
    Button btnSupportMain;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    SharedPreferences sharedpreferences;
    String firebaseAppToken = "";

    String userId, displayName, email, photoUrl;

    User loadedUserInfo;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        checkFirebaseMessageToken();
    }

    private void checkFirebaseMessageToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKENDEBUG", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.d("TOKENDEBUG", "Token obtained");
                        Log.d("TOKENDEBUG", token);

                        if(!firebaseAppToken.equals(token)) {
                            Log.d("TOKENDEBUG", "Received new Token");
                            Log.d("TOKENDEBUG", token);
                            Log.d("TOKENDEBUG", firebaseAppToken);

                            try {
                                JAUCFirebaseMessageService fbMessageService = new JAUCFirebaseMessageService();
                                fbMessageService.sendTokenToFirebase(token);
                            } catch (Error error) {
                                error.printStackTrace();
                            }

                            firebaseAppToken = token;
                        }
                    }
                });

        userId = mAuth.getCurrentUser().getUid();
        displayName = mAuth.getCurrentUser().getDisplayName();
        email = mAuth.getCurrentUser().getEmail();
        photoUrl = mAuth.getCurrentUser().getPhotoUrl().toString();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity( new Intent(getBaseContext(), SupportHistoryActivity.class));
            }
        });

        button = findViewById(R.id.logout_test);
        btnOpenBookingHistory = findViewById(R.id.btn_booking_history);
        btnBook = findViewById(R.id.btn_book);
        btnOpenUserProfile = findViewById(R.id.btn_user_profile);
        btnSupportMain = findViewById(R.id.support_request);
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
                if(loadedUserInfo instanceof StripeCustomer) {
                    i.putExtra("Stripe", loadedUserInfo);
                }
                startActivity(i);
            }
        });

        btnOpenBookingHistory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), BookingHistoryActivity.class);
                i.putExtra("User", mAuth.getCurrentUser().getUid());
                if(loadedUserInfo instanceof StripeCustomer) {
                    i.putExtra("Stripe", loadedUserInfo);
                }
                startActivity(i);
            }
        });

        btnBook.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intentBook = new Intent(getBaseContext(), originActivity.class);
                    intentBook.putExtra("User", mAuth.getCurrentUser().getUid());
                    startActivity(intentBook);
            }
        });

        btnSupportMain.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle extras = new Bundle();
                extras.putString("userId",mAuth.getCurrentUser().getUid());
                extras.putString("displayName",mAuth.getCurrentUser().getDisplayName());
                extras.putString("email",mAuth.getCurrentUser().getEmail());
                extras.putString("photoUrl", mAuth.getCurrentUser().getPhotoUrl().toString());

                Intent intentSupport = new Intent(getBaseContext(), SupportHistoryActivity.class);
                intentSupport.putExtras(extras);

                startActivity(intentSupport);
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

        new GetUserDetails().execute(mAuth.getCurrentUser().getUid());
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
                String url = Constants.USERURL + "/" + mAuth.getCurrentUser().getUid() + ".json";
                resultAsyncTask = new HttpHandler().makeHttpGetRequest(url, TAG);
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0 && !resultAsyncTask.equalsIgnoreCase("null")) {
                return convertJsonToUser(resultAsyncTask, user[0]);
            } else {
                try {
                    JSONObject userQuery = new JSONObject();
                    if (mAuth.getCurrentUser().getDisplayName() != null) {
                        userQuery.put("displayName", mAuth.getCurrentUser().getDisplayName());
                    }
                    if (mAuth.getCurrentUser().getPhotoUrl() != null) {
                        userQuery.put("photoUrl", mAuth.getCurrentUser().getPhotoUrl());
                    }
                    userQuery.put("key", mAuth.getCurrentUser().getUid());
                    userQuery.put("email", mAuth.getCurrentUser().getEmail());
                    userQuery.put("device", firebaseAppToken);
                    return convertJsonToUser(new HttpHandler().makeHttpPatchRequest(userQuery.toString(), Constants.USERURL + "/" + mAuth.getCurrentUser().getUid() + ".json", TAG), user[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
                                ((StripeCustomer) result).setLast4(data.getString("last4"));
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
            loadedUserInfo = user;
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }


}
