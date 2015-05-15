package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.UUID;

/*
 * Each instance of this class corresponds to a piece of fabric in the stash.  The fabric is
 * assigned a unique ID.  Fields for fabric manufacturer, type, color, count, and size (width and
 * height).  One method is included to determine whether the fabric could fit the pattern of a
 * certain size, and a private method to keep the stitchable area up to date.
 */

public class StashFabric extends StashObject {

    // fabric width and height both recorded in inches

    private int mFabricCount;
    private double mFabricWidth;
    private double mFabricHeight;
    private String mFabricColor;
    private String mFabricType;
    private String mNotes;
    private StashPattern mFabricFor;
    private boolean mUsed;
    private boolean mComplete;
    private Calendar mStartDate;
    private Calendar mEndDate;

    private double mStitchWidth;
    private double mStitchHeight;
    private int mOverCount;

    private static final String JSON_COUNT = "fabric count";
    private static final String JSON_WIDTH = "fabric width";
    private static final String JSON_HEIGHT = "fabric height";
    private static final String JSON_COLOR = "fabric color";
    private static final String JSON_TYPE = "fabric type";
    private static final String JSON_SOURCE = "fabric company";
    private static final String JSON_ID = "fabric id";
    private static final String JSON_USED = "in use";
    private static final String JSON_FINISHED = "is finished";
    private static final String JSON_NOTES = "notes";
    private static final String JSON_START_DATE = "start date";
    private static final String JSON_END_DATE = "end date";

    public StashFabric(Context context) {
        // random UUID generated in parent class
        setContext(context.getApplicationContext());
        mFabricCount = StashConstants.INT_ZERO;
        mFabricWidth = StashConstants.DOUBLE_ZERO;
        mFabricHeight = StashConstants.DOUBLE_ZERO;
        mUsed = false;
        mComplete = false;
    }

    public StashFabric(JSONObject json, Context context) throws JSONException, ParseException {
        // load fabricId and necessary numbers
        mFabricCount = json.getInt(JSON_COUNT);
        setContext(context.getApplicationContext());

        mFabricWidth = json.getDouble(JSON_WIDTH);
        mFabricHeight = json.getDouble(JSON_HEIGHT);
        setId(UUID.fromString(json.getString(JSON_ID)));
        mUsed = json.getBoolean(JSON_USED);

        if (json.has(JSON_FINISHED)) {
            mComplete = json.getBoolean(JSON_FINISHED);
        } else {
            mComplete = false;
        }

        updateStitchableArea();

        // check to make sure value was assigned before loading remaining variables - otherwise an
        // exception is thrown
        if (json.has(JSON_COLOR)) {
            mFabricColor = json.getString(JSON_COLOR);
        }

        if (json.has(JSON_TYPE)) {
            mFabricType = json.getString(JSON_TYPE);
        }

        if (json.has(JSON_SOURCE)) {
            setSource(json.getString(JSON_SOURCE));
        }

        if (json.has(JSON_NOTES)) {
            mNotes = json.getString(JSON_NOTES);
        }

        if (json.has(JSON_START_DATE)) {
            mStartDate = ISO8601.toCalendar(json.getString(JSON_START_DATE));
        }

        if (json.has(JSON_END_DATE)) {
            mEndDate = ISO8601.toCalendar(json.getString(JSON_END_DATE));
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, getId().toString());
        json.put(JSON_COUNT, mFabricCount);
        json.put(JSON_WIDTH, mFabricWidth);
        json.put(JSON_HEIGHT, mFabricHeight);
        json.put(JSON_USED, mUsed);
        json.put(JSON_FINISHED, mComplete);

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

        if (mNotes != null) {
            json.put(JSON_NOTES, mNotes);
        }

        if (mStartDate != null) {
            json.put(JSON_START_DATE, ISO8601.fromCalendar(mStartDate));
        }

        if (mEndDate != null) {
            json.put(JSON_END_DATE, ISO8601.fromCalendar(mEndDate));
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

    public void setCount(int count) {
        // fabric count must be an integer, used to calculate stitchable area
        mFabricCount = count;

        updateStitchableArea();
    }

    public int getCount() {
        return mFabricCount;
    }

    public void setWidth(double width) {
        // fabric width in inches, can be decimal - used to calculate stitchable area
        mFabricWidth = width;
        updateStitchableArea();
    }

    public double getWidth() {
        return mFabricWidth;
    }

    public void setHeight(double height) {
        // fabric height in inches, can be decimal - used to calculate stitchable area
        mFabricHeight = height;
        updateStitchableArea();
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

    public void setNotes(String notes) {
        mNotes = notes;
    }

    public String getNotes() {
        return mNotes;
    }

    public boolean inUse() {
        return mUsed;
    }

    public void setUse(boolean used) {
        mUsed = used;
    }

    public boolean isFinished() {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    private void updateStitchableArea() {
        // calculates the available stitch count using the fabric size and count, minus surrounding
        // edge buffer for framing, updated every time height/width/count is changed
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        double edge_buffer;
        try {
            edge_buffer = Double.parseDouble(sharedPrefs.getString(StashPreferencesActivity.KEY_BORDER_SETTING, StashConstants.DEFAULT_BORDER));
        } catch (NumberFormatException e) {
            edge_buffer = Double.parseDouble(StashConstants.DEFAULT_BORDER);
        }

        int over_default = Integer.parseInt(sharedPrefs.getString(StashPreferencesActivity.KEY_CROSSOVER, StashConstants.OVER_TWO_DEFAULT));

        // if the fabric count is above the default, calculate it stitching over two (otherwise over one)
        if (mFabricCount > over_default) {
            mOverCount = StashConstants.OVER_TWO;
        } else {
            mOverCount = StashConstants.OVER_ONE;
        }

        // calculate the stitching width and height (in stitches) after subtracting the two borders
        mStitchWidth = (mFabricWidth - edge_buffer * StashConstants.TWO_BORDERS) * mFabricCount / mOverCount;
        mStitchHeight = (mFabricHeight - edge_buffer * StashConstants.TWO_BORDERS) * mFabricCount / mOverCount;
    }

    public void setStartDate(Calendar date) {
        mStartDate = date;
    }

    public Calendar getStartDate() {
        return mStartDate;
    }

    public void setEndDate(Calendar date) {
        mEndDate = date;
    }

    public Calendar getEndDate() {
        return mEndDate;
    }

}
