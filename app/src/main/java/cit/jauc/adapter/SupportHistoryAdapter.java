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

import cit.jauc.PaymentActivity;
import cit.jauc.R;
import cit.jauc.model.Booking;
import cit.jauc.model.SupportMessage;

public class SupportHistoryAdapter extends ArrayAdapter<SupportMessage> {

    SupportMessage message;
    Button btnRespond;

    public SupportHistoryAdapter(Context context, List<SupportMessage> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.support_history_list, parent, false);
        }

        TextView tvTitle = convertView.findViewById(R.id.tvTitle);
        TextView tvBody = convertView.findViewById(R.id.tvBody);

        btnRespond = convertView.findViewById(R.id.btnRespond);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);

        tvTitle.setText(message.getUserName() + " (you) on " + df.format(message.getDate()));

        tvBody.setText(message.getBody());

        btnRespond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent i = new Intent(getContext(), PaymentActivity.class);
//                i.putExtra("booking", booking);
//                i.putExtra("invoice", booking.getInvoice());
//                v.getContext().startActivity(i);
            }
        });


        return convertView;
    }

}
