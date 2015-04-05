package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

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

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
                //
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //
            }

            public void onPageSelected(int currentThread) {
                // keep track of which fragment is currently displayed and let the host activity know
                StashThread thread = StashData.get(getParent()).getThread(mThreads.get(currentThread));
                ActionBar actionBar = getActionBar();

                actionBar.setTitle(getString(R.string.thread));
                actionBar.setSubtitle(thread.toString());
            }
        });

        callingTab = getIntent().getIntExtra(StashThreadFragment.EXTRA_TAB_ID, StashConstants.STASH_TAB);

        // get the id for the desired thread and set the appropriate fragment as current
        UUID threadId = (UUID)getIntent().getSerializableExtra(StashThreadFragment.EXTRA_THREAD_ID);
        for (int i = 0; i < mThreads.size(); i++) {
            if (mThreads.get(i).equals(threadId)) {
                mViewPager.setCurrentItem(i);

                // if the current item is the first one, onPageSelected doesn't fire, so set it manually
                if (i == 0) {
                    StashThread thread = StashData.get(this).getThread(mThreads.get(i));
                    ActionBar actionBar = getActionBar();

                    actionBar.setTitle(getString(R.string.thread));
                    actionBar.setSubtitle(thread.toString());
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.thread_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_thread:
                // create a new thread
                StashThread thread = new StashThread(this);
                StashData.get(this).addThread(thread);

                // notify the adapter the dataset has changed
                mViewPager.getAdapter().notifyDataSetChanged();

                // switch view to the new thread
                UUID threadId = thread.getId();
                for (int i = 0; i < mThreads.size(); i++) {
                    if (mThreads.get(i).equals(threadId)) {
                        mViewPager.setCurrentItem(i);
                        break;
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
