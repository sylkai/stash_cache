package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

/**
 * Created by sylk on 8/25/2014.
 */
public class PatternListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new PatternListFragment();
    }
}
