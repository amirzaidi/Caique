package v6.caique;

import android.app.NotificationManager;
import android.content.Context;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    public static HashMap<String, ChatActivity> Instances = new HashMap<>();
    public boolean Active;
    private String CurrentChat = null;
    private ChatAdapter Adapter;
    private ListView MessageWindow;

    private Query MessageData;
    private ChildEventListener Listener;
    private HashMap<String, Integer> LoadDatas = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Bundle b = getIntent().getExtras();

        if (b == null|| !b.containsKey("chat")){
            this.finish();
        }

        CurrentChat = b.getString("chat");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(CurrentChat.hashCode());

        Instances.put(CurrentChat, this);

        MessageWindow = (ListView) findViewById(R.id.ChatList);
        Adapter = new ChatAdapter(this, R.layout.chat_message, new ArrayList<HashMap<String, String>>());
        MessageWindow.setAdapter(Adapter);

        Active = true;

        MessageData = FirebaseDatabase.getInstance().getReference().child("chat").child(CurrentChat).child("message").limitToLast(50);

        MessageData.addChildEventListener(Listener = new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                HashMap<String, String> MsgData = (HashMap<String, String>) dataSnapshot.getValue();
                String SenderId = MsgData.get("sender");
                if (!LoadDatas.containsKey(SenderId))
                {
                    LoadDatas.put(SenderId, DatabaseCache.LoadUserData(SenderId, new Runnable() {
                        @Override
                        public void run() {
                            Adapter.notifyDataSetChanged();
                        }
                    }));
                }

                Adapter.add(MsgData);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Active = false;
        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.SetMusicPlaying(false);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Active = true;
        RequestPlaying();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Instances.remove(CurrentChat);
        Active = false;
        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.SetMusicPlaying(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Instances.remove(CurrentChat);
        Active = false;
        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.SetMusicPlaying(false);
        }

        MessageData.removeEventListener(Listener);
        Adapter.clear();

        for (Map.Entry<String, Integer> x : LoadDatas.entrySet())
        {
            DatabaseCache.Cancel(x.getValue());
        }
    }

    public void SendMusic(View view) {
        String Date = String.valueOf(System.currentTimeMillis() / 1000);

        EditText Input = (EditText) findViewById(R.id.editText2);
        String Text = Input.getText().toString().trim();

        if(Text.length() > 1024){
            Text =  Text.substring(0, 1021) + "...";
        }
        else if (Text.length() == 0)
        {
            return;
        }

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", CurrentChat)
                .addData("type", "madd")
                .addData("date", Date)
                .addData("text", Text)
                .build());

        Log.d("SendMessageToServer", "Music message sent " + Text);
        Input.setText("");
    }

    public void SendMessage(View view) {
        String Date = String.valueOf(System.currentTimeMillis() / 1000);

        EditText Input = (EditText) findViewById(R.id.editText2);
        String Text = Input.getText().toString().trim();

        if(Text.length() > 1024){
            Text =  Text.substring(0, 1021) + "...";
        }
        else if (Text.length() == 0)
        {
            return;
        }

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", CurrentChat)
                .addData("type", "text")
                .addData("date", Date)
                .addData("text", Text)
                .build());

        Log.d("SendMessageToServer", "Message sent " + Text);
        Input.setText("");

    }

    public void RequestPlaying() {
        String Date = String.valueOf(System.currentTimeMillis() / 1000);

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", CurrentChat)
                .addData("type", "mplaying")
                .addData("date", Date)
                .addData("text", "")
                .build());

    }
}
