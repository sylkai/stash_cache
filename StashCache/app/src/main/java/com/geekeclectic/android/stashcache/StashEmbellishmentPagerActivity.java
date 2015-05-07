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
import java.util.UUID;

/**
 * Host activity for a viewpager to display StashEmbellishment fragments to the user, in order to allow
 * swiping between fragments on the list.  Uses a FragmentStatePagerAdapter to reduce memory load.
 * The base list is the master list, regardless of calling tab, because of issues with handling
 * addition/subtraction to the stash/shopping lists while sorting.
 */
public class StashEmbellishmentPagerActivity extends FragmentActivity {

    private ViewPager mViewPager;
    private ArrayList<UUID> mEmbellishments;
    private int callingTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create viewPager
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.embellishmentViewPager);
        setContentView(mViewPager);

        callingTab = getIntent().getIntExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, StashConstants.STASH_TAB);

        // get list of embellishments
        setEmbellishmentList();

        // create and set fragment manager to return appropriate fragments
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new FragmentStatePagerAdapter(fm) {
            @Override
            public int getCount() {
                return mEmbellishments.size();
            }

            @Override
            public Fragment getItem(int pos) {
                return StashEmbellishmentFragment.newInstance(mEmbellishments.get(pos), callingTab);
            }
        });

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
                //
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //
            }

            public void onPageSelected(int currentEmbellishment) {
                // keep track of which fragment is currently displayed and let the host activity know
                StashEmbellishment embellishment = StashData.get(getParent()).getEmbellishment(mEmbellishments.get(currentEmbellishment));
                ActionBar actionBar = getActionBar();

                actionBar.setTitle(getString(R.string.embellishment));
                actionBar.setSubtitle(embellishment.toString());
            }
        });

        // get the id for the desired embellishment and set the appropriate fragment as current
        UUID embellishmentId = (UUID)getIntent().getSerializableExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID);
        for (int i = 0; i < mEmbellishments.size(); i++) {
            if (mEmbellishments.get(i).equals(embellishmentId)) {
                mViewPager.setCurrentItem(i);

                // onPageSelected won't fire if the item up is the first one, so need to set that up separately
                if (i == 0) {
                    StashEmbellishment embellishment = StashData.get(this).getEmbellishment(mEmbellishments.get(i));
                    ActionBar actionBar = getActionBar();

                    actionBar.setTitle(getString(R.string.embellishment));
                    actionBar.setSubtitle(embellishment.toString());
                }
                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.embellishment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_embellishment:
                // create a new thread
                StashEmbellishment embellishment = new StashEmbellishment(this);
                StashData.get(this).addEmbellishment(embellishment);

                // notify the adapter the dataset has changed
                mViewPager.getAdapter().notifyDataSetChanged();

                // switch view to the new embellishment
                UUID embellishmentId = embellishment.getId();
                for (int i = 0; i < mEmbellishments.size(); i++) {
                    if (mEmbellishments.get(i).equals(embellishmentId)) {
                        mViewPager.setCurrentItem(i);
                        break;
                    }
                }

                return true;
            case R.id.menu_item_preferences:
                Intent intent = new Intent(this, StashPreferencesActivity.class);
                startActivity(intent);

                return true;
            case R.id.menu_item_help:
                Intent helpIntent = new Intent(this, StashHelpActivity.class);
                startActivity(helpIntent);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setEmbellishmentList() {
        mEmbellishments = StashData.get(this).getEmbellishmentList();
    }

}
