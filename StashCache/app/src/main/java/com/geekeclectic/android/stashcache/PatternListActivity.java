package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

/**
 * Host activity for the pattern list fragment - not currently used.
 */

public class PatternListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PatternListFragment();
    }
}
