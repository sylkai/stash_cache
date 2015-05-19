package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Fragment to display list of threads.  Long press allows user to select items to be deleted.
 */
public class StashThreadListFragment extends UpdateListFragment implements Observer, StashThreadQuantityDialogFragment.StashThreadQuantityDialogListener {

    private ArrayList<UUID> mThreads;
    private int mViewCode;
    private UpdateListFragmentsListener mCallback;
    private boolean mDialogUp;

    private static final int THREAD_GROUP_ID = R.id.thread_context_menu;
    private static final String THREAD_VIEW_ID = "com.geekeclectic.android.stashcache.thread_view_id";
    private static final String EDIT_STASH_DIALOG = "edit stash thread dialog";
    private static final String KEY_DIALOG = "com.geekeclectic.android.stashcache.key_thread_dialog";

    public StashThreadListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the current list of threads to display
        mViewCode = getArguments().getInt(THREAD_VIEW_ID);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_DIALOG)) {
            mDialogUp = savedInstanceState.getBoolean(KEY_DIALOG);
        } else {
            mDialogUp = false;
        }

        mThreads = getListFromStash();
        Collections.sort(mThreads, new StashThreadComparator(getActivity()));

        // create and set adapter using thread list
        ThreadAdapter adapter = new ThreadAdapter(mThreads);
        setListAdapter(adapter);
    }

    public static StashThreadListFragment newInstance(int viewCode) {
        Bundle args = new Bundle();
        args.putInt(THREAD_VIEW_ID, viewCode);

        StashThreadListFragment fragment = new StashThreadListFragment();
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
                    inflater.inflate(R.menu.thread_list_item_context, menu);
                    return true;
                }

                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                    // required but not used in this implementation
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    if (item.getGroupId() == THREAD_GROUP_ID) {
                        // if called by this fragment
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_thread:
                                ThreadAdapter adapter = (ThreadAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        StashThread thread = stash.getThread(adapter.getItem(i));

                                        // delete thread from the stash
                                        stash.deleteThread(thread);
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

    // has to be done after view creation to avoid an NPE
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAppropriateEmptyMessage();

        if (mDialogUp) {
            FragmentManager fm = this.getChildFragmentManager();

            StashThreadQuantityDialogFragment dialog = (StashThreadQuantityDialogFragment)fm.findFragmentByTag(EDIT_STASH_DIALOG);
            dialog.setStashThreadQuantityDialogCallback(this);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // exception handling here to make Android/Java happy
        try {
            mCallback = (UpdateListFragmentsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement UpdateListFragmentsListener");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_thread_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_thread:
                // create a new thread
                StashThread thread = new StashThread(getActivity());
                StashData.get(getActivity()).addThread(thread);

                // start StashThreadFragment with the new thread
                Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
                i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, thread.getId());
                i.putExtra(StashThreadFragment.EXTRA_TAB_ID, mViewCode);

                // be sure that the parent fragment will have its onActivityResult called (see
                // http://stackoverflow.com/questions/6147884/onactivityresult-not-being-called-in-fragment?rq=1#comment27590801_6147919)
                getParentFragment().startActivityForResult(i, 0);
                return true;
            case R.id.menu_item_edit_thread_stash:
                FragmentManager fm = this.getChildFragmentManager();

                ArrayList<UUID> threadList;
                // show the master list if on the stash tab
                if (mViewCode == StashConstants.STASH_TAB) {
                    threadList = new ArrayList<UUID>(StashData.get(getActivity()).getThreadList());
                // show the master list on the master tab or the shopping list on the shopping tab
                } else {
                    threadList = new ArrayList<UUID>(mThreads);
                }

                StashThreadQuantityDialogFragment dialog = StashThreadQuantityDialogFragment.newInstance(threadList, getActivity());
                dialog.setStashThreadQuantityDialogCallback(this);
                dialog.show(fm, EDIT_STASH_DIALOG);
                mDialogUp = true;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.thread_list_item_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == THREAD_GROUP_ID) {
            // if called by this fragment
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            StashData stash = StashData.get(getActivity());
            int position = info.position;
            ThreadAdapter adapter = (ThreadAdapter) getListAdapter();
            StashThread thread = stash.getThread(adapter.getItem(position));

            switch (item.getItemId()) {
                case R.id.menu_item_delete_thread:
                    // delete the thread from the stash
                    stash.deleteThread(thread);
                    adapter.notifyDataSetChanged();
                    mCallback.onListFragmentUpdate();
                    return super.onContextItemSelected(item);
            }
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashThread from adapter
        UUID threadId = ((ThreadAdapter)getListAdapter()).getItem(position);

        // start StashThreadPagerActivity
        Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
        i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, threadId);
        i.putExtra(StashThreadFragment.EXTRA_TAB_ID, mViewCode);

        getParentFragment().startActivityForResult(i, 0);
    }

    public void onThreadQuantitiesUpdate() {
        updateList();
        mDialogUp = false;
    }

    private void updateList() {
        if (mThreads != getListFromStash()) {
            mThreads = getListFromStash();
            Collections.sort(mThreads, new StashThreadComparator(getActivity()));

            ThreadAdapter adapter = new ThreadAdapter(mThreads);
            setListAdapter(adapter);
        }

        ((ThreadAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private ArrayList<UUID> getListFromStash() {
        if (mViewCode == StashConstants.MASTER_TAB) {
            return StashData.get(getActivity()).getThreadList();
        } else if (mViewCode == StashConstants.STASH_TAB) {
            return StashData.get(getActivity()).getThreadStashList();
        } else {
            return StashData.get(getActivity()).getThreadShoppingList();
        }
    }

    private void setAppropriateEmptyMessage() {
        if (mViewCode == StashConstants.MASTER_TAB) {
            setEmptyText("You have not entered any threads.");
        } else if (mViewCode == StashConstants.STASH_TAB) {
            setEmptyText("There are no threads in your stash.");
        } else {
            setEmptyText("There are no threads on your shopping list.");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        updateList();
    }

    @Override
    public void update(Observable observable, Object data) {
        updateList();
    }

    private class ThreadAdapter extends ArrayAdapter<UUID> {

        public ThreadAdapter(ArrayList<UUID> threads) {
            super(getActivity(), StashConstants.NO_RESOURCE, threads);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_thread, null);

                ViewHolder vh = new ViewHolder();
                vh.threadInfo = (TextView)convertView.findViewById(R.id.thread_list_item_flossIdTextView);
                vh.threadType = (TextView)convertView.findViewById(R.id.thread_list_item_flossTypeTextView);
                vh.quantity = (TextView)convertView.findViewById(R.id.thread_list_item_quantity);
                convertView.setTag(vh);
            }

            ViewHolder vh = (ViewHolder)convertView.getTag();

            // configure the view for this thread
            StashThread thread = StashData.get(getActivity()).getThread(getItem(position));

            vh.threadInfo.setText(thread.getDescriptor());
            vh.threadType.setText(thread.getType());

            if (mViewCode == StashConstants.SHOPPING_TAB) {
                vh.quantity.setText(Integer.toString(thread.getSkeinsToBuy()));
            } else if (mViewCode == StashConstants.MASTER_TAB) {
                vh.quantity.setText(Integer.toString(thread.getSkeinsOwned()) + " / " + Integer.toString(thread.getSkeinsToBuy()));
            } else {
                vh.quantity.setText(Integer.toString(thread.getSkeinsOwned()));
            }

            return convertView;
        }

    }

    private static class ViewHolder {
        public TextView threadInfo;
        public TextView threadType;
        public TextView quantity;
    }

}
