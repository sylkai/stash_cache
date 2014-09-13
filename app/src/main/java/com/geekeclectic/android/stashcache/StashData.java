package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sylk on 8/22/2014.
 */
public class StashData {

    private static final String TAG = "StashData";
    private static final String FILENAME = "stash.json";

    private static StashData sStash;
    private Context mAppContext;

    private StashCacheJSONSerializer mSerializer;

    private ArrayList<StashPattern> mPatternsData;
    private HashMap<String, StashThread> mThreadsData;
    private ArrayList<UUID> mThreadsList;
    private HashMap<String, StashFabric> mFabricData;
    private ArrayList<UUID> mFabricList;

    private StashData(Context appContext) {
        mAppContext = appContext;
        mSerializer = new StashCacheJSONSerializer(mAppContext, FILENAME);
        mThreadsData = new HashMap<String, StashThread>();
        mThreadsList = new ArrayList<UUID>();
        mFabricData = new HashMap<String, StashFabric>();
        mFabricList = new ArrayList<UUID>();
        mPatternsData = new ArrayList<StashPattern>();

        sStash = this;

        try {
            mSerializer.loadStash(sStash);
        } catch (Exception e) {
            Log.e(TAG, "error loading stash: ", e);
        }

        setThreadsList();
        setFabricList();
    }

    public static StashData get(Context c) {
        if (sStash == null) {
            sStash = new StashData(c.getApplicationContext());
        }

        return sStash;
    }

    public void setThreadData(HashMap<String, StashThread> threadMap) {
        mThreadsData = threadMap;
    }

    public HashMap<String, StashThread> getThreadData() {
        return mThreadsData;
    }

    public ArrayList<UUID> getThreadList() {
        return mThreadsList;
    }

    public void setThreadsList() {
        if (mThreadsData.size() > 0) {
            for (Map.Entry<String, StashThread> entry : mThreadsData.entrySet()) {
                StashThread thread = entry.getValue();
                mThreadsList.add(thread.getId());
            }
        }
    }

    public StashThread getThread(UUID key) {
        return mThreadsData.get(key.toString());
    }

    public void setFabricData(HashMap<String, StashFabric> fabricMap) {
        mFabricData = fabricMap;
    }

    public HashMap<String, StashFabric> getFabricData() {
        return mFabricData;
    }

    public ArrayList<UUID> getFabricList() {
        return mFabricList;
    }

    public void setFabricList() {
        if (mFabricData.size() > 0) {
            for (Map.Entry<String, StashFabric> entry : mFabricData.entrySet()) {
                StashFabric fabric = entry.getValue();
                mFabricList.add(fabric.getId());
            }
        }
    }

    public StashFabric getFabric(UUID key) {
        return mFabricData.get(key.toString());
    }

    public void setPatternData(ArrayList<StashPattern> patternList) {
        mPatternsData = patternList;
    }

    public ArrayList<StashPattern> getPatternData() {
        return mPatternsData;
    }

    public StashPattern getPattern(UUID key) {
        for (StashPattern pattern : mPatternsData) {
            if (pattern.getId().equals(key)) {
                return pattern;
            }
        }

        return null;
    }

    public void addPattern(StashPattern pattern) {
        mPatternsData.add(pattern);
    }

    public void deletePattern(StashPattern pattern) {
        mPatternsData.remove(pattern);
    }

    public void addThread(StashThread thread) {
        mThreadsData.put(thread.getKey(), thread);
        mThreadsList.add(thread.getId());
    }

    public void deleteThread(StashThread thread) {
        mThreadsData.remove(thread.getKey());
        mThreadsList.remove(thread.getId());
    }

    public void addFabric(StashFabric fabric) {
        mFabricData.put(fabric.getKey(), fabric);
        mFabricList.add(fabric.getId());
    }

    public void deleteFabric(StashFabric fabric) {
        mFabricData.remove(fabric.getKey());
        mFabricList.remove(fabric.getId());
    }

    public boolean saveStash() {
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
