package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/*
 * Each instance of this class corresponds to a piece of fabric in the stash.  The fabric is
 * assigned a unique ID.  Fields for fabric manufacturer, type, color, count, and size (width and
 * height).  One method is included to determine whether the fabric could fit the pattern of a
 * certain size, and a private method to keep the stitchable area up to date.
 */

public class StashFabric extends StashObject {

    // fabric width and height both recorded in inches
    
    protected static int OVER_COUNT = 2;

    private int mFabricCount;
    private double mFabricWidth;
    private double mFabricHeight;
    private String mFabricColor;
    private String mFabricType;
    private StashPattern mFabricFor;

    private double mStitchWidth;
    private double mStitchHeight;

    private static final String JSON_COUNT = "fabric count";
    private static final String JSON_WIDTH = "fabric width";
    private static final String JSON_HEIGHT = "fabric height";
    private static final String JSON_COLOR = "fabric color";
    private static final String JSON_TYPE = "fabric type";
    private static final String JSON_SOURCE = "fabric company";
    private static final String JSON_ID = "fabric id";

    public StashFabric() {
        // random UUID generated in parent class
    }

    public StashFabric(JSONObject json, Context context) throws JSONException {
        // load fabricId and necessary numbers
        mFabricCount = json.getInt(JSON_COUNT);
        mFabricWidth = json.getDouble(JSON_WIDTH);
        mFabricHeight = json.getDouble(JSON_HEIGHT);
        setId(UUID.fromString(json.getString(JSON_ID)));

        updateStitchableArea(context);

        // check to make sure value was assigned before loading remaining variables
        if (json.has(JSON_COLOR)) {
            mFabricColor = json.getString(JSON_COLOR);
        }

        if (json.has(JSON_TYPE)) {
            mFabricType = json.getString(JSON_TYPE);
        }

        if (json.has(JSON_SOURCE)) {
            setSource(json.getString(JSON_SOURCE));
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, getId().toString());
        json.put(JSON_COUNT, mFabricCount);
        json.put(JSON_WIDTH, mFabricWidth);
        json.put(JSON_HEIGHT, mFabricHeight);

        // store remaining variables only if value assigned
        if (mFabricColor != null) {
            json.put(JSON_COLOR, mFabricColor);
        }

        if (mFabricType != null) {
            json.put(JSON_TYPE, mFabricType);
        }

        if (getSource() != null) {
            json.put(JSON_SOURCE, getSource());
        }

        return json;
    }

    public void setColor(String color) {
        mFabricColor = color;
    }

    public String getColor() {
        return mFabricColor;
    }

    public void setType(String type) {
        mFabricType = type;
    }

    public String getType() {
        return mFabricType;
    }

    public void setUsedFor(StashPattern pattern) {
        // allows linkage with a pattern for quick reference
        mFabricFor = pattern;
    }

    public StashPattern usedFor() {
        return mFabricFor;
    }

    public void setCount(int count, Context context) {
        // fabric count must be an integer, used to calculate stitchable area
        mFabricCount = count;
        updateStitchableArea(context);
    }

    public int getCount() {
        return mFabricCount;
    }

    public void setWidth(double width, Context context) {
        // fabric width in inches, can be decimal - used to calculate stitchable area
        mFabricWidth = width;
        updateStitchableArea(context);
    }

    public double getWidth() {
        return mFabricWidth;
    }

    public void setHeight(double height, Context context) {
        // fabric height in inches, can be decimal - used to calculate stitchable area
        mFabricHeight = height;
        updateStitchableArea(context);
    }

    public double getHeight() {
        return mFabricHeight;
    }

    public boolean willFit(int width, int height) {
        // given pattern size in stitches (width and height), returns true if fabric stitchable area
        // can accommodate pattern and checks both orientations
        return ((mStitchWidth >= width && mStitchHeight >= height) || (mStitchWidth >= height && mStitchHeight >= width));
    }

    public String getInfo() {
        // returns a formatted string giving the key fabric characteristics
        return mFabricType + " - " + mFabricCount + " count, " + mFabricColor;
    }

    public String getSize() {
        // returns a formatted string giving the fabric size, as entered
        return mFabricWidth + " inches x " + mFabricHeight + " inches";
    }

    public boolean isAssigned() {
        // if fabric has been assigned to a pattern, returns true
        return (mFabricFor != null);
    }

    private void updateStitchableArea(Context context) {
        // calculates the available stitch count using the fabric size and count, minus surrounding
        // edge buffer for framing, updated every time height/width/count is changed
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        double edge_buffer = Double.parseDouble(sharedPrefs.getString(StashPreferencesActivity.KEY_BORDER_SETTING, "3.0"));

        mStitchWidth = (mFabricWidth - edge_buffer * 2) * mFabricCount / OVER_COUNT;
        mStitchHeight = (mFabricHeight - edge_buffer * 2) * mFabricCount / OVER_COUNT;
    }

}
