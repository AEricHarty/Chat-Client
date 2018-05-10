package group8.tcss450.uw.edu.chatclient;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    private String mUsername;
    private String mSendUrl;
    private android.widget.TextView mOutputTextView;
    private group8.tcss450.uw.edu.chatclient.utils.ListenManager mListenManager;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        v.findViewById(R.id.chatSendButton).setOnClickListener(this::sendMessage);
        mOutputTextView = v.findViewById(R.id.chatOutputTextView);
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        android.content.SharedPreferences prefs =
            getActivity().getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                android.content.Context.MODE_PRIVATE);

        //android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        if (!prefs.contains(getString(R.string.keys_prefs_username))) {
            throw new IllegalStateException("No username in prefs!");
        }
        mUsername = prefs.getString(getString(R.string.keys_prefs_username), "");
        mSendUrl = new android.net.Uri.Builder()
            .scheme("https")
            .appendPath(getString(R.string.ep_lab_url))
            .appendPath(getString(R.string.ep_send_message))
            .build()
            .toString();

    }

    private void sendMessage(final View theButton) {
        org.json.JSONObject messageJson = new org.json.JSONObject();
        String msg = ((android.widget.EditText) getView().findViewById(R.id.chatInputEditText))
            .getText().toString();

        try {
            messageJson.put(getString(R.string.keys_json_username), mUsername);
            messageJson.put(getString(R.string.keys_json_message), msg);
            messageJson.put(getString(R.string.keys_json_chat_id), 1);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
            .onPostExecute(this::endOfSendMsgTask)
            .onCancelled(this::handleError)
            .build().execute();
    }

    private void handleError(final String msg) {
        android.util.Log.e("CHAT ERROR!!!", msg.toString());
    }

    private void endOfSendMsgTask(final String result) {
        try {
            org.json.JSONObject res = new org.json.JSONObject(result);

            if(res.get(getString(R.string.keys_json_success)).toString()
                .equals(getString(R.string.keys_json_success_value_true))) {
                ((android.widget.EditText) getView().findViewById(R.id.chatInputEditText))
                .setText("");
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }
}
