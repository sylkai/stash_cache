/*
package com.geekeclectic.android.stashcache;

import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.util.LruCache;


*/
/**
 * Created by sylk on 10/31/2014.
 *//*

public class StashPhotoCache {
    private LruCache<String, Bitmap> mMemorycache;

    public static StashPhotoCache getInstance(FragmentManager fragmentManager) {
        final RetainFragment mRetainFragment = findOrCreateRetainFragment(fragmentManager);
    }

    class RetainFragment extends Fragment {
        private static final String TAG = "RetainFragment";
        public LruCache<String, Bitmap> mRetainedCache;

        public RetainFragment() {}

        public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
            RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
            if (fragment == null) {
                fragment = new RetainFragment();
                fm.beginTransaction().add(fragment, TAG).commit();
            }
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
*/
