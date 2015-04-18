package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Fragment to display information for a fabric in the stash and allow the user to edit it.
 */

public class StashFabricFragment extends Fragment implements Observer {

    public static final String EXTRA_FABRIC_ID = "com.geekeclectic.android.stashcache.fabric_id";
    public static final String EXTRA_TAB_ID = "com.geekeclectic.android.stashcache.fabric_calling_stash_id";

    private static final int VIEW_ID = StashConstants.FABRIC_VIEW;

    private StashFabric mFabric;
    private ArrayList<StashPattern> mPattern;
    private EditText mFabricSource;
    private EditText mFabricType;
    private EditText mFabricColor;
    private EditText mFabricCount;
    private EditText mFabricWidth;
    private EditText mFabricHeight;
    private ListView mPatternDisplay;
    private TextView mPatternInfo;
    private View mStartDateGroup;
    private ImageView mEditStartDate;
    private TextView mStartDate;
    private View mFinishDateGroup;
    private ImageView mEditFinishDate;
    private TextView mFinishDate;
    private EditText mNotes;

    private int callingTab;

    public StashFabricFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get the fabric id from the arguments bundle and use that to get the appropriate fabric
        UUID fabricId = (UUID)getArguments().getSerializable(EXTRA_FABRIC_ID);
        callingTab = getArguments().getInt(EXTRA_TAB_ID);
        mFabric = StashData.get(getActivity()).getFabric(fabricId);

        mPattern = new ArrayList<StashPattern>();
        if (mFabric.usedFor() != null) {
            mPattern.add(mFabric.usedFor());
        }
    }

    public static StashFabricFragment newInstance(UUID patternId, int tab) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_FABRIC_ID, patternId);
        args.putInt(EXTRA_TAB_ID, tab);

        StashFabricFragment fragment = new StashFabricFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    // sets up navigation to go back to proper list fragment
                    Intent i = new Intent(getActivity(), StashOverviewActivity.class);
                    i.putExtra(StashOverviewActivity.EXTRA_FRAGMENT_ID, callingTab);
                    i.putExtra(StashOverviewActivity.EXTRA_VIEW_ID, VIEW_ID);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fabric, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mFabricSource = (EditText)v.findViewById(R.id.fabric_source);
        mFabricSource.setText(mFabric.getSource());
        mFabricSource.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setSource(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricType = (EditText)v.findViewById(R.id.fabric_type);
        mFabricType.setText(mFabric.getType());
        mFabricType.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (!c.toString().equals(mFabric.getType())) {
                    mFabric.setType(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mFabric.getInfo());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricColor = (EditText)v.findViewById(R.id.fabric_color);
        mFabricColor.setText(mFabric.getColor());
        mFabricColor.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (!c.toString().equals(mFabric.getColor())) {
                    mFabric.setColor(c.toString());
                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mFabric.getInfo());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricCount = (EditText)v.findViewById(R.id.fabric_count);
        mFabricCount.setText(Integer.toString(mFabric.getCount()));
        mFabricCount.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (!Integer.toString(mFabric.getCount()).equals(c.toString())) {
                    if (c.length() > 0) {
                        mFabric.setCount(Integer.parseInt(c.toString()));
                    } else {
                        mFabric.setCount(StashConstants.INT_ZERO);
                    }

                    ActionBar actionBar = getActivity().getActionBar();
                    actionBar.setSubtitle(mFabric.getInfo());
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricWidth = (EditText)v.findViewById(R.id.fabric_width);
        mFabricWidth.setText(Double.toString(mFabric.getWidth()));
        mFabricWidth.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mFabric.setWidth(Double.parseDouble(c.toString()));
                } else {
                    mFabric.setWidth(StashConstants.DOUBLE_ZERO);
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricHeight = (EditText)v.findViewById(R.id.fabric_height);
        mFabricHeight.setText(Double.toString(mFabric.getHeight()));
        mFabricHeight.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mFabric.setHeight(Double.parseDouble(c.toString()));
                } else {
                    mFabric.setHeight(StashConstants.DOUBLE_ZERO);
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mPatternDisplay = (ListView)v.findViewById(R.id.fabric_pattern_list);
        mPatternInfo = (TextView)v.findViewById(R.id.fabric_pattern_display);

        PatternAdapter adapter = new PatternAdapter(mPattern);
        mPatternDisplay.setAdapter(adapter);
        mPatternDisplay.setEmptyView(mPatternInfo);
        mPatternDisplay.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ViewHolder vh = (ViewHolder) view.getTag();

                // start StashThreadPagerActivity
                Intent intent = new Intent(getActivity(), StashPatternPagerActivity.class);
                intent.putExtra(StashPatternFragment.EXTRA_PATTERN_ID, vh.itemId);
                intent.putExtra(StashPatternFragment.EXTRA_TAB_ID, callingTab);
                startActivity(intent);
            }
        });

        setListViewHeightBasedOnChildren(mPatternDisplay);

        mStartDateGroup = v.findViewById(R.id.fabric_start_group);
        mEditStartDate = (ImageView)v.findViewById(R.id.fabric_start_date_edit);
        mEditStartDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        mStartDate = (TextView)v.findViewById(R.id.fabric_start_date);

        mFinishDateGroup = v.findViewById(R.id.fabric_finish_group);
        mEditFinishDate = (ImageView)v.findViewById(R.id.fabric_finish_date_edit);
        mEditFinishDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            }
        });
        mFinishDate = (TextView)v.findViewById(R.id.fabric_finish_date);

        updateDateInfo();

        mNotes = (EditText)v.findViewById(R.id.fabric_notes);
        mNotes.setText(mFabric.getNotes());
        mNotes.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setNotes(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

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

    @Override
    public void update(Observable observable, Object data) {
        updateDateInfo();
    }

    private void updateDateInfo() {
        if (mFabric.isFinished()) {
            mStartDateGroup.setVisibility(View.VISIBLE);
            mFinishDateGroup.setVisibility(View.VISIBLE);

            Calendar startDate = mFabric.getStartDate();
            if (startDate != null) {
                String startDateText = startDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + startDate.get(Calendar.DAY_OF_MONTH) + ", " + startDate.get(Calendar.YEAR);
                mStartDate.setText(startDateText);
            } else {
                mStartDate.setText(R.string.no_date_set);
            }

            Calendar finishDate = mFabric.getEndDate();
            if (finishDate != null) {
                String finishDateText = finishDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + finishDate.get(Calendar.DAY_OF_MONTH) + ", " + finishDate.get(Calendar.YEAR);
                mFinishDate.setText(finishDateText);
            } else {
                mFinishDate.setText(R.string.no_date_set);
            }
        } else if (mFabric.inUse()) {
            mStartDateGroup.setVisibility(View.VISIBLE);
            mFinishDateGroup.setVisibility(View.GONE);

            Calendar useStartDate = mFabric.getStartDate();
            if (useStartDate != null) {
                String useStartDateText = useStartDate.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + useStartDate.get(Calendar.DAY_OF_MONTH) + ", " + useStartDate.get(Calendar.YEAR);
                mStartDate.setText(useStartDateText);
            } else {
                mStartDate.setText(R.string.no_date_set);
            }
        } else {
            mStartDateGroup.setVisibility(View.GONE);
            mFinishDateGroup.setVisibility(View.GONE);
        }
    }

    private class PatternAdapter extends ArrayAdapter<StashPattern> {

        public PatternAdapter(ArrayList<StashPattern> patterns) {
            super(getActivity(), StashConstants.NO_RESOURCE, patterns);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_fabric_pattern, null);

                ViewHolder vh = new ViewHolder();
                vh.name = (TextView)convertView.findViewById(R.id.fabric_pattern_list_item_nameTextView);
                vh.source = (TextView)convertView.findViewById(R.id.fabric_pattern_list_item_sourceTextView);
                convertView.setTag(vh);
            }

            ViewHolder vh =  (ViewHolder)convertView.getTag();

            // configure the view for this thread
            StashPattern pattern = getItem(position);

            vh.name.setText(pattern.getPatternName());
            vh.source.setText(pattern.getSource());
            vh.itemId = pattern.getId();

            return convertView;
        }

    }

    private static class ViewHolder {
        public TextView name;
        public TextView source;
        public UUID itemId;
    }

}