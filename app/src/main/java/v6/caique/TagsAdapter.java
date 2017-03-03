package v6.caique;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import java.util.ArrayList;

public class TagsAdapter extends ArrayAdapter<String> {
    private LayoutInflater vi;
    private Context context;

    public ArrayList<CheckBox> Tags = new ArrayList<>();

    public TagsAdapter(Context c)
    {
        super(c, R.layout.tag_list_item, ExploreFragment.Tags);
        vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        context = c;
    }

    @Override
    public View getView(int position, View row, ViewGroup parent) {

        if (row == null) {
            row = vi.inflate(R.layout.tag_list_item, null);
        }

        if (ExploreFragment.Tags.size() > position)
        {
            CheckBox Tag = (CheckBox) row.findViewById(R.id.checkBox);
            if(Tag != null) {
                Tag.setId(position);
                Tag.setText(ExploreFragment.Tags.get(position));
                Tags.add(Tag);
            }
        }

        return row;
    }
}
