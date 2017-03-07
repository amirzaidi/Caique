package v6.caique;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class InviteAdapter extends ArrayAdapter<String> {
    private LayoutInflater vi;
    private Context context;

    public InviteAdapter(Context c)
    {
        super(c, R.layout.contact_item, InviteActivity.ContactNames);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public View getView(final int position, View row, ViewGroup parent) {

        if (row == null) {
            row = vi.inflate(R.layout.contact_item, null);
        }

        if (InviteActivity.ContactNumbers.size() > position)
        {
            TextView DisplayName = (TextView) row.findViewById(R.id.DisplayName);
            DisplayName.setText(InviteActivity.ContactNames.get(position));

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String number = InviteActivity.ContactNumbers.get(position);
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + Uri.encode(number)));
                    intent.putExtra("sms_body", "Try out Caique! It's an awesome way to get in contact with people all over the world!");
                    context.startActivity(intent);
                }
            });
        }

        return row;
    }
}
