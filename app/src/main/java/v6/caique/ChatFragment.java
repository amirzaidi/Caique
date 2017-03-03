package v6.caique;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;

public class ChatFragment extends Fragment {

    private ChatFragment.OnFragmentInteractionListener mListener;
    public ChatAdapter Adapter;
    private ListView MessageWindow;
    private View RootView;
    private ListView List;

    public ChatFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void ReloadViews()
    {
        Adapter.notifyDataSetChanged();
    }

    private String SavedText;

    @Override
    public void onPause()
    {
        EditText T = (EditText) getView().findViewById(R.id.editChatText);
        if (T != null)
        {
            T.getText().toString();
        }

        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (SavedText != null)
        {
            EditText T = (EditText) getView().findViewById(R.id.editChatText);
            if (T != null)
            {
                T.setText(SavedText);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RootView = inflater.inflate(R.layout.fragment_chat, container, false);

        setHasOptionsMenu(true);
        MessageWindow = (ListView) RootView.findViewById(R.id.ChatList);

        if(CacheChats.Loaded.get(((ChatActivity) getActivity()).CurrentChat) == null) {
            CacheChats.StartListen(((ChatActivity) getActivity()).CurrentChat);
        }

        Adapter = new ChatAdapter(this.getActivity(), R.layout.chat_message, ((ChatActivity) getActivity()).CurrentChat);
        MessageWindow.setAdapter(Adapter);

        List = (ListView) RootView.findViewById(R.id.ChatList);
        List.setSelection(Adapter.getCount() - 1);
        List.setDivider(null);

        return RootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.chat, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void SendMessage() {
        String Date = String.valueOf(System.currentTimeMillis() / 1000);

        EditText Input = (EditText) getView().findViewById(R.id.editChatText);
        String Text = Input.getText().toString().trim();

        if(Text.length() > 1024){
            Text =  Text.substring(0, 1021) + "...";
        }
        else if (Text.length() == 0)
        {
            return;
        }

        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", ((ChatActivity)getActivity()).CurrentChat)
                .addData("type", "text")
                .addData("date", Date)
                .addData("text", Text)
                .build());

        Log.d("SendMessageToServer", "Message sent " + Text);
        Input.setText("");

    }

    public void SetSubbed(boolean Subbed){
        LinearLayout Bottom = (LinearLayout) RootView.findViewById(R.id.ChatBottom);
        if(Subbed){

            TextInputEditText Typer = new TextInputEditText(this.getContext());
            Typer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f));
            Typer.setEms(10);
            Typer.setId(R.id.editChatText);
            Typer.setHint("Type your message...");
            Typer.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            Typer.setPadding(12, 8, 12, 8);

            Button SendButton = new Button(this.getContext());
            SendButton.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.send_button_width), ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
            SendButton.setText("Send");
            //SendButton.setId(R.id.sendButton);
            SendButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    SendMessage();
                }
            });

            Bottom.removeAllViews();
            Bottom.addView(Typer);
            Bottom.addView(SendButton);

            setHasOptionsMenu(true);
        }
        else{
            Button SubButton = new Button(this.getContext());
            SubButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            SubButton.setText("Subscribe to chat");
            //SubButton.setId(R.id.subButton);
            SubButton.setOnClickListener(new Button.OnClickListener() {
                public void onClick(View v) {
                    SubToChat();
                }
            });

            Bottom.removeAllViews();
            Bottom.addView(SubButton);

            setHasOptionsMenu(false);
        }
    }

    private void SubToChat(){
        String ChatId = ((ChatActivity) getActivity()).CurrentChat;
        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("chat", ((ChatActivity)getActivity()).CurrentChat)
                .addData("type", "joinchat")
                .addData("text", ChatId)
                .build());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onChatFragmentInteraction(Uri uri);
        void onMusicPlayerFragmentInteraction(Uri uri);
    }

}