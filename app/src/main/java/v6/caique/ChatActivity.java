package v6.caique;

import android.app.ActionBar;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.GenericRequest;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.view.View.VISIBLE;

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

    public void showDp(View view)
    {
        ImageView chatV = (ImageView) view;
        String Sender = (String) chatV.getTag(R.id.chatdp);
        if (Sender != null)
        {
            final ImageView V = new ImageView(this);
            //V.setImageDrawable(chatV.getDrawable());
            final Context c = this;
            final StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("users/" + Sender);

            V.post(new Runnable() {
                @Override
                public void run() {
                    V.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

                    try {
                        ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                try {
                                    Glide.with(c)
                                            .using(new FirebaseImageLoader())
                                            .load(ref)
                                            .fitCenter()
                                            .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                                            .into(V);
                                }
                                catch (Exception x)
                                {
                                    Log.d("GlideChatAdapter", "Glide: " + x.getMessage());
                                }
                            }
                        });
                    }
                    catch (Exception e)
                    {
                        Log.d("ChatAdapter", "Glide: " + e.getMessage());
                    }
                }
            });

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //builder.setTitle("Picture");
            builder.setCustomTitle(null);
            builder.setView(V);

            /*builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //V.getDrawable()
                    dialog.cancel();
                }
            });

            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });*/

            builder.show();
        }
    }

    public void copyClipboard(View view)
    {
        TextView t = (TextView) view.findViewById(R.id.messageItem);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Copied", t.getText().toString()));
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
        this.setTitle(CacheChats.Loaded.get(CurrentChat).Title);

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
