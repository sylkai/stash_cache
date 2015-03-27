package com.geekeclectic.android.stashcache;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Each instance of this class corresponds to a type of embellishment in the stash.  The embellishment
 * is assigned a unique ID.  Fields for manufacturer, type, color code, number owned, and a
 * list of patterns it is used.  Also track the number needed for patterns and the number the user
 * wants to purchase.
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

    public StashEmbellishment(Context context) {
        // initialize variables, random ID is set in parent class
        mNumberOwned = 0;
        mNumberNeeded = 0;
        mNumberAdditional = 0;
        mUsedIn = new ArrayList<StashPattern>();
        setContext(context);
    }

    public StashEmbellishment(JSONObject json, Context context) throws JSONException {
        // initialize arraylist for patterns if creating from JSON object
        mUsedIn = new ArrayList<StashPattern>();
        setContext(context);

        if (json.has(JSON_SOURCE)) {
            setSource(json.getString(JSON_SOURCE));
        }

        if (json.has(JSON_CODE)) {
            mCode = json.getString(JSON_CODE);
        }

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

        if (getSource() != null) {
            json.put(JSON_SOURCE, getSource());
        }

        if (mCode != null) {
            json.put(JSON_CODE, mCode);
        }

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

    public int getNumberOwned() {
        return mNumberOwned;
    }

    /*
    * This method increases the number owned by 1 and makes sure it is on the appropriate lists (stash
    * and/or shopping).
    */
    public void increaseOwned() {
        // save whether it needed to be purchased
        boolean wasOnShoppingList = needToBuy();

        // if this will be the first one, add it to the stash list
        if (mNumberOwned == 0) {
            StashData.get(getContext()).addEmbellishmentToStash(getId());
        }

        // increases the number owned by 1
        mNumberOwned = mNumberOwned + 1;

        // if it previously needed to be on the shopping list and now does not, remove it
        if (wasOnShoppingList && !needToBuy()) {
            StashData.get(getContext()).removeEmbellishmentFromShoppingList(getId());
        }
    }

    /*
    * This method decreases the number owned by 1 and makes sure it is on the appropriate lists (stash
    * and/or shopping).
    */
    public void decreaseOwned() {
        // save whether it needed to be purchased
        boolean wasOnShoppingList = needToBuy();

        // if this is the last one owned, remove it from the stash list
        if (mNumberOwned == 1) {
            StashData.get(getContext()).removeEmbellishmentFromStash(getId());
        }

        // if the number owned is greater than 0, decrease it by 1
        if (mNumberOwned > 0) {
            mNumberOwned = mNumberOwned - 1;
        }

        // if it previously did not need to be on the shopping list and now does, add it
        if (!wasOnShoppingList && needToBuy()) {
            StashData.get(getContext()).addEmbellishmentToShoppingList(getId());
        }
    }

    public boolean isOwned() {
        return (mNumberOwned != 0);
    }

    public void resetNeeded() {
        mNumberNeeded = 0;
    }

    /*
    * This method increases the number needed by the increment provided and adds it to the shopping list
    * if appropriate.
    */
    public void addNeeded(int increment) {
        // save whether it needs to be purchased before the change
        boolean wasOnShoppingList = needToBuy();

        mNumberNeeded = mNumberNeeded + increment;

        // if it did not previously need to be bought and now does, add it to the shopping list
        if (!wasOnShoppingList && needToBuy()) {
            StashData.get(getContext()).addEmbellishmentToShoppingList(getId());
        }
    }

    /*
    * This method decreases the number needed by the increment provided and removes it to the shopping list
    * if appropriate.
    */
    public void removeNeeded(int increment) {
        // save whether it needs to be purchased before the change
        boolean wasOnShoppingList = needToBuy();

        mNumberNeeded = mNumberNeeded - increment;

        // if it was previously on the shopping list and now does not need to be, remove it
        if (wasOnShoppingList && !needToBuy()) {
            StashData.get(getContext()).removeEmbellishmentFromShoppingList(getId());
        }
    }

    public int getNumberNeeded() {
        if (mNumberNeeded > mNumberOwned) {
            return mNumberNeeded - mNumberOwned;
        } else {
            return 0;
        }
    }

    /*
    * This method increases the number of additional to buy (as set by the user) and if necessary,
    * adds it to the shopping list.
    */
    public void increaseAdditional() {
        // was not on the shopping list before so add it
        if (mNumberAdditional == 0 && !needToBuy()) {
            StashData.get(getContext()).addEmbellishmentToShoppingList(getId());
        }

        mNumberAdditional = mNumberAdditional + 1;
    }

    /*
    * This method decreases the number of additional to buy (as set by the user) and if necessary,
    * removes it from the shopping list.
    */
    public void decreaseAdditional() {
        if (mNumberAdditional > 0) {
            mNumberAdditional = mNumberAdditional - 1;
        }

        // if it not longer needs to be purchased, remove it from the shopping list
        if (!needToBuy()) {
            StashData.get(getContext()).removeEmbellishmentFromStash(getId());
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
