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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import group8.tcss450.uw.edu.chatclient.utils.ChatListenManager;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatListFragment extends Fragment {

    private static final String TAG = "ChatListFragment";

    private ArrayList<ChatListItem> mData = new ArrayList<ChatListItem>();
    private ListView mChatList;
    private String mUserName;
    private ChatSessionAdapter mAdapter;
    private ChatListenManager mListenManager;

    private ChatListFragmentInteractionListener mListener;

    public ChatListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        SharedPreferences prefs = getActivity().getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        mUserName = prefs.getString(getString(R.string.keys_prefs_username), "Problem! No Username in " + TAG + "!");

        View v = inflater.inflate(R.layout.fragment_chat_list, container, false);

        mChatList = (ListView) v.findViewById(R.id.chatList);
        mAdapter = new ChatSessionAdapter(v.getContext(), mData);
        mChatList.setAdapter(mAdapter);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof ChatListFragmentInteractionListener) {
            mListener = (ChatListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + "must implement ChatListFragmentInteractionListener");
        }
    }


    @Override
    public void onResume(){
        super.onResume();
        findChatSessions();
    }

    @Override
    public void onStop(){
        super.onStop();

    }

    public void findChatSessions(){
        //build the web service URL
        Uri uri = new Uri.Builder()
                .scheme("https")
                .appendPath(getString(R.string.ep_base_url))
                .appendPath(getString(R.string.ep_my_chats)) //TODO add actual endpoint for getMyChats
                .appendQueryParameter("username", mUserName) //TODO verify this is the correct key for username
                .build();

        //open shared preferences
        SharedPreferences prefs = getActivity().getSharedPreferences(
                getString(R.string.keys_shared_prefs),
                Context.MODE_PRIVATE);

        //check shared preferences for chatListTimestamp
        if(!prefs.contains(getString(R.string.keys_prefs_chat_time_stamp))) {
            //create listen manager to ignore seen messages.
            mListenManager = new ChatListenManager.Builder(uri.toString(),
                    this::populateChatList)
                    .setExceptionHandler(this::handleExceptionsInListener)
                    .setTimeStamp(prefs.getString(getString(R.string.keys_prefs_chat_time_stamp), "0"))
                    .setDelay(1000)
                    .build();

        } else {
            //No time stamp in setting. Must be a first time login
            //The ChatListenManager will assign itself the default timestamp 1970 to get all results.
            mListenManager = new ChatListenManager.Builder(uri.toString(),
                    this::populateChatList)
                    .setExceptionHandler(this::handleExceptionsInListener)
                    .setDelay(1000)
                    .build();
        }

        //TODO remove the rest of this method. It is here to add a hardcoded item to list.
        populateChatList(new JSONObject());
    }

    public void handleExceptionsInListener(Exception e) {
        Log.e(TAG + "LISTEN ERROR!!", e.getMessage());
    }

    private void populateChatList(JSONObject resultsJSON) {
        getActivity().runOnUiThread(() -> {

            try {
                JSONArray array = resultsJSON.getJSONArray("pending"); //TODO replace with actual key

                if (getActivity().findViewById(R.id.loadChatListProgressBar) != null) {
                    ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.loadChatListProgressBar);
                    progressBar.setVisibility(View.GONE);
                }

                for (int i =0; i < array.length(); i++) {
                    JSONObject aChatSession = array.getJSONObject(i);
                    // PARSE JSON RESULTS HERE
                    String chatName = aChatSession.getString("chatName"); //TODO replace with actual key
                    int chatId = aChatSession.getInt("chatId"); //TODO replace with actual key

                    mData.add(new ChatListItem(chatName, chatId));
                    mAdapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {

                Log.e("JSON_PARSE_ERROR", "Error when populating Chat List in " + TAG);
            }

            //TODO remove the hard coded chat session created in rest of this method.
            if (getActivity().findViewById(R.id.loadChatListProgressBar) != null) {
                ProgressBar progressBar = (ProgressBar) getActivity().findViewById(R.id.loadChatListProgressBar);
                progressBar.setVisibility(View.GONE);
            }

            String chatName = "Test Chat Session";
            int chatId = 1;

            mData.add(new ChatListItem(chatName, chatId));
            mAdapter.notifyDataSetChanged();

            chatName = "Second Test Chat Session";
            chatId = 2;

            mData.add(new ChatListItem(chatName, chatId));
            mAdapter.notifyDataSetChanged();

        });
    }

    //*******************************************************Inner Classes *************************************

    public class ChatListItem {
        private String mName;
        private int mChatId;

        public ChatListItem(String name, int chatId) {
            this.mName = name;
            this.mChatId = chatId;
        }

        public String getName() {
            return mName;
        }

        public int getId() {
            return mChatId;
        }

    }

    public class ChatSessionAdapter extends ArrayAdapter<ChatListItem> {
        private Context mContext;
        private List<ChatListItem> mList;
        private ArrayList<ChatListItem> mStringFilterList;

        public ChatSessionAdapter(Context context, ArrayList<ChatListItem> list) {
            super(context, 0, list);
            mContext = context;
            mList = list;
            mStringFilterList = list;
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View listItem = convertView;
            if(listItem == null) {
                listItem = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item, parent, false);
            }

            ChatListItem currentItem = mList.get(position);

            Button button = (Button) listItem.findViewById(R.id.chatSessionButton);
            button.setText(currentItem.getName());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onChatSelected(currentItem.getName(),
                            currentItem.getId());
                }
            });

            return listItem;
        }


    }

    public interface ChatListFragmentInteractionListener {
        /**
         * Used to handle a chat being selected from the list
         * Should create an Intent to start the ChatSessionActivity for the specified chat session.
         *
         * @param chatName the name of the chosen chat session.
         * @param chatId the chatId of the chosen chat session.
         */
        void onChatSelected(String chatName, int chatId);
    }
}
