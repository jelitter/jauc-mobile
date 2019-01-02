package cit.jauc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import cit.jauc.model.Booking;
import cit.jauc.model.Location;

public class bookingSummaryActivity extends AppCompatActivity {
    private Intent receiveIntent;
    private TextView tvDestinationAddress;
    private String destinationAddress;
    private String originAddress;
    private TextView tvOriginAddress;
    private Button btnCancel;
    private Button btnConfirm;
    private Location originLocation;
    private Double originLat;
    private Double originLon;
    private Location destinationLocation;
    private Double destinationLat;
    private Double destinationLon;
    private Booking userBooking;
    private FirebaseAuth mAuth;
    private String currentUser;

    private static final String TAG = "bookingSummaryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser().getUid();

        btnCancel = findViewById(R.id.bt_cancel);
        btnConfirm = findViewById(R.id.bt_confirm);


        tvDestinationAddress = findViewById(R.id.tv_destination);
        tvOriginAddress = findViewById(R.id.tv_origin);

        receiveIntent = getIntent();
        destinationAddress = receiveIntent.getStringExtra("destinationAddress");
        destinationLat = receiveIntent.getDoubleExtra("destinationLat",0);
        destinationLon = receiveIntent.getDoubleExtra("destinationLon", 0);
        originAddress = receiveIntent.getStringExtra("originAddress");
        originLat = receiveIntent.getDoubleExtra("originLat", 0);
        originLon = receiveIntent.getDoubleExtra("originLon", 0);

        tvDestinationAddress.setText(destinationAddress);
        tvOriginAddress.setText(originAddress);

        originLocation = new Location();
        originLocation.setLat(originLat);
        originLocation.setLon(originLon);

        destinationLocation = new Location();
        destinationLocation.setLat(destinationLat);
        destinationLocation.setLon(destinationLon);

        userBooking = new Booking();
        userBooking.setUserId(currentUser);
        userBooking.setOrigin(originLocation);
        userBooking.setDestination(destinationLocation);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_LONG).show();
                Intent backToMainCancelIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(backToMainCancelIntent);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //POST TO DATABASE
                Intent backtoMainConfirmIntent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(backtoMainConfirmIntent);
            }
        });


    }
}
