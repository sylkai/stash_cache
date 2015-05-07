package com.geekeclectic.android.stashcache;

import android.content.Context;

import java.util.Comparator;
import java.util.UUID;

/**
 * Custom comparator to allow sorting of lists for display in a logical order.  Specific to fabrics,
 * sorts by source (manufacturer, not displayed to the user), type (linen, aida, etc - displayed to
 * user, alphabetical), count, and then color (alphabetical).
 */
public class StashFabricComparator implements Comparator<UUID> {

    private Context mContext;
    private StashFabric fabric1;
    private StashFabric fabric2;

    public StashFabricComparator(Context context) {
        mContext = context;
    }

    @Override
    public int compare(UUID fabricId1, UUID fabricId2){
        StashData stash = StashData.get(mContext);
        fabric1 = stash.getFabric(fabricId1);
        fabric2 = stash.getFabric(fabricId2);

        String source1 = fabric1.getSource();
        String source2 = fabric2.getSource();

        // sources aren't null
        if (source1 != null && source2 != null) {
            // sources are equal
            if (source1.equalsIgnoreCase(source2)) {
                return compareType(fabric1.getType(), fabric2.getType());

            // sources aren't equal, sort by source
            } else {
                return source1.compareToIgnoreCase(source2);
            }

        // sources are both null so see if something else can sort
        } else if (source1 == null && source2 == null) {
            return compareType(fabric1.getType(), fabric2.getType());
        } else if (source1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

    private int compareType(String type1, String type2) {
        // types aren't null
        if (type1 != null && type2 != null) {
            // types are equal
            if (type1.equalsIgnoreCase(type2)) {
                return compareCount(fabric1.getCount(), fabric2.getCount());

            // sort by type
            } else {
                return type1.compareToIgnoreCase(type2);
            }

        // both are null so treat as equal
        } else if (type1 == null && type2 == null) {
            return compareCount(fabric1.getCount(), fabric2.getCount());
        } else if (type1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

    private int compareCount(int count1, int count2) {
        // same count, so sort by color
        if (count1 == count2) {
            return compareColor(fabric1.getColor(), fabric2.getColor());

        // sort by count
        } else {
            return (count1 - count2);
        }
    }

    private int compareColor(String color1, String color2) {
        // colors aren't null
        if (color1 != null && color2 != null) {
            // sort alphabetically
            return color1.compareToIgnoreCase(color2);

        // both null so treat as equal
        } else if (color1 == null && color2 == null) {
            return 0;
        } else if (color1 == null) {
            return 1;
        } else {
            return -1;
        }
    }
}
