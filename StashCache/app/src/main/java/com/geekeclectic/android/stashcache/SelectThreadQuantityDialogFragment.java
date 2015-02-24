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
 * Created by sylk on 2/23/2015.
 */
public class SelectThreadQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mThreads;
    private static StashPattern mPattern;
    private QuantityAdapter mAdapter;
    private SelectThreadQuantityDialogListener mSelectThreadQuantityDialogCallback;

    public interface SelectThreadQuantityDialogListener {
        public void onThreadQuantitiesUpdate();
    }

    public static SelectThreadQuantityDialogFragment newInstance(ArrayList<UUID> threads, StashPattern pattern, Context context) {
        final SelectThreadQuantityDialogFragment dialog = new SelectThreadQuantityDialogFragment();

        mThreads = threads;
        Collections.sort(mThreads, new StashThreadComparator(context));
        mPattern = pattern;

        return dialog;
    }

    public void setSelectThreadQuantityDialogCallback(SelectThreadQuantityDialogListener listener) {
        mSelectThreadQuantityDialogCallback = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();
                mSelectThreadQuantityDialogCallback.onThreadQuantitiesUpdate();
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

        builder.setAdapter(mAdapter, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNegativeButton(R.string.cancel, this);

        return builder.create();
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {

        final StashPattern mPattern;

        public QuantityAdapter(ArrayList<UUID> threads, StashPattern pattern) {
            super(getActivity(), 0, threads);

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

            vh.decreaseButton.setTag(vh);
            vh.decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button decreaseButton = (Button) v;
                    ViewHolder vh = (ViewHolder) decreaseButton.getTag();

                    if (vh.patternRef.getQuantity(vh.threadRef) != 0) {
                        vh.patternRef.decreaseQuantity(vh.threadRef);
                    }

                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.threadRef)));
                }
            });

            vh.increaseButton.setTag(vh);
            vh.increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button increaseButton = (Button)v;
                    ViewHolder vh = (ViewHolder)increaseButton.getTag();

                    vh.patternRef.increaseQuantity(vh.threadRef);
                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.threadRef)));
                }
            });

            return convertView;
        }
    }

    static class ViewHolder {
        TextView threadInfo;
        TextView quantity;
        Button decreaseButton;
        Button increaseButton;
        StashThread threadRef;
        StashPattern patternRef;
    }

}
