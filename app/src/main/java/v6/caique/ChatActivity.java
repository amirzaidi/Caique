package v6.caique;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
    public String CurrentSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Bundle b = getIntent().getExtras();
        if (b == null || !b.containsKey("chat")){
            this.finish();
        }

        CurrentChat = b.getString("chat");
        Log.d("ChatStartParam", CurrentChat);

        if (Instances.containsKey(CurrentChat))
        {
            Instances.get(CurrentChat).finish();
        }

        Instances.put(CurrentChat, this);

        ChatWindow = new ChatFragment();
        MusicPlayer = new MusicPlayerFragment();

        SetChatFragment(null);

        setTitle(CacheChats.Name(CurrentChat, "Caique"));
    }

    public void SetSubbed(boolean Subbed) {
        if (ChatWindow != null)
        {
            ChatWindow.SetSubbed(Subbed);
            MusicPlayer.SetSubbed(Subbed);
        }
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

    /*public void RemoveFromQueue(){
        Playlist.remove(0);
        ArrayList<String> PlaylistTemp = new ArrayList<>();
        for(String Song: Playlist){
            PlaylistTemp.add(Song);
        }
        Playlist.clear();
        for(String Song: PlaylistTemp){
            Playlist.add(Song);
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        Active = true;

        if (CloudMessageService.Instance != null)
        {
            CloudMessageService.Instance.StartMusic(CurrentChat);
        }

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", CurrentChat)
                .addData("type", "mplaying")
                .addData("text", "")
                .build());
    }

    public boolean isSubbed()
    {
        return CacheChats.Subs.contains(CurrentChat);
    }

    public void SkipSong(View view){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("chat", CurrentChat)
                        .addData("type", "mskip")
                        .addData("text", "")
                        .build());
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Active = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Active = false;

        if (CloudMessageService.Instance != null)
        {
            CloudMessageService.Instance.StopMusic(CurrentChat);
        }

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
