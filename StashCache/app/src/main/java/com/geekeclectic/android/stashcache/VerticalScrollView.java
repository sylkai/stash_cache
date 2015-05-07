package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * From comment here, to allow nesting of listviews in a scrollview.
 * http://stackoverflow.com/questions/3495890/how-can-i-put-a-listview-into-a-scrollview-without-it-collapsing/3495908#3495908
 */

public class VerticalScrollView extends ScrollView{

    public VerticalScrollView(Context context) {
        super(context);
    }

    public VerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                return false;

            case MotionEvent.ACTION_CANCEL:
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
                return false;

            default:
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return true;
    }
}
