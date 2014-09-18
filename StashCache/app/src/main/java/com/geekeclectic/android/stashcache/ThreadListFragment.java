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
public class ThreadListFragment extends ListFragment {

    private ArrayList<UUID> mThreads;

    private static final String TAG = "ThreadListFragment";
    private static final int THREAD_GROUP_ID = R.id.thread_context_menu;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mThreads = StashData.get(getActivity()).getThreadList();

        ThreadAdapter adapter = new ThreadAdapter(mThreads);
        setListAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, parent, savedInstanceState);

        ListView listView = (ListView)v.findViewById(android.R.id.list);

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            registerForContextMenu(listView);
        } else {
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
                        switch (item.getItemId()) {
                            case R.id.menu_item_delete_thread:
                                ThreadAdapter adapter = (ThreadAdapter)getListAdapter();
                                StashData stash = StashData.get(getActivity());
                                for (int i = adapter.getCount() - 1; i >= 0; i--) {
                                    if (getListView().isItemChecked(i)) {
                                        StashThread thread = stash.getThread(adapter.getItem(i));
                                        stash.deleteThread(thread);
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
                    // required but not used in this implementation
                }
            });
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
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
                StashThread thread = new StashThread();
                StashData.get(getActivity()).addThread(thread);
                Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
                i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, thread.getId());
                startActivityForResult(i, 0);
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
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
            int position = info.position;
            ThreadAdapter adapter = (ThreadAdapter) getListAdapter();
            StashThread thread = StashData.get(getActivity()).getThread(adapter.getItem(position));

            switch (item.getItemId()) {
                case R.id.menu_item_delete_thread:
                    StashData.get(getActivity()).deleteThread(thread);
                    mThreads = StashData.get(getActivity()).getThreadList();
                    adapter.notifyDataSetChanged();
                    return true;
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
        startActivity(i);
    }

    private class ThreadAdapter extends ArrayAdapter<UUID> {

        public ThreadAdapter(ArrayList<UUID> threads) {
            super(getActivity(), 0, threads);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_thread, null);
            }

            // configure the view for this thread
            StashThread thread = StashData.get(getActivity()).getThread(getItem(position));

            TextView threadTextView = (TextView)convertView.findViewById(R.id.thread_list_item_flossIdTextView);
            threadTextView.setText(thread.toString());

            CheckBox ownedCheckBox = (CheckBox)convertView.findViewById(R.id.thread_list_item_ownedCheckBox);
            ownedCheckBox.setChecked(thread.isOwned());

            return convertView;
        }

    }

}
