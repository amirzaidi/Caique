package v6.caique;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.atomic.AtomicInteger;

public class FirebaseIDService extends FirebaseInstanceIdService {
    private static final String TAG = "FirebaseIDService";
    public static AtomicInteger msgId = new AtomicInteger(1);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created instance!");
        //test
    }

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        if (MainActivity.Instance != null)
        {
            FirebaseMessaging fm = FirebaseMessaging.getInstance();
            fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                    .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                    .addData("type", "reg")
                    .addData("text", MainActivity.Instance.sharedPref.getString("gid", ""))
                    .build());
        }

    }
}
