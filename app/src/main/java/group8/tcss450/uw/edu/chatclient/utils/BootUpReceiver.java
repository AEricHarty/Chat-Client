package group8.tcss450.uw.edu.chatclient.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import group8.tcss450.uw.edu.chatclient.R;

public class BootUpReceiver extends BroadcastReceiver {

    private static final String TAG = "BootUpReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.keys_shared_prefs),
                context.MODE_PRIVATE);

        if(prefs.getBoolean(context.getString(R.string.keys_sp_on),false)) {
            Log.d(TAG, "Starting service.");
        } else {
            Log.e(TAG,"Did NOT Start the service");
        }
    }

    public BootUpReceiver(){

    }
}
