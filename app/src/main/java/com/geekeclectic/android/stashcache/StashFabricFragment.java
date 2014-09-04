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

import java.util.UUID;

/**
 * Created by sylk on 8/27/2014.
 */
public class StashFabricFragment extends Fragment {

    public static final String EXTRA_FABRIC_ID = "com.geekeclectic.android.stashcache.fabric_id";

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

        UUID fabricId = (UUID)getArguments().getSerializable(EXTRA_FABRIC_ID);

        mFabric = StashData.get(getActivity()).getFabric(fabricId);
    }

    public static StashFabricFragment newInstance(UUID patternId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_FABRIC_ID, patternId);

        StashFabricFragment fragment = new StashFabricFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_fabric, container, false);

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
        mFabricColor.setText(mFabric.getColor());
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
        mFabricCount.setText(Integer.toString(mFabric.getCount()));
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
        mFabricWidth.setText(Double.toString(mFabric.getWidth()));
        mFabricWidth.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mFabric.setWidth(Double.parseDouble(c.toString()));
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
                mFabric.setHeight(Double.parseDouble(c.toString()));
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