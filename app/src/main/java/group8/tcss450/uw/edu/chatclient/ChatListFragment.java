package group8.tcss450.uw.edu.chatclient;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private ListView chatList;
    private ArrayList chatArrayList;
    private ArrayAdapter<String> chatListAdapter;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);
        chatList = (ListView) v.findViewById(R.id.chatList);

        chatArrayList = new ArrayList<String>();

        // Adapter: You need three parameters 'the context, id of the layout (it will be where the data is shown),
        // and the array that contains the data
        chatListAdapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_spinner_dropdown_item, chatArrayList);

        // Here, you set the data in your ListView
        chatList.setAdapter(chatListAdapter);


        // Delete later as this is just example
        chatArrayList.add("Franklin Benjamin");
        chatArrayList.add("George Washington");
        chatArrayList.add("Group Chat");
        chatListAdapter.notifyDataSetChanged();

        return v;
    }

}
