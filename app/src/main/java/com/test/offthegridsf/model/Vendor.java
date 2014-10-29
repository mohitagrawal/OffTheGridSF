package com.test.offthegridsf.model;

import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Mohit on 9/13/2014.
 */
public class Vendor implements Serializable, Comparable<Vendor> {
    private String vendorName;
    //just maintaining eventid list. Keeping whole event will be memory intensive
    //because FacebookEvent object has list of Vendor object
    private ArrayList<String> eventsAttended;

    public Vendor() {
        eventsAttended = new ArrayList<String>();
    }


    public void addEvent(FacebookEvent event) {
        if (eventsAttended == null) {
            eventsAttended = new ArrayList<String>();
        }
        if (!eventsAttended.contains(event.getId())) {
            eventsAttended.add(event.getId());
        }
    }

    public String getName() {
        return vendorName;
    }

    public void setName(String name) {
        this.vendorName = name;
    }

    //remove event from attended list. Called when event is more than a month old
    public void removeEvent(FacebookEvent event) {
        if (eventsAttended.contains(event.getId())) {
            eventsAttended.remove(event.getId());
        }

    }

    @Override
    public String toString() {
        return vendorName;
    }

    public ArrayList<String> getEventsAttended() {
        return eventsAttended;
    }

    public void setEventsAttended(ArrayList<String> eventsAttended) {
        this.eventsAttended = eventsAttended;
    }


    //comparable interface implementation to sort according to number of
    //events attended in past
    @Override
    public int compareTo(Vendor o) {
        int left = 0;
        if (this.getEventsAttended() != null) {
            left = this.getEventsAttended().size();
        }
        int right = 0;
        if (o.getEventsAttended() != null) {
            right = o.getEventsAttended().size();
        }

        return (left < right) ? -1 : (left > right) ? 1 : 0;
    }

    public static ArrayList<Vendor> extractVendorFromEvents(ArrayList<FacebookEvent> events) {
        ArrayList<Vendor> vendorsList = new ArrayList<Vendor>();
        for (FacebookEvent event : events) {
            if (event.getVendors() != null) {
                for (Vendor vendor : event.getVendors()) {
                    vendorsList.add(vendor);
                }
            }
        }
        return vendorsList;
    }
}
