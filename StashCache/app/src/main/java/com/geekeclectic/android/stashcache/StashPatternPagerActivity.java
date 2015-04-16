package com.geekeclectic.android.stashcache;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Activity to host PatternFragments in a ViewPager (allow swiping side to side) using a
 * FragmentStatePagerAdapter to avoid clogging up the memory with fragments.  Implements the
 * ChangedFragmentListener to listen for a notice to trigger the fragment refresh for the fragments
 * loaded by the adapter to update display in case changes needed in display.
 */

public class StashPatternPagerActivity extends FragmentActivity implements StashPatternFragment.ChangedFragmentListener {

    private ViewPager mViewPager;
    private ArrayList<StashPattern> mPatterns;

    private int callingTab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize viewPager
        mViewPager = new ViewPager(this);
        mViewPager.setId(R.id.patternViewPager);
        setContentView(mViewPager);

        callingTab = getIntent().getIntExtra(StashPatternFragment.EXTRA_TAB_ID, StashConstants.STASH_TAB);

        // get list of patterns for the viewPager
        setPatternList();

        // set viewPager adapter
        FragmentManager fm = getSupportFragmentManager();
        mViewPager.setAdapter(new ObservedAdapter(fm));

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int arg0) {
                //
            }

            public void onPageScrolled(int arg0, float arg1, int arg2) {
                //
            }

            public void onPageSelected(int currentPattern) {
                // keep track of which fragment is currently displayed and let the host activity know
                StashPattern pattern = mPatterns.get(currentPattern);
                ActionBar actionBar = getActionBar();

                actionBar.setTitle(getString(R.string.pattern));
                actionBar.setSubtitle(pattern.toString());
            }
        });

        // pull the UUID from the desired fragment extra and iterate through the list to set it as
        // the current view
        UUID patternId = (UUID)getIntent().getSerializableExtra(StashPatternFragment.EXTRA_PATTERN_ID);
        for (int i = 0; i < mPatterns.size(); i++) {
            if (mPatterns.get(i).getId().equals(patternId)) {
                mViewPager.setCurrentItem(i);

                // if the first item, onPageSelected doesn't fire to set the Title, so done here
                if (i == 0) {
                    StashPattern pattern = mPatterns.get(i);
                    ActionBar actionBar = getActionBar();

                    actionBar.setTitle(getString(R.string.pattern));
                    actionBar.setSubtitle(pattern.toString());
                }

                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.pattern_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        StashPattern pattern = mPatterns.get((mViewPager.getCurrentItem()));
        MenuItem removeStash = menu.findItem(R.id.menu_item_pattern_remove_stash);
        MenuItem addStash = menu.findItem(R.id.menu_item_pattern_add_stash);

        if (callingTab != StashConstants.SHOPPING_TAB) {
            if (pattern.inStash()) {
                removeStash.setVisible(true);
                addStash.setVisible(false);
            } else {
                removeStash.setVisible(false);
                addStash.setVisible(true);
            }
        } else {
            removeStash.setVisible(false);
            addStash.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handling item selection
        switch (item.getItemId()) {
            case R.id.menu_item_pattern:
                // create new pattern and add it to the stash
                StashPattern pattern = new StashPattern(this);
                StashData.get(this).addPattern(pattern);

                // notify the adapter the list has changed
                mViewPager.getAdapter().notifyDataSetChanged();

                // find the new fragment and set it to the current view
                UUID patternId = pattern.getId();
                for (int i = 0; i < mPatterns.size(); i++) {
                    if (mPatterns.get(i).getId().equals(patternId)) {
                        mViewPager.setCurrentItem(i);
                        break;
                    }
                }
                return true;
            case R.id.menu_item_pattern_remove_stash:
                StashPattern removePattern = mPatterns.get(mViewPager.getCurrentItem());

                StashData.get(this).removePatternFromStash(removePattern);
                removePattern.setInStash(false);

                return true;
            case R.id.menu_item_pattern_add_stash:
                StashPattern addPattern = mPatterns.get(mViewPager.getCurrentItem());

                StashData.get(this).addPatternToStash(addPattern);
                addPattern.setInStash(true);

                return true;
            case R.id.menu_item_finish_project:
                // get current pattern to do things to
                StashPattern finishPattern = mPatterns.get(mViewPager.getCurrentItem());
                StashFabric finishedFabric = finishPattern.getFabric();

                 markPatternAsComplete(finishPattern, finishedFabric);

                return true;
            case R.id.menu_item_export_pattern:
                // the user wants to export just the information for this pattern, stripped of fabric
                // data (for sharing with a friend, perhaps)
                StashExporter exporter = new StashExporter();

                try {
                    File file = exporter.exportPattern(mPatterns.get(mViewPager.getCurrentItem()), this);

                    Intent saveIntent = new Intent(Intent.ACTION_SEND);

                    // better filter of apps by using "application/octet-stream" instead of "text/plain"
                    // this isn't appropriate to send to a messenger-type app, for example and this was an
                    // easy way to filter while still providing access to Dropbox/Google Drive/email
                    saveIntent.setType("application/octet-stream");
                    saveIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    startActivity(Intent.createChooser(saveIntent, getString(R.string.send_file_to)));
                } catch (IOException e) {
                    Toast.makeText(StashPatternPagerActivity.this, getString(R.string.export_error_pattern), Toast.LENGTH_SHORT).show();
                }
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setPatternList() {
        mPatterns = StashData.get(this).getPatternData();

    }

    private void markPatternAsComplete(StashPattern pattern, StashFabric fabric) {
        if (fabric != null) {
            fabric.setComplete(true);
            StashData.get(this).removeFabricFromStash(fabric.getId());
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean updateStash = sharedPreferences.getBoolean(StashPreferencesActivity.KEY_UPDATE_STASH, false);
        boolean overlapNotAllowed = sharedPreferences.getBoolean(StashPreferencesActivity.KEY_NEW_SKEIN_FOR_EACH, false);

        if (updateStash) {
            ArrayList <UUID> threadList = pattern.getThreadList();
            for (UUID threadId : threadList) {
                StashThread thread = StashData.get(this).getThread(threadId);

                int decreaseBy;
                if (overlapNotAllowed) {
                    // user wants fresh skeins for each pattern, so remove all skeins used (they can re-add excess)
                    decreaseBy = pattern.getQuantity(thread);
                } else {
                    // assume overlap on the last one, don't remove it from the stash
                    decreaseBy = pattern.getQuantity(thread) - 1;
                }

                for (int i = 0; i < decreaseBy; i++) {
                    thread.decreaseOwnedQuantity();
                }
            }

            ArrayList <UUID> embellishmentList = pattern.getEmbellishmentList();
            for (UUID embellishmentId : embellishmentList) {
                StashEmbellishment embellishment = StashData.get(this).getEmbellishment(embellishmentId);

                // treated the same way embellishments are treated when creating the shopping list
                int decreaseBy = pattern.getQuantity(embellishment);

                for (int i = 0; i < decreaseBy; i++) {
                    embellishment.decreaseOwned();
                }
            }
        }

        pattern.patternCompleted();
        updateFragments();
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
            return mPatterns.size();
        }

        @Override
        public Fragment getItem(int pos) {
            // get pattern for the fragment and create fragment
            StashPattern pattern = mPatterns.get(pos);
            Fragment fragment = StashPatternFragment.newInstance(pattern.getId(), callingTab);

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
