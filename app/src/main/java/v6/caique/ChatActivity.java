package v6.caique;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    public static HashMap<String, ChatActivity> Instances = new HashMap<String, ChatActivity>();
    public static boolean Active;
    private String CurrentChat = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Bundle b = getIntent().getExtras();
        if (b != null){
            CurrentChat = b.getString("chat");
            Instances.put(CurrentChat, this);
        }
        Active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Active = false;
        if(MyFirebaseMessagingService.Instance != null) {
            MyFirebaseMessagingService.Instance.MusicHandler(false);
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
        if(MyFirebaseMessagingService.Instance != null) {
            MyFirebaseMessagingService.Instance.MusicHandler(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Instances.remove(CurrentChat);
        Active = false;
        if(MyFirebaseMessagingService.Instance != null) {
            MyFirebaseMessagingService.Instance.MusicHandler(false);
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
                .setMessageId(Integer.toString(MyFirebaseInstanceIDService.msgId.incrementAndGet()))
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
                .setMessageId(Integer.toString(MyFirebaseInstanceIDService.msgId.incrementAndGet()))
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
                .setMessageId(Integer.toString(MyFirebaseInstanceIDService.msgId.incrementAndGet()))
                .addData("chat", CurrentChat)
                .addData("type", "mplaying")
                .addData("date", Date)
                .addData("text", "")
                .build());

    }

    public void DisplayMessage(String Message, String Sender)
    {
        LinearLayout MessageWindow = (LinearLayout) findViewById(R.id.ScrollLayout);

        TextView MessageBox = new TextView(this);
        MessageBox.setText(Sender + ": " + Message);
        MessageBox.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        MessageWindow.addView(MessageBox);
    }

    private void RequestMessages()
    {


        final DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query MessageData = mDatabase.child("chat").child(CurrentChat).child("message");
        MessageData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, HashMap<String, String>> Children = (HashMap<String, HashMap<String, String>>) dataSnapshot.getValue();
                for(Map.Entry<String, HashMap<String, String>> entry : Children.entrySet()) {
                    HashMap<String, String> value = entry.getValue();
                    String Message = value.get("text");
                    String Sender = value.get("sender");
                    DisplayMessage(Message, Sender);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }
}
