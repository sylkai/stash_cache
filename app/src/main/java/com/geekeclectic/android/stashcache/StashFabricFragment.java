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

/**
 * Created by sylk on 8/27/2014.
 */
public class StashFabricFragment extends Fragment {

    private StashFabric mFabric;
    private StashPattern mPattern;
    private EditText mFabricSource;
    private EditText mFabricType;
    private EditText mFabricColor;
    private EditText mFabricCount;
    private EditText mFabricWidth;
    private EditText mFabricHeight;
    private TextView mPatternInfo;

    public StashFabricFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFabric = new StashFabric();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fabric, container, false);

        mFabricSource = (EditText)v.findViewById(R.id.fabric_source);
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
        mFabricType.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setType(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricColor = (EditText)v.findViewById(R.id.fabric_color);
        mFabricColor.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setColor(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricCount = (EditText)v.findViewById(R.id.fabric_count);
        mFabricCount.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setCount(Integer.parseInt(c.toString()));
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricWidth = (EditText)v.findViewById(R.id.fabric_width);
        mFabricWidth.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setWidth(Integer.parseInt(c.toString()));
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mFabricHeight = (EditText)v.findViewById(R.id.fabric_height);
        mFabricHeight.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setHeight(Integer.parseInt(c.toString()));
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mPatternInfo = (TextView)v.findViewById(R.id.fabric_pattern_display);
        if (mPattern != null) {
            mPatternInfo.setText(mPattern.getPatternName());
        }

        return v;
    }
}