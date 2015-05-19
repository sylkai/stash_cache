package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * This dialog fragment allows the user to set which embellishments (and how many) a pattern calls
 * for.  The list is populated by all embellishments in the master list.  Changes are made
 * immediately and so there is no "cancel" button to communicate this to the user.
 */
public class SelectThreadQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mThreads;
    private static StashPattern mPattern;
    private QuantityAdapter mAdapter;
    private SelectThreadQuantityDialogListener mSelectThreadQuantityDialogCallback;

    // call back to the listener to tell it to refresh the display list
    public interface SelectThreadQuantityDialogListener {
        void onThreadQuantitiesUpdate();
    }

    public static SelectThreadQuantityDialogFragment newInstance(ArrayList<UUID> threads, StashPattern pattern, Context context) {
        final SelectThreadQuantityDialogFragment dialog = new SelectThreadQuantityDialogFragment();

        // make sure the threadlist is sorted for display
        mThreads = threads;
        Collections.sort(mThreads, new StashThreadComparator(context));
        mPattern = pattern;

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // target fragment set when dialog initially created, persists through rotation/sleep, managed
        // by fragmentmanager
        mSelectThreadQuantityDialogCallback = (SelectThreadQuantityDialogListener) getTargetFragment();

        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();

                // let the pattern fragment know to update the displayed thread list
                mSelectThreadQuantityDialogCallback.onThreadQuantitiesUpdate();
                break;
            case Dialog.BUTTON_NEUTRAL:
                // let the pattern fragment know to update the displayed thread list (in case changes
                // were made before selecting add new thread)
                mSelectThreadQuantityDialogCallback.onThreadQuantitiesUpdate();

                // create a new thread
                StashThread thread = new StashThread(getActivity());
                StashData.get(getActivity()).addThread(thread);

                // thread is presumably associated with this pattern
                mPattern.increaseQuantity(thread);

                // start StashThreadFragment with the new thread
                Intent i = new Intent(getActivity(), StashThreadPagerActivity.class);
                i.putExtra(StashThreadFragment.EXTRA_THREAD_ID, thread.getId());
                i.putExtra(StashThreadFragment.EXTRA_TAB_ID, StashConstants.MASTER_TAB);
                startActivityForResult(i, 0);
                break;
            default:  // user is selecting an option
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(this.getActivity());

        // create adapter to provide custom listview for displaying threads
        mAdapter = new QuantityAdapter(mThreads, mPattern);

        builder.setTitle(R.string.thread_selectQuantity);
        builder.setAdapter(mAdapter, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNeutralButton(R.string.add_new, this);

        return builder.create();
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {

        final StashPattern mPattern;

        public QuantityAdapter(ArrayList<UUID> threads, StashPattern pattern) {
            super(getActivity(), StashConstants.NO_RESOURCE, threads);

            mPattern = pattern;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_select_quantity, null);

                ViewHolder vh = new ViewHolder();
                vh.threadInfo = (TextView)convertView.findViewById(R.id.item_description);
                vh.quantity = (TextView)convertView.findViewById(R.id.quantity);
                vh.decreaseButton = (Button)convertView.findViewById(R.id.decrease_button);
                vh.increaseButton = (Button) convertView.findViewById(R.id.increase_button);
                vh.patternRef = mPattern;
                convertView.setTag(vh);
            }

            // configure view for this thread - keep in mind view may be recycled and all fields must
            // be initialized again
            ViewHolder vh = (ViewHolder)convertView.getTag();
            StashThread thread = StashData.get(getActivity()).getThread(getItem(position));
            vh.threadRef = thread;

            vh.threadInfo.setText(thread.toString());
            vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(thread)));

            // decrease the quantity of thread called for by the pattern by one when clicked
            vh.decreaseButton.setTag(vh);
            vh.decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button decreaseButton = (Button) v;
                    ViewHolder vh = (ViewHolder) decreaseButton.getTag();

                    vh.patternRef.decreaseQuantity(vh.threadRef);

                    // update the text display
                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.threadRef)));
                }
            });

            // increase the quantity of thread called for by the pattern by one when clicked
            vh.increaseButton.setTag(vh);
            vh.increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button increaseButton = (Button)v;
                    ViewHolder vh = (ViewHolder)increaseButton.getTag();

                    vh.patternRef.increaseQuantity(vh.threadRef);

                    // update the text display
                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.threadRef)));
                }
            });

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView threadInfo;
        public TextView quantity;
        public Button decreaseButton;
        public Button increaseButton;
        public StashThread threadRef;
        public StashPattern patternRef;
    }

}
