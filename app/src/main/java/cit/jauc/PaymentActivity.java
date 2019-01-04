package cit.jauc;

import android.app.ProgressDialog;
import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

import cit.jauc.lib.HttpHandler;
import cit.jauc.model.Invoice;

public class PaymentActivity extends AppCompatActivity {

    private final String TAG = "PaymentActivity";
    SharedPreferences sharedpreferences;
    Button btnPayNewCard;
    Button btnPayStoredCard;
    TextView txtInvoiceId;
    TextView txtAmount;
    TextView txtTripDetails;
    Invoice invoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        invoice = (Invoice) getIntent().getSerializableExtra("invoice");
        txtInvoiceId = findViewById(R.id.txtPaymentInvoice);
        txtAmount = findViewById(R.id.txtPaymentAmount);
        txtTripDetails = findViewById(R.id.txtPaymentTrip);
        btnPayNewCard = findViewById(R.id.btnPayNew);
        btnPayStoredCard = findViewById(R.id.btnPayToken);

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
            // TODO get token from database
            JSONObject stripeQuery = new JSONObject();
            Invoice inv = (Invoice) params[0];

            try {
                stripeQuery.put("amount", inv.getPrice());
                stripeQuery.put("description", inv.getDescription());
                stripeQuery.put("stripeCustomer", customerToken );
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                resultAsyncTask = new HttpHandler().makeHttpPostRequest(stripeQuery.toString(), Constants.STRIPEURL + "/charge", TAG);
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

            progressDialog.dismiss();

            Toast.makeText(PaymentActivity.this, "Result " + result,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }
}
