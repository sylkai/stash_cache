package com.geekeclectic.android.stashcache;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Fragment for an dialog to create a multi-choice list showing the threads in the stash and
 * allowing the user to select which threads are used in the pattern.  Once the user selects the
 * "OK" button, the interface calls back to the pattern fragment and updates the list of threads
 * associated with the pattern.
 */

public class SelectThreadDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mThreads;
    private static ArrayList<UUID> mSelectedThreads;
    private ArrayList<String> mStrings;
    private boolean[] mChecked;
    private static SelectThreadDialogListener mSelectThreadDialogCallback;

    public interface SelectThreadDialogListener {
        public void onThreadsSelected(ArrayList<UUID> mSelectedThreads);
    }

    public static SelectThreadDialogFragment newInstance(ArrayList<UUID> threads, ArrayList<UUID> selectedThreads) {
        final SelectThreadDialogFragment dialog = new SelectThreadDialogFragment();

        mThreads = threads;
        mSelectedThreads = selectedThreads;

        return dialog;
    }

    private void generateCheckedInfo() {
        mStrings = new ArrayList<String>();
        mChecked = new boolean[mThreads.size()];

        for (UUID threadId : mThreads) {
            StashThread thread = StashData.get(getActivity().getApplicationContext()).getThread(threadId);

            mStrings.add(thread.toString());
            mChecked[mThreads.indexOf(threadId)] = mSelectedThreads.contains(threadId);
        }
    }

    public void setSelectThreadDialogListener(SelectThreadDialogListener listener) {
        mSelectThreadDialogCallback = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(this.getActivity());

        generateCheckedInfo();

        builder.setMultiChoiceItems(mStrings.toArray(new String[mThreads.size()]), mChecked, new DialogInterface.OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    mSelectedThreads.add(mThreads.get(which));
                } else {
                    mSelectedThreads.remove(mThreads.get(which));
                }

                mChecked[which] = isChecked;
            }
        });

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
                mSelectThreadDialogCallback.onThreadsSelected(mSelectedThreads);
                break;
            default:  // user is selecting an option
                break;
        }
    }

}
