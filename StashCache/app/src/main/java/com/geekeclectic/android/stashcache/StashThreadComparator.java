package com.geekeclectic.android.stashcache;

import android.content.Context;

import java.util.Comparator;
import java.util.UUID;

/**
 * Custom comparator to allow sorting of lists for display in a logical order.  Regex used to check
 * if item code contains only digits found here:
 * http://stackoverflow.com/questions/10575624/java-string-see-if-a-string-contains-only-numbers-and-not-letters
 * This sorter is specific to threads and handles all cases well except for Kreinik's naming
 * conventions (and even then it does an ...ok job.  If I get really ambitious I will try to clean
 * that up.)  Help for string splitting from here:
 * http://stackoverflow.com/questions/3552756/best-way-to-get-integer-part-of-the-string-600sp
 */

public class StashThreadComparator implements Comparator<UUID> {

    private Context mContext;
    private StashThread thread1;
    private StashThread thread2;

    public StashThreadComparator(Context context) {
        mContext = context;
    }


    @Override
    public int compare(UUID threadId1, UUID threadId2) {
        StashData stash = StashData.get(mContext);
        thread1 = stash.getThread(threadId1);
        thread2 = stash.getThread(threadId2);

        String source1 = thread1.getSource();
        String source2 = thread2.getSource();

        // sources aren't null
        if (source1 != null && source2 != null) {
            // sources are equal
            if (source1.equals(source2)) {
                return compareType(thread1.getType(), thread2.getType());
            // sources aren't equal, sort by source
            } else {
                return source1.compareTo(source2);
            }

        // sources are both null so see if something else can sort
        } else if (source1 == null && source2 == null) {
            return compareType(thread1.getType(), thread2.getType());
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
            if (type1.equals(type2)) {
                return compareCode(thread1.getCode(), thread2.getCode());
            } else {
                // types aren't equal, sort by type
                return type1.compareTo(type2);
            }

        // types are both null, so treat as equivalent and check codes
        } else if (type1 == null && type2 == null) {
            return compareCode(thread1.getCode(), thread2.getCode());
        } else if (type1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

    private int compareCode(String code1, String code2) {
        // codes aren't null
        if (code1 != null && code2 != null) {
            if (code1.matches("[0-9]+") && code2.matches("[0-9]+")) {
                // just a number, sort by the number
                return Integer.parseInt(code1) - Integer.parseInt(code2);
            } else if (code1.matches("[0-9]+\\s.*") && code2.matches("[0-9]+\\s.*")) {
                // if it is a number followed by other info (color, etc.), sort by the number
                return Integer.parseInt(code1.split("\\s")[0]) - Integer.parseInt(code2.split("\\s")[0]);
            } else {
                // just sort using the normal sorting, until I get fed up with Kreinik's behavior
                return code1.compareTo(code2);
            }

        // both null so treat as equal
        } else if (code1 == null && code2 == null) {
            return 0;
        } else if (code1 == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
