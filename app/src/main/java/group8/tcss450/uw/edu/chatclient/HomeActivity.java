package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import group8.tcss450.uw.edu.chatclient.utils.RequestsListenManager;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

/**
 * Home activity after logging in
 *
 * @author Jin Byoun - jinito@uw.edu
 */
public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        ,SettingsFragment.OnSettingsInteractionListener
        ,SearchNewConnectionFragment.SearchContactFragmentInteractionListener {

    private ArrayList<SearchNewConnectionFragment.SearchConnectionListItem> searchContactList;
    private ArrayList<PendingConnectionsFragment.IncomingRequestListItem> incomingRequestsList;
    private ArrayList<PendingConnectionsFragment.OutgoingRequestListItem> outgoingRequestsList;

    private SearchNewConnectionFragment.SearchConnectionAdapter adapter;
    private PendingConnectionsFragment.IncomingRequestAdapter incomingAdapter;
    private PendingConnectionsFragment.OutgoingRequestAdapter outgoingAdapter;

    private String userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //get app color theme
        SharedPreferences themePrefs = getSharedPreferences(getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        int theme = themePrefs.getInt("colorTheme", 1);
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
                getSupportFragmentManager().beginTransaction().add(R.id.HomeContainer, new HomeInformationFragment()).commit();
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setTitle("Chat");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.HomeActivityLayout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Intent intent = getIntent();
        userName = intent.getStringExtra("username");
        System.out.println(userName);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

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
                // maybe instead of replace
                .addToBackStack(null);
        // Commit the transaction
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
        this.adapter = adapter;
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
            System.out.println(msg);
        } catch (JSONException e) {
            Log.wtf("VERIFICATION", "Error creating JSON: " + e.getMessage());
        }
//        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
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
                System.out.println(resultsJSON);
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

            searchContactList.clear();
            for (int i =0; i < array.length(); i++) {
                JSONObject aContact = array.getJSONObject(i);
                // PARSE JSON RESULTS HERE
                String first = aContact.getString("firstname");
                String last = aContact.getString("lastname");
                String username = aContact.getString("username");
                String email = aContact.getString("email");
                searchContactList.add(new SearchNewConnectionFragment.SearchConnectionListItem(first, last, username, email));
                adapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            Log.e("JSON_PARSE_ERROR", "Error when populating contacts.");
        }
    }

    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }



}
