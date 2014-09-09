package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
 * Created by sylk on 8/27/2014.
 */
public class StashThreadFragment extends Fragment {

    public static final String EXTRA_THREAD_ID = "com.geekeclectic.android.stashcache.thread_id";

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

        UUID threadId = (UUID)getArguments().getSerializable(EXTRA_THREAD_ID);
        mThread = StashData.get(getActivity()).getThread(threadId.toString());
    }

    public static StashThreadFragment newInstance(UUID threadId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_THREAD_ID, threadId);

        StashThreadFragment fragment = new StashThreadFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
                    NavUtils.navigateUpFromSameTask(getActivity());
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
        mThreadSource.setText(mThread.getCompany());
        mThreadSource.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mThread.setCompany(c.toString());
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
