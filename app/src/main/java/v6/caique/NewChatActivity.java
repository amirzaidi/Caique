package v6.caique;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewChatActivity extends Activity {

    private ArrayList<String> Tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);

        final LinearLayout Group = (LinearLayout) findViewById(R.id.TagSelect);

        FirebaseDatabase.getInstance().getReference().child("tags").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(DataSnapshot entry : dataSnapshot.getChildren()){
                            String Name = entry.getKey();
                            ToggleButton Button = new ToggleButton(getBaseContext());
                            Button.setText(Name);
                            Button.setTextOff(Name);
                            Button.setTextOn(Name);

                            final String Tag = Name.toLowerCase();
                            Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked && !Tags.contains(Tag))
                                    {
                                        Tags.add(Tag);
                                    }
                                    else if (!isChecked && Tags.contains(Tag))
                                    {
                                        Tags.remove(Tag);
                                    }
                                }
                            });

                            Group.addView(Button);
                        }
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void Send(View Caller)
    {
        EditText t = (EditText) findViewById(R.id.newChatTitle);
        String text = t.getText().toString().trim();
        if (Tags.size() > 0 && text.length() > 2)
        {
            JSONObject Obj = new JSONObject();
            try
            {
                Obj.put("title", text);
                Obj.put("tags", new JSONArray(Tags));
            }
            catch (JSONException e)
            {
                Log.d("newChat", e.getMessage());
            }

            FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                    .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                    .addData("type", "newchat")
                    .addData("text", Obj.toString())
                    .build());

            finish();
        }
    }
}
