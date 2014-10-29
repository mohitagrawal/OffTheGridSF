package com.test.offthegridsf;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;

import com.test.offthegridsf.model.AppDataCallbacks;
import com.test.offthegridsf.model.FacebookEvent;
import com.test.offthegridsf.model.PushFragmentCallback;
import com.test.offthegridsf.model.Vendor;

import java.util.ArrayList;

/**
 * Created by Mohit on 9/13/2014.
 */
public class MainTabHolderFragment extends Fragment implements PushFragmentCallback {
    ArrayList<FacebookEvent> events;
    private FragmentTabHost mTabHost;
    private AppDataCallbacks callback;

    public static Bundle createArgsBundle(ArrayList<FacebookEvent> events) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("EventsArray", events);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        events = (ArrayList<FacebookEvent>) getArguments().getSerializable("EventsArray");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mainfragment, container, false);
        mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.realtabcontent);
        if (getChildFragmentManager().getFragments() == null) {
            mTabHost.addTab(mTabHost.newTabSpec("tab1").setIndicator("Upcoming Events"),
                    EventListFragment.class, EventListFragment.createArgsBundle(events));

            //get all vendors list and pass it in sorted formal
            ArrayList<Vendor> allVendors = callback.getAllVendors();
            mTabHost.addTab(mTabHost.newTabSpec("tab2").setIndicator("All Vendors"),
                    VendorListFragment.class, VendorListFragment.createArgsBundle(allVendors, true));
        }
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String s) {
                System.out.println("Tab changed");
            }
        });
        return view;
    }

    @Override
    public void pushFragment(Fragment fragment) {
        //mTabHost.
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.realtabcontent, fragment, fragment.getClass().getName());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof AppDataCallbacks) {
            callback = (AppDataCallbacks) getActivity();
        } else {
            throw new IllegalArgumentException("Parent Activity of " + this.getClass().getName() +
                    " should implement " + AppDataCallbacks.class.getName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
