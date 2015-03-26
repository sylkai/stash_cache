package com.geekeclectic.android.stashcache;

import android.content.Context;

import java.util.UUID;

/**
 * Parent class for all objects included in the stash.  Contains id and manufacturer info.
 */

public class StashObject {

    private UUID mId;
    private Context mContext;
    private String mSource;
    private StashPhoto mPhoto;

    public StashObject() {
        mId = UUID.randomUUID();
    }

    protected void setId(UUID id) {
        mId = id;
    }

    protected void setContext(Context context) {
        mContext = context;
    }

    public UUID getId() {
        return mId;
    }

    public Context getContext() {
        return mContext;
    }

    public String getKey() {
        // UUID.toString() is used as hashmap key/JSON Object
        return mId.toString();
    }

    public void setSource(String source) {
        mSource = source;
    }

    public String getSource() {
        return mSource;
    }

    public StashPhoto getPhoto() {
        return mPhoto;
    }

    public void setPhoto(StashPhoto p) {
        mPhoto = p;
    }

    public boolean hasPhoto() {
        return (mPhoto != null);
    }

}
