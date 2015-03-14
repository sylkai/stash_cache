package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;

/**
 * Fragment parent class for the overviewpagerfragments used for the stash/master/shopping list. The
 * two methods should be overridden by the children but provides a handle for the host activity to
 * set which view is current/trigger an update without knowing specifically which tab is active.
 */
public class UpdateFragment extends Fragment {

    public interface OnTabSwipeListener {
        public void onTabSwipe(int tabSelected);
    }

    public void stashChanged() {
        //
    }

    public void setCurrentView(int view) {
        //
    }

}
