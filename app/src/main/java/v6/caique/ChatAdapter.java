package v6.caique;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

class ChatAdapter extends ArrayAdapter<CacheChats.MessageStructure> {

    private String ChatId;
    private LayoutInflater vi;
    private Context context;

    public ChatAdapter(Context c, @LayoutRes int resource, String ChatId) {
        super(c, resource, CacheChats.Loaded.get(ChatId).Messages);
        this.ChatId = ChatId;
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        if (row == null)
        {
            row = vi.inflate(R.layout.chat_message, parent, false);
        }

        CacheChats.ChatStructure Chat = CacheChats.Loaded.get(ChatId);
        if (Chat.Messages.size() > position)
        {
            //final CircleImageView imageView = (CircleImageView) row.findViewById(R.id.userdp);
            TextView MessageSender = (TextView) row.findViewById(R.id.messageItemSender);
            TextView Message = (TextView) row.findViewById(R.id.messageItem);

            CacheChats.MessageStructure Data = Chat.Messages.get(position);

            //String pic = DatabaseCache.GetUserPicUrl(SenderId, null);

            //if (pic != null && !pic.isEmpty())
            {
                //final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("users/" + SenderId);
                //imageView.setImageBitmap(null);

                /*storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        Glide.with(context)
                                .using(new FirebaseImageLoader())
                                .load(storageRef)
                                .into(imageView);
                    }
                });*/
            }

            if (position != 0 && Data.Sender.equals(Chat.Messages.get(position - 1).Sender))
            {
                //imageView.setVisibility(INVISIBLE);
                //imageView.setMaxHeight(1);
                MessageSender.setVisibility(INVISIBLE);
            }
            else
            {
                //imageView.setVisibility(VISIBLE);
                //imageView.setMaxHeight(9999);
                MessageSender.setVisibility(VISIBLE);
            }

            MessageSender.setText(CacheChats.Name(Data.Sender, "Loading.."));
            Message.setText(Data.Content);
        }

        return row;
    }
}