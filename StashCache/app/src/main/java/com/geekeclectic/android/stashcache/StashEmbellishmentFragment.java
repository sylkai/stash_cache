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

import java.util.UUID;

/**
 * Created by sylk on 1/19/2015.
 */
public class StashEmbellishmentFragment extends Fragment{

    public static final String EXTRA_EMBELLISHMENT_ID = "com.geekeclectic.android.stashcache.embellishment_id";

    private static final int CATEGORY_ID = 3;

    private StashEmbellishment mEmbellishment;
    private EditText mEmbellishmentSource;
    private EditText mEmbellishmentType;
    private EditText mEmbellishmentId;
    private EditText mNumberOwned;

    public StashEmbellishmentFragment() {
        // required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get id for thread and pull up the appropriate embellishment from the stash
        UUID embellishmentId = (UUID)getArguments().getSerializable(EXTRA_EMBELLISHMENT_ID);
        mEmbellishment = StashData.get(getActivity()).getEmbellishment(embellishmentId);
    }

    public static StashEmbellishmentFragment newInstance(UUID embellishmentId) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_EMBELLISHMENT_ID, embellishmentId);

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

        mEmbellishmentSource = (EditText)v.findViewById(R.id.embellishment_source);
        mEmbellishmentSource.setText(mEmbellishment.getSource());
        mEmbellishmentSource.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                mEmbellishment.setSource(c.toString());
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
                mEmbellishment.setType(c.toString());
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
                mEmbellishment.setCode(c.toString());
            }

            public void beforeTextChanged(CharSequence c, int start, int count, int after) {
                // intentionally left blank
            }

            public void afterTextChanged(Editable c) {
                // intentionally left blank
            }
        });

        mNumberOwned = (EditText)v.findViewById(R.id.number_owned);
        if (mEmbellishment.getNumberOwned() > 0) {
            mNumberOwned.setText(Integer.toString(mEmbellishment.getNumberOwned()));
        }
        mNumberOwned.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence c, int start, int before, int count) {
                if (c.length() > 0) {
                    mEmbellishment.setNumberOwned(Integer.parseInt(c.toString()));
                } else {
                    // user removed skeins owned information
                    mEmbellishment.setNumberOwned(0);
                }
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
