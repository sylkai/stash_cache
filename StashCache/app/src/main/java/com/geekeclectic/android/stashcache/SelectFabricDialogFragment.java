package com.geekeclectic.android.stashcache;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Dialog to select existing fabric to be linked to a pattern.  mSelectedIndex and mFabrics are
 * static at this time in order to allow the dialog to be recreated if necessary.  This may change
 * in a future iteration.
 */
public class SelectFabricDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    static final String TAG = "SelectFabricDialog";

    private static int mSelectedIndex;
    private static ArrayList<UUID> mFabrics;
    private FabricAdapter mAdapter;
    private static SelectFabricDialogListener mSelectFabricDialogCallback;

    // to send selection back to the patternfragment for linking
    public interface SelectFabricDialogListener {
        public void onSelectedFabric(UUID fabricId);
    }

    public static SelectFabricDialogFragment newInstance(ArrayList<UUID> fabrics, int currentFabric) {
        final SelectFabricDialogFragment dialog = new SelectFabricDialogFragment();
        mFabrics = fabrics;
        mSelectedIndex = currentFabric;

        return dialog;
    }

    public void setSelectFabricDialogListener(SelectFabricDialogListener listener) {
        mSelectFabricDialogCallback = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(this.getActivity());

        // create adapter to provide custom listview for displaying fabric information
        mAdapter = new FabricAdapter(mFabrics);

        builder.setSingleChoiceItems(mAdapter, mSelectedIndex, this);
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
                mSelectFabricDialogCallback.onSelectedFabric(mFabrics.get(mSelectedIndex));
                break;
            default:  // user is selecting an option
                mSelectedIndex = which;
                break;
        }
    }

    private class FabricAdapter extends ArrayAdapter<UUID> {
        public FabricAdapter(ArrayList<UUID> fabrics) {
            super(getActivity(), 0, fabrics);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_select_fabric, null);
            }

            // configure view for this fabric - KEEP IN MIND VIEW MAY BE RECYCLED AND ALL FIELDS
            // MUST BE INITIALIZED AGAIN
            StashFabric fabric = StashData.get(getActivity()).getFabric(getItem(position));

            TextView fabricInfoTextView = (TextView) convertView.findViewById(R.id.fabric_select_list_item_infoTextView);
            fabricInfoTextView.setText(fabric.getInfo());

            TextView fabricSizeTextView = (TextView) convertView.findViewById(R.id.fabric_select_list_item_sizeTextView);
            fabricSizeTextView.setText(fabric.getSize());

            TextView fabricPatternTextView = (TextView) convertView.findViewById(R.id.fabric_select_list_item_patternTextView);
            if(fabric.isAssigned()) {
                fabricPatternTextView.setText(fabric.usedFor().getPatternName());
            } else {
                fabricPatternTextView.setText(R.string.fabric_selectPattern);
            }

            return convertView;
        }

    }

}
