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
import java.util.UUID;

/**
 * Fragment to display list of fabrics.  Long press allows user to select items to be deleted.
 */
public class StashFabricListFragment extends UpdateListFragment implements Observer {

    private FabricAdapter adapter;
    private ArrayList<UUID> mFabrics;
    private UpdateListFragmentsListener mCallback;

    private static final int FABRIC_GROUP_ID = R.id.fabric_context_menu;
    private static final String FABRIC_VIEW_ID = "com.geekeclectic.android.stashcache.fabric_view_id";

    private int mViewCode;

    public StashFabricListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the current list of fabrics to display
        mViewCode = getArguments().getInt(FABRIC_VIEW_ID);

        getAppropriateList();
        Collections.sort(mFabrics, new StashFabricComparator(getActivity()));

        // create and set list adapter using fabrics list
        adapter = new FabricAdapter(mFabrics);
        setListAdapter(adapter);
    }

    public static StashFabricListFragment newInstance(int viewCode) {
        Bundle args = new Bundle();
        args.putInt(FABRIC_VIEW_ID, viewCode);

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
                                        StashFabric fabric = stash.getFabric(adapter.getItem(i));

                                        // remove fabric from the stash
                                        stash.deleteFabric(fabric);
                                    }
                                }

                                mode.finish();

                                // notify the adapter that the backing list has changed and refresh
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

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // exception handling should not be needed but required because Java
        try {
            mCallback = (UpdateListFragmentsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdateListFragmentsListener");
        }
    }

    // called after the view is created to set the empty list message because otherwise errors
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAppropriateEmptyMessage();
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
                StashFabric fabric = new StashFabric(getActivity());
                StashData.get(getActivity()).addFabric(fabric);

                // start StashFabricFragment with the new fabric
                Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
                i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, fabric.getId());
                i.putExtra(StashFabricFragment.EXTRA_TAB_ID, mViewCode);

                // be sure that the parent fragment will have its onActivityResult called (see
                // http://stackoverflow.com/questions/6147884/onactivityresult-not-being-called-in-fragment?rq=1#comment27590801_6147919)
                getParentFragment().startActivityForResult(i, 0);
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
            StashData stash = StashData.get(getActivity());
            int position = info.position;
            FabricAdapter adapter = (FabricAdapter) getListAdapter();
            StashFabric fabric = stash.getFabric(adapter.getItem(position));

            switch (item.getItemId()) {
                case R.id.menu_item_delete_fabric:
                    // delete fabric and notify adapter of data change
                    stash.deleteFabric(fabric);
                    adapter.notifyDataSetChanged();
                    mCallback.onListFragmentUpdate();
                    return true;
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashFabric from the adapter
        UUID fabricId = ((FabricAdapter)getListAdapter()).getItem(position);

        // start StashFabricPagerActivity
        Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
        i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, fabricId);
        i.putExtra(StashFabricFragment.EXTRA_TAB_ID, mViewCode);
        startActivity(i);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // sort the list in case anything changed
        Collections.sort(mFabrics, new StashFabricComparator(getActivity()));

        // make sure the adapter is notified that the data set may have changed
        FabricAdapter adapter = (FabricAdapter)getListAdapter();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void update(Observable observable, Object data) {
        Collections.sort(mFabrics, new StashFabricComparator(getActivity()));
        ((FabricAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private void setAppropriateEmptyMessage() {
        if (mViewCode == StashConstants.MASTER_TAB) {
            setEmptyText("You have not entered any fabric.");
        } else {
            setEmptyText("There is no fabric in your stash.");
        }
    }

    private void getAppropriateList() {
        if (mViewCode == StashConstants.MASTER_TAB) {
            mFabrics = StashData.get(getActivity()).getFabricList();
        } else {
            mFabrics = StashData.get(getActivity()).getStashFabricList();
        }
    }

    private class FabricAdapter extends ArrayAdapter<UUID> {
        public FabricAdapter(ArrayList<UUID> fabrics) {
            super(getActivity(), StashConstants.NO_RESOURCE, fabrics);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_fabric, null);

                ViewHolder vh = new ViewHolder();
                vh.info = (TextView) convertView.findViewById(R.id.fabric_list_item_infoTextView);
                vh.size = (TextView) convertView.findViewById(R.id.fabric_list_item_sizeTextView);
                vh.assigned = (CheckBox) convertView.findViewById(R.id.fabric_list_item_assignedCheckBox);
                convertView.setTag(vh);
            }

            ViewHolder vh = (ViewHolder)convertView.getTag();
            // configure view for this fabric
            StashFabric fabric = StashData.get(getActivity()).getFabric(getItem(position));

            vh.info.setText(fabric.getInfo());
            vh.size.setText(fabric.getSize());
            vh.assigned.setChecked(fabric.isAssigned());

            if (fabric.isFinished()) {
                vh.info.setTextColor(Color.GRAY);
                vh.size.setTextColor(Color.GRAY);
            } else {
                vh.info.setTextColor(Color.BLACK);
                vh.size.setTextColor(Color.BLACK);
            }

            return convertView;
        }

    }

    private static class ViewHolder {
        TextView info;
        TextView size;
        CheckBox assigned;
    }

}
