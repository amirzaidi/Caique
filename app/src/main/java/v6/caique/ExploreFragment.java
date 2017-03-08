package v6.caique;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ExploreFragment extends Fragment {
    /*public static class ChatStructure{
        String ID;
        String Title;
        ArrayList<String> Tags;
        Query DataQuery;
        ValueEventListener DataListener;
    }*/

    //public static HashMap<String, ChatStructure> Chats = new HashMap<>();
    private ArrayList<String> ChatIDs = new ArrayList<>();
    private ArrayList<String> Tags = new ArrayList<>();

    private OnFragmentInteractionListener mListener;
    private ExploreAdapter ChatsAdapter;
    private View RootView;

    public ExploreFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RootView = inflater.inflate(R.layout.fragment_explore, container, false);

        //ListView ChatList = (ListView) RootView.findViewById(R.id.ChatList);
        //ChatsAdapter = new ExploreAdapter(getActivity(), ChatIDs);
        //ChatList.setAdapter(ChatsAdapter);

        FirebaseDatabase.getInstance().getReference().child("tags").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                getActivity().runOnUiThread(new Runnable() {
                    final LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    final LinearLayout TagsList = (LinearLayout) getView().findViewById(R.id.TagsList);

                    @Override
                    public void run() {
                        HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                        for(final String t : Data.keySet()){
                            View Inflated = vi.inflate(R.layout.list_item_tag, TagsList, false);
                            CheckBox Box = (CheckBox) Inflated.findViewById(R.id.checkBox);
                            Box.setText(t.substring(0,1).toUpperCase() + t.substring(1).toLowerCase());
                            Box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                    if (isChecked && !Tags.contains(t))
                                    {
                                        Tags.add(t);
                                    }
                                    else if (!isChecked && Tags.contains(t))
                                    {
                                        Tags.remove(t);
                                    }
                                }
                            });

                            TagsList.addView(Inflated);

                        }
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        return RootView;
    }

    public void RequestTags(View Button){

        FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                .addData("type", "searchtag")
                .addData("text", TextUtils.join(",", Tags))
                .build());


        //Chats.clear();
/*
        Integer Iterator = 0;
        for(String s: Tags){
            if(TagsAdapter.Tags.get(Iterator).isChecked()){

                if(!CheckedTags.contains(TagsAdapter.Tags.get(Iterator))){
                    CheckedTags.add(TagsAdapter.Tags.get(Iterator).getText().toString());
                }

                Query DataQuery = Database.child("tags").child(s);
                ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                        for(Map.Entry<String, Object> entry : Data.entrySet()){

                            if(!TagRelevancy.containsKey(entry.getKey())) {
                                TagRelevancy.put(entry.getKey(), 1);
                            }
                            else {
                                TagRelevancy.put(entry.getKey(), TagRelevancy.get(entry.getKey()) + 1);
                            }
                        }

                        TagIteration++;
                        if(TagIteration == CheckedTags.size()){
                            TagIteration = 0;
                            CheckedTags.clear();
                            PrepareRelevancy();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };

                DataQuery.addValueEventListener(listener);
            }

            Iterator++;



    }

    private void PrepareRelevancy(){

        Object[] obj = TagRelevancy.entrySet().toArray();
        Arrays.sort(obj, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Map.Entry<String, Integer>) o2).getValue()
                        .compareTo(((Map.Entry<String, Integer>) o1).getValue());
            }
        });

        TagRelevancy.clear();
        for(Object o: obj){
            ChatStructure Chat = new ChatStructure();
            Chat.ID = ((Map.Entry<String, Integer>) o).getKey();
            Chats.put(((Map.Entry<String, Integer>) o).getKey(), Chat);
            ChatIDs.add(((Map.Entry<String, Integer>) o).getKey());
        }

        GetChatData();
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
                    ReloadViews(s);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            Chats.get(s).DataQuery.addValueEventListener(Chats.get(s).DataListener);

        }}*/

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
        //Chats.clear();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
