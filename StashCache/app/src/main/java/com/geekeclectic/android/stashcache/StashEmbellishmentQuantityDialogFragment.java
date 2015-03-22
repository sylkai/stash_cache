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
public class StashEmbellishmentQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mEmbellishments;
    private QuantityAdapter mAdapter;
    private StashEmbellishmentQuantityDialogListener mStashEmbellishmentQuantityDialogCallback;

    public interface StashEmbellishmentQuantityDialogListener {
        public void onEmbellishmentQuantitiesUpdate();
    }

    public static StashEmbellishmentQuantityDialogFragment newInstance(ArrayList<UUID> embellishments, Context context) {
        final StashEmbellishmentQuantityDialogFragment dialog = new StashEmbellishmentQuantityDialogFragment();

        // make sure the threadlist is sorted for display
        mEmbellishments = embellishments;
        Collections.sort(mEmbellishments, new StashEmbellishmentComparator(context));

        return dialog;
    }

    public void setStashEmbellishmentQuantityDialogCallback(StashEmbellishmentQuantityDialogListener listener) {
        mStashEmbellishmentQuantityDialogCallback = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            /*case Dialog.BUTTON_NEGATIVE:
                dialog.cancel();

                break;*/
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();

                // let the list fragment know to update the displayed thread list
                mStashEmbellishmentQuantityDialogCallback.onEmbellishmentQuantitiesUpdate();
                break;
            default:  // user is selecting an option
                break;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Builder builder = new Builder(this.getActivity());

        // create adapter to provide custom listview for displaying threads
        mAdapter = new QuantityAdapter(mEmbellishments);

        builder.setTitle(R.string.embellishment_stashQuantity);
        builder.setAdapter(mAdapter, this);
        builder.setPositiveButton(R.string.ok, this);
        // builder.setNegativeButton(R.string.cancel, this);

        return builder.create();
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {

        StashData stash;

        public QuantityAdapter(ArrayList<UUID> threads) {
            super(getActivity().getApplicationContext(), 0, threads);
            stash = StashData.get(getActivity().getApplicationContext());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // if we weren't given a view, inflate one
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_select_quantity, null);

                ViewHolder vh = new ViewHolder();
                vh.embellishmentInfo = (TextView)convertView.findViewById(R.id.item_description);
                vh.quantity = (TextView)convertView.findViewById(R.id.quantity);
                vh.decreaseButton = (Button)convertView.findViewById(R.id.decrease_button);
                vh.increaseButton = (Button) convertView.findViewById(R.id.increase_button);
                convertView.setTag(vh);
            }

            // configure view for this thread - keep in mind view may be recycled and all fields must
            // be initialized again
            ViewHolder vh = (ViewHolder)convertView.getTag();
            StashEmbellishment embellishment = stash.getEmbellishment(getItem(position));
            vh.embellishmentRef = embellishment;

            vh.embellishmentInfo.setText(embellishment.toString());
            vh.quantity.setText(Integer.toString(embellishment.getNumberOwned()));

            vh.decreaseButton.setTag(vh);
            vh.decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button decreaseButton = (Button) v;
                    ViewHolder vh = (ViewHolder) decreaseButton.getTag();

                    // if the quantity of this thread is > 0, decrease it by one
                    if (vh.embellishmentRef.getNumberOwned() > 0) {
                        vh.embellishmentRef.decreaseOwned();
                    }

                    if (vh.embellishmentRef.getNumberOwned() == 0) {
                        stash.removeEmbellishmentFromStash(vh.embellishmentRef.getId());
                    }

                    if (vh.embellishmentRef.needToBuy()) {
                        stash.addEmbellishmentToShoppingList(vh.embellishmentRef.getId());
                    }

                    // update the text display
                    vh.quantity.setText(Integer.toString(vh.embellishmentRef.getNumberOwned()));
                }
            });

            vh.increaseButton.setTag(vh);
            vh.increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button increaseButton = (Button)v;
                    ViewHolder vh = (ViewHolder)increaseButton.getTag();

                    if (vh.embellishmentRef.getNumberOwned() == 0) {
                        stash.addEmbellishmentToStash(vh.embellishmentRef.getId());
                    }

                    // increase the quantity of the thread for this pattern by 1
                    vh.embellishmentRef.increaseOwned();

                    if (!vh.embellishmentRef.needToBuy()) {
                        stash.removeEmbellishmentFromShoppingList(vh.embellishmentRef.getId());
                    }

                    // update the text display
                    vh.quantity.setText(Integer.toString(vh.embellishmentRef.getNumberOwned()));
                }
            });

            return convertView;
        }
    }

    static class ViewHolder {
        TextView embellishmentInfo;
        TextView quantity;
        Button decreaseButton;
        Button increaseButton;
        StashEmbellishment embellishmentRef;
    }

}