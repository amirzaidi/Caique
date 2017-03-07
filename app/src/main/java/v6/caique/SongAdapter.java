package v6.caique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SongAdapter extends ArrayAdapter<String> {
    private LayoutInflater vi;
    private Context context;
    private String ID;

    public SongAdapter(Context c, String ChatID)
    {
        super(c, R.layout.song_select_item, MusicPlayerFragment.SelectionUrls);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
        ID = ChatID;
    }

    @Override
    public View getView(final int position, View row, ViewGroup parent) {

        if (row == null) {
            row = vi.inflate(R.layout.song_select_item, null);
        }

        if (MusicPlayerFragment.SelectionUrls.size() > position)
        {
            TextView Song = (TextView) row.findViewById(R.id.SongToSelect);
            Song.setText(MusicPlayerFragment.SelectionNames.get(position));
            Song.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatActivity.Instances.get(ID).MusicPlayer.SelectMusic(MusicPlayerFragment.SelectionUrls.get(position));
                }
            });
        }

        return row;
    }
}
