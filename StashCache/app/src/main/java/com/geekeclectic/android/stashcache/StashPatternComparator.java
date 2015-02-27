package com.geekeclectic.android.stashcache;

import java.util.Comparator;

/**
 * Custom comparator for sorting pattern list, grouped by designer and then alphabetical.
 */
public class StashPatternComparator implements Comparator<StashPattern> {

    @Override
    public int compare(StashPattern pattern1, StashPattern pattern2) {
        if (pattern1.getSource() != null && pattern2.getSource() != null && pattern1.getSource().equals(pattern2.getSource())) {
            if (pattern1.getPatternName() != null && pattern2.getPatternName() != null) {
                return pattern1.getPatternName().compareTo(pattern2.getPatternName());
            } else if (pattern1.getPatternName() == null && pattern2.getPatternName() == null) {
                return 0;
            } else if (pattern1.getPatternName() == null) {
                return 1;
            } else {
                return -1;
            }
        } else if (pattern1.getSource() != null && pattern2.getSource() != null) {
            return pattern1.getSource().compareTo(pattern2.getSource());
        } else if (pattern1.getSource() == null && pattern2.getSource() == null) {
            return 0;
        } else if (pattern1.getSource() == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
