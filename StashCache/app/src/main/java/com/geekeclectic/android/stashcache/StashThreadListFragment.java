package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
    private int numericTab;
    private UpdateListFragmentsListener mCallback;

    private static final String TAG = "ThreadListFragment";
    private static final int THREAD_GROUP_ID = R.id.thread_context_menu;
    private static final String THREAD_VIEW_ID = "com.geekeclectic.android.stashcache.thread_view_id";
    private static final String EDIT_STASH_DIALOG = "edit stash thread dialog";

    public StashThreadListFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the current list of threads to display
        String viewCode = getArguments().getString(THREAD_VIEW_ID);

        if (viewCode.equals("master")) {
            numericTab = 1;
        } else if (viewCode.equals("stash")) {
            numericTab = 0;
        } else {
            numericTab = 2;
        }

        mThreads = getListFromStash();
        Collections.sort(mThreads, new StashThreadComparator(getActivity().getApplicationContext()));

        // create and set adapter using thread list
        ThreadAdapter adapter = new ThreadAdapter(mThreads);
        setListAdapter(adapter);

        // new InitialThreadListSortTask().execute();
    }

    public static StashThreadListFragment newInstance(String viewCode) {
        Bundle args = new Bundle();
        args.putString(THREAD_VIEW_ID, viewCode);

        StashThreadListFragment fragment = new StashThreadListFragment();
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
                                StashData stash = StashData.get(getActivity().getApplicationContext());
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAppropriateEmptyMessage(getArguments().getString(THREAD_VIEW_ID));
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
    public void onResume() {
        super.onResume();

        // re-sort the list to make sure everything is where it should be
        mThreads = getListFromStash();
        Collections.sort(mThreads, new StashThreadComparator(getActivity().getApplicationContext()));

        //remind the adapter to get the updated list
        ((ThreadAdapter)getListAdapter()).notifyDataSetChanged();
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
                StashThread thread = new StashThread();
                StashData.get(getActivity().getApplicationContext()).addThread(thread);

                // start StashThreadFragment with the new thread
                Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
                i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, thread.getId());
                i.putExtra(StashThreadFragment.EXTRA_TAB_ID, numericTab);
                startActivityForResult(i, 0);
                return true;
            case R.id.menu_item_edit_thread_stash:
                FragmentManager fm = getActivity().getSupportFragmentManager();

                ArrayList<UUID> threadList;
                if (numericTab == 0) {
                    threadList = new ArrayList<UUID>(StashData.get(getActivity().getApplicationContext()).getThreadList());
                } else {
                    threadList = new ArrayList<UUID>(mThreads);
                }

                StashThreadQuantityDialogFragment dialog = StashThreadQuantityDialogFragment.newInstance(threadList, getActivity().getApplicationContext());
                dialog.setStashThreadQuantityDialogCallback(this);
                dialog.show(fm, EDIT_STASH_DIALOG);
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
            StashData stash = StashData.get(getActivity().getApplicationContext());
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
        Log.d(TAG, threadId.toString() + " was clicked.");

        // start StashThreadPagerActivity
        Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
        i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, threadId);
        i.putExtra(StashThreadFragment.EXTRA_TAB_ID, numericTab);
        startActivity(i);
    }

    public void onThreadQuantitiesUpdate() {
        mThreads = getListFromStash();
        Collections.sort(mThreads, new StashThreadComparator(getActivity().getApplicationContext()));

        ThreadAdapter adapter = new ThreadAdapter(mThreads);
        setListAdapter(adapter);

        ((ThreadAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private ArrayList<UUID> getListFromStash() {
        if (numericTab == 1) {
            return StashData.get(getActivity().getApplicationContext()).getThreadList();
        } else if (numericTab == 0) {
            return StashData.get(getActivity().getApplicationContext()).getThreadStashList();
        } else {
            return StashData.get(getActivity().getApplicationContext()).getThreadShoppingList();
        }
    }

    private void setAppropriateEmptyMessage(String viewCode) {
        if (viewCode.equals("master")) {
            setEmptyText("You have not entered any threads.");
        } else if (viewCode.equals("stash")) {
            setEmptyText("There are no threads in your stash.");
        } else {
            setEmptyText("There are no threads on your shopping list.");
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (mThreads != getListFromStash()) {
            mThreads = getListFromStash();
            Collections.sort(mThreads, new StashThreadComparator(getActivity().getApplicationContext()));

            ThreadAdapter adapter = new ThreadAdapter(mThreads);
            setListAdapter(adapter);
        }

        ((ThreadAdapter)getListAdapter()).notifyDataSetChanged();
    }

    private class ThreadAdapter extends ArrayAdapter<UUID> {

        public ThreadAdapter(ArrayList<UUID> threads) {
            super(getActivity().getApplicationContext(), 0, threads);
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
            StashThread thread = StashData.get(getActivity().getApplicationContext()).getThread(getItem(position));

            vh.threadInfo.setText(thread.getDescriptor());
            vh.threadType.setText(thread.getType());

            if (getArguments().getString(THREAD_VIEW_ID).equals("shopping")) {
                vh.quantity.setText(Integer.toString(thread.getSkeinsToBuy()));
            } else if (getArguments().getString(THREAD_VIEW_ID).equals("master")) {
                vh.quantity.setText(Integer.toString(thread.getSkeinsOwned()) + " / " + Integer.toString(thread.getSkeinsToBuy()));
            } else {
                vh.quantity.setText(Integer.toString(thread.getSkeinsOwned()));
            }

            return convertView;
        }

    }

    static class ViewHolder {
        TextView threadInfo;
        TextView threadType;
        TextView quantity;
    }

}
