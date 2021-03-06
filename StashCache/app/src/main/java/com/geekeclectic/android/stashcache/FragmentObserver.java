package com.geekeclectic.android.stashcache;

import java.util.Observable;

/**
 * Observer for fragments to notify when to update views in the ViewPager because of data set
 * changes.  Credit to alexfu for initial design (https://gist.github.com/alexfu/5797429), modified
 * to account for use of FragmentStatePagerAdapter instead of FragmentPagerAdapter.
 *
 * Note that for FragmentStatePagerAdapter, you must remove the observer from the list of observers
 * on the destroyItem() call.
 */

public class FragmentObserver extends Observable {

    @Override
    public void notifyObservers() {
        setChanged();
        super.notifyObservers();
    }

}
