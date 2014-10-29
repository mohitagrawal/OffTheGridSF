package com.test.offthegridsf.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohit on 9/14/2014.
 */
public class AppData implements Serializable {
    public ArrayList<FacebookEvent> upcomingEvents;
    public HashMap<String, FacebookEvent> upcomingEventsMap;
    public ArrayList<FacebookEvent> pastEvents;
    public ArrayList<Vendor> allVendors; //with their past events data

    public AppData() {
        upcomingEvents = new ArrayList<FacebookEvent>();
        upcomingEventsMap = new HashMap<String, FacebookEvent>();
        pastEvents = new ArrayList<FacebookEvent>();
        allVendors = new ArrayList<Vendor>();
    }

    public void doCleanup() {
        //Find past events.
        pastEvents = FacebookEvent.getPastEvents(upcomingEvents);

        ArrayList<FacebookEvent> newPastEvents = new ArrayList<FacebookEvent>();
        ArrayList<FacebookEvent> stalePastEvents = new ArrayList<FacebookEvent>();
        if (pastEvents.size() > 0) {
            for (FacebookEvent event : pastEvents) {
                //remove from upcoming map
                upcomingEventsMap.remove(event.getId());
                //remove from list
                upcomingEvents.remove(event);
                if (event.happenedLastMonth()) {
                    //add to past events list
                    newPastEvents.add(event);
                    //addall vendors from this  event to all vendors array
                    if (event.getVendors() != null) {
                        for (Vendor vendor : event.getVendors()) {
                            allVendors.add(vendor);
                            //update this event detail in vendor object
                            vendor.addEvent(event);
                        }
                    }
                } else {
                    //clean up event references from vendor object since it is stale event now.
                    //We don't need it
                    if (allVendors != null) {
                        for (Vendor vendor : allVendors) {
                            vendor.removeEvent(event);
                        }
                    }
                    //remove event from pastEvents since it is not required anymore
                    stalePastEvents.add(event);
                }

            }
            if (newPastEvents != null && newPastEvents.size() > 0) {
                for (FacebookEvent event : newPastEvents) {
                    if (!pastEvents.contains(event)) {
                        pastEvents.add(event);
                    }
                }
            }
            if (stalePastEvents != null && stalePastEvents.size() > 0) {
                for (FacebookEvent event : stalePastEvents) {
                    if (pastEvents.contains(event)) {
                        pastEvents.remove(event);
                    }
                }
            }
        }

    }
}

