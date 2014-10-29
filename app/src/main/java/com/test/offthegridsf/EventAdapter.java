package com.test.offthegridsf;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.test.offthegridsf.model.FacebookEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Mohit on 9/13/2014.
 */
public class EventAdapter extends BaseAdapter {
    private ArrayList<FacebookEvent> events;
    private Context context;

    public EventAdapter(Context context, ArrayList<FacebookEvent> events) {
        this.context = context;
        this.events = events;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (row == null) {
            holder = new ViewHolder();
            row = inflater.inflate(R.layout.event_cell, parent, false);
            holder.eventName = (TextView) row.findViewById(R.id.eventName);
            holder.eventDetails = (TextView) row.findViewById(R.id.eventDetails);
            holder.eventTime = (TextView) row.findViewById(R.id.eventTime);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        holder.eventName.setText(events.get(position).getName());
        holder.eventDetails.setText(events.get(position).getLocation());
        String startDate = new SimpleDateFormat("EEE, MMM d, ''yy h:mm - ").format(events.get(position).getStartTime());
        String endDate = new SimpleDateFormat("h:mm a").format(events.get(position).getEndTime());
        startDate = startDate + endDate;
        holder.eventTime.setText("Time: " + startDate);
        row.setTag(holder);
        return row;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public FacebookEvent getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return events.indexOf(getItem(i));
    }

    static class ViewHolder {
        TextView eventName;
        TextView eventDetails;
        TextView eventTime;
    }
}
