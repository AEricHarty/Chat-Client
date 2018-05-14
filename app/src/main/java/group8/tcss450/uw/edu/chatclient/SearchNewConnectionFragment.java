package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchNewConnectionFragment extends Fragment {

    public ArrayList<SearchConnectionListItem> data = new ArrayList<SearchConnectionListItem>();
    private EditText searchContactTextView;
    private Button searchContactButton;
    private ListView searchContactList;
    private SearchContactFragmentInteractionListener mListener;
    private String userName;
    protected SearchConnectionAdapter adapter;

    public SearchNewConnectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle b = this.getActivity().getIntent().getExtras();
        if(b != null) {
            userName = b.getString("username");
        }
        System.out.println("Username at onCreateView: " + userName);
        View v = inflater.inflate(R.layout.fragment_search_new_connection, container, false);
        searchContactTextView = (EditText) v.findViewById(R.id.searchContactTextView);
        searchContactButton = (Button) v.findViewById(R.id.searchContactButton);
        searchContactButton.setOnClickListener(this::onClick);
        searchContactList = (ListView) v.findViewById(R.id.searchContactList);

        adapter= new SearchConnectionAdapter(v.getContext(), data);


        searchContactList.setAdapter(adapter);
        searchContactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });
        SharedPreferences prefs =
                this.getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SearchNewConnectionFragment.SearchContactFragmentInteractionListener) {
            mListener = (SearchNewConnectionFragment.SearchContactFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SearchContactFragmentInteractionListener");
        }
    }

    private void onClick(View view) {
        if (mListener != null) {
            String keyword = searchContactTextView.getText().toString();
            if (keyword == "") {
                searchContactTextView.setError("Keyword must not be empty");
            } else {
                mListener.onSearchAttempt(userName, keyword, data, adapter);
            }
        }
    }

    public static class SearchConnectionListItem{
        private String name;
        private String email;
        private String username;

        public SearchConnectionListItem(String first, String last, String username, String email) {
            this.name = first + " " + last;
            this.email = email;
            this.username = username;
        }
    }

    public class SearchConnectionAdapter extends ArrayAdapter<SearchConnectionListItem> {
        private Context mContext;
        private List<SearchConnectionListItem> mList = new ArrayList<>();

        public SearchConnectionAdapter(Context context, ArrayList<SearchConnectionListItem> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.contact_list_item, parent, false);
            }

            SearchConnectionListItem currentItem = mList.get(position);

            TextView itemName = (TextView) listItem.findViewById(R.id.contactListItemName);
            itemName.setText(currentItem.name);

            TextView itemUsername = (TextView) listItem.findViewById(R.id.contactListItemUsername);
            itemUsername.setText(currentItem.username);

            TextView itemEmail = (TextView) listItem.findViewById(R.id.contactListItemEmail);
            itemEmail.setText(currentItem.email);

            Button itemButton = (Button) listItem.findViewById(R.id.contactListItemAddButton);
            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            return listItem;
        }
    }

    public interface SearchContactFragmentInteractionListener {
        void onSearchAttempt(String userName, String keyword, ArrayList<SearchConnectionListItem> data, SearchConnectionAdapter adapter);
    }


}
