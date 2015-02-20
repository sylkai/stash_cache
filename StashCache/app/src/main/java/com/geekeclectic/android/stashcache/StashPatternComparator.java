package com.geekeclectic.android.stashcache;

import java.util.Comparator;

/**
 * Custom comparator for sorting pattern list, grouped by designer and then alphabetical.
 */
public class StashPatternComparator implements Comparator<StashPattern> {

    @Override
    public int compare(StashPattern pattern1, StashPattern pattern2) {
        if (pattern1.getSource().equals(pattern2.getSource())) {
            return pattern1.getPatternName().compareTo(pattern2.getPatternName());
        }

        return pattern1.getSource().compareTo(pattern2.getSource());
    }

}
