package v6.caique;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class QuickReply extends IntentService {

    public QuickReply() {
        super("QuickReply");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null)
        {
            Bundle b = intent.getExtras();
            Bundle bReply = RemoteInput.getResultsFromIntent(intent);
            if (bReply != null && bReply.containsKey(CloudMessageService.Reply) && b.containsKey("chat"))
            {
                String Chat = b.getString("chat");
                String text = bReply.getString("reply");

                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("chat", Chat)
                        .addData("type", "text")
                        .addData("date", String.valueOf(System.currentTimeMillis() / 1000))
                        .addData("text", text)
                        .build());

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                NotificationCompat.Builder c = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText("Replied with " + text)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentTitle(CacheChats.Loaded.get(Chat).Title)
                        .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText("Replied with " + text));

                notificationManager.notify(Chat.hashCode(), c.build());
            }
        }
    }
}
