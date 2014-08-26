package com.geekeclectic.android.stashcache;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sylk on 8/25/2014.
 */
public class PatternListFragment extends ListFragment {

    private static final String TAG = "PatternListFragment";

    private ArrayList<StashPattern> mPatterns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.pattern_list_title);
        mPatterns = StashData.get(getActivity()).getPatternData();

        PatternAdapter adapter = new PatternAdapter(mPatterns);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        StashPattern pattern = ((PatternAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, pattern.getPatternName() + "wasClicked");
    }

    private class PatternAdapter extends ArrayAdapter<StashPattern> {

        public PatternAdapter(ArrayList<StashPattern> patterns) {
            super(getActivity(), 0, patterns);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if no view given, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_pattern, null);
            }

            // configure the view for this pattern
            StashPattern pattern = getItem(position);

            TextView patternNameTextView = (TextView)convertView.findViewById(R.id.pattern_list_item_nameTextView);
            patternNameTextView.setText(pattern.getPatternName());

            TextView patternSourceTextView = (TextView)convertView.findViewById(R.id.pattern_list_item_sourceTextView);
            patternSourceTextView.setText(pattern.getPatternSource());

            return convertView;
        }
    }

}

