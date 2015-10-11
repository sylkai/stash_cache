package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;

/**
 * Fragment to display list of patterns.  Long press allows user to select items to be deleted.
 */

public class StashPatternListFragment extends UpdateListFragment implements Observer {

    private static final int PATTERN_GROUP_ID = R.id.pattern_context_menu;
    private static final String PATTERN_VIEW_ID = "com.geekeclectic.android.stashcache.pattern_view_id";
    private int mViewCode;
    private UpdateListFragmentsListener mCallback;

    private ArrayList<StashPattern> mPatterns;

    public StashPatternListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mViewCode = getArguments().getInt(PATTERN_VIEW_ID);

        mPatterns = getListFromStash();
        Collections.sort(mPatterns, new StashPatternComparator());

        PatternAdapter adapter = new PatternAdapter(mPatterns);
        setListAdapter(adapter);
    }

    public static StashPatternListFragment newInstance(int viewCode) {
        Bundle args = new Bundle();
        args.putInt(PATTERN_VIEW_ID, viewCode);

        StashPatternListFragment fragment = new StashPatternListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView listView = (ListView)v.findViewById(android.R.id.list);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(listView);
        } else {
            // set up for action mode on long press
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
                    // if the actionMenu applies to THIS LIST (necessary because of ViewPager)
                    if (item.getGroupId() == PATTERN_GROUP_ID) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_pattern:
                                PatternAdapter adapter = (PatternAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());

                                // iterate through the the items in the adapter - if the pattern is
                                // checked, remove it from the stash
                                for (int i = adapter.getCount() - 1; i >=0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        // remove pattern
                                        stash.deletePattern(adapter.getItem(i));
                                    }
                                }

                                mode.finish();

                                // notify the adapter to refresh the view
                                adapter.notifyDataSetChanged();
                                mCallback.onListFragmentUpdate();
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

    // has to be done after the view is created or running the risk of a NPE error
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAppropriateEmptyMessage();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // required to make Android and Java not sad
        try {
            mCallback = (UpdateListFragmentsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdateListFragmentsListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // add menu for pattern addition
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_pattern_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_pattern:
                // create new pattern and add it to the stash
                StashPattern pattern = new StashPattern(getActivity());
                StashData.get(getActivity()).addPattern(pattern);

                // start pattern viewPager with the desired pattern fragment
                Intent i = new Intent(getActivity(), StashPatternPagerActivity.class);
                i.putExtra(StashPatternFragment.EXTRA_PATTERN_ID, pattern.getId());
                i.putExtra(StashPatternFragment.EXTRA_TAB_ID, mViewCode);

                // be sure that the parent fragment will have its onActivityResult called (see
                // http://stackoverflow.com/questions/6147884/onactivityresult-not-being-called-in-fragment?rq=1#comment27590801_6147919)
                getParentFragment().startActivityForResult(i, 0);
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
            // if the requesting fragment was THIS ONE, get the info
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            StashData stash = StashData.get(getActivity());
            int position = info.position;
            PatternAdapter adapter = (PatternAdapter) getListAdapter();
            StashPattern pattern = adapter.getItem(position);

            switch (item.getItemId()) {
                case R.id.menu_item_delete_pattern:
                    // delete the pattern and update the view
                    stash.deletePattern(pattern);
                    adapter.notifyDataSetChanged();
                    mCallback.onListFragmentUpdate();
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashPattern from the adapter
        StashPattern pattern = ((PatternAdapter)getListAdapter()).getItem(position);

        // start StashPatternPagerActivity
        Intent i = new Intent(getActivity(), StashPatternPagerActivity.class);
        i.putExtra(StashPatternFragment.EXTRA_PATTERN_ID, pattern.getId());
        i.putExtra(StashPatternFragment.EXTRA_TAB_ID, mViewCode);

        getParentFragment().startActivityForResult(i, 0);
    }

    private ArrayList<StashPattern> getListFromStash() {
        if (mViewCode == StashConstants.SHOPPING_TAB) {
            return StashData.get(getActivity()).getFabricForList();
        } else if (mViewCode == StashConstants.STASH_TAB) {
            return StashData.get(getActivity()).getStashPatterns();
        } else {
            return StashData.get(getActivity()).getPatternData();
        }
    }

    private void updateList() {
        if (mPatterns != getListFromStash()) {
            mPatterns = getListFromStash();

            // sort the list in case anything changed
            Collections.sort(mPatterns, new StashPatternComparator());

            PatternAdapter adapter = new PatternAdapter(mPatterns);
            setListAdapter(adapter);
        }

        ((PatternAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateList();
    }

    @Override
    public void update(Observable observable, Object data) {
        updateList();
    }

    private void setAppropriateEmptyMessage() {
        if (mViewCode == StashConstants.MASTER_TAB) {
            setEmptyText("You have not entered any patterns.");
        } else if (mViewCode == StashConstants.STASH_TAB) {
            setEmptyText("There are no patterns in your stash.");
        } else {
            setEmptyText("You don't need fabric for any patterns in your stash.");
        }
    }

    private class PatternAdapter extends ArrayAdapter<StashPattern> {

        private StashCreateShoppingList mShoppingList;

        public PatternAdapter(ArrayList<StashPattern> patterns) {
            super(getActivity(), StashConstants.NO_RESOURCE, patterns);
            mShoppingList = new StashCreateShoppingList();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if no view given, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_pattern, null);

                ViewHolder vh = new ViewHolder();
                vh.patternName = (TextView)convertView.findViewById(R.id.pattern_list_item_nameTextView);
                vh.patternSource = (TextView)convertView.findViewById(R.id.pattern_list_item_sourceTextView);
                vh.patternKitted = (CheckBox)convertView.findViewById(R.id.pattern_kitted_checkbox);
                convertView.setTag(vh);
            }

            // configure the view for this pattern
            StashPattern pattern = getItem(position);
            ViewHolder vh = (ViewHolder)convertView.getTag();

            vh.patternName.setText(pattern.getPatternName());
            vh.patternSource.setText(pattern.getSource());

            vh.patternKitted.setTag(pattern);
            vh.patternKitted.setChecked(pattern.isKitted());

            // when patterns both in and out of stash are displayed (on the master list), patterns
            // no longer in stash are gray
            if (!pattern.inStash()) {
                vh.patternName.setTextColor(Color.GRAY);
                vh.patternSource.setTextColor(Color.GRAY);
            } else {
                vh.patternName.setTextColor(Color.BLACK);
                vh.patternSource.setTextColor(Color.BLACK);
            }

            vh.patternKitted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox checkBox = (CheckBox) view;
                    StashPattern pattern = (StashPattern) checkBox.getTag();
                    pattern.setKitted(checkBox.isChecked());
                    StashData.get(getActivity()).saveStash();

                    mShoppingList.updateShoppingList(getActivity());
                    mCallback.onListFragmentUpdate();
                }
            });

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView patternName;
        public TextView patternSource;
        public CheckBox patternKitted;
    }

}

