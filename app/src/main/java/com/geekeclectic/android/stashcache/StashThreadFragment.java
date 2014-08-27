package com.geekeclectic.android.stashcache;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by sylk on 8/27/2014.
 */
public class StashThreadFragment extends Fragment {

    private StashThread mThread;
    private ArrayList<StashPattern> mPatterns;
    private EditText mThreadSource;
    private EditText mThreadType;
    private EditText mThreadId;
    private TextView mPatternInfo;

    public StashThreadFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mThread = new StashThread();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_thread, container, false);

        mThreadSource = (EditText)v.findViewById(R.id.thread_source);
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
        mThreadId.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mThread.setId(c.toString());
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
