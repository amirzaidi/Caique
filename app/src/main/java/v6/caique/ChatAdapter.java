package v6.caique;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.concurrent.RejectedExecutionException;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

import static android.view.View.GONE;
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
            final ImageView imageView = (ImageView) row.findViewById(R.id.userdp);
            TextView MessageSender = (TextView) row.findViewById(R.id.messageItemSender);
            TextView Message = (TextView) row.findViewById(R.id.messageItem);

            CacheChats.MessageStructure Data = Chat.Messages.get(position);
            Message.setText(Data.Content);

            Boolean HidePic = false;

            if (position != 0)
            {
                CacheChats.MessageStructure Previous = Chat.Messages.get(position - 1);
                if (Data.Sender.equals(Previous.Sender) && Math.floor((double)Previous.Date / 60) == Math.floor((double)Data.Date / 60))
                {
                    HidePic = true;
                }
            }

            if (HidePic)
            {
                MessageSender.setVisibility(GONE);
                imageView.getLayoutParams().height = 0;
            }
            else
            {
                Date d = new Date(Data.Date*1000L);

                MessageSender.setVisibility(VISIBLE);
                MessageSender.setText(CacheChats.Name(Data.Sender, "Unknown") + " [" + DateFormat.getTimeFormat(context).format(d) + " " + DateFormat.getDateFormat(context).format(d) + "]");
                imageView.getLayoutParams().height = imageView.getLayoutParams().width;

                imageView.setImageDrawable(null);

                final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("users/" + Data.Sender);
                try {
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
                }
                catch (RejectedExecutionException e)
                {
                    Log.d("ChatAdapter", "Rejected StorageMetadata: " + e.getMessage());
                }
            }

            imageView.requestLayout();

        }

        return row;
    }
}