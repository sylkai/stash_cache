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
 * Fragment to host the viewPager managing the listView fragments displaying the lists of different
 * stash components (patterns, fabrics, threads).  Scrolling tab bar above identifies which list
 * is active.  The callback to the hosting activity keeps track of which component is currently
 * displayed, to allow the same component to be displayed when switching between stash/master/shopping.
 */

public class MasterOverviewPagerFragment extends UpdateFragment {

    public static final String TAG = "MasterOverview";

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
                // keep track of which fragment is currently displayed and let the host activity know
                mCallback.onTabSwipe(currentPage);
                currentView = currentPage;
            }
        });

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // exceptions handled because Java, not because they should ever be called
        try {
            mCallback = (OnTabSwipeListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnTabSwipeListener");
        }
    }

    @Override
    public void setCurrentView(int view) {
        // called by the host activity to set which view is active (called before onCreateView is called)
        currentView = view;
    }

    @Override
    public void stashChanged() {
        // called by the host activity to tell the lists to update their datasets
        mAdapter.updateFragments();
    }

    public class StashOverviewPagerAdapter extends FragmentStatePagerAdapter {

        private Observable mObservers = new FragmentObserver();

        public StashOverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return StashConstants.STASH_CATEGORIES;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            switch (i) {
                case StashConstants.FABRIC_VIEW: // fabric list
                    fragment = StashFabricListFragment.newInstance(StashConstants.MASTER_TAB);
                    break;
                case StashConstants.THREAD_VIEW: // thread list
                    fragment = StashThreadListFragment.newInstance(StashConstants.MASTER_TAB);
                    break;
                case StashConstants.EMBELLISHMENT_VIEW: // embellishment list
                    fragment = StashEmbellishmentListFragment.newInstance(StashConstants.MASTER_TAB);
                    break;
                default: // pattern list
                    fragment = StashPatternListFragment.newInstance(StashConstants.MASTER_TAB);
            }

            // set up the observers so that actions can trigger a refresh of the displayed list
            if (fragment instanceof Observer) {
                mObservers.addObserver((Observer) fragment);
            }

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case StashConstants.FABRIC_VIEW: // fabric list
                    return getString(R.string.fabric_list_title).toUpperCase();
                case StashConstants.THREAD_VIEW: // thread list
                    return getString(R.string.thread_list_title).toUpperCase();
                case StashConstants.EMBELLISHMENT_VIEW: // embellishment list
                    return getString(R.string.embellishment_list_title).toUpperCase();
                default: // pattern list
                    return getString(R.string.pattern_list_title).toUpperCase();
            }
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }
    }

}

