package com.geekeclectic.android.stashcache;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Extension of the EditTextPreference that overrides getSummary() to allow for use of "%s" in
 * android:summary.  From:
 * http://stackoverflow.com/questions/6703130/how-to-update-edittextpreference-existing-summary-when-i-click-ok-button
 */
public class SummaryEditTextPreference extends EditTextPreference {

    public SummaryEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SummaryEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SummaryEditTextPreference(Context context) {
        super(context);
    }

    // according to ListPreference implementation
    @Override
    public CharSequence getSummary() {
        String text = getText();
        if (TextUtils.isEmpty(text)) {
            return getEditText().getHint();
        } else {
            CharSequence summary = super.getSummary();
            if (summary != null) {
                return String.format(summary.toString(), text);
            } else {
                return null;
            }
        }
    }
}