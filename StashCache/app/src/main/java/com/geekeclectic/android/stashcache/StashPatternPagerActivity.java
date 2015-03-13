package com.geekeclectic.android.stashcache;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Activity to host PatternFragments in a ViewPager (allow swiping side to side) using a
 * FragmentStatePagerAdapter to avoid clogging up the memory with fragments.  Implements the
 * ChangedFragmentListener to listen for a notice to trigger the fragment refresh for the fragments
 * loaded by the adapter to update display in case changes needed in display.
 */

public class StashPatternPagerActivity extends FragmentActivity implements StashPatternFragment.ChangedFragmentListener {

    private ViewPager mViewPager;
    private ArrayList<StashPattern> mPatterns;

    private int callingTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize viewPager
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.patternViewPager);
        setContentView(mViewPager);

        // get list of patterns for the viewPager
        mPatterns = StashData.get(this).getPatternData();

        // set viewPager adapter
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new ObservedAdapter(fm));

        callingTab = getIntent().getIntExtra(StashPatternFragment.EXTRA_TAB_ID, 0);

        // pull the UUID from the desired fragment extra and iterate through the list to set it as
        // the current view
        UUID patternId = (UUID)getIntent().getSerializableExtra(StashPatternFragment.EXTRA_PATTERN_ID);
        for (int i = 0; i < mPatterns.size(); i++) {
            if (mPatterns.get(i).getId().equals(patternId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

    public void updateFragments() {
        ((ObservedAdapter)mViewPager.getAdapter()).updateFragments();
    }

    private class ObservedAdapter extends FragmentStatePagerAdapter {
        private Observable mObservers = new FragmentObserver();

        public ObservedAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mPatterns.size();
        }

        @Override
        public Fragment getItem(int pos) {
            // get pattern for the fragment and create fragment
            StashPattern pattern = mPatterns.get(pos);
            Fragment fragment = StashPatternFragment.newInstance(pattern.getId(), callingTab);

            // add fragment to the list of observers
            if (fragment instanceof Observer) {
                mObservers.addObserver((Observer) fragment);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // if fragment is being removed from memory, remove it from list of observers
            if (object instanceof Observer) {
                mObservers.deleteObserver((Observer) object);
            }

            super.destroyItem(container, position, object);
        }

        public void updateFragments() {
            // tell the observers to trigger their refresh
            mObservers.notifyObservers();
        }
    }

}
