package cit.jauc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import cit.jauc.R;
import cit.jauc.model.Booking;

public class BookingHistoryAdapter extends ArrayAdapter<Booking> {

    Booking booking;

    public BookingHistoryAdapter(Context context, List<Booking> bookings) {
        super(context, 0, bookings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        booking = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.booking_history_list, parent, false);
        }

        TextView tvOrigin = convertView.findViewById(R.id.tvBookingOrigin);
        TextView tvDestination = convertView.findViewById(R.id.tvBookingDestination);
        TextView tvPaid = convertView.findViewById(R.id.tvBookingPaid); //TODO Change to clickable button to open Invoice details
        TextView tvDate = convertView.findViewById(R.id.tvBookingDate);

        tvOrigin.setText("Origin: " + booking.getOrigin().getLon() + ", " + booking.getOrigin().getLat());
        tvDestination.setText("Destination: " + booking.getDestination().getLon() + ", " + booking.getDestination().getLat());
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        tvDate.setText(df.format(booking.getBookingDate()));
        tvPaid.setText((booking.getInvoice().isPaid() ? "PAID" : "UNPAID"));

        return convertView;
    }

}
