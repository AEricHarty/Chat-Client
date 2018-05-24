package group8.tcss450.uw.edu.chatclient.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import group8.tcss450.uw.edu.chatclient.R;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class InviteFragment extends Fragment {
    private String mUserName;
    private EditText mFriendName;
    private EditText mFriendEmail;
    private Button sendButton;
    private InviteFragmentInteractionListener mListener;
    private static final String TAG = "InviteFragment";

    public InviteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_connections, container, false);
        SharedPreferences prefs = getActivity().getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);
        mUserName = prefs.getString(getString(R.string.keys_prefs_username), "Problem! No Username in " + TAG + "!");

        mFriendName = (EditText) v.findViewById(R.id.friendName);
        mFriendEmail = (EditText) v.findViewById(R.id.friendEmail);
        sendButton = (Button) v.findViewById(R.id.inviteSendButton);
        sendButton.setOnClickListener(this::onClick);

        return v;
    }

    private void onClick(View view) {
        String friendName = mFriendName.getText().toString();
        String friendEmail = mFriendEmail.getText().toString();
        boolean good = true;
        if (friendName.length() == 0) {
            mFriendName.setText("Name cannot be empty.");
            good = false;
        }

        if (friendEmail.length() == 0) {
            mFriendEmail.setText("Email cannot be empty.");
            good = false;
        }

        if(good && mListener != null) {
            mListener.onGetContactsAttempt(mUserName, friendName, friendEmail);
        }
    }

    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof InviteFragmentInteractionListener) {
            mListener = (InviteFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement InviteFragmentInteractionListener");
        }
    }

    public interface InviteFragmentInteractionListener {
        void onGetContactsAttempt(String userName, String friendName, String friendEmail);
    }

}
