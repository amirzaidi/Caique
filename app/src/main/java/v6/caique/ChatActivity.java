package v6.caique;

import android.app.ActionBar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    public static HashMap<String, ChatActivity> Instances = new HashMap<>();
    public boolean Active;
    protected String CurrentChat = null;
    private ChatFragment ChatWindow;
    private ChatInfoFragment ChatInfo;
    public MusicPlayerFragment MusicPlayer;
    public ArrayList<String> Playlist = new ArrayList<>();
    public String CurrentSong;

    public static ArrayList<String> SelectionUrls = new ArrayList<>();
    public static ArrayList<String> SelectionNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                actionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar_chat);

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
        ChatInfo = new ChatInfoFragment();
        MusicPlayer = new MusicPlayerFragment();

        SetChatFragment(null);

        if (CacheChats.Loaded.containsKey(CurrentChat))
        {
            setTitle(CacheChats.Loaded.get(CurrentChat).Title);
        }
    }

    @Override
    public void onBackPressed(){
        if(!ChatWindow.isVisible()){

            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.activity_chat, ChatWindow)
                    .commit();
        }
        else{
            super.onBackPressed();
        }
    }

    public void SetSubbed(boolean Subbed) {
        if (ChatWindow != null)
        {
            ChatWindow.SetSubbed(Subbed);
            MusicPlayer.SetSubbed(Subbed);
            ChatInfo.SetButton(Subbed);
        }
    }

    public void ReloadChatViews(boolean Normal, boolean Typing){
        if(ChatWindow.isVisible()) {
            ChatWindow.ReloadViews(Normal, Typing);
        }
    }

    public void ReloadSongViews(){
        if(MusicPlayer.isVisible()) {
            MusicPlayer.ReloadViews();
        }
    }

    public void ReloadSongSelectionViews(){
        if(MusicPlayer.isVisible()){

            MusicPlayer.SelectionUrls = SelectionUrls;
            MusicPlayer.SelectionNames =  SelectionNames;
            MusicPlayer.ReloadSelectionViews();
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

    public void SetInfoFragment(View view){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_chat, ChatInfo)
                .commit();
    }
}
