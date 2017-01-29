package v6.caique;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

class ChatAdapter extends ArrayAdapter<HashMap<String, String>> {

    ArrayList<HashMap<String, String>> MessageArray;

    public ChatAdapter(Context context, @LayoutRes int resource, ArrayList<HashMap<String, String>> MessageArray) {
        super(context, resource, MessageArray);
        this.MessageArray = MessageArray;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_message, parent, false);
        }

        if (MessageArray.size() > position)
        {
            TextView MessageSender = (TextView) convertView.findViewById(R.id.messageItemSender);
            TextView Message = (TextView) convertView.findViewById(R.id.messageItem);

            HashMap<String, String> Data = MessageArray.get(position);
            String SenderId = Data.get("sender");

            if (position != 0 && SenderId.equals(MessageArray.get(position - 1).get("sender")))
            {
                MessageSender.setVisibility(INVISIBLE);
            }
            else
            {
                MessageSender.setVisibility(VISIBLE);
            }

            MessageSender.setText(DatabaseCache.GetUserName(SenderId, "Loading.."));
            Message.setText(Data.get("text"));
        }

        return convertView;
    }
}