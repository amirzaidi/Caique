package v6.caique;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

public class ChatFragment extends Fragment {

    private ChatFragment.OnFragmentInteractionListener mListener;
    public ChatAdapter Adapter;
    private ListView MessageWindow;
    private View RootView;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RootView = inflater.inflate(R.layout.fragment_chat, container, false);

        Toolbar toolbar = (Toolbar) RootView.findViewById(R.id.ChatToolbar);
        Button button = new Button(this.getContext());
        button.setText("Music");
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        toolbar.addView(button, params);

        MessageWindow = (ListView) RootView.findViewById(R.id.ChatList);
        Adapter = new ChatAdapter(this.getActivity(), R.layout.chat_message, ((ChatActivity)getActivity()).CurrentChat);
        MessageWindow.setAdapter(Adapter);

        return RootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatFragment.OnFragmentInteractionListener) {
            mListener = (ChatFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void SendMessage() {
        String Date = String.valueOf(System.currentTimeMillis() / 1000);

        EditText Input = (EditText) RootView.findViewById(R.id.editText);
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

}
