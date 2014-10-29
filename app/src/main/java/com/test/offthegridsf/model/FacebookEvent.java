package com.test.offthegridsf.model;

import com.test.offthegridsf.utils.ISO8601DateParser;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Mohit on 9/13/2014.
 */
public class FacebookEvent implements Serializable {
    private String id;
    private String name;
    private String location;
    private Date startTime;
    private Date endTime;
    private String description;
    private ArrayList<Vendor> vendors;
    private static final String LOG = "OffTheGridSF";

    public ArrayList<Vendor> getVendors() {
        return vendors;
    }

    public static ArrayList<FacebookEvent> getPastEvents(ArrayList<FacebookEvent> events) {
        Date today = new Date();
        ArrayList<FacebookEvent> pastEvents = new ArrayList<FacebookEvent>();
        for (FacebookEvent event : events) {
            if (event.getStartTime().compareTo(today) < 0) {
                pastEvents.add(event);
            }
        }
        return pastEvents;
    }

    public boolean happenedLastMonth() {
        Date today = new Date();
        Calendar cal = new GregorianCalendar();
        cal.setTime(today);
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date dateBefore = cal.getTime();
        if (this.getStartTime().after(dateBefore)) {
            return true;
        } else {
            return false;
        }
    }

    public String getDescription() {
        return description;
    }

    //initializes vendors in this event also.
    public void setDescription(String description) {
        this.description = description;
        String[] patterns = {"Vendor list below:\r\n",
                "Vendor List:\r\n",
                "Vendor Lineup:\r\n",
                "Vendor:\r\n",
                "Vendors:\r\n",
                "This weekend's featured vendors will be:\r\n",
                "Trucks that will participate on a rotating basis include:\r\n",
                "This week's lineup:\r\n",
                "Lineup includes:\r\n",
                "will include:\r\n",
                "Vendor List Below:\r\n"};
        for (int i = 0; i < patterns.length; i++) {
            if (description.toLowerCase().replace(" ", "").contains(patterns[i].toLowerCase().replace(" ", ""))) {
                String[] array = description.split("(?i)"+patterns[i].replace("\r\n", ""));
                if (array.length > 1) {
                    extractVendors(array[1]);
                } else {
                    System.out.println(LOG + " Unable to match pattern" + description);
                }
                break; //found a match no need to find more
            }
        }
        if (vendors == null) {
            System.out.println(LOG + "Failed to extract vendors from" + this.getName() + "\n" + description);
        }
    }

    private ArrayList<Vendor> extractVendors(String rawString) {
        //rawString can start with \r\n\r\n for multiple line breaks.
        if (rawString.startsWith("\r\n\r\n")) {
            rawString.replaceFirst("\r\n\r\n", "");
        }
        String array[] = rawString.trim().split("\r\n\r\n");
        String[] vendorArray;
        if (array.length > 0) {
            //lets do something
            vendors = new ArrayList<Vendor>();
            vendorArray = array[0].split("\r\n");
            for (int i = 0; i < vendorArray.length; i++) {
                if (vendorArray[i].trim().length() > 0) {
                    Vendor vendor = new Vendor();
                    vendor.setName(vendorArray[i].trim());
                    vendors.add(vendor);
                }
            }
            return vendors;
        } else {
            System.out.println(LOG + "Unable to parse string " + rawString);
            return null;
        }
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public FacebookEvent(String id, String name, String location, String startTimeString, String endTimeString) throws ParseException {
        this.id = id;
        this.name = name;
        this.location = location;
        startTime = ISO8601DateParser.parse(startTimeString);
        endTime = ISO8601DateParser.parse(endTimeString);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    //Utility Function to check if an arraylist contains a event
    public boolean hasEvent(ArrayList<FacebookEvent> events, FacebookEvent event) {
        for (FacebookEvent oneEvent : events) {
            if (oneEvent.getId() == event.getId()) {
                return true;
            }
        }
        return false;
    }
}
