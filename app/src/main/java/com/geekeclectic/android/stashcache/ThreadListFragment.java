package com.geekeclectic.android.stashcache;

import android.app.ListFragment;
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

/**
 * Created by sylk on 8/27/2014.
 */
public class ThreadListFragment extends ListFragment {

    private ArrayList<StashThread> mThreads;

    private static final String TAG = "ThreadListFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThreads = StashData.get(getActivity()).getThreadList();

        ThreadAdapter adapter = new ThreadAdapter(mThreads);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((ThreadAdapter)getListAdapter()).notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // get StashThread from adapter
        StashThread thread = ((ThreadAdapter)getListAdapter()).getItem(position);
        Log.d(TAG, thread.toString() + " was clicked.");

        // start StashThreadActivity
        Intent i = new Intent(getActivity(), StashThreadActivity.class);
        i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, thread.getId());
        startActivity(i);
    }

    private class ThreadAdapter extends ArrayAdapter<StashThread> {

        public ThreadAdapter(ArrayList<StashThread> threads) {
            super(getActivity(), 0, threads);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_thread, null);
            }

            // configure the view for this thread
            StashThread thread = getItem(position);

            TextView threadTextView = (TextView)convertView.findViewById(R.id.thread_list_item_flossIdTextView);
            threadTextView.setText(thread.toString());

            CheckBox ownedCheckBox = (CheckBox)convertView.findViewById(R.id.thread_list_item_ownedCheckBox);
            ownedCheckBox.setChecked(thread.isOwned());

            return convertView;
        }

    }

}
