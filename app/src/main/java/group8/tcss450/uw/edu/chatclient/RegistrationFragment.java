package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RegistrationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class RegistrationFragment extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;
    private View mView;

    public RegistrationFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_registration, container, false);

        Button b = mView.findViewById(R.id.registerButton2);
        b.setOnClickListener(this);

        return mView;
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            EditText userText = mView.findViewById(R.id.regUsernameText);
            String name = userText.getText().toString();
            EditText passText = mView.findViewById(R.id.regPassText);
            char[] pass = passText.getText().toString().toCharArray();
            EditText copyText = mView.findViewById(R.id.regPassText2);
            char[] copy = copyText.getText().toString().toCharArray();

            mListener.onFragmentInteraction(name, pass, copy);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(String name, char[] password, char[] passcopy);
    }
}
