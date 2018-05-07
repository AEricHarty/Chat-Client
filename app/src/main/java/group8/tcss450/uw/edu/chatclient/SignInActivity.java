package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import group8.tcss450.uw.edu.chatclient.model.Credentials;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

/**
 * Launcher activity.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class SignInActivity extends AppCompatActivity implements
        LoginFragment.OnLoginFragmentInteractionListener,
        RegisterFragment.OnRegisterFragmentInteractionListener {

    private Credentials mCredentials;

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


        setContentView(R.layout.activity_sign_in);
        //setContentView(R.layout.activity_home);

        if(savedInstanceState == null) {
            if (findViewById(R.id.signinActivity) != null) {
                SharedPreferences prefs =
                        getSharedPreferences(
                                getString(R.string.keys_shared_prefs),
                                Context.MODE_PRIVATE);
                if (prefs.getBoolean(getString(R.string.keys_prefs_stay_logged_in),
                        false)) {
                    loadHome();
                } else {

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.signinActivity, new LoginFragment(),
                                    getString(R.string.keys_fragment_login))
                            .commit();
                }
            }
        }
    }

    /**
     * Builds JSON and starts new AsyncTask to send Login post.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    @Override
    public void onLoginAttempt(Credentials cred) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_login))
                .build();
        //build the JSONObject
        JSONObject msg = cred.asJSONObject();
        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleLoginOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Transitions to the registerFragment.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    @Override
    public void onRegisterClicked() {
        RegisterFragment registerFragment = new RegisterFragment();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.signinActivity, registerFragment)
                .addToBackStack(null);
        transaction.commit();
    }

    /**
     * Builds JSON and starts new AsyncTask to send Registration post.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    @Override
    public void onRegisterAttempt(Credentials cred) {
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_register))
                .build();
        //build the JSONObject
        JSONObject msg = cred.asJSONObject();
        mCredentials = cred;
        //instantiate and execute the AsyncTask.
        //Feel free to add a handler for onPreExecution so that a progress bar
        //is displayed or maybe disable buttons. You would need a method in
        //LoginFragment to perform this.
        new SendPostAsyncTask.Builder(uri.toString(), msg)
                .onPostExecute(this::handleRegisterOnPost)
                .onCancelled(this::handleErrorsInTask)
                .build().execute();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleLoginOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            if (success) {
                checkStayLoggedIn();
                loadHome();
            } else {
                //Login was unsuccessful. Don’t switch fragments and inform the user
                /*LoginFragment frag =
                        (LoginFragment) getSupportFragmentManager()
                                .findFragmentByTag(
                                        getString(R.string.keys_fragment_login));
                frag.setError("Log in unsuccessful");*/
                TextView fail = findViewById(R.id.loginFailMsg);
                fail.setVisibility(View.VISIBLE);
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
     * Checks if Stay Logged In is selected and saves to SharedPreferences if needed.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    private void checkStayLoggedIn() {
        if (((CheckBox) findViewById(R.id.logCheckBox)).isChecked()) {
            SharedPreferences prefs =
                    getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            prefs.edit().putString(
                    getString(R.string.keys_prefs_username),
                    mCredentials.getUsername())
                    .apply();
            prefs.edit().putBoolean(
                    getString(R.string.keys_prefs_stay_logged_in),
                    true)
                    .apply();
        }


    }

    /**
     * Transitions to the HomeActivity.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void loadHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Handle onPostExecute of the AsynceTask. The result from our webservice is
     * a JSON formatted String. Parse it for success or failure.
     * @param result the JSON formatted String response from the web service
     */
    private void handleRegisterOnPost(String result) {
        try {
            JSONObject resultsJSON = new JSONObject(result);
            boolean success = resultsJSON.getBoolean("success");
            loadRegisterResult(success);
        } catch (JSONException e) {
            //It appears that the web service didn’t return a JSON formatted String
            //or it didn’t have what we expected in it.
            Log.e("JSON_PARSE_ERROR", result
                    + System.lineSeparator()
                    + e.getMessage());
        }
    }

    /**
     * Loads the RegisterResultFragment.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    public void loadRegisterResult(boolean success) {
        //getSupportFragmentManager().popBackStack(0, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        RegisterResultFragment resultFragment = new RegisterResultFragment();
        Bundle args = new Bundle();
        args.putBoolean("result", success);
        resultFragment.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.signinActivity, resultFragment);
        if(!success){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    /**
     * Handle errors that may occur during the AsyncTask.
     * @param result the error message provide from the AsyncTask
     */
    private void handleErrorsInTask(String result) {
        Log.e("ASYNCT_TASK_ERROR", result);
    }
}