package com.geekeclectic.android.stashcache;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by sylk on 9/15/2014.
 */
public class PickOneDialogFragment extends DialogFragment implements OnClickListener {

    static final String TAG = "PickOneDialog";

    private static int mSelectedIndex;
    private static int mResourceArray;
    private static OnDialogPickOneListener mDialogPickOneCallback;

    public interface OnDialogPickOneListener {
        public void onSelectedOption(int dialogId);
    }

    public static PickOneDialogFragment newInstance(int res, int selected) {
        final PickOneDialogFragment dialog = new PickOneDialogFragment();
        mResourceArray = res;
        mSelectedIndex = selected;

        return dialog;
    }

    public void setDialogPickOneListener(OnDialogPickOneListener listener) {
        mDialogPickOneCallback = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
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
                break;
        }
    }

}
