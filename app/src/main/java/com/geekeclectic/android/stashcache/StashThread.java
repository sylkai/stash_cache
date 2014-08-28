package com.geekeclectic.android.stashcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by sylk on 8/22/2014.
 */
public class StashThread {

    private String mSource;
    private String mId;
    private String mType;
    private int mSkeinsOwned;
    private ArrayList<StashPattern> mUsedIn;

    private static final String JSON_SOURCE = "source";
    private static final String JSON_TYPE = "floss type";
    private static final String JSON_ID = "id code";
    private static final String JSON_OWNED = "number owned";

    public StashThread() {
        mSkeinsOwned = 0;
        mUsedIn = new ArrayList<StashPattern>();
    }

    public StashThread(JSONObject json) throws JSONException {
        mSource = json.getString(JSON_SOURCE);
        mId = json.getString(JSON_ID);
        mSkeinsOwned = json.getInt(JSON_OWNED);

        if (json.has(JSON_TYPE)) {
            mType = json.getString(JSON_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_SOURCE, mSource);
        json.put(JSON_ID, mId);
        json.put(JSON_OWNED, mSkeinsOwned);

        if (mType != null) {
            json.put(JSON_TYPE, mType);
        }

        return json;
    }

    public void usedInPattern(StashPattern pattern) {
        mUsedIn.add(pattern);
    }

    public void removePattern(StashPattern pattern) {
        int index = mUsedIn.indexOf(pattern);

        if (index != -1) {
            mUsedIn.remove(index);
        }
    }

    public void setCompany(String source) {
        mSource = source;
    }

    public String getCompany() {
        return mSource;
    }

    public void setId(String idCode) {
        mId = idCode;
    }

    public String getId() {
        return mId;
    }

    public void setType(String type) {
        mType = type;
    }

    public String getType() {
        return mType;
    }

    public String getKey() {
        return mSource + " " + mType + " " + mId;
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
        if (mType != null) {
            return getKey();
        } else {
            return mSource + " " + mId;
        }
    }

}
