package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * information fragment from the nagivation drawer
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class HomeInformationFragment extends Fragment {


    public HomeInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_information, container, false);
    }

}
