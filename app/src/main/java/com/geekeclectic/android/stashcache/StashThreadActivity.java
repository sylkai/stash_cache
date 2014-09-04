package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

import java.util.UUID;

/**
 * Created by sylk on 8/27/2014.
 */
public class StashThreadActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        UUID threadId = (UUID)getIntent().getSerializableExtra(StashThreadFragment.EXTRA_THREAD_ID);

        return StashThreadFragment.newInstance(threadId);
    }

}
