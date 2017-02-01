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

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

class ChatAdapter extends ArrayAdapter<HashMap<String, String>> {

    ArrayList<HashMap<String, String>> MessageArray;
    private LayoutInflater vi;
    private Context context;

    public ChatAdapter(Context c, @LayoutRes int resource, ArrayList<HashMap<String, String>> MessageArray) {
        super(c, resource, MessageArray);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.MessageArray = MessageArray;
        this.context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        if (row == null)
        {
            row = vi.inflate(R.layout.chat_message, parent, false);
        }

        if (MessageArray.size() > position)
        {
            //final CircleImageView imageView = (CircleImageView) row.findViewById(R.id.userdp);
            TextView MessageSender = (TextView) row.findViewById(R.id.messageItemSender);
            TextView Message = (TextView) row.findViewById(R.id.messageItem);

            HashMap<String, String> Data = MessageArray.get(position);
            String SenderId = Data.get("sender");

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

            if (position != 0 && SenderId.equals(MessageArray.get(position - 1).get("sender")))
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

            MessageSender.setText(DatabaseCache.GetUserName(SenderId, "Loading.."));
            Message.setText(Data.get("text"));
        }

        return row;
    }
}