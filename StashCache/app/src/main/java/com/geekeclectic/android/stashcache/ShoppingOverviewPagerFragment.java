package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;

/**
 * Fragment to host the viewPager managing the listView fragments displaying the lists of different
 * stash components (patterns, fabrics, threads).  Scrolling tab bar above identifies which list
 * is active.  Callback to the host activity keeps track of which fragment (fabric for/threads/embellishments)
 * is currently displayed, to allow for maintaining the active fragment between switching categories.
 */

public class ShoppingOverviewPagerFragment extends UpdateFragment {

    public static final String TAG = "ShoppingOverview";

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

    // it turns out Android only calls onActivityResult down one layer, not two, so with nested fragments
    // needing the call I need to do it manually (see http://stackoverflow.com/a/16449850 for reference)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Fragment fragment = mAdapter.getCurrentFragment(mViewPager.getCurrentItem());

        if (fragment != null) {
            fragment.onActivityResult(requestCode, resultCode, intent);
        }
    }

    // allows the host activity to set which view should be active
    @Override
    public void setCurrentView(int view) {
        currentView = view;
    }

    @Override
    public void stashChanged() {
        // called from the host activity to tell the lists to update their datasets
        mAdapter.updateFragments();
    }

    public class StashOverviewPagerAdapter extends FragmentStatePagerAdapter {

        private Observable mObservers = new FragmentObserver();
        private WeakReference<Fragment> threadList;
        private WeakReference<Fragment> embellishmentList;
        private WeakReference<Fragment> patternList;

        public StashOverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return StashConstants.SHOPPING_CATEGORIES;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;
            switch (i) {
                case StashConstants.SHOPPING_THREAD_VIEW: // thread list
                    fragment = StashThreadListFragment.newInstance(StashConstants.SHOPPING_TAB);
                    break;
                case StashConstants.SHOPPING_EMBELLISHMENT_VIEW: // embellishment list
                    fragment = StashEmbellishmentListFragment.newInstance(StashConstants.SHOPPING_TAB);
                    break;
                default: // pattern list
                    fragment = StashPatternListFragment.newInstance(StashConstants.SHOPPING_TAB);
            }

            if (fragment instanceof Observer) {
                mObservers.addObserver((Observer) fragment);
            }

            return fragment;
        }

        // I need a reference for the currently active fragments so that I can return the fragment
        // to call the onActivityResult method, so need to store the references here because getItem
        // is called on item creation only, not when recreating from saved state (see http://stackoverflow.com/a/29287415
        // for reference, using weak references since this is FragmentStatePagerAdapter)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);

            // save the appropriate reference depending on the position
            switch (position) {
                case StashConstants.THREAD_VIEW: // thread list
                    threadList = new WeakReference<Fragment>(createdFragment);
                    break;
                case StashConstants.EMBELLISHMENT_VIEW: // embellishment list
                    embellishmentList = new WeakReference<Fragment>(createdFragment);
                    break;
                default: // pattern list
                    patternList = new WeakReference<Fragment>(createdFragment);
            }

            return createdFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case StashConstants.SHOPPING_THREAD_VIEW: // thread list
                    return getString(R.string.thread_list_title).toUpperCase();
                case StashConstants.SHOPPING_EMBELLISHMENT_VIEW: // embellishment list
                    return getString(R.string.embellishment_list_title).toUpperCase();
                default: // pattern list
                    return getString(R.string.shopping_pattern_list_title).toUpperCase();
            }
        }

        // have to remember to remove the observer when the item is destroyed!
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Fragment fragment = (Fragment)object;

            if (fragment instanceof Observer) {
                mObservers.deleteObserver((Observer) fragment);
            }

            super.destroyItem(container, position, object);
        }

        public void updateFragments() {
            mObservers.notifyObservers();
        }

        // returns the current fragment for that position, if it exists so that actions can be
        // called on it (like onActivityResult)
        public Fragment getCurrentFragment(int position) {
            Fragment fragment;
            switch (position) {
                case StashConstants.THREAD_VIEW: // thread list
                    if (threadList != null) {
                        fragment = threadList.get();
                        return fragment;
                    } else {
                        return null;
                    }
                case StashConstants.EMBELLISHMENT_VIEW: // embellishment list
                    if (embellishmentList != null) {
                        fragment = embellishmentList.get();
                        return fragment;
                    } else {
                        return null;
                    }
                default: // pattern list
                    if (patternList != null) {
                        fragment = patternList.get();
                        return fragment;
                    } else {
                        return null;
                    }
            }
        }
    }

}

