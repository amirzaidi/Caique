package v6.caique;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class Adapter extends ArrayAdapter<String> {

    ArrayList<String> MessageArray;

    public Adapter(Context context, @LayoutRes int resource, ArrayList<String> data) {
        super(context, resource, data);

        this.MessageArray = data;
    }

    @Override

    public View getView(int position, View convertView, ViewGroup parent) {
        // Check if an existing view is being reused, otherwise inflate the view

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.chat_message, parent, false);
            TextView Message = (TextView) convertView.findViewById(R.id.messageItem);
            Message.setText(MessageArray.get(position));
        }
        else{
            TextView Message = (TextView) convertView.findViewById(R.id.messageItem);
            Message.setText(MessageArray.get(position));
        }

        // Return the completed view to render on screen
        return convertView;
    }
}