package v6.caique;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.content.Context;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";

    private static AtomicInteger Id = new AtomicInteger(0);
    private static int SubTopics = 32;

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
            if(Data.get("type").equals("text") && !Data.get("text").trim().isEmpty()) {
                SendNotif = !prepareMessages(Data.get("chat"), Data.get("text").trim(), Data.get("sender"));
            }

            if (Data.containsKey("chats")) {
                try {
                    JSONArray a = new JSONArray(Data.get("chats"));
                    for (int i = 0; i < a.length(); i++) {
                        Sub(Instance, a.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
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
                        MainActivity.Instance.updateText(Data.get("text"));
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
