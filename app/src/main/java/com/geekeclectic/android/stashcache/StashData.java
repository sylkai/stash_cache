package com.geekeclectic.android.stashcache;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sylk on 8/22/2014.
 */
public class StashData {

    private static StashData sStash;
    private Context mAppContext;

    private ArrayList<StashPattern> mPatternsData;
    private HashMap<String, StashThread> mThreadsData;
    private HashMap<String, StashFabric> mFabricData;

    private StashData(Context appContext) {
        mAppContext = appContext;
        HashMap<String, StashThread> mThreadsData = new HashMap<String, StashThread>();
        HashMap<String, StashFabric> mFabricData = new HashMap<String, StashFabric>();
        ArrayList<StashPattern> mPatternsData = new ArrayList<StashPattern>();
    }

    public static StashData get (Context c) {
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

    public ArrayList<StashThread> getThreadList() {
        ArrayList<StashThread> threadList = new ArrayList<StashThread>(mThreadsData.values());
        return threadList;
    }

    public StashThread getThread(String key) {
        return mThreadsData.get(key);
    }

    public void setFabricData(HashMap<String, StashFabric> fabricMap) {
        mFabricData = fabricMap;
    }

    public HashMap<String, StashFabric> getFabricData() {
        return mFabricData;
    }

    public ArrayList<StashFabric> getFabricList() {
        ArrayList<StashFabric> fabricList = new ArrayList<StashFabric>(mFabricData.values());
        return fabricList;
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

}
