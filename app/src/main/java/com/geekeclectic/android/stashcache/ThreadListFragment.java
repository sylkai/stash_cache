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
public class ThreadListFragment extends ListFragment {

    private ArrayList<UUID> mThreads;

    private static final String TAG = "ThreadListFragment";

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
        registerForContextMenu(listView);

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
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
        int position = info.position;
        ThreadAdapter adapter = (ThreadAdapter)getListAdapter();
        StashThread thread = StashData.get(getActivity()).getThread(adapter.getItem(position));

        switch (item.getItemId()) {
            case R.id.menu_item_delete_thread:
                StashData.get(getActivity()).deleteThread(thread);
                mThreads = StashData.get(getActivity()).getThreadList();
                adapter.notifyDataSetChanged();
                return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashThread from adapter
        StashThread thread = StashData.get(getActivity()).getThread(((ThreadAdapter)getListAdapter()).getItem(position));
        Log.d(TAG, thread.toString() + " was clicked.");

        // start StashThreadPagerActivity
        Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
        i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, thread.getId());
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
