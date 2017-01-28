package v6.caique;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    private AtomicInteger Id = new AtomicInteger(0);
    private int SubTopics = 32;

    private ExoPlayer Player;

    private DefaultTrackSelector TrackSelector;
    private DefaultLoadControl LoadControl;
    private DefaultDataSourceFactory SourceFactory;
    private DefaultExtractorsFactory ExtractorsFactory;

    public static MyFirebaseMessagingService Instance;


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
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        FirebaseMessaging Instance = FirebaseMessaging.getInstance();

        Log.d(TAG, "Message Id: " + remoteMessage.getMessageId());
        final Map<String, String> Data = remoteMessage.getData();

        // Check if message contains a data payload.
        if (Data != null && Data.size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Boolean SendNotif = true;
            if(Data.get("type").equals("text"))
            {
                if (!Data.get("text").trim().isEmpty())
                {
                    SendNotif = !prepareMessages(Data.get("chat"), Data.get("text").trim(), Data.get("sender"));
                }
            }

            else if(Data.get("type").equals("play"))
            {
                if(ChatActivity.Instances.containsKey(Data.get("chat"))) {
                    final ChatActivity Chat = ChatActivity.Instances.get(Data.get("chat"));

                    if (Chat.Active)
                    {
                        Player.prepare(new ExtractorMediaSource(Uri.parse("http://77.169.50.118:80/" + Data.get("chat")), SourceFactory, ExtractorsFactory, null, null));
                        Player.setPlayWhenReady(true);
                        Chat.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Chat.setTitle("Playing " + Data.get("text"));
                            }
                        });
                    }
                }
            }

            if (Data.containsKey("chats")) {
                ArrayList<String> Topics = new ArrayList<>();

                try {
                    JSONArray a = new JSONArray(Data.get("chats"));
                    for (int i = 0; i < a.length(); i++) {
                        Sub(Instance, a.getString(i));
                        Topics.add(a.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (MainActivity.Instance != null) {
                    final ArrayList<String> finalTopics = Topics;
                    MainActivity.Instance.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.Instance.CreateChatList(finalTopics);
                        }
                    });
                }
            }

            if (SendNotif)
            {
                sendNotification(Data.toString());
            }

            if (MainActivity.Instance != null) {
                MainActivity.Instance.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //MainActivity.Instance.updateText(Data.get("text"));
                    }
                });
            }
        }
    }


    public Boolean prepareMessages(final String Chat, final String Text, final String Sender){
        if(ChatActivity.Instances.containsKey(Chat)){
            ChatActivity.Instances.get(Chat).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ChatActivity.Instances.get(Chat).DisplayMessage(Text, Sender);
                }
            });

            return true;
        }

        return false;
    }

    public void MusicHandler(boolean Start){
        if(Start){
            if(Player != null) {
                Player.setPlayWhenReady(true);
            }
        }
        else{
            Player.setPlayWhenReady(false);
        }
    }

    public void Sub(FirebaseMessaging Instance, String Topic)
    {
        Log.d(TAG, "Sub to " + Topic);
        for (int j = 0; j < SubTopics; j++) {
            Instance.subscribeToTopic("%" + Topic + "%" + j);
        }
    }

    public void Unsub(FirebaseMessaging Instance, String Topic)
    {
        Log.d(TAG, "Unsub from " + Topic);
        for (int j = 0; j < SubTopics; j++) {
            Instance.unsubscribeFromTopic("%" + Topic + "%" + j);
        }
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Caique")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody));

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(Id.getAndIncrement(), notificationBuilder.build());
    }
}
