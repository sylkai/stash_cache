package com.geekeclectic.android.stashcache;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 2/5/2015.
 */
public class StashCreateShoppingList {

    private ArrayList<UUID> mThreadShoppingList;
    private ArrayList<UUID> mEmbellishmentShoppingList;
    private ArrayList<StashPattern> mFabricNeeded;

    public StashCreateShoppingList() {
        mThreadShoppingList = new ArrayList<UUID>();
        mEmbellishmentShoppingList = new ArrayList<UUID>();
        mFabricNeeded = new ArrayList<StashPattern>();
    }

    public void updateShoppingList(StashData stash) {
        ArrayList<StashPattern> patternList = stash.getPatternData();

        resetNeededTotals(stash);
        mThreadShoppingList.clear();
        mEmbellishmentShoppingList.clear();
        mFabricNeeded.clear();

        ArrayList<StashPattern> kittedPatterns = new ArrayList<StashPattern>();

        for (StashPattern pattern : patternList) {
            if (pattern.getKitted()) {
                kittedPatterns.add(pattern);
            }
        }

        for (StashPattern pattern : kittedPatterns) {
            if (pattern.getFabric() == null) {
                mFabricNeeded.add(pattern);
            }

            if (pattern.getThreadList() != null) {
                ArrayList<UUID> threadList = pattern.getThreadList();
                for (UUID threadId : threadList) {
                    StashThread thread = stash.getThread(threadId);
                    thread.addNeeded(pattern.getQuantity(thread));

                    if (thread.needToBuy() && !mThreadShoppingList.contains(threadId)) {
                        mThreadShoppingList.add(threadId);
                    }
                }
            }

            if (pattern.getEmbellishmentList() != null) {
                ArrayList<UUID> embellishmentList = pattern.getEmbellishmentList();
                for (UUID embellishmentId : embellishmentList) {
                    // StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
                    // embellishment.addNeeded(pattern.getQuantity(embellishment));

                    // if (embellishment.needToBuy() && !mEmbellishmentShoppingList.contains(embellishmentId)) {
                    //     mEmbellishmentShoppingList.add(embellishmentId);
                    // }
                }
            }
        }

        stash.setFabricForList(mFabricNeeded);
        stash.setThreadShoppingList(mThreadShoppingList);
        stash.setEmbellishmentShoppingList(mEmbellishmentShoppingList);
    }

    private void resetNeededTotals(StashData stash) {
        ArrayList<UUID> threadList = stash.getThreadList();
        for (UUID threadId : threadList) {
            stash.getThread(threadId).resetNeeded();
        }

        ArrayList<UUID> embellishmentList = stash.getEmbellishmentList();
        for (UUID embellishmentId : embellishmentList) {
            stash.getEmbellishment(embellishmentId).resetNeeded();
        }
    }

}
