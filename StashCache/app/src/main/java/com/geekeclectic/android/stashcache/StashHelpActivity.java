package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

/**
 * Created by sylk on 4/20/2015.
 */
public class StashHelpActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return new StashHelpFragment();
    }
}
