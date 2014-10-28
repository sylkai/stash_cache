package com.geekeclectic.android.stashcache;

import java.util.UUID;

/**
 * Parent class for all objects included in the stash.  Contains id and manufacturer info.
 */

public class StashObject {

    private UUID mId;
    private String mSource;

    public StashObject() {
        mId = UUID.randomUUID();
    }

    protected void setId(UUID id) {
        mId = id;
    }

    public UUID getId() {
        return mId;
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

}
