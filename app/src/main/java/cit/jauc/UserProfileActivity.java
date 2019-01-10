package cit.jauc;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.stripe.android.SourceCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.Card;
import com.stripe.android.model.Source;
import com.stripe.android.model.SourceParams;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import cit.jauc.lib.HttpHandler;
import cit.jauc.model.StripeCustomer;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity";
    String username;
    TextView txtFullName;
    TextView txtLast4;
    Button btnAddCard;
    Button btnAddNewCard;
    CardInputWidget mCardInputWidget;
    String userEmail;
    String userId;
    String last4;

    LinearLayout lvNewCard;
    LinearLayout lvStoredCard;
    ProgressDialog progDailog;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        StripeCustomer stripe = (StripeCustomer) getIntent().getSerializableExtra("Stripe");
        if (stripe != null) {
            last4 = (stripe.getLast4() != null) ? stripe.getLast4() : null;
            if (last4 != null) {
                txtLast4.setText(last4);
                lvNewCard.setVisibility(LinearLayout.VISIBLE);
                lvStoredCard.setVisibility(LinearLayout.GONE);
            }
        }

        userId = mAuth.getCurrentUser().getUid();
        username = (mAuth.getCurrentUser().getDisplayName() == null )? mAuth.getCurrentUser().getEmail(): mAuth.getCurrentUser().getDisplayName();
        txtFullName = findViewById(R.id.txtProfileName);
        txtFullName.setText(username);
        userEmail = mAuth.getCurrentUser().getEmail();

        mCardInputWidget = findViewById(R.id.card_input_widget);
        btnAddCard = findViewById(R.id.btnAddPayment);
        btnAddNewCard = findViewById(R.id.btnChangeCard);
        txtLast4 = findViewById(R.id.txtStoredCard);
        lvNewCard = findViewById(R.id.lvNewCard);
        lvStoredCard = findViewById(R.id.lvStoredCard);

        btnAddNewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lvNewCard.setVisibility(LinearLayout.VISIBLE);
            }
        });

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
                progDailog = new ProgressDialog(UserProfileActivity.this);
                progDailog.setMessage("Contacting payment provider...");
                progDailog.setIndeterminate(false);
                progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progDailog.setCancelable(true);
                progDailog.show();
                final Card cardToSave = mCardInputWidget.getCard();
                if (cardToSave == null) {
                    Toast.makeText(UserProfileActivity.this, "Wrong credit card details", Toast.LENGTH_SHORT).show();
                } else {
                    cardToSave.setName(txtFullName.getText().toString().trim());
                    Stripe mStripe = new Stripe(UserProfileActivity.this, "pk_test_S6GwdcyEn8QWC8a83g5qsc86");

                    SourceParams cardSourceParams = SourceParams.createCardParams(cardToSave);
                    // The asynchronous way to do it. Call this method on the main thread.
                    mStripe.createSource(
                            cardSourceParams,
                            new SourceCallback() {
                                @Override
                                public void onError(Exception error) {
                                    // Tell the user that something went wrong
                                    Toast.makeText(UserProfileActivity.this,
                                            error.getLocalizedMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                    progDailog.dismiss();
                                }

                                @Override
                                public void onSuccess(Source source) {
                                    last4 =  cardToSave.getLast4();
                                    new StripeCustomerWorkflow().execute(userId, userEmail, source.getId());
                                }
                            });
                }
            }
        });
    }


    private class StripeCustomerWorkflow extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog.setMessage("Saving card details...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... params) {
            String resultAsyncTask = "";
            String customerToken = "";
            JSONObject stripeQuery = new JSONObject();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String userId = mAuth.getCurrentUser().getUid();
            String userEmail = params[1];
            String cardToken = params[2];
            try {
                stripeQuery.put("source", cardToken);
                stripeQuery.put("email", userEmail);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                String jsonresponse = new HttpHandler().makeHttpPostRequest(stripeQuery.toString(), Constants.STRIPEURL + "/create", TAG);
                JSONObject data = new JSONObject(jsonresponse);
                if (data.has("object") && data.getString("object").equalsIgnoreCase("customer")) {
                    customerToken = (data.has("id")) ? data.getString("id") : "";
                }
                JSONObject query = new JSONObject();
                query.put("customerToken", customerToken);
                query.put("last4", last4);
                resultAsyncTask = new HttpHandler().makeHttpPatchRequest(query.toString(), Constants.USERURL + "/" + userId + ".json", TAG);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
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

            txtLast4.setText(last4);
            if(last4.equalsIgnoreCase("")) {
                lvNewCard.setVisibility(LinearLayout.VISIBLE);
                lvStoredCard.setVisibility(LinearLayout.GONE);
            } else {
                lvNewCard.setVisibility(LinearLayout.GONE);
                lvStoredCard.setVisibility(LinearLayout.VISIBLE);
            }

            Toast.makeText(UserProfileActivity.this, "Card saved.",
                    Toast.LENGTH_SHORT).show();
            progDailog.dismiss();
            lvNewCard.setVisibility(LinearLayout.GONE);
            lvStoredCard.setVisibility(LinearLayout.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
