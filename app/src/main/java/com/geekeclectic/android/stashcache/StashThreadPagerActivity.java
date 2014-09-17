package com.geekeclectic.android.stashcache;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 9/4/2014.
 */
public class StashThreadPagerActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private ArrayList<UUID> mThreads;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.threadViewPager);
        setContentView(mViewPager);

        mThreads = StashData.get(this).getThreadList();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mThreads.size();
            }

            @Override
            public Fragment getItem(int pos) {
                StashThread thread = StashData.get(getParent()).getThread(mThreads.get(pos));
                return StashThreadFragment.newInstance(thread.getId());
            }
        });

        UUID threadId = (UUID)getIntent().getSerializableExtra(StashThreadFragment.EXTRA_THREAD_ID);
        for (int i = 0; i < mThreads.size(); i++) {
            if (mThreads.get(i).equals(threadId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
