package com.geekeclectic.android.stashcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by sylk on 8/22/2014.
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
        mSkeinsOwned = 0;
        mUsedIn = new ArrayList<StashPattern>();
        mId = UUID.randomUUID();
    }

    public StashThread(JSONObject json) throws JSONException {
        mSource = json.getString(JSON_SOURCE);
        mCode = json.getString(JSON_CODE);
        mSkeinsOwned = json.getInt(JSON_OWNED);

        if (json.has(JSON_TYPE)) {
            mType = json.getString(JSON_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        json.put(JSON_SOURCE, mSource);
        json.put(JSON_CODE, mCode);
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
        if (mType != null) {
            return mSource + " " + mType + " " + mCode;
        } else {
            return mSource + " " + mCode;
        }
    }

}
