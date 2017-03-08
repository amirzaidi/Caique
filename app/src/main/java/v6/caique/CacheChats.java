package v6.caique;

import android.os.AsyncTask;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class CacheChats {
    static class MessageStructure {
        public long Date;
        public String Sender;
        public String Content;
        public Boolean IsFile = false;
    }

    static class ChatStructure {
        public Query DataQuery;
        public ValueEventListener DataListener;
        public Query MessageQuery;
        public ChildEventListener MessageListener;
        public Query RankQuery;
        public ChildEventListener RankListener;
        public String Title = "";
        public ArrayList<String> Tags = new ArrayList<>();
        public LinkedList<MessageStructure> Messages = new LinkedList<>();
        public HashMap<String, MessageStructure> Typing = new HashMap<>();
        public HashMap<String, Integer> Ranks = new HashMap<>();
    }

    private static int MaxMsgs = 50;
    private static DatabaseReference Database = FirebaseDatabase.getInstance().getReference();
    private static String GID;
    private static Query SubsQuery;
    private static ChildEventListener SubsListener;
    public static LinkedList<String> Subs = new LinkedList<>();
    public static HashMap<String, ChatStructure> Loaded = new HashMap<>();
    private static HashMap<String, String> Names = new HashMap<>();

    public static void Restart(String NewGoogleID)
    {
        if (GID != NewGoogleID)
        {
            GID = NewGoogleID;

            if (SubsQuery != null && SubsListener != null)
            {
                SubsQuery.removeEventListener(SubsListener);
            }

            Subs.clear();
            FilterSubs();

            SubsQuery = Database.child("user").child(GID).child("member");
            SubsListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    final String ChatId = dataSnapshot.getKey();

                    Subs.add(ChatId);
                    StartListen(ChatId);
                    UpdateMainActivity();

                    if (ChatActivity.Instances.containsKey(ChatId))
                    {
                        ChatActivity.Instances.get(ChatId).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ChatActivity.Instances.get(ChatId).SetSubbed(true);
                            }
                        });
                    }

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            CloudMessageService.Sub(ChatId);
                        }
                    });
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //Impossible for now
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Subs.remove(dataSnapshot.getKey());
                    UpdateMainActivity();

                    final String ChatId = dataSnapshot.getKey();
                    if (ChatActivity.Instances.containsKey(ChatId))
                    {
                        ChatActivity.Instances.get(ChatId).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ChatActivity.Instances.get(ChatId).SetSubbed(false);
                            }
                        });
                    }

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            CloudMessageService.Unsub(ChatId);
                        }
                    });
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            SubsQuery.addChildEventListener(SubsListener);
        }
    }

    public static void StartListen(final String ChatId)
    {
        if (!Loaded.containsKey(ChatId))
        {
            final ChatStructure Chat = new ChatStructure();

            Chat.DataQuery = Database.child("chat").child(ChatId).child("data");
            Chat.DataListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    HashMap<String, Object> Data = (HashMap<String, Object>) dataSnapshot.getValue();
                    if (Data != null)
                    {
                        Chat.Title = (String) Data.get("title");
                        Chat.Tags = new ArrayList<>();

                        ArrayList<String> Tags = (ArrayList<String>) Data.get("tags");
                        for (String Tag : Tags)
                        {
                            if (Tag != null)
                            {
                                Chat.Tags.add(Tag);
                            }
                        }

                        UpdateChatDisplay(ChatId);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            Chat.MessageQuery = Database.child("chat").child(ChatId).child("message").limitToLast(MaxMsgs);
            Chat.MessageListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    while (Chat.Messages.size() >= MaxMsgs)
                    {
                        Chat.Messages.removeFirst();
                    }

                    HashMap<String, Object> snapshotValue = (HashMap<String, Object>) dataSnapshot.getValue();

                    MessageStructure Message = new MessageStructure();
                    Message.Date = (long) snapshotValue.get("date");
                    Message.Sender = (String) snapshotValue.get("sender");
                    Message.Content = (String) snapshotValue.get("text");
                    Message.IsFile = !"text".equals(snapshotValue.get("type"));

                    Name(Message.Sender);
                    Chat.Messages.addLast(Message);

                    if (Chat.Typing.containsKey(Message.Sender))
                    {
                        Chat.Typing.remove(Message.Sender);
                    }

                    UpdateChatDisplay(ChatId);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    //UpdateChatDisplay(ChatId);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    //UpdateChatDisplay(ChatId);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            Chat.RankQuery = Database.child("chat").child(ChatId).child("ranks");
            Chat.RankListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Name(dataSnapshot.getKey());
                    Chat.Ranks.put(dataSnapshot.getKey(), (int)dataSnapshot.getValue());
                    UpdateChatDisplay(ChatId);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Name(dataSnapshot.getKey());
                    Chat.Ranks.put(dataSnapshot.getKey(), (int)dataSnapshot.getValue());
                    UpdateChatDisplay(ChatId);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Name(dataSnapshot.getKey());
                    Chat.Ranks.remove(dataSnapshot.getKey());
                    UpdateChatDisplay(ChatId);
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };

            Chat.DataQuery.addValueEventListener(Chat.DataListener);
            Chat.MessageQuery.addChildEventListener(Chat.MessageListener);
            Chat.RankQuery.addChildEventListener(Chat.RankListener);

            Loaded.put(ChatId, Chat);
        }
    }

    public static void FilterSubs()
    {
        try {
            for (String ChatId : Loaded.keySet()) {
                if (!Subs.contains(ChatId)) {
                    ChatStructure Chat = Loaded.remove(ChatId);

                    Chat.DataQuery.removeEventListener(Chat.DataListener);
                    Chat.MessageQuery.removeEventListener(Chat.MessageListener);
                    Chat.RankQuery.removeEventListener(Chat.RankListener);
                }
            }
        }
        catch (Exception e){

        }
    }

    public static String Name(final String Id)
    {
        if (!Names.containsKey(Id))
        {
            Names.put(Id, null);

            Database.child("user").child(Id).child("data").child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Names.put(Id, (String) dataSnapshot.getValue());

                    if (Id == GID && MainActivity.Instance != null)
                    {
                        MainActivity.Instance.ReloadNavbar();
                    }

                    UpdateAllChats();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Names.remove(Id);
                }
            });

            return Id;
        }

        String Name = Names.get(Id);
        if (Name == null)
        {
            Name = Id;
        }

        return Name;
    }
    private static void UpdateMainActivity()
    {
        UpdateChatDisplays(new String[] {});
    }

    private static void UpdateAllChats()
    {
        UpdateChatDisplays(Loaded.keySet().toArray(new String[Loaded.size()]));
    }

    private static void UpdateChatDisplay(String ChatId)
    {
        UpdateChatDisplays(new String[] { ChatId });
    }

    private static void UpdateChatDisplays(String[] ChatIds)
    {
        if (MainActivity.Instance != null)
        {
            MainActivity.Instance.Subs.Adapter.notifyDataSetChanged();
            MainActivity.Instance.Explore.ReloadChats();

            for (String ChatId : ChatIds)
            {
                if (ChatActivity.Instances.containsKey(ChatId))
                {
                    ChatActivity.Instances.get(ChatId).ReloadChatViews(true, true);
                }
            }
        }
    }
}
