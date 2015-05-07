package com.geekeclectic.android.stashcache;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ProgressBar;

/**
 * Custom dialog to create a transparent loading indicator, adapted from StackOverflow:
 * http://stackoverflow.com/questions/3225889/how-to-center-progress-indicator-in-progressdialog-easily-when-no-title-text-pa
 */
public class TransparentProgressDialog extends Dialog {

    public static TransparentProgressDialog show(Context context) {
        return show(context, null, null, false);
    }

    public static TransparentProgressDialog show(Context context, CharSequence title,
                                        CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static TransparentProgressDialog show(Context context, CharSequence title,
                                        CharSequence message, boolean indeterminate,
                                        boolean cancelable, OnCancelListener cancelListener) {
        TransparentProgressDialog dialog = new TransparentProgressDialog(context);
        dialog.setTitle(title);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        /* The next line will add the ProgressBar to the dialog. */
        dialog.addContentView(new ProgressBar(context), new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        dialog.show();

        return dialog;
    }

    public TransparentProgressDialog(Context context) {
        super(context, R.style.NewDialog);
    }

}