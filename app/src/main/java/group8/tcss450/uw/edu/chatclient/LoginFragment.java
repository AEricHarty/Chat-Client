package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import group8.tcss450.uw.edu.chatclient.model.Credentials;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnLoginFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private OnLoginFragmentInteractionListener mListener;
    private View mView;

    public LoginFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_login, container, false);

        Button b = mView.findViewById(R.id.loginButton);
        b.setOnClickListener(this);

        b = mView.findViewById(R.id.registerButton);
        b.setOnClickListener(this::onRegisterClick);

        return mView;
    }

    /**
     * Performs client side checks on login information, if they pass fires onLoginAttempt.
     *
     * @author Eric Harty - hartye@uw.edu
     */
    @Override
    public void onClick(View view) {
        if (mListener != null) {
            EditText emailText = mView.findViewById(R.id.logEmailText);
            String email = emailText.getText().toString();
            EditText passText = mView.findViewById(R.id.logPasswordText);
            Editable password = passText.getText();
            boolean good = true;

            //Client side checks here
            if(email.length() == 0 || password.length() == 0){
                emailText.setError("Both fields must be filled");
                good = false;
            }else{
                if(email.length() < 4){
                    emailText.setError("Username must be more than 3 chars in length");
                    good = false;
                }
                if(password.length() < 4){
                    passText.setError("Password must be more than 3 chars in length");
                    good = false;
                }
            }

            if(good){
                Credentials cred = new Credentials.Builder(null, password)
                        .addEmail(email)
                        .build();
                mListener.onLoginAttempt(cred);
            }
        }
    }

    public void onRegisterClick(View view) {
        if (mListener != null) {
            mListener.onRegisterClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginFragmentInteractionListener) {
            mListener = (OnLoginFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoginFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Allows an external source to set an error message on this fragment. This may
     * be needed if an Activity includes processing that could cause login to fail.
     * @param err the error message to display.
     */
    public void setError(String err) {
        //Log in unsuccessful for reason: err. Try again.
        //you may want to add error stuffs for the user here.
        ((TextView) getView().findViewById(R.id.logEmailText))
                .setError("Login Unsuccessful");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnLoginFragmentInteractionListener {
        void onLoginAttempt(Credentials cred);
        void onRegisterClicked();
    }
}