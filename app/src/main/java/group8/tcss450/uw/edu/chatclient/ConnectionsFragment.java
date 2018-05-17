package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */

public class ConnectionsFragment extends Fragment {

    private ListView connectionList;
    private ConnectionsAdapter connectionAdapter;
    private ArrayList<Connection> connectionListData = new ArrayList<>();
    private SearchView searchView;
    private String userName;
    private ConnectionsFragmentInteractionListener mListener;
    private HashSet<Connection> currentSelectedConnections = new HashSet<>();

    public ConnectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_connections, container, false);
        Bundle b = this.getActivity().getIntent().getExtras();
        if(b != null) {
            userName = b.getString("username");
        }


        connectionList = (ListView) v.findViewById(R.id.connectionList);

        connectionAdapter = new ConnectionsAdapter(v.getContext(), connectionListData);

        // Here, you set the data in your ListView
        connectionList.setAdapter(connectionAdapter);

//        connectionListData.add(new Connection("1", "lam", "pham", "ppham95@uw.edu"));
//        connectionListData.add(new Connection("1", "ding", "dong", "dingidongo9@gmail.com"));



        searchView=(SearchView) v.findViewById(R.id.searchBox);
        searchView.setIconified(false);
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                connectionAdapter.getFilter().filter(text);
                return false;
            }
        });
        if (mListener != null) {
            mListener.onGetContactsAttempt(userName, connectionListData, connectionAdapter);
        }
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
        if (context instanceof ConnectionsFragmentInteractionListener) {
            mListener = (ConnectionsFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ConnectionsFragmentInteractionListener");
        }
    }

    public static class Connection {
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private boolean selected;

        public Connection(String userId, String firstName, String lastName, String email) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.selected = false;
        }
    }

    public class ConnectionsAdapter extends ArrayAdapter<Connection> {
        private Context mContext;
        private List<Connection> mList;
        ArrayList<Connection> mStringFilterList;

        public ConnectionsAdapter(Context context, ArrayList<Connection> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
            mStringFilterList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.connection_list_item, parent, false);
            }

            Connection currentItem = mList.get(position);

            CheckedTextView itemName = (CheckedTextView) listItem.findViewById(R.id.connectionListItemName);
            itemName.setText(currentItem.firstName + " " + currentItem.lastName);
            CheckBox itemCheckBox = (CheckBox) listItem.findViewById(R.id.connectionListItemCheckBox);
            itemCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        currentSelectedConnections.add(currentItem);
                    } else {
                        if(currentSelectedConnections.contains(currentItem)) {
                            currentSelectedConnections.remove(currentItem);
                        }
                    }
                    for (Connection s : currentSelectedConnections) {
                        System.out.println(s.email);
                    }
                }
            });

            return listItem;
        }


//        @NonNull
//        @Override
//        public Filter getFilter() {
//            Filter filter = new Filter() {
//                protected FilterResults performFiltering(CharSequence constraint) {
//                    FilterResults results = new FilterResults();
//                    if (constraint != null && constraint.length() > 0) {
//                        ArrayList<Connection> filterList = new ArrayList<>();
//                        System.out.println(mStringFilterList);
//                        for (int i = 0; i < mStringFilterList.size(); i++) {
//                            if ((mStringFilterList.get(i).firstName + " " + mStringFilterList.get(i).lastName.toUpperCase())
//                                    .contains(constraint.toString().toUpperCase())) {
//
//                                Connection conn = new Connection(mStringFilterList.get(i).userId,
//                                        mStringFilterList.get(i).firstName,
//                                        mStringFilterList.get(i).lastName,
//                                        mStringFilterList.get(i).email);
//                                filterList.add(conn);
//                            }
//                        }
//                        results.count = filterList.size();
//                        results.values = filterList;
//                    } else {
//                        results.count = mStringFilterList.size();
//                        results.values = mStringFilterList;
//                    }
//                    return results;
//                }
//
//                @Override
//                protected void publishResults(CharSequence constraint, FilterResults results) {
//                    mList = (ArrayList<Connection>) results.values;
//                    notifyDataSetChanged();
//                }
//            };
//            return filter;
//        }
    }

    public interface ConnectionsFragmentInteractionListener {
        void onGetContactsAttempt(String userName, ArrayList<Connection> data, ConnectionsAdapter adapter);
    }
}
