package com.geekeclectic.android.stashcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/*
 * Each instance of this class corresponds to a type of thread in the stash.  The thread is
 * assigned a unique ID.  Fields for thread manufacturer, type, color code, number owned, and a
 * list of patterns it is used.
 */

public class StashThread {

    private UUID mId;
    private String mSource;
    private String mCode;
    private String mType;
    private int mSkeinsOwned;
    private ArrayList<StashPattern> mUsedIn;

    private static final String JSON_SOURCE = "source";
    private static final String JSON_TYPE = "floss type";
    private static final String JSON_CODE = "id code";
    private static final String JSON_OWNED = "number owned";
    private static final String JSON_ID = "program id";

    public StashThread() {
        // initialize variables
        mId = UUID.randomUUID();
        mSkeinsOwned = 0;
        mUsedIn = new ArrayList<StashPattern>();
    }

    public StashThread(JSONObject json) throws JSONException {
        // initialize arraylist for patterns if creating from JSON object
        mUsedIn = new ArrayList<StashPattern>();

        mSource = json.getString(JSON_SOURCE);
        mCode = json.getString(JSON_CODE);
        mSkeinsOwned = json.getInt(JSON_OWNED);
        mId = UUID.fromString(json.getString(JSON_ID));

        if (json.has(JSON_TYPE)) {
            mType = json.getString(JSON_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_SOURCE, mSource);
        json.put(JSON_CODE, mCode);
        json.put(JSON_OWNED, mSkeinsOwned);
        json.put(JSON_ID, mId.toString());

        if (mType != null) {
            json.put(JSON_TYPE, mType);
        }

        return json;
    }

    public void usedInPattern(StashPattern pattern) {
        // add link to pattern object where thread is used to the list
        mUsedIn.add(pattern);
    }

    public void removePattern(StashPattern pattern) {
        // if pattern is on the list, remove it
        if (mUsedIn.contains(pattern)) {
            mUsedIn.remove(pattern);
        }
    }

    public void setCompany(String source) {
        mSource = source;
    }

    public String getCompany() {
        return mSource;
    }

    public void setCode(String idCode) {
        mCode = idCode;
    }

    public String getCode() {
        return mCode;
    }

    public UUID getId() {
        return mId;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public String getKey() {
        // UUID.toString() is used as hashmap key/JSON Object
        return mId.toString();
    }

    public void setSkeinsOwned(int number) {
        mSkeinsOwned = number;
    }

    public int getSkeinsOwned() {
        return mSkeinsOwned;
    }

    public boolean isOwned() {
        return (mSkeinsOwned != 0);
    }

    @Override
    public String toString() {
        // returns formatted string for display on list
        if (mType != null) {
            return mSource + " " + mCode + " - " + mType;
        } else {
            return mSource + " " + mCode;
        }
    }

}
