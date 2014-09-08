package com.geekeclectic.android.stashcache;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Created by sylk on 8/22/2014.
 */
public class StashFabric {

    // fabric width and height both recorded in inches

    private static int EDGE_BUFFER = 2;

    private UUID mFabricId;
    private int mFabricCount;
    private double mFabricWidth;
    private double mFabricHeight;
    private String mFabricColor;
    private String mFabricType;
    private String mFabricSource;
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
        mFabricId = UUID.randomUUID();
    }

    public StashFabric(JSONObject json) throws JSONException {
        mFabricCount = json.getInt(JSON_COUNT);
        mFabricWidth = json.getInt(JSON_WIDTH);
        mFabricHeight = json.getInt(JSON_HEIGHT);
        mFabricId = UUID.fromString(json.getString(JSON_ID));

        updateStitchableArea();

        if (json.has(JSON_COLOR)) {
            mFabricColor = json.getString(JSON_COLOR);
        }

        if (json.has(JSON_TYPE)) {
            mFabricType = json.getString(JSON_TYPE);
        }

        if (json.has(JSON_SOURCE)) {
            mFabricSource = json.getString(JSON_SOURCE);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_ID, mFabricId.toString());
        json.put(JSON_COUNT, mFabricCount);
        json.put(JSON_WIDTH, mFabricWidth);
        json.put(JSON_HEIGHT, mFabricHeight);

        if (mFabricColor != null) {
            json.put(JSON_COLOR, mFabricColor);
        }

        if (mFabricType != null) {
            json.put(JSON_TYPE, mFabricType);
        }

        if (mFabricSource != null) {
            json.put(JSON_SOURCE, mFabricSource);
        }

        return json;
    }

    public void setSource(String source) {
        mFabricSource = source;
    }

    public String getSource() {
        return mFabricSource;
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
        mFabricFor = pattern;
    }

    public StashPattern usedFor() {
        return mFabricFor;
    }

    public void setCount(int count) {
        mFabricCount = count;
        updateStitchableArea();
    }

    public int getCount() {
        return mFabricCount;
    }

    public void setWidth(double width) {
        mFabricWidth = width;
        updateStitchableArea();
    }

    public double getWidth() {
        return mFabricWidth;
    }

    public void setHeight(double height) {
        mFabricHeight = height;
        updateStitchableArea();
    }

    public double getHeight() {
        return mFabricHeight;
    }

    public boolean willFit(int width, int height) {
        return (mStitchWidth >= width && mStitchHeight >= height);
    }

    public UUID getId() {
        return mFabricId;
    }

    public String getKey() {
        return mFabricId.toString();
    }

    public String getInfo() {
        return mFabricType + " - " + mFabricCount + " count, " + mFabricColor;
    }

    public String getSize() {
        return mFabricWidth + " inches x " + mFabricHeight + " inches";
    }

    public boolean isAssigned() {
        return (mFabricFor != null);
    }

    private void updateStitchableArea() {
        mStitchWidth = (mFabricWidth - EDGE_BUFFER * 2) * mFabricCount;
        mStitchHeight = (mFabricHeight - EDGE_BUFFER * 2) * mFabricCount;
    }

}
