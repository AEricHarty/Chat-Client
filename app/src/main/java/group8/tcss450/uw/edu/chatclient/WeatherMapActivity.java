package group8.tcss450.uw.edu.chatclient;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import group8.tcss450.uw.edu.chatclient.model.Credentials;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

/**
 * Activity for the Map and major Weather features
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class WeatherMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, AdapterView.OnItemSelectedListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    /**The desired interval for location updates. Inexact. Updates may be more or less frequent.*/
    //public static final long UPDATE_INTERVAL = 10800000; //Every 3 hrs
    //public static final long UPDATE_INTERVAL = 1080000; //More frequently
    public static final long UPDATE_INTERVAL = 30000;
    public static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;
    private static final String TAG = "WeatherMapActivity ERROR->";
    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    private GoogleMap mGoogleMap;
    private double mLat, mLng;
    private Marker mMarker;
    private String mWhenChoice = "Now";
    private String mWhereChoice = "Here";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    private EditText mZIPView;
    private TextView mResultView;
    private int mChoiceFlag;
    private ProgressBar mProgressBar;
    private Button mSubmitButton;

    /**@author Eric Harty - hartye@uw.edu*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map);

        mZIPView = (EditText) findViewById(R.id.weatherZIPEditText);
        mZIPView.setVisibility(View.GONE);
        mLat = getIntent().getDoubleExtra(LATITUDE, 0.0);
        mLng = getIntent().getDoubleExtra(LONGITUDE, 0.0);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        Spinner whereSpinner = (Spinner)findViewById(R.id.weatherWhereSpinner);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.weatherSWhereArray, android.R.layout.simple_spinner_item);
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whereSpinner.setAdapter(whereAdapter);
        whereSpinner.setOnItemSelectedListener(this);

        Spinner whenSpinner = (Spinner) findViewById(R.id.weatherWhenSpinner);
        ArrayAdapter<CharSequence> whenAdapter = ArrayAdapter.createFromResource(this,
                R.array.weatherSWhenArray, android.R.layout.simple_spinner_item);
        whenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whenSpinner.setAdapter(whenAdapter);
        whenSpinner.setOnItemSelectedListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.weatherMapProgressBar);
        mProgressBar.setVisibility(View.GONE);

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

        mResultView = (TextView) findViewById(R.id.weatherResultView);


        mSubmitButton = (Button) findViewById(R.id.weatherSubmitButton);
        mSubmitButton.setOnClickListener(this::onSubmitClick);

    }

    /**@author Eric Harty - hartye@uw.edu*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        // Hardcode - Add a marker in Tacoma, WA, and move the camera.
        //LatLng latLng = new LatLng(47.2529, -122.4443);
        LatLng latLng = new LatLng(mLat, mLng);
        mGoogleMap.addMarker(new MarkerOptions().
                position(latLng).
                title("Marker in Tacoma"));
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
        mGoogleMap.setOnMapClickListener(this);
    }

    /**@author Eric Harty - hartye@uw.edu*/
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("LAT/LONG", latLng.toString());
        if (mMarker != null) {
            mMarker.setPosition(latLng);
        } else {
            mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));
        }

        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
    }

    /**@author Eric Harty - hartye@uw.edu*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) parent.getAdapter().getItem(position);
        //Log.d("--->","The item is " + item + " from: " + parent);
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.weatherWhenSpinner) {
            mWhenChoice = item;
        } else if(spinner.getId() == R.id.weatherWhereSpinner) {
            mWhereChoice = item;
            if (item.equals("ZIP")){
                mZIPView.setVisibility(View.VISIBLE);
            } else{
                mZIPView.setVisibility(View.GONE);
            }
        }
    }

    /**@author Eric Harty - hartye@uw.edu*/
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.weatherWhereSpinner) {
            mZIPView.setVisibility(View.GONE);
        }
    }

    /**@author Eric Harty - hartye@uw.edu*/
    public void onSubmitClick(View view) {
        String lat;
        String lon;
        //There's probably a better design pattern to handle this but it's the end of sprint 4
        if(mWhenChoice.equals("Now")){
            mChoiceFlag = 0;
        } else if (mWhenChoice.equals("Tomorrow")){
            mChoiceFlag = 1;
        } else if (mWhenChoice.equals("Five Days")){
            mChoiceFlag = 2;
        }
        if(mWhereChoice.equals("Here")){
            if(mCurrentLocation != null){
                lat = Double.toString(mCurrentLocation.getLatitude());
                lon = Double.toString(mCurrentLocation.getLongitude());
            } else {
                lat = Double.toString(mLat);
                lon = Double.toString(mLng);
            }
            getLocationGPS(lat, lon);
        } else if(mWhereChoice.equals("Pin")){
            if(mMarker != null){
                lat = Double.toString(mMarker.getPosition().latitude);
                lon = Double.toString(mMarker.getPosition().longitude);
            } else {
                lat = Double.toString(mLat);
                lon = Double.toString(mLng);
            }
            getLocationGPS(lat, lon);
        } else if(mWhereChoice.equals("Zip")){

        } else if(mWhereChoice.equals("Saved")){
            String l;
            SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            l = prefs.getString(getString(R.string.location_key), "41556_PC");
            if(mWhenChoice.equals("Now")){
                getCurrentWeather(l);
            } else if (mWhenChoice.equals("Tomorrow")){
                getNextWeather(l);
            } else if (mWhenChoice.equals("Five Days")){
                getFiveWeather(l);
            } else {
                System.out.println("Error with Spinner!");
            }
        }
    }

    /**
     * Confirms the ZIP is 5 digits before sending.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void checkZIP() {
        String zip = (mZIPView.getText().toString());
        if (zip.length() != 5){
            mZIPView.setError("5-Digit ZIP");
        } else{
            getLocationZIP();
        }
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     */
    private void handlePre() {
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressBar.setProgress(0);
        mSubmitButton.setEnabled(false);
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getLocationGPS(String lat, String lon) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_locate_gps))
                .build();
        //build the JSONObject
        //Pass lat and lon as username and email so we can use credentials.asJSON
        Credentials cred = new Credentials.Builder(lat, null)
                .addEmail(lon)
                .build();
        JSONObject msg = cred.asJSONObject();
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handlePre)
                .onPostExecute(this::handleLocationPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getLocationZIP() {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_locate_zip))
                .build();
        //build the JSONObject
        //Pass zip as username so we can use credentials.asJSON
        String zip = (mZIPView.getText().toString());
        Credentials cred = new Credentials.Builder(zip, null)
                .build();
        JSONObject msg = cred.asJSONObject();
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handlePre)
                .onPostExecute(this::handleLocationZIPPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * @param result the JSON formatted String response from the web service
     */
    private void handleLocationPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            if (resultsJSON.has("Key")){
                String location = resultsJSON.getString("Key");
                CheckBox save = (CheckBox) findViewById(R.id.weatherCheckBox);
                if(save.isChecked()){
                    saveLocation(location);
                }
                // Use the flags to see which task to follow with
                if(mChoiceFlag == 0){
                    getCurrentWeather(location);
                } else if (mChoiceFlag == 1){
                    getNextWeather(location);
                } else if (mChoiceFlag == 2){
                    getFiveWeather(location);
                    Log.d("++++++++++","!!!!!!!!!got here!!!!!!!!!");
                }
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**
     * Parses the differently formatted ZIP requests
     *
     * @author Eric Harty - hartye@uw.edu
     *
     * @param result the JSON formatted String response from the web service
     */
    private void handleLocationZIPPost(String result) {
        try {
            JSONArray resultsJSON = new JSONArray(result);

            JSONObject response = resultsJSON.getJSONObject(0);
            if (response.has("Key")) {
                String location = response.getString("Key");
                CheckBox save = (CheckBox) findViewById(R.id.weatherCheckBox);
                if(save.isChecked()){
                    saveLocation(location);
                }
                // Use the flags to see which task to follow with
                if(mChoiceFlag == 0){
                    getCurrentWeather(location);
                } else if (mChoiceFlag == 1){
                    getNextWeather(location);
                } else if (mChoiceFlag == 2){
                    getFiveWeather(location);
                }
            }
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**@author Eric Harty - hartye@uw.edu*/
    public void saveLocation(String key) {
        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        prefs.edit().putString(
                getString(R.string.location_key), key)
                .apply();
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getCurrentWeather(String location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_current))
                .build();
        //build the JSONObject
        //Pass location as username so we can use credentials.asJSON
        Credentials cred = new Credentials.Builder(location, null)
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleCurrentWeatherPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getNextWeather(String location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_next))
                .build();
        //build the JSONObject
        //Pass location as username so we can use credentials.asJSON
        Credentials cred = new Credentials.Builder(location, null)
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleNextWeatherPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Builds JSON and starts new AsyncTask to send to weather service.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void getFiveWeather(String location) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_weather))
                .appendPath(getString(R.string.ep_weather_five))
                .build();
        //build the JSONObject
        //Pass location as username so we can use credentials.asJSON
        Credentials cred = new Credentials.Builder(location, null)
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleFiveWeatherPost)
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
    private void handleCurrentWeatherPost(final String jsonResult) {
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
            String output = String.format(getString(R.string.weather_single_msg),
                    description, temp);
            mResultView.setText(output);
        }
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handleNextWeatherPost(final String jsonResult) {
        String description = "";
        int temp = -99;
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("Headline")) {
                JSONObject response = json.getJSONObject("Headline");
                if (response.has("Text"))
                description = response.getString("Text");
            }
            if (json.has("DailyForecasts")){
                JSONArray forecast = new JSONArray(json);
                if (forecast.getJSONObject(5).has("Temperature")) {
                    JSONObject temperature = forecast.getJSONObject(5).getJSONObject("Temperature");
                    if (temperature.has("Maximum")) {
                        JSONObject type = json.getJSONObject("Maximum");
                        if (type.has("Value")) {
                            JSONObject val = json.getJSONObject("Value");
                            temp = val.getInt("Value");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp != -99){
            String output = String.format(getString(R.string.weather_single_msg),
                    description, temp);
            mResultView.setText(output);
        }
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
    }

    /**
     * @author Eric Harty - hartye@uw.edu
     *
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param jsonResult the JSON formatted String response from the web service
     */
    private void handleFiveWeatherPost(final String jsonResult) {
        String description = "";
        int temp[] = {-99, -99, -99, -99, -99};
        try {

            JSONObject json = new JSONObject(jsonResult);
            if (json.has("Headline")) {
                JSONObject response = json.getJSONObject("Headline");
                if (response.has("Text"))
                    description = response.getString("WeatherText");
            }
            if (json.has("DailyForecasts")){
                JSONArray forecast = new JSONArray(json);
                if (forecast.getJSONObject(5).has("Temperature")) {
                    //Loop through all five days
                    for (int i = 0; i < 5; i++){
                        JSONObject temperature = forecast.getJSONObject(i).getJSONObject("Temperature");
                        if (temperature.has("Maximum")) {
                            JSONObject type = json.getJSONObject("Maximum");
                            if (type.has("Value")) {
                                JSONObject val = json.getJSONObject("Value");
                                temp[i] = val.getInt("Value");
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp[0] != -99){
            String output = String.format(getString(R.string.weather_five_msg), description,
                    temp[0], temp[1], temp[2], temp[3], temp[4]);
            mResultView.setText(output);
        }
        mProgressBar.setVisibility(View.GONE);
        mSubmitButton.setEnabled(true);
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


}
