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
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
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

        callingTab = getIntent().getIntExtra(StashFabricFragment.EXTRA_TAB_ID, StashConstants.STASH_TAB);

        // get list of fabrics for viewPager
        getFabricList();

        // set fragment manager to display fragments
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new ObservedAdapter(fm));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
                //
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //
            }

            public void onPageSelected(int currentFabric) {
                // keep track of which fragment is currently displayed and let the host activity know
                StashFabric fabric = StashData.get(getParent()).getFabric(mFabrics.get(currentFabric));
                ActionBar actionBar = getActionBar();

                actionBar.setTitle(getString(R.string.fabric));
                actionBar.setSubtitle(fabric.getInfo());
            }
        });

        // get UUID for the fabric to be displayed and set the appropriate fragment
        UUID fabricId = (UUID)getIntent().getSerializableExtra(StashFabricFragment.EXTRA_FABRIC_ID);
        for (int i = 0; i < mFabrics.size(); i++) {
            if (mFabrics.get(i).equals(fabricId)) {
                mViewPager.setCurrentItem(i);

                // onPageSelected won't fire if the item up is the first one, so need to set that up separately
                if (i == 0) {
                    StashFabric fabric = StashData.get(this).getFabric(mFabrics.get(i));
                    ActionBar actionBar = getActionBar();

                    actionBar.setTitle(getString(R.string.fabric));
                    actionBar.setSubtitle(fabric.getInfo());
                }

                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fabric_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        StashFabric fabric = StashData.get(this).getFabric(mFabrics.get((mViewPager.getCurrentItem())));
        MenuItem removeFinish = menu.findItem(R.id.menu_item_fabric_remove_finish);

        if (fabric.isFinished()) {
            removeFinish.setVisible(true);
        } else {
            removeFinish.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_fabric:
                // create a new fabric and add it to the stash
                StashFabric fabric = new StashFabric(this);
                StashData.get(this).addFabric(fabric);

                // notify the adapter that the list has changed
                mViewPager.getAdapter().notifyDataSetChanged();

                // switch the current view to the new fabric
                UUID fabricId = fabric.getId();
                for (int i = 0; i < mFabrics.size(); i++) {
                    if (mFabrics.get(i).equals(fabricId)) {
                        mViewPager.setCurrentItem(i);
                        break;
                    }
                }
                return true;
            case R.id.menu_item_fabric_remove_finish:
                StashFabric removeFinish = StashData.get(this).getFabric(mFabrics.get(mViewPager.getCurrentItem()));
                StashPattern pattern = removeFinish.usedFor();

                removeFinish.setComplete(false);
                pattern.removeFinish(removeFinish);

                // remove current fabric if one exists and clear its inUse flag
                if (pattern.getFabric() != null) {
                    pattern.getFabric().setUse(false);
                    pattern.getFabric().setUsedFor(null);
                }

                pattern.setFabric(removeFinish);

                StashData.get(this).addFabricToStash(removeFinish.getId());
                updateFragments();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getFabricList() {
        mFabrics = StashData.get(this).getFabricList();
    }

    public void updateFragments() {
        ((ObservedAdapter)mViewPager.getAdapter()).updateFragments();
    }

    private class ObservedAdapter extends FragmentStatePagerAdapter {
        private Observable mObservers = new FragmentObserver();

        public ObservedAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mFabrics.size();
        }

        @Override
        public Fragment getItem(int pos) {
            // get pattern for the fragment and create fragment
            UUID fabricId = mFabrics.get(pos);
            Fragment fragment = StashFabricFragment.newInstance(fabricId, callingTab);

            // add fragment to the list of observers
            if (fragment instanceof Observer) {
                mObservers.addObserver((Observer) fragment);
            }

            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // if fragment is being removed from memory, remove it from list of observers
            if (object instanceof Observer) {
                mObservers.deleteObserver((Observer) object);
            }

            super.destroyItem(container, position, object);
        }

        public void updateFragments() {
            // tell the observers to trigger their refresh
            mObservers.notifyObservers();
        }
    }

}
