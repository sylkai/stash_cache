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
 * Created by sylk on 9/4/2014.
 */
public class StashPatternPagerActivity extends FragmentActivity implements StashPatternFragment.ChangedFragmentListener {

    private ViewPager mViewPager;
    private ArrayList<StashPattern> mPatterns;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.patternViewPager);
        setContentView(mViewPager);

        mPatterns = StashData.get(this).getPatternData();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new ObservedAdapter(fm));

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
            //mObservers.deleteObservers();
            StashPattern pattern = mPatterns.get(pos);

            Fragment fragment = StashPatternFragment.newInstance(pattern.getId());

            if (fragment instanceof Observer) {
                mObservers.addObserver((Observer) fragment);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof Observer) {
                mObservers.deleteObserver((Observer) object);
            }

            super.destroyItem(container, position, object);
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }
    }

}
