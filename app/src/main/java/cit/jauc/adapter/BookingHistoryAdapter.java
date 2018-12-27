package cit.jauc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import cit.jauc.R;
import cit.jauc.model.Booking;

public class BookingHistoryAdapter extends ArrayAdapter<Booking> {


    public BookingHistoryAdapter(Context context, List<Booking> bookings) {
        super(context, 0, bookings);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Booking booking = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.booking_history_list, parent, false);
        }

        TextView tvOrigin = convertView.findViewById(R.id.tvBookingOrigin);
        TextView tvDestination = convertView.findViewById(R.id.tvBookingDestination);
        TextView tvPaid = convertView.findViewById(R.id.tvBookingPaid);
        TextView tvDate = convertView.findViewById(R.id.tvBookingDate);

        tvOrigin.setText("Origin: " + booking.getOrigin().getLon() + ", " + booking.getOrigin().getLat());
        tvDestination.setText("Destination: " + booking.getDestination().getLon() + ", " + booking.getDestination().getLat());

        return convertView;
    }

}
