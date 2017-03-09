package v6.caique;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class ChatFragment extends Fragment {

    private ChatFragment.OnFragmentInteractionListener mListener;
    public ChatAdapter Adapter;
    public ChatTypingAdapter TypingAdapter;
    private ListView MessageWindow;
    private View RootView;
    private ListView List;

    public ChatFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void ReloadViews(boolean Normal, boolean Typing)
    {
        if (Normal)
        {
            Adapter.notifyDataSetChanged();
        }

        if (Typing)
        {
            TypingAdapter.Refill();
        }
    }

    private String SavedText;

    @Override
    public void onPause()
    {
        EditText T = (EditText) getView().findViewById(R.id.editChatText);
        if (T != null)
        {
            SavedText = T.getText().toString();
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

    private LinearLayout Bottom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RootView = inflater.inflate(R.layout.fragment_chat, container, false);
        Bottom = (LinearLayout) RootView.findViewById(R.id.ChatBottom);

        setHasOptionsMenu(true);
        MessageWindow = (ListView) RootView.findViewById(R.id.ChatList);
        ListView MessageTypingWindow = (ListView) RootView.findViewById(R.id.ChatTyping);

        if(CacheChats.Loaded.get(((ChatActivity) getActivity()).CurrentChat) == null) {
            CacheChats.StartListen(((ChatActivity) getActivity()).CurrentChat);
        }

        String Chat = ((ChatActivity) getActivity()).CurrentChat;
        Adapter = new ChatAdapter(getContext(), R.layout.list_item_message, Chat);
        TypingAdapter = new ChatTypingAdapter(getContext(), R.layout.list_item_message, Chat, new ArrayList<CacheChats.MessageStructure>());
        MessageWindow.setAdapter(Adapter);
        MessageTypingWindow.setAdapter(TypingAdapter);

        List = (ListView) RootView.findViewById(R.id.ChatList);
        List.setSelection(Adapter.getCount() - 1);
        List.setDivider(null);

        SetSubbed(((ChatActivity)getActivity()).isSubbed());

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
                .addData("date", String.valueOf(System.currentTimeMillis() / 1000))
                .addData("text", Text)
                .build());

        Log.d("SendMessageToServer", "Message sent " + Text);
        Input.setText("");

    }

    public void SetSubbed(boolean Subbed){
        if(this.getContext() != null) {
            if (Subbed) {
                EditText Typer = new EditText(getContext());
                Typer.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));
                Typer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                Typer.setId(R.id.editChatText);
                Typer.setHint("Type your message...");
                Typer.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
                Typer.setPadding(16, 36, 16, 36);
                Typer.setGravity(Gravity.CENTER_VERTICAL);
                Typer.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (MainActivity.Typing)
                        {
                            String Text = s.toString().trim();
                            if (Text.length() != 0 && s.charAt(s.length() - 1) == ' ') {
                                if (Text.length() > 1024) {
                                    Text = Text.substring(0, 1021) + "...";
                                }

                                FirebaseMessaging fm = FirebaseMessaging.getInstance();
                                fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                                        .addData("chat", ((ChatActivity) getActivity()).CurrentChat)
                                        .addData("type", "typing")
                                        .addData("date", String.valueOf(System.currentTimeMillis() / 1000))
                                        .addData("text", Text)
                                        .build());
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                Button SendButton = new Button(this.getContext());
                SendButton.setLayoutParams(new LinearLayout.LayoutParams((int) getResources().getDimension(R.dimen.send_button_width), ViewGroup.LayoutParams.WRAP_CONTENT, 0f));
                SendButton.setText("Send");
                SendButton.setOnClickListener(new Button.OnClickListener() {
                    public void onClick(View v) {
                        SendMessage();
                    }
                });

                Bottom.removeAllViews();
                Bottom.addView(Typer);
                Bottom.addView(SendButton);

                setHasOptionsMenu(true);
            } else {
                Button SubButton = new Button(Bottom.getContext());
                SubButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                SubButton.setText("Subscribe to chat");
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