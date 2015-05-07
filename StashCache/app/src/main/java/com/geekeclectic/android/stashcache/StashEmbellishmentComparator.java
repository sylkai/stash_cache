package com.geekeclectic.android.stashcache;

import android.content.Context;

import java.util.Comparator;
import java.util.UUID;

/**
 * Custom comparator to allow sorting of lists for display in a logical order.  Regex used to check
 * if item code contains only digits found here:
 * http://stackoverflow.com/questions/10575624/java-string-see-if-a-string-contains-only-numbers-and-not-letters
 * This sorter is specific to embellishments and doesn't have the issues caused by Kreinik that the
 * thread sorter deals with.
 */
public class StashEmbellishmentComparator implements Comparator<UUID> {

    private Context mContext;
    StashEmbellishment embellishment1;
    StashEmbellishment embellishment2;

    public StashEmbellishmentComparator(Context context) {
        mContext = context;
    }


    @Override
    public int compare(UUID embellishmentId1, UUID embellishmentId2) {
        StashData stash = StashData.get(mContext);
        embellishment1 = stash.getEmbellishment(embellishmentId1);
        embellishment2 = stash.getEmbellishment(embellishmentId2);

        String source1 = embellishment1.getSource();
        String source2 = embellishment2.getSource();

        // sources aren't null
        if (source1 != null && source2 != null) {
            // sources are equal
            if (source1.equalsIgnoreCase(source2)) {
                return compareType(embellishment1.getType(), embellishment2.getType());
            // sort by source
            } else {
                return source1.compareToIgnoreCase(source2);
            }

        // sources are both null so see if something else can sort
        } else if (source1 == null && source2 == null) {
            return compareType(embellishment1.getType(), embellishment2.getType());
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
            if(type1.equalsIgnoreCase(type2)) {
                return compareCode(embellishment1.getCode(), embellishment2.getCode());
            // sort by type
            } else {
                return type1.compareToIgnoreCase(type2);
            }

        // types are both null, so see if possible to sort by code
        } else if (type1 == null && type2 == null) {
            return compareCode(embellishment1.getCode(), embellishment2.getCode());
        } else if (type1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

    private int compareCode(String code1, String code2) {
        // codes aren't null
        if (code1 != null && code2 != null) {
            // code contains only digits, so sort as ints
            if (code1.matches("[0-9]+") && code2.matches("[0-9]+")) {
                return Integer.parseInt(code1) - Integer.parseInt(code2);
                // sort codes as strings
            } else {
                return code1.compareToIgnoreCase(code2);
            }

        // both are null so treat as equal
        } else if (code1 == null && code2 == null) {
            return 0;
        } else if (code1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
