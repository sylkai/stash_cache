package com.geekeclectic.android.stashcache;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sylk on 8/22/2014.
 */
public class StashData {

    private ArrayList<StashPattern> mPatternsData;
    private HashMap<String, StashThread> mThreadsData;
    private HashMap<String, StashFabric> mFabricData;

    public StashData() {
        HashMap<String, StashThread> mThreadsData = new HashMap<String, StashThread>();
        HashMap<String, StashFabric> mFabricData = new HashMap<String, StashFabric>();
        ArrayList<StashPattern> mPatternsData = new ArrayList<StashPattern>();
    }

    public void setThreadData(HashMap<String, StashThread> threadMap) {
        mThreadsData = threadMap;
    }

    public HashMap<String, StashThread> getThreadData() {
        return mThreadsData;
    }

    public void setFabricData(HashMap<String, StashFabric> fabricMap) {
        mFabricData = fabricMap;
    }

    public HashMap<String, StashFabric> getFabricData() {
        return mFabricData;
    }

    public void setPatternData(ArrayList<StashPattern> patternList) {
        mPatternsData = patternList;
    }

    public ArrayList<StashPattern> getPatternData() {
        return mPatternsData;
    }

}
