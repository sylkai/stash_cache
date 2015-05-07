package com.geekeclectic.android.stashcache;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Credit to Kirk at StackOverflow (http://stackoverflow.com/questions/5660887/android-dialog-single-choice-get-radio-selected)
 * for his clear answer on how to handle radio buttons in dialogs and showing how to construct a
 * reusable dialog where the user selects one option (passing in an array of strings).
 */

public class PickOneDialogFragment extends DialogFragment implements OnClickListener {

    static final String TAG = "PickOneDialog";
    public static final String EXTRA_RESOURCE = "com.geekeclectic.android.stashcache.pick_one_res";
    public static final String EXTRA_SELECTED = "com.geekeclectic.android.stashcache.selected";

    private static int mSelectedIndex;
    private static int mResourceArray;
    private static OnDialogPickOneListener mDialogPickOneCallback;

    // listener needs to know which id was selected
    public interface OnDialogPickOneListener {
        void onSelectedOption(int dialogId);
    }

    // store the info in arguments to recreate the dialog if the device is rotated
    public static PickOneDialogFragment newInstance(int res, int selected) {
        Bundle args = new Bundle();
        final PickOneDialogFragment dialog = new PickOneDialogFragment();
        args.putInt(EXTRA_RESOURCE, res);
        args.putInt(EXTRA_SELECTED, selected);
        dialog.setArguments(args);

        return dialog;
    }

    // set the calling fragment as listener, after dialog creation
    public void setDialogPickOneListener(OnDialogPickOneListener listener) {
        mDialogPickOneCallback = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mResourceArray = getArguments().getInt(EXTRA_RESOURCE);
        mSelectedIndex = getArguments().getInt(EXTRA_SELECTED);

        final Builder builder = new Builder(this.getActivity());

        builder.setSingleChoiceItems(mResourceArray, mSelectedIndex, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();

                // send the selected value to the registered callback
                mDialogPickOneCallback.onSelectedOption(mSelectedIndex);
                break;
            default:  // user is selecting an option
                mSelectedIndex = which;
                getArguments().putInt(EXTRA_SELECTED, mSelectedIndex);
                break;
        }
    }

}
