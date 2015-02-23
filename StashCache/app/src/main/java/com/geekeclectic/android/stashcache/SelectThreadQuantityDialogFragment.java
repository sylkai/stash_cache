package com.geekeclectic.android.stashcache;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by sylk on 2/23/2015.
 */
public class SelectThreadQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mThreads;
    private static StashPattern mPattern;
    private static HashMap<UUID, Integer> mQuantities;
    private QuantityAdapter mAdapter;

    public static SelectThreadQuantityDialogFragment newInstance(ArrayList<UUID> threads, StashPattern pattern) {
        final SelectThreadQuantityDialogFragment dialog = new SelectThreadQuantityDialogFragment();

        mThreads = threads;
        mPattern = pattern;

        mQuantities = mPattern.getQuantities();

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();
                break;
            default:  // user is selecting an option
                break;
        }
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {
        public QuantityAdapter(ArrayList<UUID> threads) {
            super(getActivity(), 0, threads);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_select_quantity, null);
            }

            // configure view for this thread - keep in mind view may be recycled and all fields must
            // be initialized again

            StashThread thread = StashData.get(getActivity()).getThread(getItem(position));

            TextView threadInfoTextView = (TextView)convertView.findViewById(R.id.item_description);
            threadInfoTextView.setText(thread.toString());

            EditText threadQuantityEditText = (EditText)convertView.findViewById(R.id.quantity);
            if (mQuantities.get(thread.getId()) != null) {
                threadQuantityEditText.setText(mQuantities.get(thread.getId()));
            }

            Button decreaseButton = (Button)convertView.findViewById(R.id.decrease_button);
            decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            })

        }
    }

}
