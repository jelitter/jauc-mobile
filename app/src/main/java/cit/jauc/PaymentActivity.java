package cit.jauc;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import cit.jauc.lib.HttpHandler;
import cit.jauc.model.Booking;
import cit.jauc.model.Invoice;
import cit.jauc.model.StripeCustomer;

public class PaymentActivity extends AppCompatActivity {

    private final String TAG = "PaymentActivity";
    Button btnPayNewCard;
    Button btnPayStoredCard;
    TextView txtInvoiceId;
    TextView txtAmount;
    TextView txtTripDetails;
    Invoice invoice;
    Booking booking;
    String userId;
    String customerEmail;
    FirebaseAuth mAuth;
    StripeCustomer stripe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();

        invoice = (Invoice) getIntent().getSerializableExtra("invoice");
        booking = (Booking) getIntent().getSerializableExtra("booking");
        userId = mAuth.getCurrentUser().getUid();
        customerEmail = mAuth.getCurrentUser().getEmail();
        stripe = (StripeCustomer) getIntent().getSerializableExtra("stripe");

        txtInvoiceId = findViewById(R.id.txtPaymentInvoice);
        txtAmount = findViewById(R.id.txtPaymentAmount);
        txtTripDetails = findViewById(R.id.txtPaymentTrip);
        btnPayNewCard = findViewById(R.id.btnPayNew);
        btnPayStoredCard = findViewById(R.id.btnPayToken);

        txtTripDetails.setText("Trip " + ((booking.getOrigin().getAddress().length() > 4) ? "from " + booking.getOrigin().getAddress() : "")
                + ((booking.getDestination().getAddress().length() >4 ) ? " To " + booking.getDestination().getAddress() : ""));
        txtInvoiceId.setText(invoice.getId());
        Locale locale = new Locale("en", "IE");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        txtAmount.setText(currencyFormatter.format(invoice.getPrice()));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btnPayStoredCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new StripePaymentWorkflow().execute(invoice);
            }
        });

        btnPayNewCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), UserProfileActivity.class);
                i.putExtra("User", userId);
                if(stripe instanceof StripeCustomer) {
                    i.putExtra("Stripe", stripe);
                }
                startActivity(i);
            }
        });
    }

    private class StripePaymentWorkflow extends AsyncTask<Object, Integer, String> {

        ProgressDialog progressDialog = new ProgressDialog(PaymentActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Processing payment...");
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Object... params) {
            String resultAsyncTask = "";
            String customerToken = "";
            JSONObject stripeQuery = new JSONObject();
            Invoice inv = (Invoice) params[0];
            try {
                String userInfo = new HttpHandler().makeHttpGetRequest(Constants.USERURL + "/" + userId + ".json", TAG);
                JSONObject json = new JSONObject(userInfo);
                if (json.has("customerToken")) {
                    customerToken = json.getString("customerToken");
                } else {
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                stripeQuery.put("amount", inv.getPrice() * 100);
                String description;
                if (inv.getDescription().equalsIgnoreCase("")) {
                    description = "Trip to: " + booking.getDestination().getAddress();
                } else {
                    description = inv.getDescription();
                }
                stripeQuery.put("description", description);
                stripeQuery.put("stripeCustomer", customerToken);
                stripeQuery.put("emailReceipt", customerEmail);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultAsyncTask = new HttpHandler().makeHttpPostRequest(stripeQuery.toString(), Constants.STRIPEURL + "/charge", TAG);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (resultAsyncTask.length() > 0) {
                JSONObject updateQuery = new JSONObject();
                try {
                    updateQuery.put("paid", true);
                    updateQuery.put("stripeDebugString", resultAsyncTask);
                    new HttpHandler().makeHttpPatchRequest(updateQuery.toString(), Constants.INVOICESURL + "/" + invoice.getId() + ".json", TAG);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return resultAsyncTask;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            if(result != null) {
                Toast.makeText(getApplicationContext(), "Payment successful.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
