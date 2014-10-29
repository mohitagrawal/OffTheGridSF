package com.test.offthegridsf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;
import com.test.offthegridsf.model.FacebookEvent;
import com.test.offthegridsf.model.PushFragmentCallback;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Mohit on 9/13/2014.
 */
public class EventListFragment extends ListFragment implements AdapterView.OnItemClickListener {
    private ArrayList<FacebookEvent> events;
    private ProgressDialog progressDialog;
    private PushFragmentCallback callback;

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
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventAdapter adapter = new EventAdapter(getActivity(), events);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);
        TextView emptyView = new TextView(getActivity());
        emptyView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));
    }

    private void parseEventDetails(FacebookEvent event, Response response) {
        try {
            GraphObject go = response.getGraphObject();
            JSONObject jso = go.getInnerJSONObject();
            event.setDescription(jso.getString("description"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (event.getVendors() != null) {
            pushVendorListFragment(event);
        } else {
            Toast.makeText(getActivity(), "Unable to find vendors", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        FacebookEvent selectedEvent = (FacebookEvent) getListAdapter().getItem(i);
        if (selectedEvent.getVendors() == null || selectedEvent.getVendors().size() == 0) {
            //fetch event details if we don't have vendors info
            fetchEventDetails(selectedEvent);
        } else {
            pushVendorListFragment(selectedEvent);
        }
    }

    private void pushVendorListFragment(FacebookEvent event) {
        VendorListFragment mainFragment = new VendorListFragment();
        mainFragment.setArguments(VendorListFragment.createArgsBundle(event.getVendors(),false));
        callback.pushFragment(mainFragment);
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setMessage("Loading....");
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    private void fetchEventDetails(final FacebookEvent event) {

        Session session = Session.getActiveSession();
        showProgressDialog();
        new Request(
                session,
                "/" + event.getId(),
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        hideProgressDialog();
                        parseEventDetails(event, response);
                    }
                }
        ).executeAsync();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getParentFragment() instanceof PushFragmentCallback) {
            callback = (PushFragmentCallback) getParentFragment();
        } else {

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callback = null;
    }
}
