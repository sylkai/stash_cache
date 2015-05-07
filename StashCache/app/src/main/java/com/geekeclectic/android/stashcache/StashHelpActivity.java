package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

/**
 * Activity to host the help fragment.
 */
public class StashHelpActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new StashHelpFragment();
    }
}
