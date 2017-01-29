package v6.caique;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapterMaterial extends BaseAdapter {
    public static class Str2D
    {
        public String Id;
        public String Name;
    }

    private LayoutInflater vi;
    private Context context;
    private ArrayList<Str2D> Items = new ArrayList<>();

    public ListAdapterMaterial(Context c)
    {
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    public void add(Str2D item) {
        Items.add(item);
        notifyDataSetChanged();
    }

    public void clear() {
        Items.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return Items.size();
    }

    @Override
    public Str2D getItem(int position) {
        return Items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        Log.d("Position", position + " ");

        if (row == null) {
            row = vi.inflate(R.layout.list_item_chat, null);
        }

        if (Items.size() > position)
        {
            final Str2D rowData = Items.get(position);

            TextView nameTextView = (TextView) row.findViewById(R.id.itemname);
            nameTextView.setText(rowData.Name);

            TextView descTextView = (TextView) row.findViewById(R.id.itemdesc);
            descTextView.setText(rowData.Id);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent newChatActivity = new Intent(MainActivity.Instance, ChatActivity.class);
                    Bundle b = new Bundle();
                    b.putString("chat", rowData.Id);
                    newChatActivity.putExtras(b);
                    context.startActivity(newChatActivity);
                }
            });
        }

        return row;
    }
}
