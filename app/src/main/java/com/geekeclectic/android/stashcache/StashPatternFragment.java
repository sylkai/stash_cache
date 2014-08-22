package com.geekeclectic.android.stashcache;

import android.os.Bundle;
import android.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.UUID;

public class StashPatternFragment extends Fragment {

    private StashPattern mPattern;
    private UUID mPatternId;
    private EditText mTitleField;
    private EditText mDesignerField;
    private EditText mWidthField;
    private EditText mHeightField;


    public StashPatternFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPattern = new StashPattern();
        mPatternId = mPattern.getId();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pattern, container, false);

        mTitleField = (EditText)v.findViewById(R.id.pattern_name);
        mTitleField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mPattern.setTitle(c.toString());
                getActivity().setTitle(mPattern.getTitle());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mDesignerField = (EditText)v.findViewById(R.id.designer_name);
        mDesignerField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mPattern.setDesigner(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mWidthField = (EditText)v.findViewById(R.id.pattern_width);
        mWidthField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mPattern.setWidth(Integer.parseInt(c.toString()));
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mHeightField = (EditText)v.findViewById(R.id.pattern_height);
        mHeightField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mPattern.setHeight(Integer.parseInt(c.toString()));
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


}
