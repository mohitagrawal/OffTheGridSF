package com.test.offthegridsf;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.test.offthegridsf.model.Vendor;

/**
 * Created by Mohit on 9/13/2014.
 */
public class VendorFragment extends Fragment {
    public static final String VENDOR = "Vendor";
    private Vendor vendor;
    private TextView vendorName;

    public static Bundle createArgsBundle(Vendor vendor) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(VENDOR, vendor);
        return bundle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        vendor = (Vendor) getArguments().getSerializable(VENDOR);
        View view = inflater.inflate(R.layout.vendorfragment, container, false);
        vendorName = (TextView) view.findViewById(R.id.vendorOccurences);
        if (vendor.getEventsAttended() != null && vendor.getEventsAttended().size() > 0) {
            vendorName.setText("In last 30 days " + vendor.getName() + " has appeared in " + vendor.getEventsAttended().size() + " event(s)");
        } else {
            vendorName.setText("In last 30 days " + vendor.getName() + " has not appeared in any events");
        }
        return view;
    }
}
