package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

/**
 * StashData class is a singleton to serve as the "database" for the items in the stash.  Thread
 * and fabric are stored in hashmaps with the UUID associated with the fabric/thread as the key for
 * fast lookup.  Patterns are stored in an ArrayList.  Based off of example in BigNerdRanch Android
 * Programming book.
 */

public class StashData {

    private static final String TAG = "StashData";
    private static final String FILENAME = "stash.json";

    private static StashData sStash;
    private Context mAppContext;

    private StashDataJSONSerializer mSerializer;

    private ArrayList<StashPattern> mPatternsData;
    private HashMap<UUID, StashThread> mThreadsData;
    private HashMap<UUID, StashFabric> mFabricData;
    private HashMap<UUID, StashEmbellishment> mEmbellishmentData;

    private ArrayList<StashPattern> mStashPatterns;
    private ArrayList<UUID> mFabricList;
    private ArrayList<UUID> mStashFabricList;
    private ArrayList<StashPattern> mFabricForList;
    private ArrayList<UUID> mThreadsList;
    private ArrayList<UUID> mStashThreadsList;
    private ArrayList<UUID> mShoppingThreadsList;
    private ArrayList<UUID> mEmbellishmentList;
    private ArrayList<UUID> mStashEmbellishmentList;
    private ArrayList<UUID> mShoppingEmbellishmentList;

    // general stash functions
    private StashData(Context appContext) {
        // set variables
        mAppContext = appContext;
        mSerializer = new StashDataJSONSerializer(mAppContext, FILENAME);

        mThreadsData = new HashMap<UUID, StashThread>();
        mFabricData = new HashMap<UUID, StashFabric>();
        mEmbellishmentData = new HashMap<UUID, StashEmbellishment>();
        mPatternsData = new ArrayList<StashPattern>();

        mThreadsList = new ArrayList<UUID>();
        mStashThreadsList = new ArrayList<UUID>();
        mShoppingThreadsList = new ArrayList<UUID>();
        mFabricList = new ArrayList<UUID>();
        mStashFabricList = new ArrayList<UUID>();
        mFabricForList = new ArrayList<StashPattern>();
        mEmbellishmentList = new ArrayList<UUID>();
        mStashEmbellishmentList = new ArrayList<UUID>();
        mShoppingEmbellishmentList = new ArrayList<UUID>();
        mStashPatterns = new ArrayList<StashPattern>();

        // need reference to ourselves to pass into the serializer
        sStash = this;

        // pass stash to JSON serializer to be filled in
        try {
            mSerializer.loadStash(sStash);
        } catch (Exception e) {
            Log.e(TAG, "error loading stash: ", e);
        }

        // sort the lists, in case something got messed up in the read-in (should be sorted)
        // by doing an initial sort, there is not a long delay when calling a list for the first time
        // after loading the data from JSON
        Collections.sort(mThreadsList, new StashThreadComparator(mAppContext));
        Collections.sort(mStashThreadsList, new StashThreadComparator(mAppContext));
        Collections.sort(mEmbellishmentList, new StashEmbellishmentComparator(mAppContext));
        Collections.sort(mStashEmbellishmentList, new StashEmbellishmentComparator(mAppContext));
        Collections.sort(mFabricList, new StashFabricComparator(mAppContext));
        Collections.sort(mPatternsData, new StashPatternComparator());
    }

    public static StashData get(Context c) {
        if (sStash == null) {
            // create new Stash if none exists, otherwise return existing Stash
            sStash = new StashData(c.getApplicationContext());
        }

        return sStash;
    }

    public boolean saveStash() {
        // called at appropriate lifecycle events to save the stash to file
        try {
            mSerializer.saveStash(sStash);
            Log.d(TAG, "stash saved to file");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error saving stash: ", e);
            return false;
        }
    }

    public void deleteStash() {
        // clears ALL stash data and then saves it
        mEmbellishmentData.clear();
        mEmbellishmentList.clear();
        mStashEmbellishmentList.clear();
        mShoppingEmbellishmentList.clear();

        mFabricData.clear();
        mFabricList.clear();
        mStashFabricList.clear();

        mThreadsData.clear();
        mThreadsList.clear();
        mStashThreadsList.clear();
        mShoppingThreadsList.clear();

        mPatternsData.clear();
        mStashPatterns.clear();
        mFabricForList.clear();

        saveStash();
    }


    // pattern-specific stash functions
    public void setPatternData(ArrayList<StashPattern> patternList, ArrayList<StashPattern> stashPatternList) {
        // set the loaded patternlist from JSON
        mPatternsData = patternList;
        mStashPatterns = stashPatternList;
    }

    public ArrayList<StashPattern> getPatternData() {
        // pass patternlist for saving stash
        return mPatternsData;
    }

    public ArrayList<StashPattern> getStashPatterns() {
        return mStashPatterns;
    }

    public StashPattern getPattern(UUID key) {
        // given a patternId, iterate through the patterns in the list to return the correct
        // pattern object; returns null if none match
        for (StashPattern pattern : mPatternsData) {
            if (pattern.getId().equals(key)) {
                return pattern;
            }
        }

        return null;
    }

    public void addPatternToStash(StashPattern pattern) {
        if (!mStashPatterns.contains(pattern)) {
            mStashPatterns.add(pattern);
        }
    }

    public void removePatternFromStash(StashPattern pattern) {
        mStashPatterns.remove(pattern);
    }

    public void addPattern(StashPattern pattern) {
        // adds a pattern to the database
        mPatternsData.add(pattern);

        if (pattern.inStash()) {
            mStashPatterns.add(pattern);
        }
    }

    public void deletePattern(StashPattern pattern) {
        // clean up all references to this pattern
        if (pattern.getFabric() != null) {
            StashFabric fabric = pattern.getFabric();
            fabric.setUsedFor(null);
        }

        ArrayList<UUID> threadList = pattern.getThreadList();
        for (UUID threadId : threadList) {
            StashThread thread = getThread(threadId);
            thread.removePattern(pattern);
        }

        ArrayList<UUID> embellishmentList = pattern.getEmbellishmentList();
        for (UUID embellishmentId : embellishmentList) {
            StashEmbellishment embellishment = getEmbellishment(embellishmentId);
            embellishment.removePattern(pattern);
        }

        ArrayList<UUID> completedProjects = pattern.getFinishes();
        for (UUID finishId : completedProjects) {
            StashFabric fabric = getFabric(finishId);
            deleteFabric(fabric);
        }

        // removes the pattern from the database
        mPatternsData.remove(pattern);
        mStashPatterns.remove(pattern);
        mFabricForList.remove(pattern);
    }


    // fabric-specific stash functions
    public void setFabricData(HashMap<UUID, StashFabric> fabricMap, ArrayList<UUID> fabricList, ArrayList<UUID> stashFabricList) {
        // set loaded fabricMap from JSON
        mFabricData = fabricMap;
        mFabricList = fabricList;
        mStashFabricList = stashFabricList;
    }

    public void setFabricForList(ArrayList<StashPattern> patternList) {
        mFabricForList = patternList;
    }

    public ArrayList<UUID> getStashFabricList() {
        return mStashFabricList;
    }

    public ArrayList<UUID> getFabricList() {
        // pass fabriclist for list adapters
        return mFabricList;
    }

    public ArrayList<StashPattern> getFabricForList() {
        // return list of patterns marked as kitted and needing fabric for the shopping list
        return mFabricForList;
    }

    public StashFabric getFabric(UUID key) {
        // given a UUID key, look up the toString() and return fabric object
        return mFabricData.get(key);
    }

    public void addFabricToStash(UUID key) {
        if (!mStashFabricList.contains(key)) {
            mStashFabricList.add(key);
        }
    }

    public void removeFabricFromStash(UUID key) {
        mStashFabricList.remove(key);
    }

    public void addFabric(StashFabric fabric) {
        // adds fabric to the hashmap and to the list of IDs powering the adapter
        mFabricData.put(fabric.getId(), fabric);
        mFabricList.add(fabric.getId());
        mStashFabricList.add(fabric.getId());
    }

    public void deleteFabric(StashFabric fabric) {
        // clean up associations with this fabric
        if (fabric.isAssigned()) {
            StashPattern pattern = fabric.usedFor();
            pattern.setFabric(null);
        }

        // removes fabric from the hashmap and the list powering the adapter
        mFabricData.remove(fabric.getId());
        mFabricList.remove(fabric.getId());
        mStashFabricList.remove(fabric.getId());
    }


    //thread specific stash functions
    public void setThreadData(HashMap<UUID, StashThread> threadMap, ArrayList<UUID> threadList, ArrayList<UUID> stashList) {
        // set the loaded threadmap from JSON
        mThreadsData = threadMap;
        mThreadsList = threadList;
        mStashThreadsList = stashList;
    }

    public void setThreadShoppingList(ArrayList<UUID> shoppingList) {
        // store the shopping list provided by the shoppinglist creator
        mShoppingThreadsList = shoppingList;
    }

    public ArrayList<UUID> getThreadList() {
        // pass master threadlist for list adapters
        return mThreadsList;
    }

    public ArrayList<UUID> getThreadStashList() {
        // pass the stash-only threadlist for list adapters
        return mStashThreadsList;
    }

    public ArrayList<UUID> getThreadShoppingList() {
        // pass the shopping threadlist for list adapters
        Collections.sort(mShoppingThreadsList, new StashThreadComparator(mAppContext));
        return mShoppingThreadsList;
    }

    public void addThreadToStash(UUID threadId) {
        // needed to update the list as the user edits the quantities of thread owned
        // only one entry per thread on the list
        if (!mStashThreadsList.contains(threadId)) {
            mStashThreadsList.add(threadId);
        }
    }

    public void removeThreadFromStash(UUID threadId) {
        // needed to update the list as the user edits the quantities of thread owned
        mStashThreadsList.remove(threadId);
    }

    public void addThreadToShoppingList(UUID threadId) {
        if (!mShoppingThreadsList.contains(threadId)) {
            mShoppingThreadsList.add(threadId);
        }
    }

    public void removeThreadFromShoppingList(UUID threadId) {
        mShoppingThreadsList.remove(threadId);
    }

    public StashThread getThread(UUID key) {
        // given a UUID key, look up the toString() and return thread object
        return mThreadsData.get(key);
    }

    public void addThread(StashThread thread) {
        // adds a thread to the hashmap and to the list of IDs powering the adapter
        mThreadsData.put(thread.getId(), thread);
        mThreadsList.add(thread.getId());
        if (thread.isOwned() && !mStashThreadsList.contains(thread.getId())) {
            mStashThreadsList.add(thread.getId());
        }
    }

    public void deleteThread(StashThread thread) {
        // clean up associations to this item
        ArrayList<StashPattern> patternList = thread.getPatternsList();
        for (StashPattern pattern : patternList) {
            pattern.removeThread(thread, false);
        }
        patternList.clear();

        // removes a thread from the hashmap and the lists powering the adapters
        mThreadsData.remove(thread.getId());
        mThreadsList.remove(thread.getId());
        mStashThreadsList.remove(thread.getId());
        mShoppingThreadsList.remove(thread.getId());
    }


    // embellishment specific stash functions
    public void setEmbellishmentData(HashMap<UUID, StashEmbellishment> embellishmentMap, ArrayList<UUID> embellishmentList, ArrayList<UUID> stashList) {
        // set the loaded embellishmentmap from JSON
        mEmbellishmentData = embellishmentMap;
        mEmbellishmentList = embellishmentList;
        mStashEmbellishmentList = stashList;
    }

    public void setEmbellishmentShoppingList(ArrayList<UUID> shoppingList) {
        // set the embellishment shopping list (provided by the shopping list class)
        mShoppingEmbellishmentList = shoppingList;
    }

    public ArrayList<UUID> getEmbellishmentList() {
        // pass embellishmentlist for list adapters
        return mEmbellishmentList;
    }

    public ArrayList<UUID> getEmbellishmentStashList() {
        // pass the stash embellishmentlist for list adapters
        return mStashEmbellishmentList;
    }

    public ArrayList<UUID> getEmbellishmentShoppingList() {
        // pass the embellishment shopping list for list adapters
        Collections.sort(mShoppingEmbellishmentList, new StashEmbellishmentComparator(mAppContext));
        return mShoppingEmbellishmentList;
    }

    public void addEmbellishmentToStash(UUID embellishmentId) {
        // needed to update the list as the user edits the quantities of embellishments owned
        // only one entry per embellishment on the list
        if (!mStashEmbellishmentList.contains(embellishmentId)) {
            mStashEmbellishmentList.add(embellishmentId);
        }
    }

    public void removeEmbellishmentFromStash(UUID embellishmentId) {
        // needed to update the list as the user edits the quantities of embellishments owned
        mStashEmbellishmentList.remove(embellishmentId);
    }

    public void addEmbellishmentToShoppingList(UUID embellishmentId) {
        if (!mShoppingEmbellishmentList.contains(embellishmentId)) {
            mShoppingEmbellishmentList.add(embellishmentId);
        }
    }

    public void removeEmbellishmentFromShoppingList(UUID embellishmentId) {
        mShoppingEmbellishmentList.remove(embellishmentId);
    }

    public StashEmbellishment getEmbellishment(UUID key) {
        // given a UUID key, look up the toString() and return embellishment object
        return mEmbellishmentData.get(key);
    }

    public void addEmbellishment(StashEmbellishment embellishment) {
        // adds an embellishment to the hashmap and to the list of IDs powering the adapter
        mEmbellishmentData.put(embellishment.getId(), embellishment);
        mEmbellishmentList.add(embellishment.getId());
        if (embellishment.isOwned() && !mStashEmbellishmentList.contains(embellishment.getId())) {
            mStashEmbellishmentList.add(embellishment.getId());
        }
    }

    public void deleteEmbellishment(StashEmbellishment embellishment) {
        // clean up all associations to the embellishment
        ArrayList<StashPattern> patternList = embellishment.getPatternList();
        for (StashPattern pattern : patternList) {
            pattern.removeEmbellishment(embellishment, false);
        }
        patternList.clear();

        // removes embellishment from the hashmap and the lists powering the adapters
        mEmbellishmentData.remove(embellishment.getId());
        mEmbellishmentList.remove(embellishment.getId());
        mStashEmbellishmentList.remove(embellishment.getId());
        mShoppingEmbellishmentList.remove(embellishment.getId());
    }

}
