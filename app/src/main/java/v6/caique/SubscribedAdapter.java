package v6.caique;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SubscribedAdapter extends ArrayAdapter<String> {

    private LayoutInflater vi;
    private Context context;
    private ArrayList<String> Items;

    public SubscribedAdapter(Context c, ArrayList<String> Items)
    {
        super(c, R.layout.list_item_chat, Items);
        this.Items = Items;
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        Log.d("Position", position + " ");

        if (row == null) {
            row = vi.inflate(R.layout.list_item_chat, null);
        }

        if (Items.size() > position)
        {
            final String ChatId = Items.get(position);

            TextView nameTextView = (TextView) row.findViewById(R.id.itemname);
            nameTextView.setText(DatabaseCache.GetChatName(ChatId, "Loading"));

            ArrayList<String> Tags = DatabaseCache.GetChatTags(ChatId, new ArrayList<String>());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < Tags.size(); i++)
            {
                if (i != 0)
                {
                    sb.append(", ");
                }

                sb.append(Tags.get(i));
            }

            TextView descTextView = (TextView) row.findViewById(R.id.itemdesc);
            descTextView.setText("Tags: " + sb.toString());

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
