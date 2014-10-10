package com.geekeclectic.android.stashcache;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/*
 * Each instance of this class corresponds to a pattern in the stash.  The pattern is assigned a
 * unique ID. Fields to track name, designer, pattern size (height and width), associated fabric,
 * and associated threads.
 */

public class StashPattern {

    private UUID mPatternId;
    private ArrayList<UUID> mThreads;
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

        // initialize threadList
        mThreads = new ArrayList<UUID>();
    }

    public StashPattern(JSONObject json, HashMap<String, StashThread> threadMap, HashMap<String, StashFabric> fabricMap) throws JSONException {
        mPatternId = UUID.fromString(json.getString(JSON_PATTERN));

        // because values are only stored if they exist, we need to check for the tag before
        // getting the value
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
            // look up fabricId in fabricMap to get appropriate fabric object
            mPatternFabric = fabricMap.get(json.getString(JSON_FABRIC));

            // set link in fabric object to the pattern
            mPatternFabric.setUsedFor(this);
        }

        mThreads = new ArrayList<UUID>();
        if (json.has(JSON_THREADS)) {
            JSONArray array = json.getJSONArray(JSON_THREADS);
            for (int i = 0; i < array.length(); i++) {
                // look up threadId in threadMap to get appropriate thread object
                StashThread thread = threadMap.get(array.getString(i));

                // set link in thread object to the pattern
                thread.usedInPattern(this);

                // add threadId to list
                mThreads.add(thread.getId());
            }
        }

    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_PATTERN, mPatternId.toString());

        // values are only stored if they exist - nothing is stored if no value has been entered
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
            // store the fabricId as a string for lookup when loading
            json.put(JSON_FABRIC, mPatternFabric.getKey());
        }

        if (!mThreads.isEmpty()) {
            // store threads as an array to group the list together and indicate when done
            JSONArray array = new JSONArray();
            for (UUID threadId : mThreads) {
                // store the threadId as a string for lookup when loading
                array.put(threadId.toString());
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
        mThreads.add(thread.getId());
    }

    public void removeThread(StashThread thread) {
        mThreads.remove(thread.getId());
    }

    public ArrayList<UUID> getThreadList() {
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
