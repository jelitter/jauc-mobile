package cit.jauc;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cit.jauc.lib.HttpFirebaseHandler;
import cit.jauc.model.User;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    Context activity;
    String username;
    TextView txtFullName;
    Button btnAddCard;
    CardInputWidget mCardInputWidget;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        username = getIntent().getStringExtra("User");
        txtFullName = findViewById(R.id.txtProfileName);
        btnAddCard = findViewById(R.id.btnAddPayment);
        mCardInputWidget = findViewById(R.id.card_input_widget);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btnAddCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card cardToSave = mCardInputWidget.getCard();
                if (cardToSave == null) {
                    Toast.makeText(UserProfileActivity.this, "Wrong credit card details", Toast.LENGTH_SHORT).show();
                } else {
                    cardToSave.setName(txtFullName.getText().toString().trim());
                    Stripe stripe = new Stripe(UserProfileActivity.this, "pk_test_S6GwdcyEn8QWC8a83g5qsc86");
                    stripe.createToken(
                            cardToSave,
                            new TokenCallback() {
                                public void onSuccess(Token token) {
                                    // Send token to your server
                                    if (currentUser != null) {
                                        new PostToken().execute(currentUser.getKey(), token.getId());
                                    }
                                }

                                public void onError(Exception error) {
                                    // Show localized error message
                                    Toast.makeText(UserProfileActivity.this,
                                            error.getLocalizedMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }
                    );
                }
            }
        });
        new GetUserProfile().execute(username);
    }

    private class GetUserProfile extends AsyncTask<String, Integer, User> {

        @Override
        protected User doInBackground(String... userId) {
            String resultAsyncTask = "";
            try {
                resultAsyncTask = new HttpFirebaseHandler().makeHttpGetRequest(Constants.USERURL + "/" + userId[0] + ".json", TAG);
            } catch (IOException e) {
                Log.w(TAG, "closingInputStream:failure", e);
            }

            if (resultAsyncTask.length() > 0) {
                return convertJsonToUser(resultAsyncTask);
            }
            return null;
        }

        private User convertJsonToUser(String resultAsyncTask) {
            User result = new User();

            try {
                JSONObject data = new JSONObject(resultAsyncTask);

                String id = (data.has("key")) ? data.getString("key") : "";
                if (!id.equalsIgnoreCase("")) {
                    result.setKey(data.getString("key"));
                    result.setDisplayName(data.getString("displayName"));
                    result.setEmail(data.getString("email"));
                    result.setPhotoUrl(data.getString("photoUrl"));
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(User user) {
            super.onPostExecute(user);
            currentUser = user;
            txtFullName.setText(user.getDisplayName());

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }

    private class PostToken extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            JSONObject query = new JSONObject();
            String userId = params[0];
            try {
                query.put("stripeToken", params[1]);
//                query.put("carID", (booking.getCar() != null) ? booking.getCar().getId() : null);
//                query.put("rating", params[0]);
//                query.put("userId", username);
//                query.put("bookingId", booking.getId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                new HttpFirebaseHandler().makeHttpPatchRequest(query.toString(), Constants.USERURL + "/" + userId + ".json", TAG);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resultAsyncTask.length() > 0) {
                return resultAsyncTask;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(UserProfileActivity.this, "Card saved.",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
