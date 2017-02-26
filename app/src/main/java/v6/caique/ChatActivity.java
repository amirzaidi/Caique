package v6.caique;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

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

public class ChatActivity extends AppCompatActivity {

    public static HashMap<String, ChatActivity> Instances = new HashMap<>();
    public boolean Active;
    protected String CurrentChat = null;
    private ChatFragment ChatWindow;
    public MusicPlayerFragment MusicPlayer;
    public ArrayList<String> Playlist = new ArrayList<>();

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

        ChatWindow = new ChatFragment();
        MusicPlayer = new MusicPlayerFragment();
        SetChatFragment(null);

        DatabaseReference Database = FirebaseDatabase.getInstance().getReference();

        Query DataQuery = Database.child("chat").child(CurrentChat).child("data");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                String Title = (String) Data.get("title");

                ChatActivity.Instances.get(CurrentChat).setTitle(Title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        DataQuery.addValueEventListener(listener);
    }

    public void ReloadChatViews(){
        if(ChatWindow.isVisible()) {
            ChatWindow.ReloadViews();
        }
    }

    public void ReloadSongViews(){
        if(MusicPlayer.isVisible()) {
            MusicPlayer.ReloadViews();
        }
    }

    public void SendMessage(View view) {
        ChatWindow.SendMessage();
    }

    public void AddMusic(View view){
        MusicPlayer.SendMusic();
    }

    public void RemoveFromQueue(){
        Playlist.remove(0);
        ArrayList<String> PlaylistTemp = new ArrayList<>();
        for(String Song: Playlist){
            PlaylistTemp.add(Song);
        }
        Playlist.clear();
        for(String Song: PlaylistTemp){
            Playlist.add(Song);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Active = true;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("chat", CurrentChat)
                        .addData("type", "mplaying")
                        .addData("text", "")
                        .build());
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("chat", CurrentChat)
                        .addData("type", "mqueue")
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

    public void SetMusicPlayerFragment(MenuItem item)
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_chat, MusicPlayer)
                .commit();
    }

    public void SetChatFragment(MenuItem item)
    {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_chat, ChatWindow)
                .commit();
    }
}
