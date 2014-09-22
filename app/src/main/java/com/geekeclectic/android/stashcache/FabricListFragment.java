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
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 8/27/2014.
 */
public class FabricListFragment extends ListFragment {

    //private ArrayList<StashFabric> mFabrics;
    FabricAdapter adapter;
    ArrayList<UUID> mFabrics;

    private static final String TAG = "FabricListFragment";
    private static final int FABRIC_GROUP_ID = R.id.fabric_context_menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mFabrics = StashData.get(getActivity()).getFabricList();

        adapter = new FabricAdapter(mFabrics);
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
                    inflater.inflate(R.menu.fabric_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                    // required but not used here
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getGroupId() == FABRIC_GROUP_ID) {
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_fabric:
                                FabricAdapter adapter = (FabricAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        StashFabric fabric = stash.getFabric(adapter.getItem(i));
                                        stash.deleteFabric(fabric);
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
                StashFabric fabric = new StashFabric();
                StashData.get(getActivity()).addFabric(fabric);
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
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            int position = info.position;
            FabricAdapter adapter = (FabricAdapter) getListAdapter();
            StashFabric fabric = StashData.get(getActivity()).getFabric(adapter.getItem(position));

            switch (item.getItemId()) {
                case R.id.menu_item_delete_fabric:
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
