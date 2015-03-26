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
 * Fragment to display information for an embellishment in the stash and allow the user to edit it.
 */
public class StashEmbellishmentFragment extends Fragment{

    public static final String EXTRA_EMBELLISHMENT_ID = "com.geekeclectic.android.stashcache.embellishment_id";
    public static final String EXTRA_TAB_ID = "com.geekeclectic.android.stashcache.embellishment_calling_stash_id";

    private static final int VIEW_ID = StashConstants.EMBELLISHMENT_VIEW;

    private StashEmbellishment mEmbellishment;
    private EditText mEmbellishmentSource;
    private EditText mEmbellishmentType;
    private EditText mEmbellishmentId;

    private TextView mNumberOwned;
    private Button mOwnedDecrease;
    private Button mOwnedIncrease;

    private TextView mNumberNeeded;

    private TextView mNumberToBuy;
    private Button mToBuyDecrease;
    private Button mToBuyIncrease;

    private TextView mTotalToBuy;

    private ArrayList<StashPattern> mPatternList;
    private ListView mPatternDisplayList;

    private int callingTab;

    public StashEmbellishmentFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get id for embellishment and pull up the appropriate embellishment from the stash
        UUID embellishmentId = (UUID)getArguments().getSerializable(EXTRA_EMBELLISHMENT_ID);
        callingTab = getArguments().getInt(EXTRA_TAB_ID);
        mEmbellishment = StashData.get(getActivity().getApplicationContext()).getEmbellishment(embellishmentId);
        mPatternList = mEmbellishment.getPatternList();
    }

    public static StashEmbellishmentFragment newInstance(UUID embellishmentId, int tab) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EMBELLISHMENT_ID, embellishmentId);
        args.putInt(EXTRA_TAB_ID, tab);

        // set arguments (embellishmentId) and attach to the fragment
        StashEmbellishmentFragment fragment = new StashEmbellishmentFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    // navigate up to stash overview and sets embellishment fragment as current
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
        StashData.get(getActivity().getApplicationContext()).saveStash();
    }

    @TargetApi(11)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_embellishment, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mEmbellishmentSource = (EditText)v.findViewById(R.id.embellishment_source);
        mEmbellishmentSource.setText(mEmbellishment.getSource());
        mEmbellishmentSource.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (!c.toString().equals(mEmbellishment.getSource())) {
                    mEmbellishment.setSource(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mEmbellishment.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mEmbellishmentType = (EditText)v.findViewById(R.id.embellishment_type);
        mEmbellishmentType.setText(mEmbellishment.getType());
        mEmbellishmentType.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (!c.toString().equals(mEmbellishment.getType())) {
                    mEmbellishment.setType(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mEmbellishment.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mEmbellishmentId = (EditText)v.findViewById(R.id.embellishment_id);
        mEmbellishmentId.setText(mEmbellishment.getCode());
        mEmbellishmentId.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (!c.toString().equals(mEmbellishment.getCode())) {
                    mEmbellishment.setCode(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mEmbellishment.toString());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mNumberOwned = (TextView)v.findViewById(R.id.embellishment_stash_quantity);
        mNumberOwned.setText(Integer.toString(mEmbellishment.getNumberOwned()));

        mNumberNeeded = (TextView)v.findViewById(R.id.embellishment_quantity_needed);
        mNumberNeeded.setText(Integer.toString(mEmbellishment.getNumberNeeded()));

        mTotalToBuy = (TextView)v.findViewById(R.id.embellishment_quantity_total);
        mTotalToBuy.setText(Integer.toString(mEmbellishment.getNumberToBuy()));

        mNumberToBuy = (TextView)v.findViewById(R.id.embellishment_toBuy_quantity);
        mNumberToBuy.setText(Integer.toString(mEmbellishment.getAdditionalNeeded()));

        mOwnedDecrease = (Button)v.findViewById(R.id.embellishment_decrease_button_owned);
        mOwnedDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmbellishment.decreaseOwned();

                mNumberOwned.setText(Integer.toString(mEmbellishment.getNumberOwned()));
                mNumberNeeded.setText(Integer.toString(mEmbellishment.getNumberNeeded()));
                mTotalToBuy.setText(Integer.toString(mEmbellishment.getNumberToBuy()));
            }
        });

        mOwnedIncrease = (Button)v.findViewById(R.id.embellishment_increase_button_owned);
        mOwnedIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmbellishment.increaseOwned();

                mNumberOwned.setText(Integer.toString(mEmbellishment.getNumberOwned()));
                mNumberNeeded.setText(Integer.toString(mEmbellishment.getNumberNeeded()));
                mTotalToBuy.setText(Integer.toString(mEmbellishment.getNumberToBuy()));
            }
        });

        mToBuyDecrease = (Button)v.findViewById(R.id.embellishment_decrease_button_toBuy);
        mToBuyDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmbellishment.decreaseAdditional();

                mTotalToBuy.setText(Integer.toString(mEmbellishment.getNumberToBuy()));
                mNumberToBuy.setText(Integer.toString(mEmbellishment.getAdditionalNeeded()));
            }
        });

        mToBuyIncrease = (Button)v.findViewById(R.id.embellishment_increase_button_toBuy);
        mToBuyIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmbellishment.increaseAdditional();

                mTotalToBuy.setText(Integer.toString(mEmbellishment.getNumberToBuy()));
                mNumberToBuy.setText(Integer.toString(mEmbellishment.getAdditionalNeeded()));
            }
        });

        mPatternDisplayList = (ListView)v.findViewById(R.id.embellishment_pattern_list);
        PatternAdapter adapter = new PatternAdapter(mPatternList);
        mPatternDisplayList.setAdapter(adapter);
        mPatternDisplayList.setEmptyView(v.findViewById(R.id.embellishment_pattern_display));
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
            super(getActivity().getApplicationContext(), 0, patterns);
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
            vh.quantity.setText(Integer.toString(pattern.getQuantity(mEmbellishment)));
            vh.itemId = pattern.getId();

            return convertView;
        }

    }

    private static class ViewHolder {
        TextView info;
        TextView quantity;
        UUID itemId;
    }
}
