package cit.jauc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import cit.jauc.model.Booking;

public class BookingDetailsActivity extends AppCompatActivity {

    Booking booking;
    TextView txDate;
    TextView txFrom;
    TextView txTo;
    TextView txCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        booking = (Booking) getIntent().getSerializableExtra("Booking");

        txDate = findViewById(R.id.txTripDetailsDate);
        txFrom = findViewById(R.id.txTripDetailsFrom);
        txTo = findViewById(R.id.txTripDetailsTo);
        txCar = findViewById(R.id.txTripDetailsCar);

        txFrom.setText("Origin: " + booking.getOrigin().getLon() + ", " + booking.getOrigin().getLat());
        txTo.setText("Destination: " + booking.getDestination().getLon() + ", " + booking.getDestination().getLat());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        txDate.setText(df.format(booking.getBookingDate()));
        txCar.setText("Trip on a " + booking.getCar().getName() + " with plate " + booking.getCar().getPlate());

        //        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

}
