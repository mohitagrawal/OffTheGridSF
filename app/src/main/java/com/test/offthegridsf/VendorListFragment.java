package com.test.offthegridsf;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.test.offthegridsf.model.AppDataCallbacks;
import com.test.offthegridsf.model.PushFragmentCallback;
import com.test.offthegridsf.model.Vendor;

import java.util.ArrayList;

/**
 * Created by Mohit on 9/13/2014.
 */
public class VendorListFragment extends ListFragment implements AdapterView.OnItemClickListener {
    public static final String VENDORS_ARRAY = "VendorsArray";
    private ArrayList<Vendor> vendors;
    private PushFragmentCallback listener;
    private AppDataCallbacks dataCallback;
    private boolean fromTab;

    public static Bundle createArgsBundle(ArrayList<Vendor> vendors, boolean fromTab) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(VENDORS_ARRAY, vendors);
        bundle.putBoolean("FromTab", fromTab);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vendors = (ArrayList<Vendor>) getArguments().getSerializable(VENDORS_ARRAY);
        fromTab = getArguments().getBoolean("FromTab");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (fromTab) {
            vendors = dataCallback.getAllVendors();
        }
        ArrayAdapter<Vendor> adapter = new ArrayAdapter<Vendor>(getActivity(), android.R.layout.simple_list_item_1,
                vendors.toArray(new Vendor[vendors.size()]));
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        TextView emptyView = new TextView(getActivity());
        emptyView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
        emptyView.setText("No vendors");
        emptyView.setVisibility(View.GONE);
        getListView().setEmptyView(emptyView);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Vendor selectedVendor = (Vendor) getListAdapter().getItem(i);
        showVendorDetails(selectedVendor);
    }

    public void showVendorDetails(Vendor vendor) {
        //push fragment
        VendorFragment vendorFragment = new VendorFragment();
        vendorFragment.setArguments(VendorFragment.createArgsBundle(vendor));
        listener.pushFragment(vendorFragment);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getParentFragment() instanceof PushFragmentCallback) {
            listener = (PushFragmentCallback) getParentFragment();
        } else {
            throw new IllegalArgumentException("Parent frament of " + this.getClass().getName() +
                    " should implement " + PushFragmentCallback.class.getName());
        }

        if (getActivity() instanceof AppDataCallbacks) {
            dataCallback = (AppDataCallbacks) getActivity();
        } else {
            throw new IllegalArgumentException("Parent Activity of " + this.getClass().getName() +
                    " should implement " + AppDataCallbacks.class.getName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        dataCallback = null;
    }
}

