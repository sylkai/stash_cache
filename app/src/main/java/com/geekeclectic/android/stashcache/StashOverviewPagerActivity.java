package com.geekeclectic.android.stashcache;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * Created by sylk on 9/4/2014.
 */
public class StashOverviewPagerActivity extends FragmentActivity {

    static final int ITEMS = 3;

    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stash_overview);

        mViewPager = (ViewPager)findViewById(R.id.stashViewPager);

    }

    public class StashOverviewPagerAdapter extends FragmentStatePagerAdapter {


        public StashOverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 1: // fabric list
                    return new FabricListFragment();
                case 2: // thread list
                    return new ThreadListFragment();
                default: // pattern list
                    return new PatternListFragment();
            }
        }

        @Override
        public int getCount() {
            return ITEMS;
        }
    }

}

