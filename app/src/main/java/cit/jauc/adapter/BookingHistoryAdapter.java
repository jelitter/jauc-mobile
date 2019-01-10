package cit.jauc.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cit.jauc.BookingHistoryActivity;
import cit.jauc.PaymentActivity;
import cit.jauc.R;
import cit.jauc.model.Booking;
import cit.jauc.model.StripeCustomer;

public class BookingHistoryAdapter extends ArrayAdapter<Booking> {

    Booking booking;
    Button btnPay;
    TextView tvPaid;
    StripeCustomer stripe;

    public BookingHistoryAdapter(Context context, List<Booking> bookings) {
        super(context, 0, bookings);
         stripe = ((BookingHistoryActivity) context).getStripe();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        booking = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.booking_history_list, parent, false);
        }

        TextView tvOrigin = convertView.findViewById(R.id.tvBookingOrigin);
        TextView tvDestination = convertView.findViewById(R.id.tvBookingDestination);
        TextView tvDate = convertView.findViewById(R.id.tvBookingDate);
        tvPaid = convertView.findViewById(R.id.tvBookingPaid);
        btnPay = convertView.findViewById(R.id.btnPayInvoice);

        tvOrigin.setText("Origin: " + booking.getOrigin().getAddress());
        tvDestination.setText("Destination: " + booking.getDestination().getAddress());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        tvDate.setText(df.format(booking.getBookingDate()));
        tvPaid.setText((booking.getInvoice().isPaid() ? "PAID with Invoice #" + booking.getInvoice().getId() : "Not Yet Paid"));

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), PaymentActivity.class);
                i.putExtra("booking", booking);
                i.putExtra("invoice", booking.getInvoice());
                if(stripe != null) {
                    i.putExtra("stripe", stripe);
                }
                v.getContext().startActivity(i);
            }
        });

        if (booking.getInvoice().getId() == null) {
            btnPay.setVisibility(Button.GONE);
            tvPaid.setText("Booking not yet approved");
            tvPaid.setVisibility(TextView.VISIBLE);
        }
        if(booking.getInvoice().isPaid()) {
            btnPay.setVisibility(Button.GONE);
        } else {
            tvPaid.setVisibility(TextView.GONE);
        }

        return convertView;
    }

}
