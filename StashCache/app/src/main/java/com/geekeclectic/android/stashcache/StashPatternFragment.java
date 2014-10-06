package com.geekeclectic.android.stashcache;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class StashPatternFragment extends Fragment implements PickOneDialogFragment.OnDialogPickOneListener, SelectFabricDialogFragment.SelectFabricDialogListener {

    public static final String EXTRA_PATTERN_ID = "com.geekeclectic.android.stashcache.pattern_id";
    public static final String TAG = "StashPatternFragment";
    private static final int PICK_NEW_FABRIC_REQUEST = 0;
    private static final int RESULT_OK = 0;

    private static final String DIALOG_FABRIC = "fabric";
    private static final int CATEGORY_ID = 0;

    private StashPattern mPattern;
    private StashPatternFragment mFragment;
    private StashFabric mFabric;
    private ArrayList<StashThread> mThreadList;
    private UUID mPatternId;
    private EditText mTitleField;
    private EditText mSourceField;
    private EditText mWidthField;
    private EditText mHeightField;
    private ImageButton mEditFabric;
    private TextView mFabricInfo;
    private TextView mThreadInfo;


    public StashPatternFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mPatternId = (UUID)getArguments().getSerializable(EXTRA_PATTERN_ID);
        mPattern = StashData.get(getActivity()).getPattern(mPatternId);
        mFragment = this;

        mFabric = mPattern.getFabric();
        mThreadList = mPattern.getThreadList();
    }

    public static StashPatternFragment newInstance(UUID patternId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_PATTERN_ID, patternId);

        StashPatternFragment fragment = new StashPatternFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(getActivity()) != null) {
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
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_pattern, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (NavUtils.getParentActivityName(getActivity()) != null) {
                getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        mTitleField = (EditText)v.findViewById(R.id.pattern_name);
        mTitleField.setText(mPattern.getPatternName());
        mTitleField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mPattern.setPatternName(c.toString());
                getActivity().setTitle(mPattern.getPatternName());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mSourceField = (EditText)v.findViewById(R.id.designer_name);
        mSourceField.setText(mPattern.getPatternSource());
        mSourceField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mPattern.setPatternSource(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mWidthField = (EditText)v.findViewById(R.id.pattern_width);
        mWidthField.setText(Integer.toString(mPattern.getWidth()));
        mWidthField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mPattern.setWidth(Integer.parseInt(c.toString()));
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mHeightField = (EditText)v.findViewById(R.id.pattern_height);
        mHeightField.setText(Integer.toString(mPattern.getHeight()));
        mHeightField.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mPattern.setHeight(Integer.parseInt(c.toString()));
                }
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mEditFabric = (ImageButton)v.findViewById(R.id.pattern_fabric_edit);
        mEditFabric.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                PickOneDialogFragment dialog = PickOneDialogFragment.newInstance(R.array.fabric_choice_picker, 0);
                dialog.setDialogPickOneListener(mFragment);
                dialog.show(fm, DIALOG_FABRIC);
            }
        });

        mFabricInfo = (TextView)v.findViewById(R.id.pattern_fabric_display);
        updateFabricInfo();

        mThreadInfo = (TextView)v.findViewById(R.id.pattern_thread_display);

        return v;
    }

    public void onSelectedOption(int selectedIndex) {
        if (selectedIndex == 0) {
            Log.d(TAG, "User chose to use existing fabric");

            ArrayList<UUID> fabricStash = StashData.get(getActivity()).getFabricList();
            ArrayList<UUID> possibleFabrics = new ArrayList<UUID>();

            for (UUID fabricId : fabricStash) {
                StashFabric fabric = StashData.get(getActivity()).getFabric(fabricId);
                if (fabric.willFit(mPattern.getWidth(), mPattern.getHeight()) || fabric.willFit(mPattern.getHeight(), mPattern.getWidth())) {
                    possibleFabrics.add(fabricId);
                }
            }

            int previousFabric = possibleFabrics.indexOf(mFabric.getId());

            FragmentManager fm = getActivity().getSupportFragmentManager();
            SelectFabricDialogFragment dialog = SelectFabricDialogFragment.newInstance(possibleFabrics, previousFabric);
            dialog.setSelectFabricDialogListener(mFragment);
            dialog.show(fm, DIALOG_FABRIC);
        } else {
            Log.d(TAG, "User chose to create new fabric");

            if (mFabric != null) {
                mFabric.setUsedFor(null);
            }

            mFabric = new StashFabric();
            StashData.get(getActivity()).addFabric(mFabric);

            mFabric.setUsedFor(mPattern);
            mPattern.setFabric(mFabric);

            Intent i = new Intent(getActivity(), StashFabricPagerActivity.class);
            i.putExtra(StashFabricFragment.EXTRA_FABRIC_ID, mFabric.getId());
            startActivityForResult(i, PICK_NEW_FABRIC_REQUEST);
        }
    }

    public void onSelectedFabric(UUID fabricId) {
        if (mFabric != null) {
            mFabric.setUsedFor(null);
        }

        mFabric = StashData.get(getActivity()).getFabric(fabricId);

        mFabric.setUsedFor(mPattern);
        mPattern.setFabric(mFabric);

        updateFabricInfo();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_NEW_FABRIC_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateFabricInfo();
            }
        }
    }

    private void updateFabricInfo() {
        if (mFabric != null) {
            mFabricInfo.setText(mFabric.getInfo() + "\n");
            mFabricInfo.append(mFabric.getSize());
        } else {
            mFabricInfo.setText(R.string.pattern_no_fabric);
        }
    }


}
