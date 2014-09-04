package com.geekeclectic.android.stashcache;

import android.app.Fragment;

import java.util.UUID;

/**
 * Created by sylk on 8/27/2014.
 */
public class StashFabricActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        UUID fabricId = (UUID)getIntent().getSerializableExtra(StashFabricFragment.EXTRA_FABRIC_ID);

        return StashFabricFragment.newInstance(fabricId);
    }

}
