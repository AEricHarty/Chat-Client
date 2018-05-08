package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import group8.tcss450.uw.edu.chatclient.model.Credentials;


/**
 * A simple {@link Fragment} subclass.
 *
 *  @author Eric Harty - hartye@uw.edu
 */
public class RegisterResultFragment extends Fragment {

    private OnVerifyFragmentInteractionListener mListener;
    private View mView;
    private Button verifyButton;
    private Button resendButton;
    private EditText codeInput;
    private String userName;
    private String userEmai;

    public RegisterResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_register_result, container, false);

        verifyButton = (Button) mView.findViewById(R.id.verifyButton);
        resendButton = (Button) mView.findViewById(R.id.resendVerificationButton);
        codeInput = (EditText) mView.findViewById(R.id.verificationCodeInput);




        if(getArguments() != null){
            boolean success = getArguments().getBoolean("result");
            userName = getArguments().getString("username");
            userEmai = getArguments().getString("email");
            TextView resultMsg = mView.findViewById(R.id.resultDisplayMsg);
            TextView enterCodeMessage = (TextView) mView.findViewById(R.id.enterCodeMessage);
            if(success){
                resultMsg.setText("An email with verification code has been sent to " + userEmai + ".");
//                resultMsg.setText(getString(R.string.register_succeed_msg));
                verifyButton.setVisibility(View.VISIBLE);
                resendButton.setVisibility(View.VISIBLE);
                codeInput.setVisibility(View.VISIBLE);
                enterCodeMessage.setVisibility(View.VISIBLE);
            } else{
                resultMsg.setText(getString(R.string.register_fail_msg));
                verifyButton.setVisibility(View.INVISIBLE);
                resendButton.setVisibility(View.INVISIBLE);
                codeInput.setVisibility(View.INVISIBLE);
                enterCodeMessage.setVisibility(View.INVISIBLE);
            }
        }

        verifyButton.setOnClickListener(this::onClick);
        resendButton.setOnClickListener(this::onClickResend);

        return mView;
    }

    private void onClickResend(View view) {
        if (mListener != null) {
            mListener.onResendCode(userName, userEmai);
        }
    }

    private void onClick(View view) {
        if (mListener != null) {

            String code = codeInput.getText().toString();

            boolean good = true;

            //Client side checks here
            if(code.length() == 0 ){
                codeInput.setError("Please input your code");
                good = false;
            }else{
                //Uses regex to check for <>@<>.XXX email addresses
                Pattern pattern = Pattern.compile("[0-9][0-9][0-9][0-9]");
                Matcher mat = pattern.matcher(code);
                if(!mat.matches()){
                    codeInput.setError("Verification code has to be consisted of 4 numbers");
                    good = false;
                }
            }
            if(good){
                mListener.onVerifyAttempt(userName, userEmai, code);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof RegisterFragment.OnRegisterFragmentInteractionListener) {
            mListener = (RegisterResultFragment.OnVerifyFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerifyFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnVerifyFragmentInteractionListener {
        void onVerifyAttempt(String userName, String userEmail, String code);
        void onResendCode(String userName, String userEmail);
    }

}
