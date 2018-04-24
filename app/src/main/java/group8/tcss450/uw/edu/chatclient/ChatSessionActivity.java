package group8.tcss450.uw.edu.chatclient;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Activity for a single chat session.
 *
 * @author Phu Lam Pham
 */
public class ChatSessionActivity extends AppCompatActivity {

    /* Adapter for populating the chat messages. */
    private ChatArrayAdapter chatArrayAdapter;
    /* The list of chat messages. */
    private ListView listView;
    /* User's input text message */
    private EditText chatText;
    /* The send message button */
    private Button buttonSend;
    /* MIGHT CHANGE LATER
       For now use to find left or right display messages.
     */
    private boolean side = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat_session);

        buttonSend = (Button) findViewById(R.id.send);

        listView = (ListView) findViewById(R.id.msgview);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_right);
        listView.setAdapter(chatArrayAdapter);

        chatText = (EditText) findViewById(R.id.msg);
        chatText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                sendChatMessage();
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    // Right now is alternating between left and right Later on based on who is sending, display accordingly
    private boolean sendChatMessage() {
        chatArrayAdapter.add(new ChatMessage(side, chatText.getText().toString()));
        chatText.setText("");
        side = !side;
        return true;
    }

    public class ChatMessage {
        public boolean left;
        public String message;

        public ChatMessage(boolean left, String message) {
            super();
            this.left = left;
            this.message = message;
        }
    }

    class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

        private TextView chatText;
        private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
        private Context context;

        @Override
        public void add(ChatMessage object) {
            chatMessageList.add(object);
            super.add(object);
        }

        public ChatArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.context = context;
        }

        public int getCount() {
            return this.chatMessageList.size();
        }

        public ChatMessage getItem(int index) {
            return this.chatMessageList.get(index);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ChatMessage chatMessageObj = getItem(position);
            View row = convertView;
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (chatMessageObj.left) {
                row = inflater.inflate(R.layout.chat_right, parent, false);
            }else{
                row = inflater.inflate(R.layout.chat_left, parent, false);
            }

            // For now display time, use it later to display user name and time
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = new Date();

            chatText = (TextView) row.findViewById(R.id.senderInfo);
            chatText.setText(formatter.format(date));
            chatText = (TextView) row.findViewById(R.id.message);
            chatText.setText(chatMessageObj.message);
            return row;
        }
    }
}
