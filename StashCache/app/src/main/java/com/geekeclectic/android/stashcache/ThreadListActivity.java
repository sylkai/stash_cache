package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

/**
 * Created by sylk on 8/27/2014.
 */
public class ThreadListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new ThreadListFragment();
    }

}
