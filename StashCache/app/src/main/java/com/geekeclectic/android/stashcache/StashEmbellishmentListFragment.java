package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.os.Build;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
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
    private int mViewCode;
    private UpdateListFragmentsListener mCallback;
    private boolean mDialogUp;

    private static final int EMBELLISHMENT_GROUP_ID = R.id.embellishment_context_menu;
    private static final String EMBELLISHMENT_VIEW_ID = "com.geekeclectic.android.stashcache.embellishment_view_id";
    private static final String EDIT_STASH_DIALOG = "embellishment stash dialog";
    private static final String KEY_DIALOG = "com.geekeclectic.android.stashcache.key_embellishment_dialog";

    public StashEmbellishmentListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the current list of embellishments to display
        mViewCode = getArguments().getInt(EMBELLISHMENT_VIEW_ID, 0);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_DIALOG)) {
            mDialogUp = savedInstanceState.getBoolean(KEY_DIALOG);
        } else {
            mDialogUp = false;
        }

        mEmbellishments = getListFromStash();
        Collections.sort(mEmbellishments, new StashEmbellishmentComparator(getActivity()));

        // create and set adapter using embellishment list
        EmbellishmentAdapter adapter = new EmbellishmentAdapter(mEmbellishments);
        setListAdapter(adapter);
    }

    public static StashEmbellishmentListFragment newInstance(int viewId) {
        Bundle args = new Bundle();
        args.putInt(EMBELLISHMENT_VIEW_ID, viewId);

        StashEmbellishmentListFragment fragment = new StashEmbellishmentListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // need to save if the dialog is active
        savedInstanceState.putBoolean(KEY_DIALOG, mDialogUp);
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

        // to make Java happy
        try {
            mCallback = (UpdateListFragmentsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdateListFragmentsListener");
        }
    }

    // called after the view is created to set the empty message (since otherwise it could be called
    // before the list object was created)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAppropriateEmptyMessage();

        if (mDialogUp) {
            FragmentManager fm = this.getChildFragmentManager();

            StashEmbellishmentQuantityDialogFragment dialog = (StashEmbellishmentQuantityDialogFragment)fm.findFragmentByTag(EDIT_STASH_DIALOG);
            dialog.setStashEmbellishmentQuantityDialogCallback(this);
        }
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
                StashEmbellishment embellishment = new StashEmbellishment(getActivity());
                StashData.get(getActivity()).addEmbellishment(embellishment);

                // start StashEmbellishmentFragment with the new embellishment
                Intent i = new Intent(getActivity(), StashEmbellishmentPagerActivity.class);
                i.putExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID, embellishment.getId());
                i.putExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, mViewCode);

                // be sure that the parent fragment will have its onActivityResult called (see
                // http://stackoverflow.com/questions/6147884/onactivityresult-not-being-called-in-fragment?rq=1#comment27590801_6147919)
                getParentFragment().startActivityForResult(i, 0);
                return true;
            case R.id.menu_item_edit_embellishment_stash:
                FragmentManager fm = this.getChildFragmentManager();

                ArrayList<UUID> embellishmentList;
                if (mViewCode == 0) {
                    embellishmentList = new ArrayList<UUID>(StashData.get(getActivity()).getEmbellishmentList());
                } else {
                    embellishmentList = new ArrayList<UUID>(mEmbellishments);
                }

                StashEmbellishmentQuantityDialogFragment dialog = StashEmbellishmentQuantityDialogFragment.newInstance(embellishmentList, getActivity());
                dialog.setStashEmbellishmentQuantityDialogCallback(this);
                dialog.show(fm, EDIT_STASH_DIALOG);
                mDialogUp = true;
                return true;
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

        // start StashEmbellishmentPagerActivity
        Intent i = new Intent(getActivity(), StashEmbellishmentPagerActivity.class);
        i.putExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID, embellishmentId);
        i.putExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, mViewCode);

        getParentFragment().startActivityForResult(i, 0);
    }

    // update the list (in the case of things like changes to the stash list) and create and set
    // a new adapter (because otherwise it wasn't updating despite datasetchanged)
    public void onEmbellishmentQuantitiesUpdate() {
        updateList();
        mDialogUp = false;
    }

    private ArrayList<UUID> getListFromStash() {
        if (mViewCode == StashConstants.MASTER_TAB) {
            return StashData.get(getActivity()).getEmbellishmentList();
        } else if (mViewCode == StashConstants.STASH_TAB) {
            return StashData.get(getActivity()).getEmbellishmentStashList();
        } else {
            return StashData.get(getActivity()).getEmbellishmentShoppingList();
        }
    }

    private void setAppropriateEmptyMessage() {
        // empty message depends on the active tab,
        if (mViewCode == StashConstants.MASTER_TAB) {
            setEmptyText("You have not entered any embellishments.");
        } else if (mViewCode == StashConstants.STASH_TAB) {
            setEmptyText("There are no embellishments in your stash.");
        } else {
            setEmptyText("There are no embellishments on your shopping list.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateList();
    }

    private void updateList() {
        if (mEmbellishments != getListFromStash()) {
            mEmbellishments = getListFromStash();
            Collections.sort(mEmbellishments, new StashEmbellishmentComparator(getActivity()));

            EmbellishmentAdapter adapter = new EmbellishmentAdapter(mEmbellishments);
            setListAdapter(adapter);
        }

        ((EmbellishmentAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void update(Observable observable, Object data) {
        updateList();
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

            if (mViewCode == StashConstants.SHOPPING_TAB) {
                vh.quantity.setText(Integer.toString(embellishment.getNumberNeeded()));
            } else if (mViewCode == StashConstants.MASTER_TAB) {
                vh.quantity.setText(Integer.toString(embellishment.getNumberOwned()) + " / " + Integer.toString(embellishment.getNumberNeeded()));
            } else {
                vh.quantity.setText(Integer.toString(embellishment.getNumberOwned()));
            }

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView info;
        TextView quantity;
    }

}
