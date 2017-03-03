package v6.caique;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExploreFragment extends Fragment {


    public static class ChatStructure{
        String ID;
        String Title;
        ArrayList<String> Tags;
        Query DataQuery;
        ValueEventListener DataListener;
    }

    public static HashMap<String, ChatStructure> Chats = new HashMap<>();
    public static ArrayList<String> ChatIDs = new ArrayList<>();

    private OnFragmentInteractionListener mListener;
    private ExploreAdapter Adapter;
    private View RootView;
    private DatabaseReference Database = FirebaseDatabase.getInstance().getReference();

    public ExploreFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RootView =inflater.inflate(R.layout.fragment_explore, container, false);

        ListView ChatList = (ListView) RootView.findViewById(R.id.ChatList);
        Adapter = new ExploreAdapter(this.getActivity());
        ChatList.setAdapter(Adapter);

        final Query DataQuery = Database.child("chat").limitToFirst(10);
        final ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                for(Map.Entry<String, Object> entry : Data.entrySet()){

                    ChatStructure Chat = new ChatStructure();
                    Chat.ID = entry.getKey();
                    Chats.put(entry.getKey(), Chat);
                    ChatIDs.add(entry.getKey());
                }
                GetChatData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        DataQuery.addValueEventListener(listener);

        return RootView;
    }

    private void ReloadViews(){
        Adapter.notifyDataSetChanged();

        for(String s: ChatIDs)  {
            if(Chats.get(s).DataQuery != null) {
                Chats.get(s).DataQuery.removeEventListener(Chats.get(s).DataListener);
            }
        }
    }

    private void GetChatData(){
        for(final String s: ChatIDs) {
            Chats.get(s).DataQuery = Database.child("chat").child(s).child("data");
            Chats.get(s).DataListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                    Chats.get(s).Title = (String) Data.get("title");
                    Chats.get(s).Tags = (ArrayList<String>) Data.get("tags");
                    ReloadViews();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            Chats.get(s).DataQuery.addValueEventListener(Chats.get(s).DataListener);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ChatIDs.clear();
        Chats.clear();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
