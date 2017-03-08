package v6.caique;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.solver.Cache;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

public class ExploreFragment extends Fragment {
    public static ArrayList<String> Chats = new ArrayList<>();
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
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RootView = inflater.inflate(R.layout.fragment_explore, container, false);

        final LinearLayout TagsList = (LinearLayout) RootView.findViewById(R.id.TagsList);

        FirebaseDatabase.getInstance().getReference().child("tags").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        final String t = dataSnapshot.getKey();

                        View row = inflater.inflate(R.layout.list_item_tag, TagsList, false);
                        CheckBox Box = (CheckBox) row.findViewById(R.id.checkBox);
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

                        TagsList.addView(row);
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        Button b = (Button) RootView.findViewById(R.id.SearchButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseMessaging.getInstance().send(new RemoteMessage.Builder(getString(R.string.gcm_defaultSenderId) + "@gcm.googleapis.com")
                        .setMessageId(Integer.toString(FirebaseIDService.msgId.incrementAndGet()))
                        .addData("type", "searchtag")
                        .addData("text", TextUtils.join(",", Tags))
                        .build());
            }
        });

        return RootView;
    }

    public void ReloadChats()
    {
        if (mListener != null)
        {
            final Context context = getContext();
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            LinearLayout TagsList = (LinearLayout) getView().findViewById(R.id.ExploreChatList);

            int ScrollX = TagsList.getScrollX();
            int ScrollY = TagsList.getScrollY();

            TagsList.removeAllViews();

            for (final String ChatId : Chats)
            {
                if (CacheChats.Loaded.containsKey(ChatId))
                {
                    CacheChats.ChatStructure Chat = CacheChats.Loaded.get(ChatId);
                    View row = vi.inflate(R.layout.list_item_chat, TagsList, false);

                    final ImageView imageView = (ImageView) row.findViewById(R.id.chatdp);
                    imageView.setImageDrawable(null);

                    final StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://firebase-caique.appspot.com").child("chats/" + ChatId);
                    try
                    {
                        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                            @Override
                            public void onSuccess(StorageMetadata storageMetadata) {
                                try {
                                    Glide.with(context)
                                            .using(new FirebaseImageLoader())
                                            .load(storageRef)
                                            .centerCrop()
                                            .bitmapTransform(new CropCircleTransformation(context))
                                            .signature(new StringSignature(String.valueOf(storageMetadata.getCreationTimeMillis())))
                                            .into(imageView);
                                }
                                catch (Exception e)
                                {
                                }
                            }
                        });
                    }
                    catch (Exception e)
                    {
                    }

                    TextView nameTextView = (TextView) row.findViewById(R.id.itemname);
                    nameTextView.setText(Chat.Title);

                    TextView descTextView = (TextView) row.findViewById(R.id.itemdesc);
                    descTextView.setText("Tagged " + TextUtils.join(", ", Chat.Tags));

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

                    TagsList.addView(row);
                }
            }

            TagsList.scrollTo(ScrollX, ScrollY);
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
        Chats.clear();
        //Chats.clear();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
