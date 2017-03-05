package v6.caique;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class FavoritesAdapter extends ArrayAdapter<String>{
    private LayoutInflater vi;
    private Context context;

    public FavoritesAdapter(Context c){
        super(c, R.layout.list_item_chat, FavoritesFragment.ChatIDs);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        if (row == null) {
            row = vi.inflate(R.layout.list_item_chat, null);
        }

        if (FavoritesFragment.FavoriteChats.size() > position)
        {
            final String ChatId = FavoritesFragment.ChatIDs.get(position);
            final CacheChats.ChatStructure Chat = FavoritesFragment.FavoriteChats.get(ChatId);

            final ImageView imageView = (ImageView) row.findViewById(R.id.chatdp);
            imageView.setImageDrawable(null);

            final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("chats/" + ChatId);
            storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                @Override
                public void onSuccess(StorageMetadata storageMetadata) {
                    Glide.with(context)
                            .using(new FirebaseImageLoader())
                            .load(storageRef)
                            .centerCrop()
                            .bitmapTransform(new CropCircleTransformation(context))
                            .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                            .into(imageView);
                }
            });

            if(Chat != null) {
                TextView nameTextView = (TextView) row.findViewById(R.id.itemname);
                TextView descTextView = (TextView) row.findViewById(R.id.itemdesc);
                nameTextView.setText(Chat.Title);
                descTextView.setText("");
                if(Chat.Messages.size() > 0) {
                    if (Chat.Messages.get(Chat.Messages.size() - 1).Content.length() > 100) {
                        descTextView.setText(Chat.Messages.get(Chat.Messages.size() - 1).Content.substring(0, 97) + "...");
                    } else {
                        descTextView.setText(Chat.Messages.get(Chat.Messages.size() - 1).Content);
                    }
                }
            }


            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newChatActivity = new Intent(MainActivity.Instance, ChatActivity.class);
                    Bundle b = new Bundle();
                    b.putString("chat", ChatId);
                    newChatActivity.putExtras(b);
                    context.startActivity(newChatActivity);
                }
            });
        }

        return row;
    }
}
