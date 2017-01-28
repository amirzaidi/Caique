package v6.caique;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.concurrent.atomic.AtomicInteger;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";
    public static AtomicInteger msgId = new AtomicInteger(0);

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Created instance!");
        //test
    }

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        //Intent i = new Intent(this, MainActivity.class);
    }
}
