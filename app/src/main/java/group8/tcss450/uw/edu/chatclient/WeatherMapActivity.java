package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class WeatherMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, AdapterView.OnItemSelectedListener{

    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lng";
    private GoogleMap mGoogleMap;
    private double mLat, mLng, mSavedLat, mSavedLng;
    private Marker m;
    private String mWhenCoice = "Now";
    private String mWhereCoice = "Here";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map);

        mLat = getIntent().getDoubleExtra(LATITUDE, 0.0);
        mLng = getIntent().getDoubleExtra(LONGITUDE, 0.0);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);

        SharedPreferences prefs =
                getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        String location = prefs.getString(getString(R.string.keys_prefs_save_location), null);
        if (location != null) {
            String loc[] = location.split("-");
            mSavedLat = Double.parseDouble(loc[0]);
            mSavedLng = Double.parseDouble(loc[1]);
        } else {
            mSavedLat = 47;     //Default UWT
            mSavedLng = -122;
        }

        Spinner whereSpinner = findViewById(R.id.weatherWhereSpinner);
        ArrayAdapter<CharSequence> whereAdapter = ArrayAdapter.createFromResource(this,
                R.array.weatherSWhereArray, android.R.layout.simple_spinner_item);
        whereAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whereSpinner.setAdapter(whereAdapter);
        whereSpinner.setOnItemSelectedListener(this);

        Spinner whenSpinner = findViewById(R.id.weatherWhenSpinner);
        ArrayAdapter<CharSequence> whenAdapter = ArrayAdapter.createFromResource(this,
                R.array.weatherSWhenArray, android.R.layout.simple_spinner_item);
        whenAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        whenSpinner.setAdapter(whenAdapter);
        whenSpinner.setOnItemSelectedListener(this);


    }

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

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d("LAT/LONG", latLng.toString());
        if (m != null) {
            m.setPosition(latLng);
        } else {
            m = mGoogleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .draggable(true));
        }

        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18.0f));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = (String) parent.getAdapter().getItem(position);
        Toast.makeText(this,
                "The item is " + item + "---from: " + parent,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
