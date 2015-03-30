package com.geekeclectic.android.stashcache;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
 * for.  The list is populated by all embellishments in the master list.
 */
public class StashThreadQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mThreads;
    private QuantityAdapter mAdapter;
    private StashThreadQuantityDialogListener mStashThreadQuantityDialogCallback;

    public interface StashThreadQuantityDialogListener {
        public void onThreadQuantitiesUpdate();
    }

    public static StashThreadQuantityDialogFragment newInstance(ArrayList<UUID> threads, Context context) {
        final StashThreadQuantityDialogFragment dialog = new StashThreadQuantityDialogFragment();

        // make sure the threadlist is sorted for display
        mThreads = threads;
        Collections.sort(mThreads, new StashThreadComparator(context));

        return dialog;
    }

    public void setStashThreadQuantityDialogCallback(StashThreadQuantityDialogListener listener) {
        mStashThreadQuantityDialogCallback = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {

            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();

                // let the list fragment know to update the displayed thread list
                mStashThreadQuantityDialogCallback.onThreadQuantitiesUpdate();
                break;
            default:  // user is selecting an option
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(this.getActivity());

        // create adapter to provide custom listview for displaying threads
        mAdapter = new QuantityAdapter(mThreads);

        builder.setTitle(R.string.thread_stashQuantity);
        builder.setAdapter(mAdapter, this);
        builder.setPositiveButton(R.string.ok, this);

        return builder.create();
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {

        private StashData stash;

        public QuantityAdapter(ArrayList<UUID> threads) {
            super(getActivity(), StashConstants.NO_RESOURCE, threads);
            stash = StashData.get(getActivity());
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
                convertView.setTag(vh);
            }

            // configure view for this thread - keep in mind view may be recycled and all fields must
            // be initialized again
            ViewHolder vh = (ViewHolder)convertView.getTag();
            StashThread thread = stash.getThread(getItem(position));
            vh.threadRef = thread;

            vh.threadInfo.setText(thread.toString());
            vh.quantity.setText(Integer.toString(thread.getSkeinsOwned()));

            vh.decreaseButton.setTag(vh);
            vh.decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button decreaseButton = (Button) v;
                    ViewHolder vh = (ViewHolder) decreaseButton.getTag();

                    vh.threadRef.decreaseOwnedQuantity();

                    // update the text display
                    vh.quantity.setText(Integer.toString(vh.threadRef.getSkeinsOwned()));
                }
            });

            vh.increaseButton.setTag(vh);
            vh.increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button increaseButton = (Button)v;
                    ViewHolder vh = (ViewHolder)increaseButton.getTag();

                    // increase the quantity of the thread for this pattern by 1
                    vh.threadRef.increaseOwnedQuantity();

                    // update the text display
                    vh.quantity.setText(Integer.toString(vh.threadRef.getSkeinsOwned()));
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
    }

}
