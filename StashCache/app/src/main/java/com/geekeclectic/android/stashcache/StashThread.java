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

public class StashThread extends StashObject {

    private String mCode;
    private String mType;
    private int mSkeinsOwned;
    private int mSkeinsNeeded;
    private ArrayList<StashPattern> mUsedIn;

    private static final String JSON_SOURCE = "source";
    private static final String JSON_TYPE = "floss type";
    private static final String JSON_CODE = "id code";
    private static final String JSON_OWNED = "number owned";
    private static final String JSON_NEEDED = "number needed";
    private static final String JSON_ID = "program id";

    public StashThread() {
        // initialize variables, random id is set in parent class
        mSkeinsOwned = 0;
        mSkeinsNeeded = 0;
        mUsedIn = new ArrayList<StashPattern>();
    }

    public StashThread(JSONObject json) throws JSONException {
        // initialize arraylist for patterns if creating from JSON object
        mUsedIn = new ArrayList<StashPattern>();

        setSource(json.getString(JSON_SOURCE));
        mCode = json.getString(JSON_CODE);
        mSkeinsOwned = json.getInt(JSON_OWNED);
        mSkeinsNeeded = json.getInt(JSON_NEEDED);
        setId(UUID.fromString(json.getString(JSON_ID)));

        if (json.has(JSON_TYPE)) {
            mType = json.getString(JSON_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_SOURCE, getSource());
        json.put(JSON_CODE, mCode);
        json.put(JSON_OWNED, mSkeinsOwned);
        json.put(JSON_NEEDED, mSkeinsNeeded);
        json.put(JSON_ID, getKey());

        if (mType != null) {
            json.put(JSON_TYPE, mType);
        }

        return json;
    }

    public void usedInPattern(StashPattern pattern) {
        // add link to pattern object where thread is used to the list
        if (!mUsedIn.contains(pattern)) {
            mUsedIn.add(pattern);
        }
    }

    public void removePattern(StashPattern pattern) {
        // if pattern is on the list, remove it
        if (mUsedIn.contains(pattern)) {
            mUsedIn.remove(pattern);
        }
    }

    public void setCode(String idCode) {
        mCode = idCode;
    }

    public String getCode() {
        return mCode;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
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

    public void resetNeeded() {
        mSkeinsNeeded = 0;
    }

    public void addNeeded(int increment) {
        if (mSkeinsOwned == 0 && mSkeinsNeeded == 0) {
            // none owned and has not already been added to the list
            mSkeinsNeeded = increment;
        } else {
            // last skein required treated as a partial and subtracted (assuming everything rounded up)
            mSkeinsNeeded = mSkeinsNeeded + (increment - 1);
        }
    }

    public void removeNeeded(int increment) {
        // last skein required treated as a partial and subtracted (assuming everything rounded up)
        mSkeinsNeeded = mSkeinsNeeded - (increment - 1);
    }

    public int getSkeinsNeeded() {
        return mSkeinsNeeded;
    }

    public boolean needToBuy() {
        // need to take into account the partial skein assumptions when calculating mSkeinsNeeded
        return mSkeinsNeeded > (mSkeinsOwned - 1);
    }

    @Override
    public String toString() {
        // returns formatted string for display on list
        if (mType != null) {
            return getSource() + " " + mCode + " - " + mType;
        } else {
            return getSource() + " " + mCode;
        }
    }

    public String getDescriptor() {
        return getSource() + " " + mCode;
    }

}
