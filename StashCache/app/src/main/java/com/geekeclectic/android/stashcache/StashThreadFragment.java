package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
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
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Fragment to display information for a thread in the stash and allow the user to edit it.
 */

public class StashThreadFragment extends Fragment {

    public static final String EXTRA_THREAD_ID = "com.geekeclectic.android.stashcache.thread_id";

    private static final int CATEGORY_ID = 2;

    private StashThread mThread;
    private ArrayList<StashPattern> mPatterns;
    private EditText mThreadSource;
    private EditText mThreadType;
    private EditText mThreadId;
    private EditText mSkeinsOwned;
    private TextView mPatternInfo;

    public StashThreadFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get id for thread and pull up the appropriate thread from the stash
        UUID threadId = (UUID)getArguments().getSerializable(EXTRA_THREAD_ID);
        mThread = StashData.get(getActivity()).getThread(threadId);
    }

    public static StashThreadFragment newInstance(UUID threadId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_THREAD_ID, threadId);

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
                    Intent i = new Intent(getActivity(), StashOverviewPagerActivity.class);
                    i.putExtra(StashOverviewPagerActivity.EXTRA_FRAGMENT_ID, CATEGORY_ID);
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
                mThread.setSource(c.toString());
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
                mThread.setType(c.toString());
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
                mThread.setCode(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mSkeinsOwned = (EditText)v.findViewById(R.id.skeins_owned);
        mSkeinsOwned.setText(Integer.toString(mThread.getSkeinsOwned()));
        mSkeinsOwned.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mThread.setSkeinsOwned(Integer.parseInt(c.toString()));
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mPatternInfo = (TextView)v.findViewById(R.id.thread_pattern_display);

        return v;
    }

}
