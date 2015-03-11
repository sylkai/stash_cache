package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Observable;
import java.util.Observer;

/**
 * Activity to host the viewPager managing the listView fragments displaying the lists of different
 * stash components (patterns, fabrics, threads).  Scrolling tab bar above identifies which list
 * is active.  EXTRA_FRAGMENT_ID indicates which item class is displayed when navigating up through
 * hierarchy.
 */

public class ShoppingOverviewPagerFragment extends UpdateFragment {

    static final int ITEMS = 3;
    static final String TAG = "ShoppingOverview";
    public static final String EXTRA_FRAGMENT_ID = "com.geekeclectic.android.stashcache.active_fragment_id";

    private ViewPager mViewPager;
    private StashOverviewPagerAdapter mAdapter;
    private OnTabSwipeListener mCallback;
    private int currentView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_stash_overview, container, false);

        mViewPager = (ViewPager) root.findViewById(R.id.stashViewPager);
        mAdapter = new StashOverviewPagerAdapter(getChildFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(currentView, false);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
                //
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //
            }

            public void onPageSelected(int currentPage) {
                mCallback.onTabSwipe(currentPage);
                currentView = currentPage;
            }
        });

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnTabSwipeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTabSwipeListener");
        }
    }

    @Override
    public void setCurrentView(int view) {
        currentView = view;
    }

    @Override
    public void stashChanged() {
        updateFragments();
    }

    private void updateFragments() {
        mAdapter.updateFragments();
    }

    public class StashOverviewPagerAdapter extends FragmentStatePagerAdapter {

        private Observable mObservers = new FragmentObserver();

        public StashOverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return ITEMS;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            switch (i) {
                case 1: // thread list
                    fragment = StashThreadListFragment.newInstance("shopping");
                    break;
                case 2: // embellishment list
                    fragment = StashEmbellishmentListFragment.newInstance("shopping");
                    break;
                default: // pattern list
                    fragment = StashPatternListFragment.newInstance("shopping");
            }

            if (fragment instanceof Observer) {
                mObservers.addObserver((Observer) fragment);
            }

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1: // thread list
                    return getString(R.string.thread_list_title).toUpperCase();
                case 2: // embellishment list
                    return getString(R.string.embellishment_list_title).toUpperCase();
                default: // pattern list
                    return getString(R.string.shopping_pattern_list_title).toUpperCase();
            }
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }
    }

}

