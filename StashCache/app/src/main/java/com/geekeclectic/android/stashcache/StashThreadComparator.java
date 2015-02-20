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
 * that up.
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

        if (thread1.getSource().equals(thread2.getSource())) {
            if (thread1.getType().equals(thread2.getType())) {
                if (thread1.getCode().matches("[0-9]+") && thread2.getCode().matches("[0-9]+")) {
                    return Integer.parseInt(thread1.getCode()) - Integer.parseInt(thread2.getCode());
                } else {
                    return thread1.getCode().compareTo(thread2.getCode());
                }
            } else {
                return thread1.getType().compareTo(thread2.getType());
            }
        }
        return thread1.getSource().compareTo(thread2.getSource());
    }

}
