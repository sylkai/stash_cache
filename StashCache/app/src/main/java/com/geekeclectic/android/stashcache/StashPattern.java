package com.geekeclectic.android.stashcache;

import android.content.Context;

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

public class StashPattern extends StashObject {

    private ArrayList<UUID> mThreads;
    private ArrayList<UUID> mEmbellishments;
    private HashMap<UUID, Integer> mQuantities;
    private int mPatternHeight;
    private int mPatternWidth;
    private String mPatternName;
    private StashFabric mPatternFabric;
    private boolean mIsKitted;

    private static final String JSON_NAME = "name";
    private static final String JSON_HEIGHT = "height";
    private static final String JSON_WIDTH = "width";
    private static final String JSON_SOURCE = "source";
    private static final String JSON_FABRIC = "fabric id";
    private static final String JSON_THREADS = "threads";
    private static final String JSON_EMBELLISHMENTS = "embellishments";
    private static final String JSON_PATTERN = "pattern id";
    private static final String JSON_PHOTO = "photo";
    private static final String JSON_QUANTITIES = "required quantities";
    private static final String JSON_QUANTITY_ID = "id code";
    private static final String JSON_QUANTITY_ENTRY = "number";
    private static final String JSON_KITTED = "kitted";

    public StashPattern(Context context) {
        // random ID generated in parent class

        // initialize threadList
        mThreads = new ArrayList<UUID>();
        mEmbellishments = new ArrayList<UUID>();
        mQuantities = new HashMap<UUID, Integer>();
        mIsKitted = false;
        setContext(context);
    }

    public StashPattern(JSONObject json, HashMap<String, StashThread> threadMap, HashMap<String, StashFabric> fabricMap, HashMap<String, StashEmbellishment> embellishmentMap, Context context) throws JSONException {
        setId(UUID.fromString(json.getString(JSON_PATTERN)));
        setContext(context);
        mIsKitted = json.getBoolean(JSON_KITTED);

        // because values are only stored if they exist, we need to check for the tag before
        // getting the value (otherwise an exception is thrown)
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
            setSource(json.getString(JSON_SOURCE));
        }

        if (json.has(JSON_PHOTO)) {
            setPhoto(new StashPhoto(json.getJSONObject(JSON_PHOTO)));
        }

        if (json.has(JSON_FABRIC)) {
            // look up fabricId in fabricMap to get appropriate fabric object
            mPatternFabric = fabricMap.get(json.getString(JSON_FABRIC));

            // set link in fabric object to the pattern
            mPatternFabric.setUsedFor(this);
        }

        mQuantities = new HashMap<UUID, Integer>();
        mThreads = new ArrayList<UUID>();
        if (json.has(JSON_THREADS)) {
            JSONArray array = json.getJSONArray(JSON_THREADS);
            for (int i = 0; i < array.length(); i++) {
                // look up threadId in threadMap to get appropriate thread object
                StashThread thread = threadMap.get(array.getString(i));
                UUID threadId = thread.getId();

                if (mThreads.contains(threadId)) {
                    mQuantities.put(threadId, mQuantities.get(threadId) + 1);
                    thread.updateNeeded(this, mQuantities.get(threadId));
                } else {
                    // set link in thread object to the pattern
                    thread.usedInPattern(this);

                    // add threadId to list
                    mThreads.add(threadId);

                    // add thread to quantities map
                    mQuantities.put(threadId, 1);
                    thread.updateNeeded(this, mQuantities.get(threadId));
                }
            }
        }

        mEmbellishments = new ArrayList<UUID>();
        if (json.has(JSON_EMBELLISHMENTS)) {
            JSONArray array = json.getJSONArray(JSON_EMBELLISHMENTS);
            for (int i = 0; i < array.length(); i++) {
                // look up embellishmentId in embellishmentMap to get appropriate object
                StashEmbellishment embellishment = embellishmentMap.get(array.getString(i));

                if (mEmbellishments.contains(embellishment.getId())) {
                    mQuantities.put(embellishment.getId(), mQuantities.get(embellishment.getId()) + 1);
                } else {
                    // set link in embellishment object to the pattern
                    embellishment.usedInPattern(this);

                    // add embellishmentId to list
                    mEmbellishments.add(embellishment.getId());

                    // add embellishment to quantities map
                    mQuantities.put(embellishment.getId(), 1);
                }
            }
        }


        /*if (json.has(JSON_QUANTITIES)) {
            JSONArray array = json.getJSONArray(JSON_QUANTITIES);
            for (int i = 0; i < array.length(); i++) {
                // read the UUID/quantity pairs from the JSONObject
                JSONObject entry = array.getJSONObject(i);
                UUID id = UUID.fromString(entry.getString(JSON_QUANTITY_ID));
                int number = entry.getInt(JSON_QUANTITY_ENTRY);

                mQuantities.put(id, number);
            }
        }*/
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_PATTERN, getKey());
        json.put(JSON_KITTED, mIsKitted);

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

        if (getSource() != null) {
            json.put(JSON_SOURCE, getSource());
        }

        if (hasPhoto()) {
            json.put(JSON_PHOTO, getPhoto().toJSON());
        }

        if (mPatternFabric != null) {
            // store the fabricId as a string for lookup when loading
            json.put(JSON_FABRIC, mPatternFabric.getKey());
        }

        if (!mThreads.isEmpty()) {
            // store threads as an array to group the list together and indicate when done
            JSONArray array = new JSONArray();
            for (UUID threadId : mThreads) {
                int quantity = mQuantities.get(threadId);
                for (int i = 0; i < quantity; i++) {
                    // store the threadId as a string for lookup when loading
                    // duplicate entries indicate multiples needed
                    array.put(threadId.toString());
                }

            }
            json.put(JSON_THREADS, array);
        }

        if (!mEmbellishments.isEmpty()) {
            // store embellishments as an array to group the list together and indicate when done
            JSONArray array = new JSONArray();
            for (UUID embellishmentId : mEmbellishments) {
                int quantity = mQuantities.get(embellishmentId);
                for (int i = 0; i < quantity; i++) {
                    // store the embellishmentId as a string for lookup when loading
                    // duplicate entries indicate multiple needed
                    array.put(embellishmentId.toString());
                }
            }
            json.put(JSON_EMBELLISHMENTS, array);
        }

        /*if (!mQuantities.isEmpty()) {
            // store the UUID/quantity pairs in objects in an array for later retrieval
            JSONArray array = new JSONArray();
            for(UUID id : mQuantities.keySet()) {
                JSONObject entry = new JSONObject();
                entry.put(JSON_QUANTITY_ID, id.toString());
                entry.put(JSON_QUANTITY_ENTRY, mQuantities.get(id));
                array.put(entry);
            }
        }*/

        return json;
    }

    public void setPatternName(String name) {
        mPatternName = name;
    }

    public String getPatternName() {
        return mPatternName;
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

    public void removeThread(StashThread thread) {
        // get rid of connections between the thread and pattern (pattern contribution to the
        // shopping list if kitted handled in removePattern (because of overlap)
        mThreads.remove(thread.getId());
        mQuantities.remove(thread.getId());
        thread.removePattern(this);
    }

    public void removeEmbellishment(StashEmbellishment embellishment) {
        // if the pattern was marked kitted, remove the pattern's contribution to the embellishment shopping list
        if (mIsKitted) {
            embellishment.removeNeeded(mQuantities.get(embellishment.getId()));
        }

        // get rid of connections between the embellishment and pattern
        mEmbellishments.remove(embellishment.getId());
        mQuantities.remove(embellishment.getId());
        embellishment.removePattern(this);
    }

    public void decreaseQuantity(StashThread thread) {
        // decrease quantity for thread in the quantitymap by 1, remove it from map/thread list if
        // quantity goes to 0
        UUID threadId = thread.getId();
        Integer count = mQuantities.get(threadId);

        if (count == 1) {
            mQuantities.remove(threadId);
            mThreads.remove(threadId);
            thread.removePattern(this);
        } else if (count > 1) {
            mQuantities.put(threadId, count - 1);
            thread.updateNeeded(this, count - 1);
        }
    }

    public void increaseQuantity(StashThread thread) {
        // increase quantity for thread in the quantitymap by 1, add it to map/thread list if the
        // quantity had been 0
        UUID threadId = thread.getId();
        Integer count = mQuantities.get(threadId);

        if (count == null) {
            mQuantities.put(threadId, 1);
            mThreads.add(threadId);
            thread.usedInPattern(this);
            thread.updateNeeded(this, mQuantities.get(threadId));
        } else if (count > 0) {
            mQuantities.put(threadId, count + 1);
            thread.updateNeeded(this, mQuantities.get(threadId));
        }
    }

    public void decreaseQuantity(StashEmbellishment embellishment) {
        // decrease quantity for embellishment in the quantitymap by 1, remove it from map/thread list if
        // quantity goes to 0
        UUID embellishmentId = embellishment.getId();
        Integer count = mQuantities.get(embellishmentId);

        if (count == 1) {
            mQuantities.remove(embellishmentId);
            mEmbellishments.remove(embellishmentId);
            embellishment.removePattern(this);
        } else if (count > 1) {
            mQuantities.put(embellishmentId, count - 1);
        }
    }

    public void increaseQuantity(StashEmbellishment embellishment) {
        // increase quantity for embellishment in the quantitymap by 1, add it to map/thread list if the
        // quantity had been 0
        UUID embellishmentId = embellishment.getId();
        Integer count = mQuantities.get(embellishmentId);

        if (count == null) {
            mQuantities.put(embellishmentId, 1);
            mEmbellishments.add(embellishmentId);
            embellishment.usedInPattern(this);
        } else if (count > 0) {
            mQuantities.put(embellishmentId, count + 1);
        }
    }

    public int getQuantity(StashObject object) {
        if (mQuantities.get(object.getId()) != null) {
            return mQuantities.get(object.getId());
        } else {
            return 0;
        }
    }

    public HashMap<UUID, Integer> getQuantities() {
        return mQuantities;
    }

    public ArrayList<UUID> getThreadList() {
        return mThreads;
    }

    public ArrayList<UUID> getEmbellishmentList() {
        return mEmbellishments;
    }

    public void setKitted(boolean isKitted) {
        mIsKitted = isKitted;
    }

    public boolean isKitted() {
        return mIsKitted;
    }

    @Override
    public String toString() {
        return mPatternName;
    }

}
