package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.io.IOException;

/**
 * Activity to host the viewPager managing the listView fragments displaying the lists of different
 * stash components (patterns, fabrics, threads).  Scrolling tab bar above identifies which list
 * is active.  EXTRA_FRAGMENT_ID indicates which item class is displayed when navigating up through
 * hierarchy.
 */

public class StashOverviewPagerActivity extends FragmentActivity {

    static final int ITEMS = 4;
    static final String TAG = "StashOverview";
    public static final String EXTRA_FRAGMENT_ID = "com.geekeclectic.android.stashcache.active_fragment_id";

    private ViewPager mViewPager;
    private StashOverviewPagerAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stash_overview);

        // initialize viewPager and adapter
        mViewPager = (ViewPager)findViewById(R.id.stashViewPager);
        mAdapter = new StashOverviewPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);

        // if this was created with an intent, check which list is supposed to be active and set
        // it as the current item
        if (getIntent() != null) {
            int mFragmentId = getIntent().getIntExtra(EXTRA_FRAGMENT_ID, 0);
            mViewPager.setCurrentItem(mFragmentId, false);
        }

        SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.drop_down_list, android.R.layout.simple_spinner_dropdown_item);

       ActionBar.OnNavigationListener mOnNavigationListener = new ActionBar.OnNavigationListener() {
            // get the strings provided for the ArrayAdapter
           String[] strings = getResources().getStringArray(R.array.drop_down_list);

           @Override
           public boolean onNavigationItemSelected(int position, long itemId) {
               String selection = strings[position];

               if (selection.equals("Master List")) {
                   Intent i = new Intent(getApplicationContext(), MasterOverviewPagerActivity.class);
                   startActivity(i);
               } else if (selection.equals("Shopping List")) {
                   StashCreateShoppingList createList = new StashCreateShoppingList();
                   createList.updateShoppingList(StashData.get(getParent()));

                   // Intent i = new Intent(getParent(), ShoppingOverviewPagerActivity.class);
                   // startActivity(i);
               }

               return true;
           }
        };

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        actionBar.setListNavigationCallbacks(mSpinnerAdapter, mOnNavigationListener);
        actionBar.setSelectedNavigationItem(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stash_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling item selection
        switch (item.getItemId()) {
            case R.id.menu_item_import_stash:
                Log.d(TAG, "User chose to input stash.");

                StashImporter importer = new StashImporter();
                try {
                    importer.importStash(getApplicationContext());
                } catch (IOException e) {
                    //
                }
                StashData.get(getApplicationContext()).saveStash();
                return super.onOptionsItemSelected(item);
            case R.id.menu_item_delete_stash:
                StashData.get(getApplicationContext()).deleteStash();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class StashOverviewPagerAdapter extends FragmentStatePagerAdapter {

        public StashOverviewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return ITEMS;
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 1: // fabric list
                    return StashFabricListFragment.newInstance("stash");
                case 2: // thread list
                    return StashThreadListFragment.newInstance("stash");
                case 3: // embellishment list
                    return StashEmbellishmentListFragment.newInstance("stash");
                default: // pattern list
                    return StashPatternListFragment.newInstance("stash");
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 1: // fabric list
                    return getString(R.string.fabric_list_title).toUpperCase();
                case 2: // thread list
                    return getString(R.string.thread_list_title).toUpperCase();
                case 3: // embellishment list
                    return getString(R.string.embellishment_list_title).toUpperCase();
                default: // pattern list
                    return getString(R.string.pattern_list_title).toUpperCase();
            }
        }
    }

}

