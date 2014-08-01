package com.geekeclectic.android.stashcache;

import java.util.UUID;

public class Pattern {

    private UUID mId;
    private String mTitle;
    private String mDesigner;
    private boolean mIsKit;
    private int mWidth;
    private int mHeight;

    public Pattern() {
        // Generate unique identifier
        mId = UUID.randomUUID();
    }

    @Override
    public String toString() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isKit() {
        return mIsKit;
    }

    public void setKit(boolean kit) {
        mIsKit = kit;
    }

    public void setDesigner(String designer) {
        mDesigner = designer;
    }

    public String getDesigner() {
        return mDesigner;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    public UUID getId() {
        return mId;
    }

}
