package com.geekeclectic.android.stashcache;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by sylk on 9/15/2014.
 */
public class PickOneFragment extends DialogFragment {

    static final String TAG = "PickOneDialog";

    static int mSelectedIndex;
    static int mResourceArray;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                //.setSingleChoiceItems(R.array.fabric_choice_picker, null, this)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

}
