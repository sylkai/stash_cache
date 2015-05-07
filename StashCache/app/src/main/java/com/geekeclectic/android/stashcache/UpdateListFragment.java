package com.geekeclectic.android.stashcache;

import android.support.v4.app.ListFragment;

/**
 * Fragment parent class for the list fragments that defines an interface to allow the list to notify
 * the host activity that changes have happened (so that the host can notify other fragments as
 * needed).
 */
public class UpdateListFragment extends ListFragment {

    public interface UpdateListFragmentsListener {
        void onListFragmentUpdate();
    }

}
