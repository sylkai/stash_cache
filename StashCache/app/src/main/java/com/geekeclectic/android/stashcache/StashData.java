package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * StashData class is a singleton to serve as the "database" for the items in the stash.  Thread
 * and fabric are stored in hashmaps with the toString() representation of the UUID associated with
 * the fabric/thread as the key for fast lookup.  Patterns are stored in an ArrayList.  Based off
 * of example in BigNerdRanch Android Programming book.
 */

public class StashData {

    private static final String TAG = "StashData";
    private static final String FILENAME = "stash.json";

    private static StashData sStash;
    private Context mAppContext;

    private StashDataJSONSerializer mSerializer;

    private ArrayList<StashPattern> mPatternsData;
    private HashMap<String, StashThread> mThreadsData;
    private ArrayList<UUID> mThreadsList;
    private HashMap<String, StashFabric> mFabricData;
    private ArrayList<UUID> mFabricList;
    private HashMap<String, StashEmbellishment> mEmbellishmentData;
    private ArrayList<UUID> mEmbellishmentList;

    private StashData(Context appContext) {
        // set variables
        mAppContext = appContext;
        mSerializer = new StashDataJSONSerializer(mAppContext, FILENAME);
        mThreadsData = new HashMap<String, StashThread>();
        mThreadsList = new ArrayList<UUID>();
        mFabricData = new HashMap<String, StashFabric>();
        mFabricList = new ArrayList<UUID>();
        mEmbellishmentData = new HashMap<String, StashEmbellishment>();
        mEmbellishmentList = new ArrayList<UUID>();
        mPatternsData = new ArrayList<StashPattern>();

        sStash = this;

        // pass stash to JSON serializer to be filled in
        try {
            mSerializer.loadStash(sStash);
        } catch (Exception e) {
            Log.e(TAG, "error loading stash: ", e);
        }

        // create initial thread/fabric lists from maps for use with list adapters
        setThreadsList();
        setFabricList();
        setEmbellishmentList();
    }

    public static StashData get(Context c) {
        if (sStash == null) {
            // create new Stash if none exists, otherwise return existing Stash
            sStash = new StashData(c.getApplicationContext());
        }

        return sStash;
    }

    public void setThreadData(HashMap<String, StashThread> threadMap) {
        // set the loaded threadmap from JSON
        mThreadsData = threadMap;
    }

    public HashMap<String, StashThread> getThreadData() {
        // pass threadmap for saving stash/building links
        return mThreadsData;
    }

    public ArrayList<UUID> getThreadList() {
        // pass threadlist for list adapters
        return mThreadsList;
    }

    public void setThreadsList() {
        // to set the initial threadlist for adapters, iterate through map to add all threads
        // to the list
        if (mThreadsData.size() > 0) {
            for (Map.Entry<String, StashThread> entry : mThreadsData.entrySet()) {
                StashThread thread = entry.getValue();
                mThreadsList.add(thread.getId());
            }
        }
    }

    public StashThread getThread(UUID key) {
        // given a UUID key, look up the toString() and return thread object
        return mThreadsData.get(key.toString());
    }

    public void setEmbellishmentData(HashMap<String, StashEmbellishment> embellishmentMap) {
        // set the loaded embellishmentmap from JSON
        mEmbellishmentData = embellishmentMap;
    }

    public HashMap<String, StashEmbellishment> getEmbellishmentData() {
        // pass embellishmentmap for saving stash/building links
        return mEmbellishmentData;
    }

    public ArrayList<UUID> getEmbellishmentList() {
        // pass embellishmentlist for list adapters
        return mEmbellishmentList;
    }

    public void setEmbellishmentList() {
        // to set the initial embellishmentlist for adapters, iterate through map to add all threads
        // to the list
        if (mEmbellishmentData.size() > 0) {
            for (Map.Entry<String, StashEmbellishment> entry : mEmbellishmentData.entrySet()) {
                StashEmbellishment embellishment = entry.getValue();
                mEmbellishmentList.add(embellishment.getId());
            }
        }
    }

    public StashEmbellishment getEmbellishment(UUID key) {
        // given a UUID key, look up the toString() and return embellishment object
        return mEmbellishmentData.get(key.toString());
    }

    public void setFabricData(HashMap<String, StashFabric> fabricMap) {
        // set loaded fabricMap from JSON
        mFabricData = fabricMap;
    }

    public HashMap<String, StashFabric> getFabricData() {
        // pass fabricmap for saving stash/building links
        return mFabricData;
    }

    public ArrayList<UUID> getFabricList() {
        // pass fabriclist for list adapters
        return mFabricList;
    }

    public void setFabricList() {
        // to set the initial fabric list for adapters, iterate through map to add all fabrics to
        // the list
        if (mFabricData.size() > 0) {
            for (Map.Entry<String, StashFabric> entry : mFabricData.entrySet()) {
                StashFabric fabric = entry.getValue();
                mFabricList.add(fabric.getId());
            }
        }
    }

    public StashFabric getFabric(UUID key) {
        // given a UUID key, look up the toString() and return fabric object
        return mFabricData.get(key.toString());
    }

    public void setPatternData(ArrayList<StashPattern> patternList) {
        // set the loaded patternlist from JSON
        mPatternsData = patternList;
    }

    public ArrayList<StashPattern> getPatternData() {
        // pass patternlist for saving stash
        return mPatternsData;
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

    public void addPattern(StashPattern pattern) {
        // adds a pattern to the database
        mPatternsData.add(pattern);
    }

    public void deletePattern(StashPattern pattern) {
        // removes a pattern from the database
        mPatternsData.remove(pattern);
    }

    public void addThread(StashThread thread) {
        // adds a thread to the hashmap and to the list of IDs powering the adapter
        mThreadsData.put(thread.getId().toString(), thread);
        mThreadsList.add(thread.getId());
    }

    public void deleteThread(StashThread thread) {
        // removes a thread from the hashmap and the list powering the adapter
        mThreadsData.remove(thread.getKey());
        mThreadsList.remove(thread.getId());
    }

    public void addFabric(StashFabric fabric) {
        // adds fabric to the hashmap and to the list of IDs powering the adapter
        mFabricData.put(fabric.getId().toString(), fabric);
        mFabricList.add(fabric.getId());
    }

    public void deleteFabric(StashFabric fabric) {
        // removes fabric from the hashmap and the list powering the adapter
        mFabricData.remove(fabric.getKey());
        mFabricList.remove(fabric.getId());
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

}
