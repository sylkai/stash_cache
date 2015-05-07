package com.geekeclectic.android.stashcache;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is intended be the item storing information for a photo associated with a stash item.
 * Aimed at release of 1.1, not initial.
 */

public class StashPhoto {

    private static final String JSON_FILENAME = "filename";

    private String mFilename;

    public StashPhoto(String filename) {
        mFilename = filename;
    }

    public StashPhoto(JSONObject json) throws JSONException {
        mFilename = json.getString(JSON_FILENAME);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, mFilename);
        return json;
    }

    public String getFilename() {
        return mFilename;
    }

}
