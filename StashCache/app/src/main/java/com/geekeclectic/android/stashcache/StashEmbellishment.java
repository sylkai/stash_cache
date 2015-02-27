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
    private int mNumberNeeded;
    private int mNumberAdditional;
    private ArrayList<StashPattern> mUsedIn;

    private static final String JSON_SOURCE = "source";
    private static final String JSON_TYPE = "embellishment type";
    private static final String JSON_CODE = "code";
    private static final String JSON_OWNED = "number owned";
    private static final String JSON_NEEDED = "number needed";
    private static final String JSON_ADDITIONAL = "additional to buy";
    private static final String JSON_ID = "program ID";

    public StashEmbellishment() {
        // initialize variables, random ID is set in parent class
        mNumberOwned = 0;
        mNumberNeeded = 0;
        mNumberAdditional = 0;
        mUsedIn = new ArrayList<StashPattern>();
    }

    public StashEmbellishment(JSONObject json) throws JSONException {
        // initialize arraylist for patterns if creating from JSON object
        mUsedIn = new ArrayList<StashPattern>();

        setSource(json.getString(JSON_SOURCE));
        mCode = json.getString(JSON_CODE);
        mNumberOwned = json.getInt(JSON_OWNED);
        mNumberNeeded = json.getInt(JSON_NEEDED);
        mNumberAdditional = json.getInt(JSON_ADDITIONAL);
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
        json.put(JSON_NEEDED, mNumberNeeded);
        json.put(JSON_ADDITIONAL, mNumberAdditional);
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

    public void increaseOwned() {
        mNumberOwned = mNumberOwned + 1;
    }

    public void decreaseOwned() {
        if (mNumberOwned > 0) {
            mNumberOwned = mNumberOwned - 1;
        }
    }

    public boolean isOwned() {
        return (mNumberOwned != 0);
    }

    public void resetNeeded() {
        mNumberNeeded = 0;
    }

    public void addNeeded(int increment) {
        mNumberNeeded = mNumberNeeded + increment;
    }

    public void removeNeeded(int increment) {
        mNumberNeeded = mNumberNeeded - increment;
    }

    public int getNumberNeeded() {
        if (mNumberNeeded > mNumberOwned) {
            return mNumberNeeded - mNumberOwned;
        } else {
            return 0;
        }
    }

    public void increaseAdditional() {
        mNumberAdditional = mNumberAdditional + 1;
    }

    public void decreaseAdditional() {
        if (mNumberAdditional > 0) {
            mNumberAdditional = mNumberAdditional - 1;
        }
    }

    public int getAdditionalNeeded() {
        return mNumberAdditional;
    }

    public int getNumberToBuy() {
        if (mNumberNeeded > mNumberOwned) {
            return (mNumberNeeded - mNumberOwned) + mNumberAdditional;
        } else {
            return mNumberAdditional;
        }
    }

    public boolean needToBuy() {
        return (mNumberNeeded + mNumberAdditional) > mNumberOwned;
    }

    public ArrayList<StashPattern> getPatternList() {
        return mUsedIn;
    }

    @Override
    public String toString() {
        // returns formatted string for display on list
        if (mType != null) {
            return getSource() + " " + mType + " - " + mCode;
        } else {
            return getSource() + " " + mCode;
        }
    }
}
