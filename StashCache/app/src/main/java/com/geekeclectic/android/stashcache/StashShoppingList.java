package com.geekeclectic.android.stashcache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sylk on 2/5/2015.
 */
public class StashShoppingList {

    private ArrayList<UUID> mShoppingList;
    private ArrayList<StashPattern> mFabricNeeded;

    public StashShoppingList() {
        mShoppingList = new ArrayList<UUID>();
        mFabricNeeded = new ArrayList<StashPattern>();
    }

    public void createShoppingList(StashData stash) {
        ArrayList<StashPattern> patternList = stash.getPatternData();
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

            ArrayList<UUID> threadList = pattern.getThreadList();
            for (UUID threadId : threadList) {
                StashThread thread = stash.getThread(threadId);
                thread.addNeeded(pattern.getQuantity(thread));

                if (thread.needToBuy() && !mShoppingList.contains(threadId)) {
                    mShoppingList.add(threadId);
                }
            }
        }
    }

    public ArrayList<UUID> getShoppingList() {
        return mShoppingList;
    }

    public ArrayList<StashPattern> getPatternsNeedingFabric() {
        return mFabricNeeded;
    }

}
