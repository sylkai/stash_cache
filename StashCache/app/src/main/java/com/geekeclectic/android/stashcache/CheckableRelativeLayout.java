package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Extension of RelativeLayout that implements the Checkable interface so it can be used as the
 * root element of a custom listView for setSingleChoiceItems() in AlertDialog
 */

public class CheckableRelativeLayout extends RelativeLayout implements Checkable {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private boolean mChecked;

    public CheckableRelativeLayout(Context context, AttributeSet attributes) {
        super(context, attributes);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

    public void toggle() {
        setChecked(!mChecked);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void setChecked(boolean checked) {
        if (mChecked != checked) {
            mChecked = checked;
            refreshDrawableState();
        }
    }

}
