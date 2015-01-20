package com.geekeclectic.android.stashcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 1/19/2015.
 */
public class StashEmbellishment extends StashObject {

    private String mCode;
    private String mType;
    private int mNumberOwned;
    private ArrayList<StashPattern> mUsedIn;

    private static final String JSON_SOURCE = "source";
    private static final String JSON_TYPE = "embellishment type";
    private static final String JSON_CODE = "code";
    private static final String JSON_OWNED = "number owned";
    private static final String JSON_ID = "program ID";

    public StashEmbellishment() {
        // initialize variables, random ID is set in parent class
        mNumberOwned = 0;
        mUsedIn = new ArrayList<StashPattern>();
    }

    public StashEmbellishment(JSONObject json) throws JSONException {
        // initialize arraylist for patterns if creating from JSON object
        mUsedIn = new ArrayList<StashPattern>();

        setSource(json.getString(JSON_SOURCE));
        mCode = json.getString(JSON_CODE);
        mNumberOwned = json.getInt(JSON_OWNED);
        setId(UUID.fromString(json.getString(JSON_ID)));

        if (json.has(JSON_TYPE)) {
            mType = json.getString(JSON_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_SOURCE, getSource());
        json.put(JSON_CODE, mCode);
        json.put(JSON_OWNED, mNumberOwned);
        json.put(JSON_ID, getKey());

        if (mType != null) {
            json.put(JSON_TYPE, mType);
        }

        return json;
    }

    public void usedInPattern(StashPattern pattern) {
        // add link to pattern object where embellishment is used to the list
        mUsedIn.add(pattern);
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

    public void setNumberOwned(int number) {
        mNumberOwned = number;
    }

    public int getNumberOwned() {
        return mNumberOwned;
    }

    public boolean isOwned() {
        return (mNumberOwned != 0);
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
}
