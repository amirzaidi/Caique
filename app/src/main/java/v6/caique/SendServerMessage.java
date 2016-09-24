package v6.caique;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class SendServerMessage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_server_message);

        FirebaseMessaging.getInstance().subscribeToTopic("2");
    }

    public void SendMessageToServer()
    {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(MyFirebaseInstanceIDService.msgId.incrementAndGet()))
                .addData("Heyo Captain Jack!", "Bring me back to the railroad track!")
                .addData("Het werkt perfect", "en vlekkeloos")
                .build());

        Log.d("SendMessageToServer", "Message sent");
    }

    public void SendMessage(View view){

        SendMessageToServer();
        this.finish();

    }
}
