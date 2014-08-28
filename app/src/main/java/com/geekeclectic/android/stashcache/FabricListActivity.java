package com.geekeclectic.android.stashcache;

import android.app.Fragment;

/**
 * Created by sylk on 8/27/2014.
 */
public class FabricListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new FabricListFragment();
    }

}
