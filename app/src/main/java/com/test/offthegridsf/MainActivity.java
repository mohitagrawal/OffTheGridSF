package com.test.offthegridsf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.facebook.widget.ProfilePictureView;
import com.google.gson.Gson;
import com.test.offthegridsf.model.AppData;
import com.test.offthegridsf.model.AppDataCallbacks;
import com.test.offthegridsf.model.FacebookEvent;
import com.test.offthegridsf.model.Vendor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends FragmentActivity implements AppDataCallbacks {
    private boolean isResumed = false;
    private UiLifecycleHelper uiHelper;
    private LoginButton loginButton;
    private ProfilePictureView profilePictureView;
    private ProgressDialog progressDialog;
    private AppData appData;
    //this object will be updated when we know wendor names
    //events will be updated on this object
    private GraphUser loginUser;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        profilePictureView = (ProfilePictureView) findViewById(R.id.selection_profile_pic);
        profilePictureView.setCropped(true);
        loginButton = (LoginButton) findViewById(R.id.login_button);
    }

    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        profilePictureView.setProfileId(user.getId());
                    }
                }

            }
        });
        request.executeAsync();
    }

    private void updateUI() {

        TextView userName = (TextView) findViewById(R.id.userName);
        if (loginUser != null) {
            userName.setText("Hello " + loginUser.getFirstName());
            loginButton.setVisibility(View.GONE);
            makeMeRequest(Session.getActiveSession());

        } else {
            loginButton.setVisibility(View.VISIBLE);
            userName.setText("Hi. Please login");
        }

    }

    private void fetchEvents() {
        showProgressDialog();
        /* make the API call */
        Session session = Session.getActiveSession();
        new Request(
                session,
                "/OffTheGridSF/events",
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
            /* handle the result */
                        parseGraphObjects(response);
                       /* showMainFragment();*/
                    }
                }
        ).executeAsync();
    }

    private void parseGraphObjects(Response response) {
        if (appData == null) {
            appData = new AppData();
        }

        try {
            GraphObject go = response.getGraphObject();
            JSONObject jso = go.getInnerJSONObject();
            JSONArray arr = jso.getJSONArray("data");
            for (int i = 0; i < (arr.length()); i++) {
                JSONObject json_obj = arr.getJSONObject(i);
                String id = json_obj.getString("id");
                String name = json_obj.getString("name");
                String location = json_obj.getString("location");
                String startTime = json_obj.getString("start_time");
                String endTime = json_obj.getString("end_time");
                FacebookEvent event = new FacebookEvent(id, name, location, startTime, endTime);
                //if already there and then don't add it
                if (!appData.upcomingEventsMap.containsKey(event.getId())) {
                    appData.upcomingEvents.add(event);
                    appData.upcomingEventsMap.put(event.getId(), event);
                } else {
                    System.out.println("Data already exist");
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (appData.upcomingEvents != null && appData.upcomingEvents.size() > 0) {
            fetchEventVendors();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;
        restoreAppData();
        loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                loginUser = user;
                updateUI();
                if (loginUser != null) {
                    fetchEvents();
                }
            }
        });
        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;

        saveAppData();


        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(this);
    }

    private void restoreAppData() {
        SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
        Gson gson = new Gson();
        String json = preferences.getString("AppData", "");
        appData = gson.fromJson(json, AppData.class);
        //if app is loaded first time. There won't be any data.
        if (appData == null) {
            appData = new AppData();
        } else {
            //clean up stale events and update if some events are now past events
            appData.doCleanup();
        }
    }

    private void saveAppData() {
        // Store values between instances here
        SharedPreferences preferences = getSharedPreferences("sharedPrefs", 0);
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(appData);
        editor.putString("AppData", json);
        // Commit to storage
        editor.commit();
    }


    private void fetchEventVendors() {
        RequestBatch requestBatch = new RequestBatch();
        for (final FacebookEvent event : appData.upcomingEvents) {
            if (event.getVendors() == null) {
                Session session = Session.getActiveSession();
                showProgressDialog();
                Request request = new Request(session, "/" + event.getId(), null, HttpMethod.GET);
                request.setCallback(new Request.Callback() {
                    @Override
                    public void onCompleted(Response response) {
                        parseEventDetails(event, response);
                    }
                });


                requestBatch.add(request);
            }
        }
        requestBatch.addCallback(new com.facebook.RequestBatch.Callback() {
            @Override
            public void onBatchCompleted(RequestBatch batch) {
                hideProgressDialog();
                //we are done loading all the data
                showMainFragment();
                Log.e("EventsBatch", "onBatchCompleted()");
            }
        });
        if (requestBatch.size() > 0) {
            requestBatch.executeAsync();
        } else {
            hideProgressDialog();
            showMainFragment();
        }
    }

    private void parseEventDetails(FacebookEvent event, Response response) {
        try {
            GraphObject go = response.getGraphObject();
            JSONObject jso = go.getInnerJSONObject();
            event.setDescription(jso.getString("description"));
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (event.getVendors() == null) {
            Toast.makeText(this, "Unable to find vendors", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }


    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
        /*    FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }*/
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
       /*     if (state.equals(SessionState.OPENED)) {
                showListFragment(SELECTION, false);
            } else if (state.isClosed()) {
                showListFragment(SPLASH, false);
            }*/
        }
    }

    private void showMainFragment() {
        if (appData.upcomingEvents != null && appData.upcomingEvents.size() > 0) {
            MainTabHolderFragment mainTabHolderFragment = new MainTabHolderFragment();
            mainTabHolderFragment.setArguments(MainTabHolderFragment.createArgsBundle(appData.upcomingEvents));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, mainTabHolderFragment, MainTabHolderFragment.class.getName()).commit();
        } else {
            Toast.makeText(this, "No events found !!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onBackPressed() {

        MainTabHolderFragment tabHolderFragment = (MainTabHolderFragment) getSupportFragmentManager().findFragmentByTag(MainTabHolderFragment.class.getName());
        if (tabHolderFragment != null) {
            if (tabHolderFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                tabHolderFragment.getChildFragmentManager().popBackStack();
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public HashMap<String, FacebookEvent> getUpcomingEventsMap() {
        return appData.upcomingEventsMap;
    }

    @Override
    public ArrayList<Vendor> getAllVendors() {
        return appData.allVendors;
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage("Updating Events Info....");
        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
