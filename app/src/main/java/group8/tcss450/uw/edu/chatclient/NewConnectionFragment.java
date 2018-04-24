package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * make a new connection from the home screen navigation drawer
 *
 * @author Jin Byoun - jinito@uw.edu
 */
public class NewConnectionFragment extends Fragment {


    public NewConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_connection, container, false);
    }

}
