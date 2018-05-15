package group8.tcss450.uw.edu.chatclient;


import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchNewConnectionFragment extends Fragment {

    private String mUsername;
    private String mSendUrl;
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
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath(getString(R.string.ep_send_request))
                            .build();
                    //build the JSONObject
                    JSONObject msg = new JSONObject();
                    try {
                        msg.put("username", userName);
                        msg.put("connection", currentItem.username);
                        System.out.println(msg);
                    } catch (JSONException e) {
                        Log.wtf("SEND REQUEST", "Error creating JSON: " + e.getMessage());
                    }

                    new SendPostAsyncTask.Builder(uri.toString(), msg)
                            .onPostExecute(this::handleAddContact)
                            .onCancelled(this::handleErrorsInTask)
                            .build().execute();
                }

                private void handleErrorsInTask(String result) {
                    Log.e("ASYNCT_TASK_ERROR", result);
                }

                private void handleAddContact(String result) {
                    try {
                        JSONObject resultsJSON = new JSONObject(result);
                        boolean success = resultsJSON.getBoolean("success");
                        if (success) {
                            itemButton.setEnabled(false);
                            itemButton.setText("Sent");
                        }
                    } catch (JSONException e) {
                        //It appears that the web service didn’t return a JSON formatted String
                        //or it didn’t have what we expected in it.
                        Log.e("JSON_PARSE_ERROR", result
                                + System.lineSeparator()
                                + e.getMessage());
                    }
                }
            });

            return listItem;
        }
    }

    private void sendRequest(final View theButton, String newUsername) {
        Log.e("test1", "gets to sendRequest");
        JSONObject messageJson = new JSONObject();

        try {
            messageJson.put(getString(R.string.keys_json_current_username), mUsername);
            messageJson.put(getString(R.string.keys_json_connection_username), newUsername);
            //messageJson.put(getString(R.string.keys_json_connection_verification), 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendPostAsyncTask.Builder(mSendUrl, messageJson)
                .onPostExecute(this::endOfSendRequestTask)
                .onCancelled(this::handleError)
                .build().execute();
    }
    private void handleError(final String msg) {
        Log.e("new Connections ERROR!!!", msg.toString());
    }

    private void endOfSendRequestTask(final String result) {
        Log.e("test2", "gets to endofSendRequestTask");
        try {
            JSONObject res = new JSONObject(result);
            Log.e("test", "gets to try part of endofSendRequestTask");
            if(res.get(getString(R.string.keys_json_success)).toString()
                    .equals(getString(R.string.keys_json_success_value_true))) {
                ((EditText) getView().findViewById(R.id.newConnectionUsernameInputEditText))
                        .setText("");
                Log.e("test3", "gets to success, should make toast");
                Toast.makeText(getActivity(),"Connection Request Sent!",Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e("test4", "does not get to success");
            e.printStackTrace();

        }
    }

    public interface SearchContactFragmentInteractionListener {
        void onSearchAttempt(String userName, String keyword, ArrayList<SearchConnectionListItem> data, SearchConnectionAdapter adapter);
    }

}
