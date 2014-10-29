package com.test.offthegridsf.model;

import com.test.offthegridsf.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Mohit on 9/13/2014.
 */
public interface AppDataCallbacks {
    HashMap<String, FacebookEvent> getUpcomingEventsMap();

    ArrayList<Vendor> getAllVendors();
}
