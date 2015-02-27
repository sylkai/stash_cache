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

    public StashThreadComparator(Context context) {
        mContext = context;
    }


    @Override
    public int compare(UUID threadId1, UUID threadId2) {
        StashData stash = StashData.get(mContext);
        StashThread thread1 = stash.getThread(threadId1);
        StashThread thread2 = stash.getThread(threadId2);

        if (thread1.getSource() != null && thread2.getSource() != null && thread1.getSource().equals(thread2.getSource())) {
            if (thread1.getType() != null && thread2.getType() != null && thread1.getType().equals(thread2.getType())) {
                if (thread1.getCode() != null && thread2.getCode() != null) {
                    if (thread1.getCode().matches("[0-9]+") && thread2.getCode().matches("[0-9]+")) {
                        // just a number, sort by the number
                        return Integer.parseInt(thread1.getCode()) - Integer.parseInt(thread2.getCode());
                    } else if (thread1.getCode().matches("[0-9]+\\s.*") && thread2.getCode().matches("[0-9]+\\s.*")) {
                        // if it is a number followed by other info (color, etc.), sort by the number
                        return Integer.parseInt(thread1.getCode().split("\\s")[0]) - Integer.parseInt(thread2.getCode().split("\\s")[0]);
                    } else {
                        // just sort using the normal sorting, until I get fed up with Kreinik's behavior
                        return thread1.getCode().compareTo(thread2.getCode());
                    }
                } else if (thread1.getCode() == null && thread2.getCode() == null) {
                    return 0;
                } else if (thread1.getCode() == null) {
                    return 1;
                } else {
                    return -1;
                }
            } else if (thread1.getType() != null && thread2.getType() != null) {
                return thread1.getType().compareTo(thread2.getType());
            } else if (thread1.getType() == null && thread2.getType() == null) {
                return 0;
            } else if (thread1.getType() == null) {
                return 1;
            } else {
                return -1;
            }
        } else if (thread1.getSource() != null && thread2.getSource() != null) {
            return thread1.getSource().compareTo(thread2.getSource());
        } else if (thread1.getSource() == null && thread2.getSource() == null) {
            return 0;
        } else if (thread1.getSource() == null) {
            return 1;
        } else {
            return -1;
        }
    }

}
