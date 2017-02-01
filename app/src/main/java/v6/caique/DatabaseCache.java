package v6.caique;


import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DatabaseCache {
    public static HashMap<String, HashMap<String, Object>> UserData = new HashMap<>();
    public static HashMap<String, HashMap<String, Object>> ChatData = new HashMap<>();
    private static DatabaseReference Database = FirebaseDatabase.getInstance().getReference();
    private static AtomicInteger Ids = new AtomicInteger(0);
    private static HashMap<Integer, QueryStructure> CancelQueries = new HashMap<>();

    static class QueryStructure
    {
        public Query Query;
        public ValueEventListener Listener;
    }

    public static String GetUserName(String UserId, String Alt)
    {
        if (UserData.containsKey(UserId))
        {
            return (String)UserData.get(UserId).get("name");
        }

        return Alt;
    }

    public static String GetUserPicUrl(String UserId, String Alt)
    {
        if (UserData.containsKey(UserId))
        {
            return (String)UserData.get(UserId).get("picture");
        }

        return Alt;
    }

    public static String GetChatName(String ChatId, String Alt)
    {
        if (ChatData.containsKey(ChatId))
        {
            return (String)ChatData.get(ChatId).get("title");
        }

        return Alt;
    }

    public static String GetChatPicUrl(String ChatId, String Alt)
    {
        if (ChatData.containsKey(ChatId))
        {
            return (String)ChatData.get(ChatId).get("picture");
        }

        return Alt;
    }

    public static ArrayList<String> GetChatTags(String ChatId, ArrayList<String> Alt)
    {
        if (ChatData.containsKey(ChatId))
        {
            return (ArrayList<String>)ChatData.get(ChatId).get("tags");
        }

        return Alt;
    }

    public static int LoadUserData(final String UserId, final Runnable OnUpdate)
    {
        return MakeCancelQuery(Database.child("user").child(UserId).child("data"), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData.put(UserId, (HashMap<String, Object>) dataSnapshot.getValue());
                OnUpdate.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void LoadUserDataOnce(final String UserId, final Runnable OnUpdate)
    {
        Database.child("user").child(UserId).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData.put(UserId, (HashMap<String, Object>) dataSnapshot.getValue());
                OnUpdate.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static int LoadChatData(final String ChatId, final Runnable OnUpdate)
    {
        return MakeCancelQuery(Database.child("chat").child(ChatId).child("data"), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatData.put(ChatId, (HashMap<String, Object>) dataSnapshot.getValue());
                OnUpdate.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static void LoadChatDataOnce(final String ChatId, final Runnable OnUpdate)
    {
        Database.child("chat").child(ChatId).child("data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatData.put(ChatId, (HashMap<String, Object>) dataSnapshot.getValue());
                OnUpdate.run();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private static int MakeCancelQuery(Query Query, ValueEventListener Listener)
    {
        QueryStructure QS = new QueryStructure();
        QS.Query = Query;
        QS.Listener = Listener;

        int I = Ids.incrementAndGet();
        CancelQueries.put(I, QS);
        QS.Query.addValueEventListener(QS.Listener);

        return I;
    }

    public static void Cancel(int Id)
    {
        QueryStructure QS = CancelQueries.remove(Id);
        QS.Query.removeEventListener(QS.Listener);
    }
}
