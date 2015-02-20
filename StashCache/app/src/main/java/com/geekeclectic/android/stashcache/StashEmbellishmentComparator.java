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

    public StashEmbellishmentComparator(Context context) {
        mContext = context;
    }


    @Override
    public int compare(UUID embellishmentId1, UUID embellishmentId2) {
        StashData stash = StashData.get(mContext);
        StashEmbellishment embellishment1 = stash.getEmbellishment(embellishmentId1);
        StashEmbellishment embellishment2 = stash.getEmbellishment(embellishmentId2);

        if (embellishment1.getSource().equals(embellishment2.getSource())) {
            if (embellishment1.getType().equals(embellishment2.getType())) {
                if (embellishment1.getCode().matches("[0-9]+") && embellishment2.getCode().matches("[0-9]+")) {
                    return Integer.parseInt(embellishment1.getCode()) - Integer.parseInt(embellishment2.getCode());
                } else {
                    return embellishment1.getCode().compareTo(embellishment2.getCode());
                }
            } else {
                return embellishment1.getType().compareTo(embellishment2.getType());
            }
        }
        return embellishment1.getSource().compareTo(embellishment2.getSource());
    }

}
