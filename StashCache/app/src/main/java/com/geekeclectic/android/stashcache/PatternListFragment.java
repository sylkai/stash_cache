package com.geekeclectic.android.stashcache;

import android.os.Build;
import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by sylk on 8/25/2014.
 */
public class PatternListFragment extends ListFragment {

    private static final String TAG = "PatternListFragment";
    private static final int PATTERN_GROUP_ID = R.id.pattern_context_menu;

    private ArrayList<StashPattern> mPatterns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPatterns = StashData.get(getActivity()).getPatternData();

        PatternAdapter adapter = new PatternAdapter(mPatterns);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView listView = (ListView)v.findViewById(android.R.id.list);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(listView);
        } else {
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    // required but not used here
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.pattern_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // required but not used here
                    return false;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getGroupId() == PATTERN_GROUP_ID) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_pattern:
                                PatternAdapter adapter = (PatternAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());
                                for (int i = adapter.getCount() - 1; i >=0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        stash.deletePattern(adapter.getItem(i));
                                    }
                                }

                                mode.finish();
                                adapter.notifyDataSetChanged();
                                return true;
                            default:
                                return false;
                        }
                    }

                    return false;
                }

                public void onDestroyActionMode(ActionMode mode) {
                    // required but not used here
                }
            });
        }


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((PatternAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_pattern_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_pattern:
                StashPattern pattern = new StashPattern();
                StashData.get(getActivity()).addPattern(pattern);
                Intent i = new Intent(getActivity(), StashPatternPagerActivity.class);
                i.putExtra(StashPatternFragment.EXTRA_PATTERN_ID, pattern.getId());
                startActivityForResult(i, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.pattern_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == PATTERN_GROUP_ID) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            int position = info.position;
            PatternAdapter adapter = (PatternAdapter) getListAdapter();
            StashPattern pattern = adapter.getItem(position);

            switch (item.getItemId()) {
                case R.id.menu_item_delete_pattern:
                    StashData.get(getActivity()).deletePattern(pattern);
                    adapter.notifyDataSetChanged();
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashPattern from the adapter
        StashPattern pattern = ((PatternAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, pattern.getPatternName() + "wasClicked");

        // start StashPatternPagerActivity
        Intent i = new Intent(getActivity(), StashPatternPagerActivity.class);
        i.putExtra(StashPatternFragment.EXTRA_PATTERN_ID, pattern.getId());
        startActivity(i);
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

