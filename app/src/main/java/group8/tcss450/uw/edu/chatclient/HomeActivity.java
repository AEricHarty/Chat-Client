package group8.tcss450.uw.edu.chatclient;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import group8.tcss450.uw.edu.chatclient.model.BadgeDrawerArrowDrawable;
import group8.tcss450.uw.edu.chatclient.model.Credentials;
import group8.tcss450.uw.edu.chatclient.utils.ContactsIntentService;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

/**
 * Home activity after logging in
 *
 * @author Jin Byoun - jinito@uw.edu
 * @author Eric Harty - hartye@uw.edu added weather and location services
 */
public class HomeActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, SettingsFragment.OnSettingsInteractionListener,
        SearchNewConnectionFragment.SearchContactFragmentInteractionListener, LocationListener,
        ConnectionsFragment.ConnectionsFragmentInteractionListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        HomeInformationFragment.OnHomeFragmentInteractionListener {

    private ArrayList<SearchNewConnectionFragment.SearchConnectionListItem> searchContactList;
    private ArrayList<ConnectionsFragment.Connection> connectionList;

    private DataUpdateReceiver mDataUpdateReceiver;
    private SearchNewConnectionFragment.SearchConnectionAdapter searchConnectionAdapter;
    private ConnectionsFragment.ConnectionsAdapter connectionsAdapter;

    private ActionBarDrawerToggle mToggle;

    private static final String TAG = "HomeActivity ERROR->";
    /**The desired interval for location updates. Inexact. Updates may be more or less frequent.*/
    //public static final long UPDATE_INTERVAL = 10800000; //Every 3 hrs
    //public static final long UPDATE_INTERVAL = 1080000; //More frequently
    public static final long UPDATE_INTERVAL = 30000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private GoogleApiClient mGoogleApiClient;
    private static final int MY_PERMISSIONS_LOCATIONS = 814;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private boolean mWeatherChecked = false;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get app color theme
        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        int theme = prefs.getInt("colorTheme", 1);
        // apply app theme to activity
        if( theme == 1) {
            setTheme(R.style.BlueAndOragneAppTheme);
        } else if (theme == 2) {
            setTheme(R.style.GreenAndAmberAppTheme);
        } else if (theme == 3) {
            setTheme(R.style.RedAndBlueAppTheme);
        } else if (theme == 4) {
            setTheme(R.style.BrownAndPinkAppTheme);
        } else {
            Log.wtf("SignInActivity", "Why is the theme option set to " + Integer.toString(theme)+ "?!?!");
        }
        setContentView(R.layout.activity_home);

        if(savedInstanceState == null) {
            if (findViewById(R.id.HomeContainer) != null) {
                getSupportFragmentManager().beginTransaction().add(R.id.HomeContainer,
                        new HomeInformationFragment(), getString(R.string.home_info_tag))
                        .commit();
            }
        }
        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Chat");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.HomeActivityLayout);

        mToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(getString(R.string.keys_sp_on),true);
        editor.apply();
        Intent intent = getIntent();
        userName = intent.getStringExtra("username");

        //Ask for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION
                            , Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_LOCATIONS);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Switch ContactsIntentService from background to foreground mode.
        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        //Check to see if service should already be running
        if(prefs.getBoolean(getString(R.string.keys_sp_on),false)) {
            //stop service from the background
            ContactsIntentService.stopServiceAlarm(this);
            //restart service but in the foreground
            ContactsIntentService.startServiceAlarm(this, true);
        }

        //check to see if the Intent came from the ContactsIntentService.
        //if so, load the PendingConnectionsFragment.
        if(mDataUpdateReceiver == null) {
            mDataUpdateReceiver = new DataUpdateReceiver();
        }
        IntentFilter iFilter = new IntentFilter(ContactsIntentService.RECEIVED_UPDATE);
        registerReceiver(mDataUpdateReceiver, iFilter);
        if(getIntent().hasExtra(getString(R.string.keys_extra_results))) {
            loadFragment(new PendingConnectionsFragment());
        }
    }

    @Override
    protected void onPause(){
        super.onPause();

        //switch ContactsIntentService from foreground to background mode.
        SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        if(prefs.getBoolean(getString(R.string.keys_sp_on),false)) {
            //stop service running in foreground
            ContactsIntentService.stopServiceAlarm(this);
            //restart service in background
            ContactsIntentService.startServiceAlarm(this, false);
        }
        if(mDataUpdateReceiver != null) {
            unregisterReceiver(mDataUpdateReceiver);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted. Do the locations-related task you need to do.
                } else {
                    // permission denied. Disable the functionality that depends on this permission.
                    Log.d("PERMISSION DENIED", "Nothing to see or do here.");

                    //Shut down the app. In production release, you would let the user
                    //know why the app is shutting down…maybe ask for permission again?
                    //finishAndRemoveTask();
                }
                return;
            }
            // other 'case' lines to check for other permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.HomeActivityLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.logOutOption) {
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);

            //SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().remove(getString(R.string.keys_prefs_username)).apply();
            prefs.edit().putBoolean(
                    getString(R.string.keys_prefs_stay_logged_in),
                    false)
                    .apply();
            //noinspection SimplifiableIfStatement
            //Home setting
            Intent myIntent = new Intent(this,   SignInActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(myIntent);
            finish();
        }else if (id == R.id.settingOption){
            loadFragment(new SettingsFragment());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(mToggle.getDrawerArrowDrawable() != null && mToggle.getDrawerArrowDrawable() instanceof  BadgeDrawerArrowDrawable) {
            ((BadgeDrawerArrowDrawable)mToggle.getDrawerArrowDrawable()).setEnabled(false);
        }

        if (id == R.id.nav_connections) {
            loadFragment(new ConnectionsFragment());
        } else if (id == R.id.nav_new_connections) {
//            loadFragment(new NewConnectionFragment());
            loadFragment(new SearchNewConnectionFragment());
        } else if (id == R.id.nav_home) {
            loadFragment(new HomeInformationFragment());
        } else if (id == R.id.nav_pending_connections){
            loadFragment(new PendingConnectionsFragment());
        } else if (id == R.id.nav_chat_list) {
            //loadFragment(new ChatFragment());

            android.content.Intent intent = new android.content.Intent(this, ChatSessionActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK|android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.HomeActivityLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Loads the fragments
    private void loadFragment(Fragment frag) {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.HomeContainer, frag)
                .addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSettingsInteraction(int theme) {
//        Intent restartIntent = new Intent(this, HomeActivity.class);
        SharedPreferences themePrefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor themeEditor = themePrefs.edit();
        themeEditor.putInt("colorTheme", theme);
        themeEditor.apply();
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    public void onSearchAttempt(String username, String keyword,
                                ArrayList<SearchNewConnectionFragment.SearchConnectionListItem> data,
                                SearchNewConnectionFragment.SearchConnectionAdapter adapter) {
        searchContactList = data;
        searchConnectionAdapter = adapter;
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_search_contact))
                .build();
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", username);
            msg.put("term", keyword);
            //System.out.println(msg);
        } catch (JSONException e) {
            Log.wtf("VERIFICATION", "Error creating JSON: " + e.getMessage());
        }
//        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        searchContactList.clear();
        ProgressBar searchConnectionProgrsesBar = (ProgressBar) findViewById(R.id.searchConnectionProgressBar);
        searchConnectionProgrsesBar.setVisibility(View.VISIBLE);
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleSearchContact)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    private void handleSearchContact(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                // System.out.println(resultsJSON);
                populateSearchContactResult(resultsJSON);
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    private void populateSearchContactResult(JSONObject resultsJSON) {
        try {
            JSONArray array = resultsJSON.getJSONArray("message");
            if (findViewById(R.id.searchConnectionProgressBar) != null) {
                ProgressBar searchConnectionProgressBar = (ProgressBar) findViewById(R.id.searchConnectionProgressBar);
                searchConnectionProgressBar.setVisibility(View.GONE);
            }
            for (int i =0; i < array.length(); i++) {
                JSONObject aContact = array.getJSONObject(i);
                // PARSE JSON RESULTS HERE
                String first = aContact.getString("firstname");
                String last = aContact.getString("lastname");
                String username = aContact.getString("username");
                String email = aContact.getString("email");
                searchContactList.add(new SearchNewConnectionFragment.SearchConnectionListItem(first,
                        last, username, email));
                searchConnectionAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", "Error when populating contacts.");
        }
    }

    @Override
    public void onGetContactsAttempt(String username, ArrayList<ConnectionsFragment.Connection> data,
                                     ConnectionsFragment.ConnectionsAdapter adapter) {
        this.connectionsAdapter = adapter;
        this.connectionList = data;
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_contacts))
                .build();
        //build the JSONObject
        JSONObject msg = new JSONObject();
        try {
            msg.put("username", username);
            //System.out.println(msg);
        } catch (JSONException e) {
            Log.wtf("VERIFICATION", "Error creating JSON: " + e.getMessage());
        }
//        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleGetContacts)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();

    }

    private void handleGetContacts(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                //System.out.println(resultsJSON);
                populateGetContactsResult(resultsJSON);
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    private void populateGetContactsResult(JSONObject resultsJSON) {
        try {
            JSONArray array = resultsJSON.getJSONArray("message");

            connectionList.clear();
            if (findViewById(R.id.loadConnectionsProgressBar) != null) {
                ProgressBar loadingConnectionsProgressBar = (ProgressBar) findViewById(R.id.loadConnectionsProgressBar);
                loadingConnectionsProgressBar.setVisibility(View.GONE);
            }
            for (int i =0; i < array.length(); i++) {
                JSONObject aContact = array.getJSONObject(i);
                // PARSE JSON RESULTS HERE
                String memberId = aContact.getString("memberid");
                String first = aContact.getString("firstname");
                String last = aContact.getString("lastname");
                String username = aContact.getString("username");
                String email = aContact.getString("email");
                connectionList.add(new ConnectionsFragment.Connection(memberId, first, last, email));
                connectionsAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", "Error when populating contacts.");
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // If the initial location was never previously requested, we use
        // FusedLocationApi.getLastLocation() to get it. If it was previously requested, we store
        // its value in the Bundle and check for it in onCreate(). We
        // do not request it again unless the user specifically requests location updates by pressing
        // the Start Updates button.
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED
                    &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " +
                connectionResult.getErrorCode());
    }

    /**Callback that fires when the location changes.*/
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(TAG, mCurrentLocation.toString());
        HomeInformationFragment homeFragment = (HomeInformationFragment) getSupportFragmentManager().
                findFragmentByTag(getString(R.string.home_info_tag));
        homeFragment.setLocation(location);
        if (!mWeatherChecked) {
            getLocation();
            mWeatherChecked = true;
        }
    }

    /**Requests location updates from the FusedLocationApi.*/
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**Removes location updates from the FusedLocationApi.*/
    protected void stopLocationUpdates() {
        // Remove location requests when the activity is in a paused or stopped state.
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getLocation() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_locate_gps))
                .build();
        //build the JSONObject
        //Pass lat and lon as username and email so we can use credentials.asJSON
        String lat = Double.toString(mCurrentLocation.getLatitude());
        String lon = Double.toString(mCurrentLocation.getLongitude());
        Credentials cred = new Credentials.Builder(lat, null)
                .addEmail(lon)
                .build();
        JSONObject msg = cred.asJSONObject();
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleLocationPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLocationPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            int location;
            location = resultsJSON.getInt("Key");
            if (location != 0) getWeather(location);
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getWeather(int location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_current))
                .build();
        //build the JSONObject
        //Pass location as username so we can use credentials.asJSON
        String loc = Integer.toString(location);
        Credentials cred = new Credentials.Builder(loc, null)
                .build();
        JSONObject msg = cred.asJSONObject();
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleWeatherPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handleWeatherPost(final String jsonResult) {
        String description = "";
        double temp = -99;
        try {
            JSONArray json = new JSONArray(jsonResult);
            if (json.getJSONObject(0).has("WeatherText")) {
                description = json.getJSONObject(0).getString("WeatherText");
            }
            if (json.getJSONObject(0).has("Temperature")) {
                JSONObject response = json.getJSONObject(0).getJSONObject("Temperature");
                if (response.has("Imperial")) {
                    JSONObject type = response.getJSONObject("Imperial");
                    if (type.has("Value")) {
                        temp = type.getDouble("Value");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp != -99){
            String weather = description + ": " + Double.toString(temp);
            HomeInformationFragment homeFragment = (HomeInformationFragment) getSupportFragmentManager().
                    findFragmentByTag(getString(R.string.home_info_tag));
            homeFragment.setWeather(weather);

        }
    }

    /**
     * Transitions to the WeatherMapActivity.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    @Override
    public void onMoreWeatherClicked() {
        Intent i = new Intent(this, WeatherMapActivity.class);
        i.putExtra(WeatherMapActivity.LATITUDE, mCurrentLocation.getLatitude());
        i.putExtra(WeatherMapActivity.LONGITUDE, mCurrentLocation.getLongitude());
        i.putExtra("username", userName);
        startActivity(i);
    }

    //used to add notification icon to hamburger button.
    private void addHamburgerButtonBadge(String msg){
        //check if hamburger button already has badge.
        if(mToggle.getDrawerArrowDrawable() != null && mToggle.getDrawerArrowDrawable() instanceof BadgeDrawerArrowDrawable) {
            ((BadgeDrawerArrowDrawable)mToggle.getDrawerArrowDrawable()).setEnabled(true);
        } else {
            BadgeDrawerArrowDrawable badgeDrawable = new BadgeDrawerArrowDrawable(getSupportActionBar().getThemedContext());
            mToggle.setDrawerArrowDrawable(badgeDrawable);
            badgeDrawable.setText(msg);
        }
    }

    // This internal class is to listen for pending connections while the HomeActivity is in the foreground.
    private class DataUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ContactsIntentService.RECEIVED_UPDATE)) {
                Log.d(TAG, "Hey I just got your broadcast!");
//                loadFragment(new PendingConnectionsFragment());

                //add badge to navigation drawer pending connections item.
                addHamburgerButtonBadge("!");

            }
        }
    }

}
