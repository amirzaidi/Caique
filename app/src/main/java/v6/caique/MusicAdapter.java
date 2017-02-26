package v6.caique;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MusicAdapter extends ArrayAdapter<MusicPlayerFragment.SongStructure> {

    private LayoutInflater vi;
    private Context context;

    public MusicAdapter(Context c, @LayoutRes int resource) {
        super(c, resource, MusicPlayerFragment.Songs);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        if (row == null)
        {
            row = vi.inflate(R.layout.song_queue_item, parent, false);
        }

        if (MusicPlayerFragment.Songs.size() > position)
        {
            TextView Song = (TextView) row.findViewById(R.id.SongItem);

            String SongName = MusicPlayerFragment.Songs.get(position).SongName;

            Song.setText(SongName);
        }
        else if (MusicPlayerFragment.Songs.size() == 0){
            return row;
        }

        return row;
    }
}
