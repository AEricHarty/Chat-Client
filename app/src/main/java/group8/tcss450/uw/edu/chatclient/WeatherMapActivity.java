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

import java.text.DecimalFormat;

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
                title("You are here"));
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
        //This feels weird to me
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

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.weatherWhereSpinner) {
            mZIPView.setVisibility(View.GONE);
        }
    }

    /**
     * Determines what is selected in the spinner set and sends the appropriate AsyncTask
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void onSubmitClick(View view) {
        CheckBox save = (CheckBox) findViewById(R.id.weatherCheckBox);
        String lat;
        String lon;
        //There's probably a better way to do this with less cyclomatic complexity
        if(mWhereChoice.equals("Here")){
            if(mCurrentLocation != null){
                lat = Double.toString(mCurrentLocation.getLatitude());
                lon = Double.toString(mCurrentLocation.getLongitude());
            } else {
                lat = Double.toString(mLat);
                lon = Double.toString(mLng);
            }
            String loc = lat + "," + lon;
            if(save.isChecked()) saveLocation(loc);
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(loc);
            }
        } else if(mWhereChoice.equals("Pin")){
            if(mMarker != null){
                lat = Double.toString(mMarker.getPosition().latitude);
                lon = Double.toString(mMarker.getPosition().longitude);
            } else {
                lat = Double.toString(mLat);
                lon = Double.toString(mLng);
            }
            String loc = lat + "," + lon;
            if(save.isChecked()) saveLocation(loc);
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(loc);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(loc);
            }
        } else if(mWhereChoice.equals("ZIP")){
            checkZIP();
        } else if(mWhereChoice.equals("Saved")){
            String l;
            SharedPreferences prefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                    Context.MODE_PRIVATE);
            l = prefs.getString(getString(R.string.location_key), "98403");
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(l);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(l);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
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
        CheckBox save = (CheckBox) findViewById(R.id.weatherCheckBox);
        String zip = (mZIPView.getText().toString());
        if (zip.length() != 5){
            mZIPView.setError("5-Digit ZIP");
        } else{
            if(save.isChecked()) saveLocation(zip);
            if(mWhenChoice.equals(getString(R.string.weather_now))){
                getCurrentWeather(zip);
            } else if (mWhenChoice.equals(getString(R.string.weather_tomorrow))){
                getNextWeather(zip);
            } else if (mWhenChoice.equals(getString(R.string.weather_ten))){
                getFiveWeather(zip);
            }
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
                .onPreExecute(this::handlePre)
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
                .appendPath(getString(R.string.ep_weather_forecast))
                .build();
        //build the JSONObject
        //Pass location as username so we can use credentials.asJSON
        Credentials cred = new Credentials.Builder(location, null)
                .addEmail("1")
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handlePre)
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
                .appendPath(getString(R.string.ep_weather_forecast))
                .build();
        //build the JSONObject
        //Pass location as username so we can use credentials.asJSON
        Credentials cred = new Credentials.Builder(location, null)
                .addEmail("7")
                .build();
        JSONObject msg = cred.asJSONObject();
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPreExecute(this::handlePre)
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
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("current")) {
                JSONObject current = json.getJSONObject("current");
                if (current.has("temp_f")) {
                    temp = current.getDouble("temp_f");
                }
                if (current.has("condition")) {
                    JSONObject cond = current.getJSONObject("condition");
                    if (cond.has("text")) {
                        description = cond.getString("text");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp != -99){
            //Truncate the temp
            DecimalFormat df = new DecimalFormat("#.#");
            String output = String.format(getString(R.string.weather_single_msg),
                    description, df.format(temp));
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
        double temp = -99;
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("forecast")) {
                JSONObject result = json.getJSONObject("forecast");
                JSONArray days = result.getJSONArray("forecastday");
                JSONObject forecast = days.getJSONObject(0).getJSONObject("day");
                if (forecast.has("maxtemp_f")) {
                    temp = forecast.getDouble("maxtemp_f");
                }
                if (forecast.has("condition")) {
                    JSONObject cond = forecast.getJSONObject("condition");
                    if (cond.has("text")) {
                        description = cond.getString("text");
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp != -99){
            //Truncate the temp
            DecimalFormat df = new DecimalFormat("#.#");
            String output = String.format(getString(R.string.weather_single_msg),
                    description, df.format(temp));
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
        double temp[] = {-99, -99, -99, -99, -99, -99, -99};
        try {
            JSONObject json = new JSONObject(jsonResult);
            if (json.has("forecast")) {
                JSONObject result = json.getJSONObject("forecast");
                JSONArray days = result.getJSONArray("forecastday");
                for(int i =0; i < 7; i++){
                    JSONObject forecast = days.getJSONObject(i).getJSONObject("day");
                    if (forecast.has("maxtemp_f")) {
                        temp[i] = forecast.getDouble("maxtemp_f");
                    }
                    if (forecast.has("condition")) {
                        JSONObject cond = forecast.getJSONObject("condition");
                        if (cond.has("text")) {
                            description = cond.getString("text");
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
        if(description.length() != 0 && temp[0] != -99){
            //Truncate the temp
            DecimalFormat df = new DecimalFormat("#.#");
            String output = String.format(getString(R.string.weather_five_msg), description,
                    df.format(temp[0]), df.format(temp[1]), df.format(temp[2]), df.format(temp[3]),
                    df.format(temp[4]), df.format(temp[5]), df.format(temp[6]));
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
