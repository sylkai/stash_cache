package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.FragmentManager;
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
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Fragment to display list of embellishments.  Long press allows user to select items to be deleted.
 */
public class StashEmbellishmentListFragment extends UpdateListFragment implements Observer, StashEmbellishmentQuantityDialogFragment.StashEmbellishmentQuantityDialogListener {

    private ArrayList<UUID> mEmbellishments;
    private int numericTab;
    private UpdateListFragmentsListener mCallback;

    private static final String TAG = "EmbellishmentListFragment";
    private static final int EMBELLISHMENT_GROUP_ID = R.id.embellishment_context_menu;
    private static final String EMBELLISHMENT_VIEW_ID = "com.geekeclectic.android.stashcache.embellishment_view_id";
    private static final String EDIT_STASH_DIALOG = "embellishment stash dialog";

    public StashEmbellishmentListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the current list of embellishments to display
        String viewCode = getArguments().getString(EMBELLISHMENT_VIEW_ID);

        if (viewCode.equals("master")) {
            numericTab = 1;
        } else if (viewCode.equals("stash")) {
            numericTab = 0;
        } else {
            numericTab = 2;
        }

        mEmbellishments = getListFromStash();
        Collections.sort(mEmbellishments, new StashEmbellishmentComparator(getActivity()));

        // create and set adapter using embellishment list
        EmbellishmentAdapter adapter = new EmbellishmentAdapter(mEmbellishments);
        setListAdapter(adapter);
    }

    public static StashEmbellishmentListFragment newInstance(String viewCode) {
        Bundle args = new Bundle();
        args.putString(EMBELLISHMENT_VIEW_ID, viewCode);

        StashEmbellishmentListFragment fragment = new StashEmbellishmentListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView listView = (ListView)v.findViewById(android.R.id.list);


        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(listView);
        } else {
            // set up list behavior on long-press for deletion
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    // required but not used in this implementation
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.embellishment_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                    // required but not used in this implementation
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getGroupId() == EMBELLISHMENT_GROUP_ID) {
                        // if called by this fragment
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_embellishment:
                                EmbellishmentAdapter adapter = (EmbellishmentAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        StashEmbellishment embellishment = stash.getEmbellishment(adapter.getItem(i));

                                        // remove embellishment from the stash
                                        stash.deleteEmbellishment(embellishment);
                                    }
                                }

                                mode.finish();

                                // refresh the list
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
                    // required but not used in this implementation
                }
            });
        }

        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (UpdateListFragmentsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdateListFragmentsListener");
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAppropriateEmptyMessage(getArguments().getString(EMBELLISHMENT_VIEW_ID));
    }

    @Override
    public void onResume() {
        super.onResume();
        ((EmbellishmentAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_embellishment_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_embellishment:
                // create a new embellishment
                StashEmbellishment embellishment = new StashEmbellishment();
                StashData.get(getActivity()).addEmbellishment(embellishment);

                // start StashEmbellishmentFragment with the new embellishment
                Intent i = new Intent(getActivity(), StashEmbellishmentPagerActivity.class);
                i.putExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID, embellishment.getId());
                i.putExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, numericTab);
                startActivityForResult(i, 0);
                return super.onOptionsItemSelected(item);
            case R.id.menu_item_edit_embellishment_stash:
                FragmentManager fm = getActivity().getSupportFragmentManager();

                ArrayList<UUID> embellishmentList;
                if (numericTab == 0) {
                    embellishmentList = new ArrayList<UUID>(StashData.get(getActivity()).getEmbellishmentList());
                } else {
                    embellishmentList = new ArrayList<UUID>(mEmbellishments);
                }

                StashEmbellishmentQuantityDialogFragment dialog = StashEmbellishmentQuantityDialogFragment.newInstance(embellishmentList, getActivity());
                dialog.setStashEmbellishmentQuantityDialogCallback(this);
                dialog.show(fm, EDIT_STASH_DIALOG);
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.embellishment_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == EMBELLISHMENT_GROUP_ID) {
            // if called by this fragment
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            StashData stash = StashData.get(getActivity());
            int position = info.position;
            EmbellishmentAdapter adapter = (EmbellishmentAdapter) getListAdapter();
            StashEmbellishment embellishment = stash.getEmbellishment(adapter.getItem(position));

            switch (item.getItemId()) {
                case R.id.menu_item_delete_embellishment:
                    // delete the embellishment from the stash
                    stash.deleteEmbellishment(embellishment);
                    adapter.notifyDataSetChanged();
                    mCallback.onListFragmentUpdate();
                    return super.onContextItemSelected(item);
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashEmbellishment from adapter
        UUID embellishmentId = ((EmbellishmentAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, embellishmentId.toString() + " was clicked.");

        // start StashEmbellishmentPagerActivity
        Intent i = new Intent(getActivity(), StashEmbellishmentPagerActivity.class);
        i.putExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID, embellishmentId);
        i.putExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, numericTab);
        startActivity(i);
    }

    public void onEmbellishmentQuantitiesUpdate() {
        mEmbellishments = getListFromStash();
        Collections.sort(mEmbellishments, new StashEmbellishmentComparator(getActivity()));

        EmbellishmentAdapter adapter = new EmbellishmentAdapter(mEmbellishments);
        setListAdapter(adapter);

        ((EmbellishmentAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private ArrayList<UUID> getListFromStash() {
        if (numericTab == 1) {
            return StashData.get(getActivity()).getEmbellishmentList();
        } else if (numericTab == 0) {
            return StashData.get(getActivity()).getEmbellishmentStashList();
        } else {
            return StashData.get(getActivity()).getEmbellishmentShoppingList();
        }
    }

    private void setAppropriateEmptyMessage(String viewCode) {
        // empty message depends on the active tab,
        if (viewCode.equals("master")) {
            setEmptyText("You have not entered any embellishments.");
        } else if (viewCode.equals("stash")) {
            setEmptyText("There are no embellishments in your stash.");
        } else {
            setEmptyText("There are no embellishments on your shopping list.");
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        ((EmbellishmentAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private class EmbellishmentAdapter extends ArrayAdapter<UUID> {

        public EmbellishmentAdapter(ArrayList<UUID> embellishments) {
            super(getActivity(), 0, embellishments);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_embellishment, null);

                ViewHolder vh = new ViewHolder();
                vh.info = (TextView)convertView.findViewById(R.id.embellishment_list_item_idTextView);
                vh.quantity = (TextView)convertView.findViewById(R.id.embellishment_list_item_quantity);
                convertView.setTag(vh);
            }

            ViewHolder vh = (ViewHolder)convertView.getTag();
            // configure the view for this embellishment
            StashEmbellishment embellishment = StashData.get(getActivity()).getEmbellishment(getItem(position));

            vh.info.setText(embellishment.toString());

            if (getArguments().getString(EMBELLISHMENT_VIEW_ID).equals("shopping")) {
                vh.quantity.setText(Integer.toString(embellishment.getNumberNeeded()));
            } else if (getArguments().getString(EMBELLISHMENT_VIEW_ID).equals("master")) {
                vh.quantity.setText(Integer.toString(embellishment.getNumberOwned()) + " / " + Integer.toString(embellishment.getNumberNeeded()));
            } else {
                vh.quantity.setText(Integer.toString(embellishment.getNumberOwned()));
            }

            return convertView;
        }

    }

    static class ViewHolder {
        TextView info;
        TextView quantity;
    }

}
