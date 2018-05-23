package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import group8.tcss450.uw.edu.chatclient.utils.ListenManager;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private String mUsername;
    private String mSendUrl;
    private String mLeaveChatUrl;
    private String mAddToChatUrl;

    private TextView mOutputTextView;
    private ListenManager mListenManager;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        setHasOptionsMenu(true);
        v.findViewById(R.id.chatSendButton).setOnClickListener(this::sendMessage);
        mOutputTextView = (TextView) v.findViewById(R.id.chatOutputTextView);

        v.findViewById(R.id.chatLeaveChatButton).setOnClickListener(this::leaveChat);
        Button home = (Button) v.findViewById(R.id.chatGoHomeButton);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("inChatFragmentOnCLick", "Go Home!");
                Intent myintent = new Intent(getActivity(), HomeActivity.class);
                startActivity(myintent);
            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        mSendUrl = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_send_message))
                .build()
                .toString();

        Uri retrieve = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_get_message))
                .appendQueryParameter("chatId", "1")
                .build();

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
        if (prefs.contains(getString(R.string.keys_prefs_time_stamp))) {
            //ignore all of the seen messages. You may want to store these messages locally
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setTimeStamp(prefs.getString(getString(R.string.keys_prefs_time_stamp),
                            "0"))
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        } else {
            //no record of a saved timestamp. must be a first time login
            mListenManager = new ListenManager.Builder(retrieve.toString(),
                    this::publishProgress)
                    .setExceptionHandler(this::handleError)
                    .setDelay(1000)
                    .build();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mListenManager.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        String latestMessage = mListenManager.stopListening();
        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);
        // Save the most recent message timestamp
        prefs.edit().putString(
                getString(R.string.keys_prefs_time_stamp),
                latestMessage)
                .apply();
    }

    private void addToChat(final View theButton) {
        JSONObject messageJson = new JSONObject();

        // String userToAdd = ((EditText) getView().findViewById(R.id.name of username input box to add))
        //                .getText().toString();

        try {
            //messageJson.put(getString(R.string.keys_json_username), userToAdd);

            messageJson.put("test", "test"); // can remove if you want, not needed

            // messageJson.put(getString(R.string.keys_json_username), mUsername);
            // need to get chat id somehow
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
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
                // ((EditText) getView().findViewById(R.id.name of username input box to add))
                // .setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void leaveChat(final View theButton) {
        JSONObject messageJson = new JSONObject();

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);

            // messageJson.put(getString(R.string.keys_json_username), mUsername);
            // need to get chat id somehow
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
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
               // ((EditText) getView().findViewById(R.id.chatInputEditText))
                       // .setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(final View theButton) {
        JSONObject messageJson = new JSONObject();
        String msg = ((EditText) getView().findViewById(R.id.chatInputEditText))
                .getText().toString();
        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), msg);
            messageJson.put(getString(R.string.keys_json_chat_id), 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendMsgTask)
                .onCancelled(this::handleError)
                .build().execute();
    }

    private void handleError(final String msg) {
        Log.e("CHAT ERROR!!!", msg.toString());
    }

    private void endOfSendMsgTask(final String result) {
        try {
            JSONObject res = new JSONObject(result);
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
                ((EditText) getView().findViewById(R.id.chatInputEditText))
                        .setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleError(final Exception e) {
        Log.e("LISTEN ERROR!!!", e.getMessage());
    }

    private void publishProgress(JSONObject messages) {
        final String[] msgs;
        if(messages.has(getString(R.string.keys_json_messages))) {
            try {
                JSONArray jMessages =
                        messages.getJSONArray(getString(R.string.keys_json_messages));
                msgs = new String[jMessages.length()];
                for (int i = 0; i < jMessages.length(); i++) {
                    JSONObject msg = jMessages.getJSONObject(i);
                    String username =
                            msg.get(getString(R.string.keys_json_username)).toString();
                    String userMessage =
                            msg.get(getString(R.string.keys_json_message)).toString();
                    msgs[i] = username + ":" + userMessage;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            getActivity().runOnUiThread(() -> {
                for (String msg : msgs) {
                    mOutputTextView.append(msg);
                    mOutputTextView.append(System.lineSeparator());
                }
            });
        }
    }
}