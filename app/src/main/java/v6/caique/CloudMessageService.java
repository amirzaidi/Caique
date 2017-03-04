package v6.caique;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

import static com.google.android.exoplayer2.ExoPlayer.STATE_IDLE;

public class CloudMessageService extends FirebaseMessagingService {
    private static final String TAG = "CloudMessageService";

    private static int SubTopics = 32;

    private static ExoPlayer Player;
    private static Semaphore Waiter = new Semaphore(1);

    private static DefaultTrackSelector TrackSelector;
    private static DefaultLoadControl LoadControl;
    private static DefaultDataSourceFactory SourceFactory;
    private static DefaultExtractorsFactory ExtractorsFactory;

    public static CloudMessageService Instance;

    public CloudMessageService()
    {
        super();
        Instance = this;
    }

    @Override
    public void onCreate()
    {
        if (Player == null)
        {
            SourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Caique"));
            TrackSelector = new DefaultTrackSelector();
            LoadControl = new DefaultLoadControl(new DefaultAllocator(false, 8 * 1024), 500, 1000, 500, 500);
            ExtractorsFactory = new DefaultExtractorsFactory();

            Player = ExoPlayerFactory.newSimpleInstance(this, TrackSelector, LoadControl);
        }

        Log.d(TAG, "New Instance");
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message Id: " + remoteMessage.getMessageId());
        final Map<String, String> Data = remoteMessage.getData();

        if (Data != null && Data.size() > 0)
        {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (Data.containsKey("chat"))
            {
                final String ChatId = Data.get("chat");
                if (CacheChats.Subs.contains(ChatId)) {

                    String Type = Data.get("type");
                    boolean Active = ChatActivity.Instances.containsKey(ChatId) && ChatActivity.Instances.get(ChatId).Active;

                    if (Type.equals("text") && !Active) {
                        sendNotification(ChatId, CacheChats.Name(Data.get("sender"), "Unknown") + ": " + Data.get("text"));
                    } else if ((Type.equals("start") || Type.equals("play")) && ChatActivity.Instances.containsKey(ChatId)) {

                        final ChatActivity Chat = ChatActivity.Instances.get(ChatId);

                        if (Data.get("text") == null || Data.get("text").isEmpty())
                        {
                            if (Active) {
                                Chat.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Chat.setTitle(CacheChats.Name(ChatId, "Caique"));
                                        Chat.CurrentSong = "";
                                        Chat.MusicPlayer.SetCurrentlyPlaying();
                                        Chat.Playlist.clear();
                                        Chat.ReloadSongViews();
                                    }
                                });
                            }

                            return;
                        }

                        try {
                            JSONObject ParseMain = new JSONObject(Data.get("text"));
                            JSONObject Playing = ParseMain.getJSONObject("Playing");
                            String mTitle = "";
                            if (!Playing.isNull("name"))
                            {
                                mTitle = Playing.get("name").toString();
                            }

                            final String Title = mTitle;

                            if (Active) {
                                final ArrayList<String> NewPlaylist = new ArrayList<>();
                                JSONArray Array = ParseMain.getJSONArray("Titles");

                                for (int i = 0; i < Array.length(); i++){
                                    NewPlaylist.add(Array.getString(i));
                                }

                                Chat.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Chat.CurrentSong = Title;
                                        if (Title.isEmpty())
                                        {
                                            Chat.setTitle(CacheChats.Name(ChatId, "Caique"));
                                        }
                                        else
                                        {
                                            Chat.setTitle("Playing " + Title);
                                        }

                                        Chat.MusicPlayer.SetCurrentlyPlaying();
                                        Chat.Playlist = NewPlaylist;
                                        Chat.ReloadSongViews();
                                    }
                                });

                                StartMusic(ChatId, Type.equals("start"));
                            } else {
                                sendNotification(ChatId, "♫ " + Title + " ♫");
                            }
                        }
                        catch (JSONException e)
                        {
                            Log.d("JSONMainPlaying", e.getMessage());
                        }
                    }
                }
                else
                {
                    Unsub(ChatId);
                }
            }
            else if (Data.containsKey("type") && Data.get("type").equals("reg"))
            {
                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("type", "reg")
                        .addData("text", MainActivity.Instance.sharedPref.getString("gidtoken", ""))
                        .build());
            }
            else
            {
                sendNotification(null, Data.toString());
            }
        }
    }

    private static String LastChat;
    public void StartMusic(final String Chat, boolean Force) {
        if (CurrentSettings.MusicInChats && (Force || !Chat.equals(LastChat) || Player.getPlaybackState() != 3)) {
            LastChat = Chat;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Waiter.acquire();

                        Player.prepare(new ExtractorMediaSource(Uri.parse("http://77.169.50.118:80/" + Chat), SourceFactory, new DefaultExtractorsFactory(), null, null));
                        Player.setPlayWhenReady(true);

                        Waiter.release();
                    } catch (InterruptedException e) {
                    }
                }
            }).start();
        }
    }

    public void StartMusic(String Chat) {
        StartMusic(Chat, false);
    }


    public void StopMusic(final String Chat){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //if (Player != null) {
                    try {
                        Waiter.acquire();

                        //Player.setPlayWhenReady(false);
                        if (Chat.equals(LastChat))
                        {
                            Player.stop();
                            //Player.seekTo(0);
                            while (Player.getPlaybackState() != STATE_IDLE) {
                                Thread.sleep(25);
                            }
                        }

                        Waiter.release();
                    } catch (InterruptedException e) {
                    }
                //}
            }
        }).start();
    }

    public static void Sub(final String ChatId)
    {
        final FirebaseMessaging Instance = FirebaseMessaging.getInstance();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Random r = new Random();
                    Log.d(TAG, "Sub to " + ChatId);
                    for (int j = 0; j < SubTopics; j++) {
                        Thread.sleep(50 + (int)Math.ceil(50 * r.nextDouble()));
                        Instance.subscribeToTopic("%" + ChatId + "%" + j);
                    }
                }
                catch (InterruptedException Ex) {}
            }
        });
    }

    public static void Unsub(final String ChatId)
    {
        final FirebaseMessaging Instance = FirebaseMessaging.getInstance();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Random r = new Random();
                    Log.d(TAG, "Unsub from " + ChatId);
                    for (int j = 0; j < SubTopics; j++) {
                        Thread.sleep(50 + (int)Math.ceil(50 * r.nextDouble()));
                        Instance.unsubscribeFromTopic("%" + ChatId + "%" + j);
                    }
                } catch (InterruptedException Ex) {
                }
            }
        });
    }

    private void sendNotification(String Chat, String text) {

        Intent intent;

        if (Chat == null)
        {
            intent = new Intent(this, MainActivity.class);
            Chat = "Log";
        }
        else
        {
            intent = new Intent(this, ChatActivity.class).putExtra("chat", Chat);
            Log.d("NotificationIntent", Chat);
            Chat = CacheChats.Loaded.get(Chat).Title;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(Chat.hashCode(), new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(Chat)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)).build()
        );
    }
}
