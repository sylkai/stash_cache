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
 * Host activity for a viewpager to display StashEmbellishment fragments to the user, in order to allow
 * swiping between fragments on the list.  Uses a FragmentStatePagerAdapter to reduce memory load.
 */
public class StashEmbellishmentPagerActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private ArrayList<UUID> mEmbellishments;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create viewPager
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.embellishmentViewPager);
        setContentView(mViewPager);

        // get list of embellishments
        mEmbellishments = StashData.get(this).getEmbellishmentList();

        // create and set fragment manager to return appropriate fragments
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mEmbellishments.size();
            }

            @Override
            public Fragment getItem(int pos) {
                return StashEmbellishmentFragment.newInstance(mEmbellishments.get(pos));
            }
        });

        // get the id for the desired embellishment and set the appropriate fragment as current
        UUID embellishmentId = (UUID)getIntent().getSerializableExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID);
        for (int i = 0; i < mEmbellishments.size(); i++) {
            if (mEmbellishments.get(i).equals(embellishmentId)) {
                mViewPager.setCurrentItem(i);
                break;
            }
        }
    }

}
