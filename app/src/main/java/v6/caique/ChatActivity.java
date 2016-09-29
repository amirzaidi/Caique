package v6.caique;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.vision.text.Line;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static String ChatID;
    public static boolean InChat;
    public static HashMap<String, ChatActivity> Instances = new HashMap<String, ChatActivity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Instances.put("-KSqbu0zMurmthzBE7GF", this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Instances.remove("-KSqbu0zMurmthzBE7GF");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Instances.remove("-KSqbu0zMurmthzBE7GF");
    }


    public void SendMessage(View view) {
        long unixTime = System.currentTimeMillis() / 1000;

        String Date = String.valueOf(unixTime);

        EditText Input = (EditText) findViewById(R.id.editText2);
        String Text = Input.getText().toString();
        String FinalText;

        if(Text.trim().length() > 1024){
            FinalText =  Text.trim().substring(0, 1021) + "...";
        }
        else{
            FinalText  = Text.trim();
        }

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(MyFirebaseInstanceIDService.msgId.incrementAndGet()))
                .addData("chat", "-KSqbu0zMurmthzBE7GF")
                .addData("sender", "1")
                .addData("type", "text")
                .addData("date", Date)
                .addData("text", FinalText)
                .build());

        Log.d("SendMessageToServer", "Message sent");

    }

    public void DisplayMessage(String Message, String Sender)
    {
        EditText MessageInput = (EditText) findViewById(R.id.editText2);
        MessageInput.setText("");

        LinearLayout MessageWindow = (LinearLayout) findViewById(R.id.ScrollLayout);

        TextView MessageBox = new TextView(this);
        MessageBox.setText(Sender + ": " + Message);
        MessageBox.setId(101);
        MessageBox.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        MessageWindow.addView(MessageBox);
    }
}
