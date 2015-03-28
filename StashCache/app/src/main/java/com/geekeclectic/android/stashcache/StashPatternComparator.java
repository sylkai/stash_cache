package com.geekeclectic.android.stashcache;

import java.util.Comparator;

/**
 * Custom comparator for sorting pattern list, grouped by designer and then alphabetical.
 */
public class StashPatternComparator implements Comparator<StashPattern> {

    @Override
    public int compare(StashPattern pattern1, StashPattern pattern2) {
        String source1 = pattern1.getSource();
        String source2 = pattern2.getSource();

        // designers aren't null
        if (source1 != null && source2 != null) {
            // designers are the same
            if (source1.equalsIgnoreCase(source2)) {
                return compareName(pattern1.getPatternName(), pattern2.getPatternName());
            // sort by designer
            } else {
                return source1.compareToIgnoreCase(source2);
            }

        // no designer provided, so check and sort by pattern name if possible
        } else if (source1 == null && source2 == null) {
            return compareName(pattern1.getPatternName(), pattern2.getPatternName());
        } else if (source1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

    private int compareName(String name1, String name2) {
        // pattern names aren't null
        if (name1 != null && name2 != null) {
            // sort by pattern name (alphabetical)
            return name1.compareToIgnoreCase(name2);

        // both null, so treat as equal
        } else if (name1 == null && name2 == null) {
            return 0;
        } else if (name1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
