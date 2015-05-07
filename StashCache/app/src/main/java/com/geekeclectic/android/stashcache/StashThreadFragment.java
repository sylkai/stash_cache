package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Fragment to display information for a thread in the stash and allow the user to edit it.
 */

public class StashThreadFragment extends Fragment {

    public static final String EXTRA_THREAD_ID = "com.geekeclectic.android.stashcache.thread_id";
    public static final String EXTRA_TAB_ID = "com.geekeclectic.android.stashcache.thread_calling_stash_id";

    private static final int VIEW_ID = StashConstants.THREAD_VIEW;

    private StashThread mThread;
    private EditText mThreadSource;
    private EditText mThreadType;
    private EditText mThreadId;
    private Button mOwnedDecrease;
    private TextView mSkeinsOwned;
    private Button mOwnedIncrease;

    private TextView mTotalKitted;
    private TextView mKittedSkeins;

    private Button mToBuyDecrease;
    private TextView mSkeinsToBuy;
    private Button mToBuyIncrease;

    private TextView mTotalToBuy;

    private ArrayList<StashPattern> mPatternList;
    private ListView mPatternDisplayList;

    private int callingTab;

    public StashThreadFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get id for thread and pull up the appropriate thread from the stash
        UUID threadId = (UUID)getArguments().getSerializable(EXTRA_THREAD_ID);
        callingTab = getArguments().getInt(EXTRA_TAB_ID);
        mThread = StashData.get(getActivity()).getThread(threadId);
        mPatternList = mThread.getPatternsList();
    }

    public static StashThreadFragment newInstance(UUID threadId, int tab) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_THREAD_ID, threadId);
        args.putInt(EXTRA_TAB_ID, tab);

        // set arguments (threadId) and attach to the fragment
        StashThreadFragment fragment = new StashThreadFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    // navigate up to stash overview and sets thread fragment as current
                    Intent i = new Intent(getActivity(), StashOverviewActivity.class);
                    i.putExtra(StashOverviewActivity.EXTRA_FRAGMENT_ID, callingTab);

                    // adjust if necessary because the calling tab was the shopping tab
                    if (callingTab == StashConstants.SHOPPING_TAB) {
                        i.putExtra(StashOverviewActivity.EXTRA_VIEW_ID, VIEW_ID - 1);
                    } else {
                        i.putExtra(StashOverviewActivity.EXTRA_VIEW_ID, VIEW_ID);
                    }

                    NavUtils.navigateUpTo(getActivity(), i);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        StashData.get(getActivity()).saveStash();
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_thread, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mThreadSource = (EditText)v.findViewById(R.id.thread_source);
        mThreadSource.setText(mThread.getSource());
        mThreadSource.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                // listener appears to be called on fragment creation so only make changes
                // if things have actually been changed
                if (!c.toString().equals(mThread.getSource())) {
                    mThread.setSource(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mThread.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mThreadType = (EditText)v.findViewById(R.id.thread_type);
        mThreadType.setText(mThread.getType());
        mThreadType.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                // listener appears to be called on fragment creation so only make changes
                // if things have actually been changed
                if (!c.toString().equals(mThread.getType())) {
                    mThread.setType(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mThread.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mThreadId = (EditText)v.findViewById(R.id.thread_id);
        mThreadId.setText(mThread.getCode());
        mThreadId.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                // listener appears to be called on fragment creation so only make changes
                // if things have actually been changed
                if (!c.toString().equals(mThread.getCode())) {
                    mThread.setCode(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mThread.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mSkeinsOwned = (TextView)v.findViewById(R.id.thread_stash_quantity);
        mSkeinsOwned.setText(Integer.toString(mThread.getSkeinsOwned()));

        mTotalKitted = (TextView)v.findViewById(R.id.thread_total_quantity_needed);
        mTotalKitted.setText(Integer.toString(mThread.getTotalNeeded()));

        mKittedSkeins = (TextView)v.findViewById(R.id.thread_quantity_needed);
        mKittedSkeins.setText(Integer.toString(mThread.getSkeinsNeeded()));

        mTotalToBuy = (TextView)v.findViewById(R.id.thread_quantity_total);
        mTotalToBuy.setText(Integer.toString(mThread.getSkeinsToBuy()));

        mSkeinsToBuy = (TextView)v.findViewById(R.id.thread_toBuy_quantity);
        mSkeinsToBuy.setText(Integer.toString(mThread.getAdditionalSkeins()));

        mOwnedDecrease = (Button)v.findViewById(R.id.thread_decrease_button_owned);
        mOwnedDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mThread.decreaseOwnedQuantity();

                mSkeinsOwned.setText(Integer.toString(mThread.getSkeinsOwned()));
                mKittedSkeins.setText(Integer.toString(mThread.getSkeinsNeeded()));
                mTotalToBuy.setText(Integer.toString(mThread.getSkeinsToBuy()));
            }
        });

        mOwnedIncrease = (Button)v.findViewById(R.id.thread_increase_button_owned);
        mOwnedIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mThread.increaseOwnedQuantity();

                mSkeinsOwned.setText(Integer.toString(mThread.getSkeinsOwned()));
                mKittedSkeins.setText(Integer.toString(mThread.getSkeinsNeeded()));
                mTotalToBuy.setText(Integer.toString(mThread.getSkeinsToBuy()));
            }
        });

        mToBuyDecrease = (Button)v.findViewById(R.id.thread_decrease_button_toBuy);
        mToBuyDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mThread.decreaseAdditionalQuantity();

                mSkeinsToBuy.setText(Integer.toString(mThread.getAdditionalSkeins()));
                mTotalToBuy.setText(Integer.toString(mThread.getSkeinsToBuy()));
            }
        });

        mToBuyIncrease = (Button)v.findViewById(R.id.thread_increase_button_toBuy);
        mToBuyIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mThread.increaseAdditionalQuantity();

                mSkeinsToBuy.setText(Integer.toString(mThread.getAdditionalSkeins()));
                mTotalToBuy.setText(Integer.toString(mThread.getSkeinsToBuy()));
            }
        });

        mTotalToBuy = (TextView)v.findViewById(R.id.thread_quantity_total);
        mTotalToBuy.setText(Integer.toString(mThread.getSkeinsToBuy()));

        mPatternDisplayList = (ListView)v.findViewById(R.id.thread_pattern_list);
        PatternAdapter adapter = new PatternAdapter(mPatternList);
        mPatternDisplayList.setAdapter(adapter);
        mPatternDisplayList.setEmptyView(v.findViewById(R.id.thread_pattern_display));
        mPatternDisplayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewHolder vh = (ViewHolder)view.getTag();

                // start StashThreadPagerActivity
                Intent intent = new Intent(getActivity(), StashPatternPagerActivity.class);
                intent.putExtra(StashPatternFragment.EXTRA_PATTERN_ID, vh.itemId);
                intent.putExtra(StashPatternFragment.EXTRA_TAB_ID, callingTab);
                startActivity(intent);
            }
        });

        setListViewHeightBasedOnChildren(mPatternDisplayList);

        return v;
    }

    private void setListViewHeightBasedOnChildren(ListView listView) {
        // method modified from an answer here: http://stackoverflow.com/questions/3495890/how-can-i-put-a-listview-into-a-scrollview-without-it-collapsing/3495908#3495908
        // to set the height based on a maximum number of items on the listView
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = listView.getPaddingTop() + listView.getPaddingBottom();
        if (listAdapter.getCount() > StashConstants.DISPLAY_LIST_ITEMS) {
            for (int i = 0; i < StashConstants.DISPLAY_LIST_ITEMS; i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup)
                    listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        } else {
            for (int i = 0; i < listAdapter.getCount(); i++) {
                View listItem = listAdapter.getView(i, null, listView);
                if (listItem instanceof ViewGroup)
                    listItem.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private class PatternAdapter extends ArrayAdapter<StashPattern> {

        public PatternAdapter(ArrayList<StashPattern> patterns) {
            super(getActivity(), StashConstants.NO_RESOURCE, patterns);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_thread_pattern, null);

                ViewHolder vh = new ViewHolder();
                vh.info = (TextView)convertView.findViewById(R.id.thread_pattern_item_description);
                vh.quantity = (TextView)convertView.findViewById(R.id.thread_pattern_quantity);
                convertView.setTag(vh);
            }

            ViewHolder vh =  (ViewHolder)convertView.getTag();

            // configure the view for this thread
            StashPattern pattern = getItem(position);

            vh.info.setText(pattern.toString());
            vh.quantity.setText(Integer.toString(pattern.getQuantity(mThread)));
            vh.itemId = pattern.getId();

            return convertView;
        }

    }

    private static class ViewHolder {
        public TextView info;
        public TextView quantity;
        public UUID itemId;
    }

}
