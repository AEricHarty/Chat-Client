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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.RequestsListenManager;
import group8.tcss450.uw.edu.chatclient.utils.SendPostAsyncTask;

public class PendingConnectionsFragment extends Fragment {
    public ArrayList<IncomingRequestListItem> incomingData = new ArrayList<IncomingRequestListItem>();
    public ArrayList<OutgoingRequestListItem> outgoingData = new ArrayList<OutgoingRequestListItem>();

    private ListView incomingRequestsList;
    private ListView outgoingRequestsList;
    private String userName;

    private RequestsListenManager mIncomingListenManager;
    private RequestsListenManager mOutgoingListenManager;

    protected IncomingRequestAdapter incomingAdapter;
    protected OutgoingRequestAdapter outgoingAdapter;

    public PendingConnectionsFragment() {
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
        View v = inflater.inflate(R.layout.fragment_pending_connections, container, false);



        incomingRequestsList = (ListView) v.findViewById(R.id.incomingRequestList);
        incomingAdapter= new IncomingRequestAdapter(v.getContext(), incomingData);

        incomingRequestsList.setAdapter(incomingAdapter);
        incomingRequestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });

        outgoingRequestsList = (ListView) v.findViewById(R.id.outgoingRequestList);
        outgoingAdapter = new OutgoingRequestAdapter(v.getContext(), outgoingData);

        outgoingRequestsList.setAdapter(outgoingAdapter);
        outgoingRequestsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });

        findIncomingRequests();
        findOutgoingRequests();
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIncomingListenManager.startListening();
        mOutgoingListenManager.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mIncomingListenManager.stopListening();
        mOutgoingListenManager.stopListening();
    }

    //Adapter and item for incoming requests:
    public static class IncomingRequestListItem{
        private String name;
        private String email;
        private String username;

        public IncomingRequestListItem(String first, String last, String username, String email) {
            this.name = first + " " + last;
            this.email = email;
            this.username = username;
        }
    }

    public class IncomingRequestAdapter extends ArrayAdapter<IncomingRequestListItem> {
        private Context mContext;
        private List<IncomingRequestListItem> mList = new ArrayList<>();

        public IncomingRequestAdapter(Context context, ArrayList<IncomingRequestListItem> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.incoming_request_list_item, parent, false);
            }

            Button itemAcceptButton = (Button) listItem.findViewById(R.id.incomingRequestItemAcceptButton);

            IncomingRequestListItem currentItem = mList.get(position);

            TextView itemName = (TextView) listItem.findViewById(R.id.incomingRequestItemName);
            itemName.setText(currentItem.name);

            TextView itemUsername = (TextView) listItem.findViewById(R.id.incomingRequestItemUsername);
            itemUsername.setText(currentItem.username);

            TextView itemEmail = (TextView) listItem.findViewById(R.id.incomingRequestItemEmail);
            itemEmail.setText(currentItem.email);

            Button itemDenyButton = (Button) listItem.findViewById(R.id.incomingRequestItemDenyButton);
            itemDenyButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath(getString(R.string.ep_pending))
                            .appendPath(getString(R.string.ep_pending_incoming))
                            .build();
                    //build the JSONObject
                    JSONObject msg = new JSONObject();
                    try {
                        msg.put(getString(R.string.keys_json_username), userName);
                        msg.put(getString(R.string.keys_json_otherUsername), currentItem.username);
                        msg.put(getString(R.string.keys_json_answer), 0);
                        System.out.println(msg);
                    } catch (JSONException e) {
                        Log.wtf("DENY INCOMING REQUEST ERROR", "Error creating JSON: " + e.getMessage());
                    }

                    new SendPostAsyncTask.Builder(uri.toString(), msg)
                            .onPostExecute(this::handleDenyIncoming)
                            .onCancelled(this::handleErrorsInTask)
                            .build().execute();
                }

                private void handleErrorsInTask(String result) {
                    Log.e("ASYNC_TASK_ERROR", result);
                }

                private void handleDenyIncoming(String result) {
                    try {
                        JSONObject resultsJSON = new JSONObject(result);
                        boolean success = resultsJSON.getBoolean("success");
                        if (success) {
                            itemDenyButton.setEnabled(false);
                            itemAcceptButton.setEnabled(false);
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

            itemAcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath(getString(R.string.ep_pending))
                            .appendPath(getString(R.string.ep_pending_incoming))
                            .build();
                    //build the JSONObject
                    JSONObject msg = new JSONObject();
                    try {
                        msg.put(getString(R.string.keys_json_username), userName);
                        msg.put(getString(R.string.keys_json_otherUsername), currentItem.username);
                        msg.put(getString(R.string.keys_json_answer), 1);
                        System.out.println(msg);
                    } catch (JSONException e) {
                        Log.wtf("ACCEPT INCOMING REQUEST ERROR", "Error creating JSON: " + e.getMessage());
                    }

                    new SendPostAsyncTask.Builder(uri.toString(), msg)
                            .onPostExecute(this::handleAcceptIncoming)
                            .onCancelled(this::handleErrorsInTask)
                            .build().execute();
                }

                private void handleErrorsInTask(String result) {
                    Log.e("ASYNC_TASK_ERROR", result);
                }

                private void handleAcceptIncoming(String result) {
                    try {
                        JSONObject resultsJSON = new JSONObject(result);
                        boolean success = resultsJSON.getBoolean("success");
                        if (success) {
                            itemDenyButton.setEnabled(false);
                            itemAcceptButton.setEnabled(false);
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

    //Adapter and item for outgoing requests:
    public static class OutgoingRequestListItem{
        private String name;
        private String email;
        private String username;

        public OutgoingRequestListItem(String first, String last, String username, String email) {
            this.name = first + " " + last;
            this.email = email;
            this.username = username;
        }
    }

    public class OutgoingRequestAdapter extends ArrayAdapter<OutgoingRequestListItem> {
        private Context mContext;
        private List<OutgoingRequestListItem> mList = new ArrayList<>();

        public OutgoingRequestAdapter(Context context, ArrayList<OutgoingRequestListItem> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.outgoing_request_list_item, parent, false);
            }

            OutgoingRequestListItem currentItem = mList.get(position);

            TextView itemName = (TextView) listItem.findViewById(R.id.outgoingRequestItemName);
            itemName.setText(currentItem.name);

            TextView itemUsername = (TextView) listItem.findViewById(R.id.outgoingRequestItemUsername);
            itemUsername.setText(currentItem.username);

            TextView itemEmail = (TextView) listItem.findViewById(R.id.outgoingRequestItemEmail);
            itemEmail.setText(currentItem.email);

            Button itemButton = (Button) listItem.findViewById(R.id.outgoingRequestItemCancelButton);
            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri uri = new Uri.Builder()
                            .scheme("https")
                            .appendPath(getString(R.string.ep_base_url))
                            .appendPath(getString(R.string.ep_pending))
                            .appendPath(getString(R.string.ep_pending_outgoing))
                            .build();
                    //build the JSONObject
                    JSONObject msg = new JSONObject();
                    try {
                        msg.put(getString(R.string.keys_json_username), userName);
                        msg.put(getString(R.string.keys_json_otherUsername), currentItem.username);
                        System.out.println(msg);
                    } catch (JSONException e) {
                        Log.wtf("CANCEL OUTGOING REQUEST ERROR", "Error creating JSON: " + e.getMessage());
                    }

                    new SendPostAsyncTask.Builder(uri.toString(), msg)
                            .onPostExecute(this::handleCancelOutgoing)
                            .onCancelled(this::handleErrorsInTask)
                            .build().execute();
                }

                private void handleErrorsInTask(String result) {
                    Log.e("ASYNC_TASK_ERROR", result);
                }

                private void handleCancelOutgoing(String result) {
                    try {
                        JSONObject resultsJSON = new JSONObject(result);
                        boolean success = resultsJSON.getBoolean("success");
                        if (success) {
                            itemButton.setEnabled(false);
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

    public void findIncomingRequests() {

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_pending))
                .appendPath(getString(R.string.ep_pending_incoming))
                .appendQueryParameter("username", userName)
                .build();

        mIncomingListenManager = new RequestsListenManager.Builder(uri.toString(),
                this::populateIncomingRequestsResult)
                .setExceptionHandler(this::handleExceptionsInListener)
                .setDelay(1000)
                .build();

    }

    public void handleExceptionsInListener(Exception e) {
        Log.e("LISTEN ERROR!!", e.getMessage());
    }

    private void populateIncomingRequestsResult(JSONObject resultsJSON) {
        getActivity().runOnUiThread(() -> {

            try {
                JSONArray array = resultsJSON.getJSONArray("incoming");

                incomingData.clear();
                ProgressBar incomingProgressBar = getActivity().findViewById(R.id.incomingProgressBar);
                ProgressBar outgoingProgressBar = getActivity().findViewById(R.id.outgoingProgressBar);
                incomingProgressBar.setVisibility(View.GONE);
                outgoingProgressBar.setVisibility(View.GONE);
                for (int i =0; i < array.length(); i++) {
                    JSONObject aContact = array.getJSONObject(i);
                    // PARSE JSON RESULTS HERE
                    String first = aContact.getString("firstname");
                    String last = aContact.getString("lastname");
                    String username = aContact.getString("username");
                    String email = aContact.getString("email");
                    incomingData.add(new PendingConnectionsFragment.IncomingRequestListItem(first, last, username, email));
                    incomingAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {

                Log.e("JSON_PARSE_ERROR", "Error when populating incoming requests.");
            }
        });
    }

    public void findOutgoingRequests() {

        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_pending))
                .appendPath(getString(R.string.ep_pending_outgoing))
                .appendQueryParameter("username", userName)
                .build();

        mOutgoingListenManager = new RequestsListenManager.Builder(uri.toString(),
                this::populateOutgoingRequestsResult)
                .setExceptionHandler(this::handleExceptionsInListener)
                .setDelay(1000)
                .build();

    }

    private void populateOutgoingRequestsResult(JSONObject resultsJSON) {
        getActivity().runOnUiThread(() -> {


            try {
                JSONArray array = resultsJSON.getJSONArray("outgoing");

                outgoingData.clear();
                ProgressBar incomingProgressBar = getActivity().findViewById(R.id.incomingProgressBar);
                ProgressBar outgoingProgressBar = getActivity().findViewById(R.id.outgoingProgressBar);
                incomingProgressBar.setVisibility(View.GONE);
                outgoingProgressBar.setVisibility(View.GONE);
                for (int i =0; i < array.length(); i++) {
                    JSONObject aContact = array.getJSONObject(i);
                    // PARSE JSON RESULTS HERE
                    String first = aContact.getString("firstname");
                    String last = aContact.getString("lastname");
                    String username = aContact.getString("username");
                    String email = aContact.getString("email");
                    outgoingData.add(new PendingConnectionsFragment.OutgoingRequestListItem(first, last, username, email));
                    outgoingAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                Log.e("JSON_PARSE_ERROR", "Error when populating outgoing requests.");
            }
        });
    }
}
