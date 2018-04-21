package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterResultFragment extends Fragment {

    public RegisterResultFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, container, false);

        TextView resultMsg = v.findViewById(R.id.resultDisplayMsg);
        resultMsg.setText(getArguments().getString("result"));

        return v;
    }

    public void updateContent(boolean success) {
        TextView resultMsg = getView().findViewById(R.id.resultDisplayMsg);
        if(success){
            resultMsg.setText(getString(R.string.register_succeed_msg));
        } else{
            resultMsg.setText(getString(R.string.register_fail_msg));
        }
    }
}
