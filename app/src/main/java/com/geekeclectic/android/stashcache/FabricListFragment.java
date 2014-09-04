package com.geekeclectic.android.stashcache;

import android.support.v4.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 8/27/2014.
 */
public class FabricListFragment extends ListFragment {

    private ArrayList<StashFabric> mFabrics;

    private static final String TAG = "FabricListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFabrics = StashData.get(getActivity()).getFabricList();

        FabricAdapter adapter = new FabricAdapter(mFabrics);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((FabricAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashFabric from the adapter
        StashFabric fabric = ((FabricAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, fabric.getId() + " was selected.");

        // start StashFabricActivity
        Intent i = new Intent(getActivity(), StashFabricActivity.class);
        i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, UUID.fromString(fabric.getId()));
        startActivity(i);
    }

    private class FabricAdapter extends ArrayAdapter<StashFabric> {
        public FabricAdapter(ArrayList<StashFabric> fabrics) {
            super(getActivity(), 0, fabrics);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_fabric, null);
            }

            // configure view for this fabric
            StashFabric fabric = getItem(position);

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
