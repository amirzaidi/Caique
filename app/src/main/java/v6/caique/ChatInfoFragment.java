package v6.caique;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatInfoFragment extends Fragment {

    private View RootView;
    private HashMap<String, Boolean> Tags = new HashMap<>();

    public ChatInfoFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RootView =inflater.inflate(R.layout.fragment_chat_info, container, false);

        SetDP();
        SetTags();
        SetTitle();
        SetButton();

        return RootView;
    }

    public void SetButton(){

        if(this.getContext() != null) {

            LinearLayout MainFrame = (LinearLayout) RootView.findViewById(R.id.mainframe);
            Switch FavSwitch = new Switch(this.getContext());

            if (MainActivity.Instance.sharedPref.contains(((ChatActivity) getActivity()).CurrentChat)) {
                FavSwitch.setChecked(true);
            } else {
                FavSwitch.setChecked(false);
            }

            FavSwitch.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            FavSwitch.setText("Favorite chat");
            FavSwitch.setPadding((int) getResources().getDimension(R.dimen.appbar_padding_top), (int) getResources().getDimension(R.dimen.fab_margin), (int) getResources().getDimension(R.dimen.appbar_padding_top), (int) getResources().getDimension(R.dimen.fab_margin));
            FavSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SetFavorite(isChecked);
                }
            });

            Button UnsubButton = new Button(this.getContext());
            UnsubButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            UnsubButton.setText("Unsubscribe from chat");
            UnsubButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    UnsubFromChat();
                    ((ChatActivity) getActivity()).SetChatFragment(null);
                }
            });

            MainFrame.addView(FavSwitch);
            MainFrame.addView(UnsubButton);
        }
    }

    public void ShowFullDP(){
        final ImageView V = new ImageView(getContext());
        final Context c = getContext();
        final StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("chats/" + ((ChatActivity)getActivity()).CurrentChat);
        V.post(new Runnable() {
            @Override
            public void run() {
                V.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) getResources().getDimension(R.dimen.full_dp_height)));
                try {
                    ref.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            try {
                                Glide.with(c)
                                        .using(new FirebaseImageLoader())
                                        .load(ref)
                                        .fitCenter()
                                        .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                                        .into(V);
                            } catch (Exception x) {
                                Log.d("GlideChatAdapter", "Glide: " + x.getMessage());
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.d("ChatAdapter", "Glide: " + e.getMessage());
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        //builder.setTitle("Picture");
        builder.setCustomTitle(null);
        builder.setView(V);
        builder.show();
    }

    public void SetDP(){
        final ImageView Picture = (ImageView) RootView.findViewById(R.id.chat_dp);
        Picture.setImageDrawable(null);

        final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("chats/" + ((ChatActivity)getActivity()).CurrentChat);
        try {
            storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    try {
                        Glide.with(getContext())
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .centerCrop()
                                .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                                .into(Picture);
                    }
                    catch (Exception x)
                    {
                    }
                }
            });
        }
        catch(Exception e){
        }

        Picture.requestLayout();
    }

    public void SetTitle(){
        EditText Title = (EditText) RootView.findViewById(R.id.title_input);
        Title.setText(CacheChats.Loaded.get(((ChatActivity)getActivity()).CurrentChat).Title);
    }

    public void SetTags(){
        FirebaseDatabase.getInstance().getReference().child("tags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                getActivity().runOnUiThread(new Runnable() {

                    final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final LinearLayout TagsView = (LinearLayout) RootView.findViewById(R.id.tags);

                    @Override
                    public void run() {
                        HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                        for (final String t : Data.keySet()) {
                            Tags.put(t, CacheChats.Loaded.get(((ChatActivity) getActivity()).CurrentChat).Tags.contains(t));
                            View Inflated = vi.inflate(R.layout.list_item_tag, TagsView, false);
                            CheckBox Box = (CheckBox) Inflated.findViewById(R.id.checkBox);
                            Box.setPadding((int) getResources().getDimension(R.dimen.appbar_padding_top), 0, (int) getResources().getDimension(R.dimen.appbar_padding_top), 0);
                            Box.setText(t.substring(0, 1).toUpperCase() + t.substring(1).toLowerCase());
                            if (CacheChats.Loaded.get(((ChatActivity) getActivity()).CurrentChat).Tags.contains(t)) {
                                Box.setChecked(true);
                            } else {
                                Box.setChecked(false);
                            }
                            Box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked) {
                                        Tags.put(t, true);
                                    } else {
                                        Tags.put(t, false);
                                    }
                                }
                            });

                            TagsView.addView(Inflated);
                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void SetFavorite(boolean Checked){
        if(Checked){
            MainActivity.Instance.sharedPref.edit().putBoolean(((ChatActivity)getActivity()).CurrentChat, Checked).apply();
        }
        else{
            MainActivity.Instance.sharedPref.edit().remove(((ChatActivity)getActivity()).CurrentChat).apply();
        }
    }

    public void SendUpdate() {
        ArrayList<String> EnabledTags = new ArrayList<>();

        for(Map.Entry<String, Boolean> entry: Tags.entrySet()){
            if(entry.getValue()){
                EnabledTags.add(entry.getKey());
            }
        }

        EditText Input = (EditText) RootView.findViewById(R.id.title_input);
        String Text = Input.getText().toString().trim();

        if (Text.length() == 0)
        {
            Input.setText(CacheChats.Loaded.get(((ChatActivity)getActivity()).CurrentChat).Title);
            Text = CacheChats.Loaded.get(((ChatActivity)getActivity()).CurrentChat).Title;
        }

        JSONObject UpdateObject = new JSONObject();
        JSONArray TagsArray = new JSONArray();

        try {
            for(String s: EnabledTags){
                TagsArray.put(s);
            }
            UpdateObject.put("title", Text);
            UpdateObject.put("tags", TagsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", ((ChatActivity)getActivity()).CurrentChat)
                .addData("type", "update")
                .addData("text", UpdateObject.toString())
                .addData("date", String.valueOf(System.currentTimeMillis() / 1000))
                .build());
    }

    private void UnsubFromChat(){
        ((LinearLayout) RootView.findViewById(R.id.mainframe)).removeAllViews();
        if(MainActivity.Instance.sharedPref.contains(((ChatActivity)getActivity()).CurrentChat)) {
            MainActivity.Instance.sharedPref.edit().remove(((ChatActivity)getActivity()).CurrentChat).apply();
        }

        String ChatId = ((ChatActivity) getActivity()).CurrentChat;
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", ChatId)
                .addData("type", "leavechat")
                .addData("text", ChatId)
                .build());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SetDP();
    }

}
