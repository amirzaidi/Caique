package v6.caique;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.content.Context;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultAllocator;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tagmanager.InstallReferrerService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class CloudMessageService extends FirebaseMessagingService {
    private static final String TAG = "CloudMessageService";

    private static int SubTopics = 32;

    private ExoPlayer Player;

    private DefaultTrackSelector TrackSelector;
    private DefaultLoadControl LoadControl;
    private DefaultDataSourceFactory SourceFactory;
    private DefaultExtractorsFactory ExtractorsFactory;

    public static CloudMessageService Instance;

    public CloudMessageService()
    {
        super();
        Instance = this;
    }

    @Override
    public void onCreate()
    {
        SourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Caique"));
        TrackSelector = new DefaultTrackSelector(new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                return false;
            }
        }));
        LoadControl = new DefaultLoadControl(new DefaultAllocator(8 * 1024), 500, 1000, 500, 500);
        ExtractorsFactory = new DefaultExtractorsFactory();

        Player = ExoPlayerFactory.newSimpleInstance(this, TrackSelector, LoadControl);

        Log.d(TAG, "New Instance");
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        final FirebaseMessaging Instance = FirebaseMessaging.getInstance();

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
                    } else if (Type.equals("play")) {
                        if (Active) {
                            Player.prepare(new ExtractorMediaSource(Uri.parse("http://77.169.50.118:80/" + Data.get("chat")), SourceFactory, ExtractorsFactory, null, null));
                            Player.setPlayWhenReady(true);

                            final ChatActivity Chat = ChatActivity.Instances.get(Data.get("chat"));
                            Chat.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Chat.setTitle("Playing " + Data.get("text"));
                                }
                            });
                        } else {
                            sendNotification(ChatId, "Playing " + Data.get("text"));
                        }
                    }
                }
                else
                {
                    Unsub(ChatId);
                }
            }
            else
            {
                sendNotification(null, Data.toString());
            }
        }
    }

    public void SetMusicPlaying(boolean Start){
        if(Player != null) {
            Player.setPlayWhenReady(Start);
        }
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
            Chat = "Log";
            intent = new Intent(this, MainActivity.class);
        }
        else
        {
            Chat = CacheChats.Loaded.get(Chat).Title;
            intent = new Intent(this, ChatActivity.class).putExtra("chat", Chat);
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
