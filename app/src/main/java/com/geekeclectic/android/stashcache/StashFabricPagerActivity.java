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
public class StashFabricPagerActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private ArrayList<StashFabric> mFabrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.fabricViewPager);
        setContentView(mViewPager);

        mFabrics = StashData.get(this).getFabricList();

        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mFabrics.size();
            }

            @Override
            public Fragment getItem(int pos) {
                StashFabric fabric = mFabrics.get(pos);
                return StashFabricFragment.newInstance(UUID.fromString(fabric.getId()));
            }
        });

        UUID fabricId = (UUID)getIntent().getSerializableExtra(StashFabricFragment.EXTRA_FABRIC_ID);
        for (int i = 0; i < mFabrics.size(); i++) {
            if (mFabrics.get(i).getId().equals(fabricId.toString())) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
