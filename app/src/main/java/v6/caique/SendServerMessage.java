package v6.caique;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class SendServerMessage {

    Boolean Error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_server_message);
    }

    public void SendMessageToServer()
    {
        EditText Input = (EditText)findViewById(R.id.ChatId);
        String ChatID = Input.getText().toString();

        Input = (EditText)findViewById(R.id.SenderId);
        String SenderID = Input.getText().toString();

        Input = (EditText)findViewById(R.id.MessageType);
        String MessageType = Input.getText().toString();

        Input = (EditText)findViewById(R.id.Date);
        String Date = Input.getText().toString();

        Input = (EditText)findViewById(R.id.Text);
        String Text = Input.getText().toString();

        if(!ChatID.isEmpty() && !SenderID.isEmpty() && !Date.isEmpty()) {
            FirebaseMessaging fm = FirebaseMessaging.getInstance();
            fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(MyFirebaseInstanceIDService.msgId.incrementAndGet()))
                .addData("chat", ChatID)
                .addData("sender", SenderID)
                .addData("type", MessageType)
                .addData("date", Date)
                .addData("text", Text)
                .build());

            Log.d("SendMessageToServer", "Message sent");
            Error = false;
        }
        else
        {
            Error = true;
        }
    }

    public void SendMessage(View view){

        SendMessageToServer();
        if(!Error)
        {
            this.finish();
        }

    }
}
