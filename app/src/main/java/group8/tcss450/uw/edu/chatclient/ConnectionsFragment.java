package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */

public class ConnectionsFragment extends Fragment {

    private EditText editTxt;
    private ImageButton btn;
    private ListView list;
    private ArrayList arrayList;
    private ArrayAdapter<String> adapter;
    private SearchView sv;

    public ConnectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_connections, container, false);


        // Code populate connections list using ArrayAdapter.
        // Inflate the layout for this fragment
        editTxt = (EditText) v.findViewById(R.id.addConnectionTextBox);
        btn = (ImageButton) v.findViewById(R.id.addConnectionButton);
        list = (ListView) v.findViewById(R.id.connectionsList);
        arrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        adapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_dropdown_item, arrayList);

        // Here, you set the data in your ListView
        list.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            // For now add new item to ListView. Later on, use this to send a friend request
            public void onClick(View view) {

                // this line adds the data of your EditText and puts in your array
                arrayList.add(editTxt.getText().toString());
                editTxt.setText("");
                // next thing you have to do is check if your adapter has changed
                adapter.notifyDataSetChanged();
            }
        });



        sv=(SearchView) v.findViewById(R.id.searchBox);
        sv.setIconified(false);
        sv.setIconifiedByDefault(false);
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                adapter.getFilter().filter(text);
                return false;
            }
        });




        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
}
