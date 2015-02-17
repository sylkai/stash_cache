package com.geekeclectic.android.stashcache;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
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
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Fragment to display list of fabrics.  Long press allows user to select items to be deleted.
 */
public class StashFabricListFragment extends ListFragment {

    FabricAdapter adapter;
    ArrayList<UUID> mFabrics;

    private static final String TAG = "FabricListFragment";
    private static final int FABRIC_GROUP_ID = R.id.fabric_context_menu;
    private static final String FABRIC_VIEW_ID = "com.geekeclectic.android.stashcache.fabric_view_id";

    public StashFabricListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the current list of fabrics to display
        String viewCode = getArguments().getString(FABRIC_VIEW_ID);
        mFabrics = StashData.get(getActivity()).getFabricList();

        // create and set list adapter using fabrics list
        adapter = new FabricAdapter(mFabrics);
        setListAdapter(adapter);
    }

    public static StashFabricListFragment newInstance(String viewCode) {
        Bundle args = new Bundle();
        args.putString(FABRIC_VIEW_ID, viewCode);

        StashFabricListFragment fragment = new StashFabricListFragment();
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
            // set up list behavior on long-press (for deletion)
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    // required but not used here
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.fabric_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                    // required but not used here
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    // if the action menu is the one associated with this list (because fragments)
                    if (item.getGroupId() == FABRIC_GROUP_ID) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_fabric:
                                FabricAdapter adapter = (FabricAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        // if the item is checked, remove the item from the stash
                                        StashFabric fabric = stash.getFabric(adapter.getItem(i));
                                        stash.deleteFabric(fabric);
                                    }
                                }

                                mode.finish();

                                // notify the adapter that the backing list has changed and refresh
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
        ((FabricAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_fabric_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_fabric:
                // create a new fabric and add it to the stash
                StashFabric fabric = new StashFabric();
                StashData.get(getActivity()).addFabric(fabric);

                // start StashFabricFragment with the new fabric
                Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
                i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, fabric.getId());
                startActivityForResult(i, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo contextMenuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.fabric_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == FABRIC_GROUP_ID) {
            // if called by this fragment
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            int position = info.position;
            FabricAdapter adapter = (FabricAdapter) getListAdapter();
            StashFabric fabric = StashData.get(getActivity()).getFabric(adapter.getItem(position));

            switch (item.getItemId()) {
                case R.id.menu_item_delete_fabric:
                    // delete fabric and notify adapter of data change
                    StashData.get(getActivity()).deleteFabric(fabric);
                    adapter.notifyDataSetChanged();
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashFabric from the adapter
        UUID fabricId = ((FabricAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, fabricId.toString() + " was selected.");

        // start StashFabricPagerActivity
        Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
        i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, fabricId);
        startActivity(i);
    }

    private class FabricAdapter extends ArrayAdapter<UUID> {
        public FabricAdapter(ArrayList<UUID> fabrics) {
            super(getActivity(), 0, fabrics);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_fabric, null);
            }

            // configure view for this fabric
            StashFabric fabric = StashData.get(getActivity()).getFabric(getItem(position));

            TextView fabricInfoTextView = (TextView) convertView.findViewById(R.id.fabric_list_item_infoTextView);
            fabricInfoTextView.setText(fabric.getInfo());

            TextView fabricSizeTextView = (TextView) convertView.findViewById(R.id.fabric_list_item_sizeTextView);
            fabricSizeTextView.setText(fabric.getSize());

            CheckBox isAssignedCheckBox = (CheckBox) convertView.findViewById(R.id.fabric_list_item_assignedCheckBox);
            isAssignedCheckBox.setChecked(fabric.isAssigned());

            return convertView;
        }

    }

}
