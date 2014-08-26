package com.geekeclectic.android.stashcache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.*;

public class StashPattern {

    private UUID mPatternId;
    private ArrayList<StashThread> mThreads;
    private int mPatternHeight;
    private int mPatternWidth;
    private String mPatternName;
    private String mPatternSource;
    private StashFabric mPatternFabric;

    private static final String JSON_NAME = "name";
    private static final String JSON_HEIGHT = "height";
    private static final String JSON_WIDTH = "width";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_FABRIC = "fabric id";
    private static final String JSON_THREADS = "threads";
    private static final String JSON_PATTERN = "pattern id";

    public StashPattern() {
        // generate random id
        mPatternId = UUID.randomUUID();
        mThreads = new ArrayList<StashThread>();
    }

    public StashPattern(JSONObject json, HashMap<String, StashThread> threadMap, HashMap<String, StashFabric> fabricMap) throws JSONException {
        mPatternId = UUID.fromString(json.getString(JSON_PATTERN));

        if (json.has(JSON_NAME)) {
            mPatternName = json.getString(JSON_NAME);
        }

        if (json.has(JSON_HEIGHT)) {
            mPatternHeight = json.getInt(JSON_HEIGHT);
        }

        if (json.has(JSON_WIDTH)) {
            mPatternWidth = json.getInt(JSON_WIDTH);
        }

        if (json.has(JSON_SOURCE)) {
            mPatternSource = json.getString(JSON_SOURCE);
        }

        if (json.has(JSON_FABRIC)) {
            mPatternFabric = fabricMap.get(json.getString(JSON_FABRIC));
        }

        mThreads = new ArrayList<StashThread>();
        if (json.has(JSON_THREADS)) {
            JSONArray array = json.getJSONArray(JSON_THREADS);
            for (int i = 0; i < array.length(); i++) {
                StashThread thread = threadMap.get(array.getString(i));
                thread.usedInPattern(this);
                mThreads.add(thread);
            }
        }

    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_PATTERN, mPatternId.toString());

        if (mPatternName != null) {
            json.put(JSON_NAME, mPatternName);
        }

        if (mPatternHeight > 0) {
            json.put(JSON_HEIGHT, mPatternHeight);
        }

        if (mPatternWidth > 0) {
            json.put(JSON_WIDTH, mPatternWidth);
        }

        if (mPatternSource != null) {
            json.put(JSON_SOURCE, mPatternSource);
        }

        if (mPatternFabric != null) {
            json.put(JSON_FABRIC, mPatternFabric.getId());
        }

        if (!mThreads.isEmpty()) {
            JSONArray array = new JSONArray();
            for (StashThread thread : mThreads) {
                array.put(thread.getKey());
            }
            json.put(JSON_THREADS, array);
        }

        return json;
    }

    public void setPatternName(String name) {
        mPatternName = name;
    }

    public String getPatternName() {
        return mPatternName;
    }

    public void setPatternSource(String source) {
        mPatternSource = source;
    }

    public String getPatternSource() {
        return mPatternSource;
    }

    public void setHeight(int height) {
        mPatternHeight = height;
    }

    public int getHeight() {
        return mPatternHeight;
    }

    public void setWidth(int width) {
        mPatternWidth = width;
    }

    public int getWidth() {
        return mPatternWidth;
    }

    public void setFabric(StashFabric fabric) {
        mPatternFabric = fabric;
    }

    public StashFabric getFabric() {
        return mPatternFabric;
    }

    public void addThread(StashThread thread) {
        mThreads.add(thread);
    }

    public ArrayList<StashThread> getThreadList() {
        return mThreads;
    }

    public UUID getId() {
        return mPatternId;
    }

    @Override
    public String toString() {
        return mPatternName;
    }

}
