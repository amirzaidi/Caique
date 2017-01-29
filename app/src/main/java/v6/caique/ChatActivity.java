package v6.caique;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ChatActivity extends AppCompatActivity {

    public static HashMap<String, ChatActivity> Instances = new HashMap<>();
    public static boolean Active;
    private String CurrentChat = null;
    private ArrayAdapter<String> Adapter;
    private ListView MessageWindow;

    private Semaphore Conc = new Semaphore(1, false);
    private int Unresolved = 0;
    private LinkedList<HashMap<String, String>> ToAdd = new LinkedList<>();
    private HashMap<String, HashMap<String, String>> SenderDatas = new HashMap<>();
    private String PreviousID = new String();

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
        Adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        MessageWindow.setAdapter(Adapter);

        Active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Active = false;
        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.MusicHandler(false);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        Active = true;
        RequestPlaying();
        RequestMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Instances.remove(CurrentChat);
        Active = false;
        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.MusicHandler(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Instances.remove(CurrentChat);
        Active = false;
        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.MusicHandler(false);
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

    public void DisplayMessage(HashMap<String, String> Message, HashMap<String, String> Sender, String SenderId)
    {
        if(PreviousID.equals(SenderId)){
            Adapter.add(Message.get("text"));
        }
        else {
            Adapter.add(Sender.get("name") + ": \n" + Message.get("text"));
        }
    }

    private void RequestMessages()
    {
        Adapter.clear();

        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        Query MessageData = mDatabase.child("chat").child(CurrentChat).child("message").limitToLast(50);

        MessageData.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Conc.acquireUninterruptibly();

                HashMap<String, String> MsgData = (HashMap<String, String>) dataSnapshot.getValue();
                final String SenderId = MsgData.get("sender");
                if (SenderDatas.containsKey(SenderId) && Unresolved == 0)
                {
                    DisplayMessage(MsgData, SenderDatas.get(SenderId), SenderId);
                    PreviousID = SenderId;
                }
                else
                {
                    ToAdd.add(MsgData);

                    if (!SenderDatas.containsKey(SenderId))
                    {
                        Unresolved++;
                        SenderDatas.put(SenderId, null);

                        mDatabase.child("user").child(SenderId).child("data").limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                HashMap<String, String> SenderData = (HashMap<String, String>) dataSnapshot.getValue();

                                Conc.acquireUninterruptibly();

                                SenderDatas.put(SenderId, SenderData);
                                if (--Unresolved == 0)
                                {
                                    while (ToAdd.size() != 0)
                                    {
                                        HashMap<String, String> MsgData = ToAdd.removeFirst();
                                        DisplayMessage(MsgData, SenderDatas.get(MsgData.get("sender")), MsgData.get("sender"));
                                        PreviousID = MsgData.get("sender");
                                        MessageWindow.setSelection(Adapter.getCount() - 1);
                                    }
                                }

                                Conc.release();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                Conc.release();
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
}
