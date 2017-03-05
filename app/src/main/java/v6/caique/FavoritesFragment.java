package v6.caique;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FavoritesFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View RootView;
    private FavoritesAdapter Adapter;

    public static HashMap<String, CacheChats.ChatStructure> FavoriteChats = new HashMap<>();
    public static ArrayList<String> ChatIDs = new ArrayList<>();

    public FavoritesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();

        FavoriteChats.clear();
        ChatIDs.clear();
        for(Map.Entry<String, CacheChats.ChatStructure> Chat: CacheChats.Loaded.entrySet()){
            if(MainActivity.Instance.sharedPref.contains(Chat.getKey())){
                FavoriteChats.put(Chat.getKey(), Chat.getValue());
                ChatIDs.add(Chat.getKey());
            }
        }

        ListView FavoritesList = (ListView) RootView.findViewById(R.id.FavoritesList);
        Adapter = new FavoritesAdapter(this.getContext());
        FavoritesList.setAdapter(Adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        return RootView;
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
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
