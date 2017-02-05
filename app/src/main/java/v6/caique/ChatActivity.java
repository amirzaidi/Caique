package v6.caique;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    public static HashMap<String, ChatActivity> Instances = new HashMap<>();
    public boolean Active;
    private String CurrentChat = null;
    private ChatAdapter Adapter;
    private ListView MessageWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle b = getIntent().getExtras();
        if (b == null|| !b.containsKey("chat")){
            this.finish();
        }

        CurrentChat = b.getString("chat");

        if (Instances.containsKey(CurrentChat))
        {
            Instances.get(CurrentChat).finish();
        }

        Instances.put(CurrentChat, this);

        //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //notificationManager.cancel(CurrentChat.hashCode());

        MessageWindow = (ListView) findViewById(R.id.ChatList);
        Adapter = new ChatAdapter(this, R.layout.chat_message, CurrentChat);
        MessageWindow.setAdapter(Adapter);
    }

    public void ReloadViews()
    {
        Adapter.notifyDataSetChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Active = true;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Thread.sleep(250);
                }
                catch (InterruptedException e)
                {
                }

                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("chat", CurrentChat)
                        .addData("type", "mplaying")
                        .addData("text", "")
                        .build());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Active = false;

        if(CloudMessageService.Instance != null) {
            CloudMessageService.Instance.StopMusic();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Active = false;
        Instances.remove(CurrentChat);
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
}
