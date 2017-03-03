package v6.caique;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;

public class MusicPlayerFragment extends Fragment {
    static class SongStructure {
        public String SongName;
        public String Chat;
        public int Index;
    }

    public static ArrayList<SongStructure> Songs = new ArrayList<>();

    private OnFragmentInteractionListener mListener;
    public MusicAdapter Adapter;
    private View RootView;
    private ListView SongQueue;
    private String ChatId = new String();

    public MusicPlayerFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChatId = ((ChatActivity)getActivity()).CurrentChat;
    }

    public void ReloadViews()
    {

        Songs.clear();
        ArrayList<String> PlaylistTemp = ((ChatActivity)getActivity()).Playlist;
        if(PlaylistTemp != null) {
            if (PlaylistTemp.size() > 0) {
                for (String Song : PlaylistTemp) {

                    SongStructure SongStruct = new SongStructure();
                    SongStruct.Chat = ChatId;
                    SongStruct.Index = Songs.size();
                    SongStruct.SongName = Song;

                    Songs.add(SongStruct);
                }
            }
        }
        Adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    private int Visibility = View.VISIBLE;
    public void SetSubbed(boolean Subbed)
    {
        if (Subbed)
        {
            Visibility = View.VISIBLE;
        }
        else
        {
            Visibility = View.GONE;
        }

        if (getView() != null)
        {
            getView().findViewById(R.id.music_frame).setVisibility(Visibility);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RootView = inflater.inflate(R.layout.fragment_music_player, container, false);
        RootView.findViewById(R.id.music_frame).setVisibility(Visibility);

        setHasOptionsMenu(true);

        Songs.clear();
        for (String Song : ((ChatActivity)getActivity()).Playlist) {

            SongStructure SongStruct = new SongStructure();
            SongStruct.Chat = ChatId;
            SongStruct.Index = Songs.size();
            SongStruct.SongName = Song;

            Songs.add(SongStruct);
        }

        SetCurrentlyPlaying(((ChatActivity)getActivity()).CurrentSong);

        SongQueue = (ListView) RootView.findViewById(R.id.SongQueue);
        Adapter = new MusicAdapter(this.getActivity(), R.layout.song_queue_item);
        SongQueue.setAdapter(Adapter);

        return RootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.music_player, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void SetCurrentlyPlaying(String SongPlaying){
        if(getActivity() != null) {
            TextView CurrentSong = (TextView) RootView.findViewById(R.id.CurrentSong);
            Button SkipButton = (Button) RootView.findViewById(R.id.SkipButton);
            CurrentSong.setText(SongPlaying);
            if (SongPlaying == null) {
                SkipButton.setVisibility(View.INVISIBLE);
            } else {
                SkipButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void SendMusic() {
        String Date = String.valueOf(System.currentTimeMillis() / 1000);

        EditText Input = (EditText) RootView.findViewById(R.id.editText2);
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
                .addData("chat", ChatId)
                .addData("type", "madd")
                .addData("date", Date)
                .addData("text", Text)
                .build());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(getActivity() != null) {
                    FirebaseMessaging fm = FirebaseMessaging.getInstance();
                    fm.send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                            .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                            .addData("chat", ChatId)
                            .addData("type", "mqueue")
                            .addData("text", "")
                            .build());
                }
            }
        });

        Log.d("SendMessageToServer", "Music message sent " + Text);
        Input.setText("");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onMusicPlayerFragmentInteraction(Uri uri);
    }
}
