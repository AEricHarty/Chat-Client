package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * information fragment from the nagivation drawer
 *
 * @author Eric Harty - hartye@uw.edu
 */
public class HomeInformationFragment extends Fragment {

    private Button mWeatherButton;
    private TextView mWeatherView;

    public HomeInformationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_home_information, container, false);
        mWeatherButton = (Button) v.findViewById(R.id.moreWeatherButton);
        mWeatherButton.setEnabled(false);
        mWeatherView = (TextView) v.findViewById(R.id.homeWeatherView);

        return v;
    }

    public void setWeather(String weather) {
        mWeatherView.setText(weather);
    }

}
