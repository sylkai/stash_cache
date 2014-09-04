package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

import java.util.UUID;

/**
 * Created by sylk on 8/25/2014.
 */
public class StashPatternActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {

        UUID patternId = (UUID)getIntent().getSerializableExtra(StashPatternFragment.EXTRA_PATTERN_ID);

        return StashPatternFragment.newInstance(patternId);

    }
}
