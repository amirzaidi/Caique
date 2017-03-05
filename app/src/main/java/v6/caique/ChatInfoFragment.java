package v6.caique;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class ChatInfoFragment extends Fragment {

    private View RootView;

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

        SetButton(((ChatActivity)getActivity()).isSubbed());

        return RootView;
    }

    public void SetButton(boolean Subbed){

        LinearLayout MainFrame = (LinearLayout) RootView.findViewById(R.id.mainframe);
        if(Subbed){

            Switch FavSwitch = new Switch(this.getContext());

            if(MainActivity.Instance.sharedPref.contains(((ChatActivity)getActivity()).CurrentChat)){
                FavSwitch.setChecked(true);
            }
            else{
                FavSwitch.setChecked(false);
            }

            FavSwitch.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            FavSwitch.setText("Favorite chat");
            FavSwitch.setPadding(0,(int) getResources().getDimension(R.dimen.fab_margin),0, (int) getResources().getDimension(R.dimen.fab_margin));
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
                }
            });

            MainFrame.addView(FavSwitch);
            MainFrame.addView(UnsubButton);
        }
        else{
            Button SubButton = new Button(this.getContext());
            SubButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            SubButton.setText("Subscribe to chat");
            SubButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    SubToChat();
                }
            });

            MainFrame.addView(SubButton);
        }
    }

    private void SetFavorite(boolean Checked){
        if(Checked){
            MainActivity.Instance.sharedPref.edit().putBoolean(((ChatActivity)getActivity()).CurrentChat, Checked).apply();
        }
        else{
            MainActivity.Instance.sharedPref.edit().remove(((ChatActivity)getActivity()).CurrentChat).apply();
        }
    }

    private void SubToChat(){
        String ChatId = ((ChatActivity) getActivity()).CurrentChat;
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", ChatId)
                .addData("type", "joinchat")
                .addData("text", ChatId)
                .build());
    }

    private void UnsubFromChat(){
        String ChatId = ((ChatActivity) getActivity()).CurrentChat;
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", ChatId)
                .addData("type", "leavechat")
                .addData("text", ChatId)
                .build());
    }


}
