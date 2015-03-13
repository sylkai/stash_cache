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
 * Host activity for a viewpager to display StashFabric fragments to the user, in order to allow
 * swiping between fragments on the list.  Uses a FragmentStatePagerAdapter to reduce memory load.
 */

public class StashFabricPagerActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private ArrayList<UUID> mFabrics;

    private int callingTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create and set ViewPager
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.fabricViewPager);
        setContentView(mViewPager);

        // get list of fabrics for viewPager
        mFabrics = StashData.get(this).getFabricList();

        callingTab = getIntent().getIntExtra(StashFabricFragment.EXTRA_TAB_ID, 0);

        // set fragment manager to display fragments
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mFabrics.size();
            }

            @Override
            public Fragment getItem(int pos) {
                return StashFabricFragment.newInstance(mFabrics.get(pos), callingTab);
            }
        });

        // get UUID for the fabric to be displayed and set the appropriate fragment
        UUID fabricId = (UUID)getIntent().getSerializableExtra(StashFabricFragment.EXTRA_FABRIC_ID);
        for (int i = 0; i < mFabrics.size(); i++) {
            if (mFabrics.get(i).equals(fabricId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
