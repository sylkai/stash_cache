package com.geekeclectic.android.stashcache;

import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
        registerForContextMenu(listView);

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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        FabricAdapter adapter = (FabricAdapter)getListAdapter();
        StashFabric fabric = StashData.get(getActivity()).getFabric(adapter.getItem(position));

        switch(item.getItemId()) {
            case R.id.menu_item_delete_fabric:
                StashData.get(getActivity()).deleteFabric(fabric);
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashFabric from the adapter
        StashFabric fabric = StashData.get(getActivity()).getFabric(((FabricAdapter)getListAdapter()).getItem(position));
        Log.d(TAG, fabric.getKey() + " was selected.");

        // start StashFabricPagerActivity
        Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
        i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, fabric.getId());
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
