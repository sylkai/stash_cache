package com.geekeclectic.android.stashcache;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This class generates the shopping lists by seeing which patterns are marked as kitted and then
 * iterating through to mark how much the pattern calls for of each thread/embellishment, and if the
 * pattern has fabric associated with it yet. It then iterates through all thread/embellishments in
 * the stash and adds them to the shopping list if they need to be purchased in order to meet the
 * requested amount.  Finally, it sends the shopping lists back to the stash so they can be accessed
 * by the UI for displaying the shopping list.
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

        // reset all information stored in thread/embellishments, to account for patterns being
        // removed from the kitted list
        resetNeededTotals(stash);

        ArrayList<StashPattern> kittedPatterns = new ArrayList<StashPattern>();

        // find the kitted patterns
        for (StashPattern pattern : patternList) {
            if (pattern.getKitted()) {
                kittedPatterns.add(pattern);
            }
        }

        // for each kitted pattern
        for (StashPattern pattern : kittedPatterns) {
            // if it doesn't have fabric, add it to the fabric needed list
            if (pattern.getFabric() == null) {
                mFabricNeeded.add(pattern);
            }

            // if there is a threadlist, go through it and set the quantity needed for this pattern
            // in each thread
            if (pattern.getThreadList() != null) {
                ArrayList<UUID> threadList = pattern.getThreadList();
                for (UUID threadId : threadList) {
                    StashThread thread = stash.getThread(threadId);
                    thread.addNeeded(pattern.getQuantity(thread));
                }
            }

            // if there is an embellishmentlist, go through it and set the quantity needed for this
            // pattern in each thread
            if (pattern.getEmbellishmentList() != null) {
                ArrayList<UUID> embellishmentList = pattern.getEmbellishmentList();
                for (UUID embellishmentId : embellishmentList) {
                    StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
                    embellishment.addNeeded(pattern.getQuantity(embellishment));
                }
            }
        }

        // go through every thread in the stash and add it to the shopping list if needed
        ArrayList<UUID> threadList = stash.getThreadList();
        for (UUID threadId : threadList) {
            StashThread thread = stash.getThread(threadId);
            if (thread.needToBuy()) {
                mThreadShoppingList.add(threadId);
            }
        }

        // go through every embellishment in the stash and add it to the shopping list if needed
        ArrayList<UUID> embellishmentList = stash.getEmbellishmentList();
        for (UUID embellishmentId : embellishmentList) {
            StashEmbellishment embellishment = stash.getEmbellishment(embellishmentId);
            if (embellishment.needToBuy()) {
                mEmbellishmentShoppingList.add(embellishmentId);
            }
        }

        // set the shopping lists
        stash.setFabricForList(mFabricNeeded);
        stash.setThreadShoppingList(mThreadShoppingList);
        stash.setEmbellishmentShoppingList(mEmbellishmentShoppingList);
    }

    private void resetNeededTotals(StashData stash) {
        // reset needed totals for the threads
        ArrayList<UUID> threadList = stash.getThreadList();
        for (UUID threadId : threadList) {
            stash.getThread(threadId).resetNeeded();
        }

        // reset needed totals for the embellishments
        ArrayList<UUID> embellishmentList = stash.getEmbellishmentList();
        for (UUID embellishmentId : embellishmentList) {
            stash.getEmbellishment(embellishmentId).resetNeeded();
        }
    }

}
