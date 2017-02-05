package v6.caique;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
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

import java.util.ArrayList;
import java.util.Map;

public class CloudMessageService extends FirebaseMessagingService {
    private static final String TAG = "CloudMessageService";

    private int SubTopics = 32;

    private ExoPlayer Player;

    private DefaultTrackSelector TrackSelector;
    private DefaultLoadControl LoadControl;
    private DefaultDataSourceFactory SourceFactory;
    private DefaultExtractorsFactory ExtractorsFactory;

    public static CloudMessageService Instance;
    public static String RegToken;

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
        Instance = this;

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
                String Type = Data.get("type");

                boolean Active = ChatActivity.Instances.containsKey(ChatId) && ChatActivity.Instances.get(ChatId).Active;

                if (Type.equals("text") && !Active)
                {
                    String ChatName = DatabaseCache.GetChatName(ChatId, null);
                    String UserName = DatabaseCache.GetUserName(Data.get("sender"), null);

                    if (ChatName == null || UserName == null)
                    {
                        DatabaseCache.LoadChatDataOnce(ChatId, new Runnable()
                        {
                            @Override
                            public void run() {
                                DatabaseCache.LoadUserDataOnce(Data.get("sender"), new Runnable()
                                {
                                    @Override
                                    public void run() {
                                        sendNotification(ChatId, DatabaseCache.GetUserName(Data.get("sender"), "") + ": " + Data.get("text"));
                                    }
                                });
                            }
                        });
                    }
                    else
                    {
                        sendNotification(ChatId, UserName + ": " + Data.get("text"));
                    }

                }
                else if(Type.equals("play"))
                {
                    if (Active)
                    {
                        if(CurrentSettings.MusicInChats) {
                            Player.prepare(new ExtractorMediaSource(Uri.parse("http://77.169.50.118:80/" + Data.get("chat")), SourceFactory, ExtractorsFactory, null, null));
                            Player.setPlayWhenReady(true);
                        }

                        final ChatActivity Chat = ChatActivity.Instances.get(Data.get("chat"));
                        Chat.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Chat.setTitle("Playing " + Data.get("text"));
                            }
                        });
                    }
                    else
                    {
                        String ChatName = DatabaseCache.GetChatName(ChatId, null);

                        if (ChatName == null)
                        {
                            DatabaseCache.LoadChatDataOnce(ChatId, new Runnable()
                            {
                                @Override
                                public void run() {
                                    sendNotification(ChatId, "Playing " + Data.get("text"));
                                }
                            });
                        }
                        else
                        {
                            sendNotification(ChatId, "Playing " + Data.get("text"));
                        }

                    }
                }
            }
            else if (Data.containsKey("chats"))
            {
                ArrayList<String> Topics = new ArrayList<>();

                try {
                    final JSONArray a = new JSONArray(Data.get("chats"));
                    for (int i = 0; i < a.length(); i++) {
                        Topics.add(a.getString(i));
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < a.length(); i++) {
                                try {
                                    Sub(Instance, a.getString(i));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                if (MainActivity.Instance != null) {
                    MainActivity.Instance.GetChatNames(Topics);
                }
            }
            else
            {
                sendNotification("Log", Data.toString());
            }
        }
    }

    public void SetMusicPlaying(boolean Start){
        if(Player != null) {
            Player.setPlayWhenReady(Start);
        }
    }

    private static ArrayList<String> Subs = new ArrayList<>();

    public void Sub(final FirebaseMessaging Instance, final String Topic)
    {
        if (!Subs.contains(Topic))
        {
            Subs.add(Topic);

            try {
                Log.d(TAG, "Sub to " + Topic);
                for (int j = 0; j < SubTopics; j++) {
                    Thread.sleep(75);
                    Instance.subscribeToTopic("%" + Topic + "%" + j);
                }
            }
            catch (InterruptedException Ex) {}
        }
    }

    /*public void Unsub(final FirebaseMessaging Instance, final String Topic)
    {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Unsub from " + Topic);
                for (int j = 0; j < SubTopics; j++) {
                    Instance.subscribeToTopic("%" + Topic + "%" + j);
                }
            }
        });
    }*/

    private void sendNotification(String chat, String text) {
        Intent intent = new Intent(this, ChatActivity.class)
                .putExtra("chat", chat);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(chat.hashCode(), new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(DatabaseCache.GetChatName(chat, "Unknown"))
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text)).build());
    }
}
