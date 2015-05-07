package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
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
    private int mSkeinsAdditional;
    private ArrayList<StashPattern> mUsedIn;
    private HashMap<StashPattern, Integer> mCalledFor;

    private static final String JSON_SOURCE = "source";
    private static final String JSON_TYPE = "floss type";
    private static final String JSON_CODE = "id code";
    private static final String JSON_OWNED = "number owned";
    private static final String JSON_NEEDED = "number needed";
    private static final String JSON_ADDITIONAL = "additional to buy";
    private static final String JSON_ID = "program id";

    public StashThread(Context context) {
        // initialize variables, random id is set in parent class
        mSkeinsOwned = 0;
        mSkeinsNeeded = 0;
        mSkeinsAdditional = 0;
        mUsedIn = new ArrayList<StashPattern>();
        mCalledFor = new HashMap<StashPattern, Integer>();
        setContext(context.getApplicationContext());
    }

    public StashThread(JSONObject json, Context context) throws JSONException {
        // initialize arraylist for patterns if creating from JSON object
        mUsedIn = new ArrayList<StashPattern>();
        mCalledFor = new HashMap<StashPattern, Integer>();
        setContext(context.getApplicationContext());

        if (json.has(JSON_SOURCE)) {
            setSource(json.getString(JSON_SOURCE));
        }

        if (json.has(JSON_CODE)) {
            mCode = json.getString(JSON_CODE);
        }

        mSkeinsOwned = json.getInt(JSON_OWNED);
        mSkeinsNeeded = json.getInt(JSON_NEEDED);
        mSkeinsAdditional = json.getInt(JSON_ADDITIONAL);
        setId(UUID.fromString(json.getString(JSON_ID)));

        // if the key is not found, JSON will throw an exception so have to check first
        if (json.has(JSON_TYPE)) {
            mType = json.getString(JSON_TYPE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();

        if (getSource() != null) {
            json.put(JSON_SOURCE, getSource());
        }

        if (mCode != null) {
            json.put(JSON_CODE, mCode);
        }

        json.put(JSON_OWNED, mSkeinsOwned);
        json.put(JSON_NEEDED, mSkeinsNeeded);
        json.put(JSON_ADDITIONAL, mSkeinsAdditional);
        json.put(JSON_ID, getId().toString());

        if (mType != null) {
            json.put(JSON_TYPE, mType);
        }

        return json;
    }

    public void usedInPattern(StashPattern pattern) {
        // add link to pattern object where thread is used to the list
        if (!mUsedIn.contains(pattern)) {
            mUsedIn.add(pattern);
            mCalledFor.put(pattern, pattern.getQuantity(this));
        }
    }

    public void removePattern(StashPattern pattern) {
        // if pattern is on the list, remove it
        if (mUsedIn.contains(pattern)) {
            mUsedIn.remove(pattern);
            mCalledFor.remove(pattern);
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

    /*
    * This method increases the number owned by 1 and makes sure it is on the appropriate lists (stash
    * and/or shopping).
    */
    public void increaseOwnedQuantity() {
        // was it already on the shopping list
        boolean wasOnShoppingList = needToBuy();

        // was not owned before this, so add the thread to the stash list
        if (!isOwned()) {
            StashData.get(getContext()).addThreadToStash(getId());
        }

        // increment up by 1
        mSkeinsOwned = mSkeinsOwned + 1;

        // if it was previously on the shopping list and now does not need to be bought, remove it
        // from the shopping list
        if (wasOnShoppingList && !needToBuy()) {
            StashData.get(getContext()).removeThreadFromShoppingList(getId());
        }
    }

    /*
    * This method decreases the number owned by 1 and makes sure it is on the appropriate lists (stash
    * and/or shopping).
    */
    public void decreaseOwnedQuantity() {
        // was it already on the shopping list
        boolean wasOnShoppingList = needToBuy();

        // if it will decrease to 0, remove it from the stash list
        if (mSkeinsOwned == 1) {
            StashData.get(getContext()).removeThreadFromStash(getId());
        }

        // if the number owned is not 0, decrease by 1 (no negatives allowed)
        if (mSkeinsOwned > 0) {
            mSkeinsOwned = mSkeinsOwned - 1;
        }

        // if it was not on the shopping list before and is now needed, add to the shopping list
        if (!wasOnShoppingList && needToBuy()) {
            StashData.get(getContext()).addThreadToShoppingList(getId());
        }
    }

    /*
    * This method increases the number of skeins marked for purchase by 1 (managed by the user in
    * addition to those called for by the shopping list).  Adds it to the shopping list if needed.
    */
    public void increaseAdditionalQuantity() {
        // if first additional skein marked for purchase and not already flagged as needed
        if (mSkeinsAdditional == 0 && !needToBuy()) {
            StashData.get(getContext()).addThreadToShoppingList(getId());
        }

        mSkeinsAdditional = mSkeinsAdditional + 1;
    }

    /*
    * This method decreases the number of skeins marked for purchase by 1 (managed by the user in
    * addition to those called for by the shopping list).  Removes it from the shopping list if needed.
    */
    public void decreaseAdditionalQuantity() {
        if (mSkeinsAdditional > 0) {
            mSkeinsAdditional = mSkeinsAdditional - 1;
        }

        // if no longer needed, remove from shopping list
        if (!needToBuy()) {
            StashData.get(getContext()).removeThreadFromShoppingList(getId());
        }
    }

    public int getAdditionalSkeins() {
        return mSkeinsAdditional;
    }

    public int getSkeinsOwned() {
        return mSkeinsOwned;
    }

    public boolean isOwned() {
        return (mSkeinsOwned > 0);
    }

    public void resetNeeded() {
        mSkeinsNeeded = 0;
    }

    /*
    * This method calculates how many skeins of thread are required to complete all patterns marked
    * as kitted, with the calculations depending on whether the user has marked the threads as
    * allowing overlap or requiring a new skein for each project.
    */
    private void calculateNeeded() {
        // check whether the user allows overlap
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean overlap = !sharedPrefs.getBoolean(StashPreferencesActivity.KEY_NEW_SKEIN_FOR_EACH, false);

        resetNeeded();
        for (StashPattern pattern : mUsedIn) {
            if (pattern.isKitted()) {
                if (mSkeinsNeeded == 0) {
                    // has not already been added to the list
                    mSkeinsNeeded = mCalledFor.get(pattern);
                } else {
                    // last skein required treated as a partial and subtracted (assuming everything rounded up)
                    if (overlap) {
                        mSkeinsNeeded = mSkeinsNeeded + (mCalledFor.get(pattern) - 1);
                    // no overlapping skeins
                    } else {
                        mSkeinsNeeded = mSkeinsNeeded + mCalledFor.get(pattern);
                    }
                }
            }
        }

        if (needToBuy()) {
            // thread needs to be purchased
            StashData.get(getContext()).addThreadToShoppingList(getId());
        } else {
            // thread does not need to be purchased
            StashData.get(getContext()).removeThreadFromShoppingList(getId());
        }
    }

    /*
    * This method updates the quantity associated with a given pattern (as stored in the map) and
    * then calls the calculateNeeded method to determine if the thread should be on the shopping
    * list.  (Required because of the possibility for overlap.)
    */
    public void updateNeeded(StashPattern pattern, int quantity) {
        mCalledFor.put(pattern, quantity);
        calculateNeeded();
    }

    public int getSkeinsNeeded() {
        if (mSkeinsNeeded > mSkeinsOwned) {
            return mSkeinsNeeded - mSkeinsOwned;
        } else {
            return 0;
        }
    }

    public int getTotalNeeded() {
        return mSkeinsNeeded;
    }

    public int getSkeinsToBuy() {
        return getSkeinsNeeded() + mSkeinsAdditional;
    }

    public boolean needToBuy() {
        // thread is marked as needed, the number needed is exceeded by the number in stash OR
        // thread is marked as additional desired for purchase (user controlled)
        return (mSkeinsNeeded > 0 && mSkeinsNeeded > mSkeinsOwned) || mSkeinsAdditional > 0;
    }

    public ArrayList<StashPattern> getPatternsList() {
        return mUsedIn;
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
