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
public class StashPatternPagerActivity extends FragmentActivity {

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
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mPatterns.size();
            }

            @Override
            public Fragment getItem(int pos) {
                StashPattern pattern = mPatterns.get(pos);
                return StashPatternFragment.newInstance(pattern.getId());
            }
        });

        UUID patternId = (UUID)getIntent().getSerializableExtra(StashPatternFragment.EXTRA_PATTERN_ID);
        for (int i = 0; i < mPatterns.size(); i++) {
            if (mPatterns.get(i).getId().equals(patternId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
