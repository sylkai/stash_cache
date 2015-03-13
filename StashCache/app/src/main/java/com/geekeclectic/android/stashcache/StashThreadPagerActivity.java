package com.geekeclectic.android.stashcache;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Host activity for a viewpager to display StashThread fragments to the user, in order to allow
 * swiping between fragments on the list.  Uses a FragmentStatePagerAdapter to reduce memory load.
 */
public class StashThreadPagerActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private ArrayList<UUID> mThreads;
    private int callingTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create viewPager
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.threadViewPager);
        setContentView(mViewPager);

        // get list of threads
        mThreads = StashData.get(this).getThreadList();
        Collections.sort(mThreads, new StashThreadComparator(this));

        // create and set fragment manager to return appropriate fragments
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mThreads.size();
            }

            @Override
            public Fragment getItem(int pos) {
                return StashThreadFragment.newInstance(mThreads.get(pos), callingTab);
            }
        });

        callingTab = getIntent().getIntExtra(StashThreadFragment.EXTRA_TAB_ID, 0);

        // get the id for the desired thread and set the appropriate fragment as current
        UUID threadId = (UUID)getIntent().getSerializableExtra(StashThreadFragment.EXTRA_THREAD_ID);
        for (int i = 0; i < mThreads.size(); i++) {
            if (mThreads.get(i).equals(threadId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
