package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import group8.tcss450.uw.edu.chatclient.model.Credentials;


/**
 * information fragment from the nagivation drawer
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class HomeInformationFragment extends Fragment implements View.OnClickListener{

    private Button mWeatherButton;
    private TextView mWeatherView;
    private HomeInformationFragment.OnHomeFragmentInteractionListener mListener;

    public HomeInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home_information, container, false);
        mWeatherButton = (Button) v.findViewById(R.id.moreWeatherButton);
        //mWeatherButton.setEnabled(false);
        mWeatherView = (TextView) v.findViewById(R.id.homeWeatherView);

        return v;
    }

    public void setWeather(String weather) {
        mWeatherView.setText(weather);
    }

    /**@author Eric Harty - hartye@uw.edu*/
    @Override
    public void onClick(View view) {

        mListener.onMoreWeatherClicked();
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
    public interface OnHomeFragmentInteractionListener {
        void onMoreWeatherClicked();
    }

}
