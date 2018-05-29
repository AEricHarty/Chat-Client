package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import group8.tcss450.uw.edu.chatclient.utils.ListenManager;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatInnerFragment extends Fragment {


    private String mLeaveChatUrl;
    private String mAddToChatUrl;
    private String mAddStrangerUrl;
    private String mUsername;
    private Bundle bundle;

    public ChatInnerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat_inner, container, false);

        // Inflate the layout for this fragment
        v.findViewById(R.id.chatInnerLeaveChatButton).setOnClickListener(this::leaveChat);

        v.findViewById(R.id.chatInnerAddChatMemberButton).setOnClickListener(this::addToChat);

        v.findViewById(R.id.chatInnerAddConnectionButton).setOnClickListener(this::addStranger);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        bundle = this.getActivity().getIntent().getExtras();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
//            throw new IllegalStateException("No username in prefs!");
            mUsername = bundle.getString("username", "");
        } else {
            mUsername = prefs.getString("username", "");
        }

        bundle = this.getActivity().getIntent().getExtras();
        int chatId = bundle.getInt("chatId");

        System.out.println("chatId in Inner fragment is: " + chatId);

        System.out.println("Username in inner Fragment is: " + mUsername);

        mLeaveChatUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_leave_chat))
                .build()
                .toString();

        mAddToChatUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_add_to_chat))
                .build()
                .toString();

        mAddStrangerUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_request))
                .build()
                .toString();
    }

    private void addToChat(final View theButton) {
        JSONObject messageJson = new JSONObject();

        int chatId = bundle.getInt("chatId");

        String userToAdd = ((EditText) getView().findViewById(R.id.chatInnerAddToChatInputEditText))
                .getText().toString();

        try {
            messageJson.put(getString(R.string.keys_json_username), userToAdd);

            messageJson.put(getString(R.string.keys_chatId), chatId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mAddToChatUrl, messageJson)
                .onPostExecute(this::endOfAddToChatTask)
                .onCancelled(this::handleAddToChatError)
                .build().execute();
    }

    private void handleAddToChatError(final String msg) {
        Log.e("Leaving Chat ERROR!!!", msg.toString());
    }

    private void endOfAddToChatTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);

            ((EditText) getView().findViewById(R.id.chatInnerAddToChatInputEditText))
                    .setText("");

            Toast.makeText(getActivity(),"Successfully added user to chat", Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void leaveChat(final View theButton) {
        JSONObject messageJson = new JSONObject();

        int chatId = bundle.getInt("chatId");

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);

            messageJson.put(getString(R.string.keys_chatId), chatId);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mLeaveChatUrl, messageJson)
                .onPostExecute(this::endOfLeaveChatTask)
                .onCancelled(this::handleLeaveChatError)
                .build().execute();
    }

    private void handleLeaveChatError(final String msg) {
        Log.e("Leaving Chat ERROR!!!", msg.toString());
    }

    private void endOfLeaveChatTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
//            Intent myintent = new Intent(getActivity(), HomeActivity.class);
//            startActivity(myintent);
            getFragmentManager().popBackStack();
            getFragmentManager().popBackStackImmediate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void addStranger(final View theButton) {

        JSONObject messageJson = new JSONObject();
        String newConnection = ((EditText) getView().findViewById(R.id.chatInnerAddConectionInputEditText))
                .getText().toString();
        try {
            messageJson.put(getString(R.string.keys_json_current_username), mUsername);
            messageJson.put(getString(R.string.keys_json_connection_username), newConnection);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mAddStrangerUrl, messageJson)
                .onPostExecute(this::endOfAddStrangerTask)
                .onCancelled(this::handleAddStrangerError)
                .build().execute();
    }

    private void handleAddStrangerError(final String msg) {
        Log.e("add Stranger Connections ERROR!!!", msg.toString());
    }

    private void endOfAddStrangerTask(final String result) {
        //Log.e("test2", "gets to endofSendRequestTask");
        try {
            JSONObject res = new JSONObject(result);

            Toast.makeText(getActivity(),"Successfully sent request", Toast.LENGTH_SHORT).show();
            ((EditText) getView().findViewById(R.id.chatInnerAddConectionInputEditText))
                    .setText("");


        } catch (JSONException e) {
            //Log.e("test4", "does not get to success");
            e.printStackTrace();

        }
    }
}
