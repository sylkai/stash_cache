package com.geekeclectic.android.stashcache;

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
 * for.  The list is populated by all embellishments in the master list. Changes made are immediately
 * saved, so there is no "cancel" button to communicate that to the user.
 */
public class SelectEmbellishmentQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mEmbellishments;
    private static StashPattern mPattern;
    private QuantityAdapter mAdapter;
    private SelectEmbellishmentQuantityDialogListener mSelectEmbellishmentQuantityDialogCallback;

    // interface called when the user selects ok, to refresh the list display
    public interface SelectEmbellishmentQuantityDialogListener {
        void onEmbellishmentQuantitiesUpdate();
    }

    public static SelectEmbellishmentQuantityDialogFragment newInstance(ArrayList<UUID> embellishments, StashPattern pattern, Context context) {
        final SelectEmbellishmentQuantityDialogFragment dialog = new SelectEmbellishmentQuantityDialogFragment();

        // sort the embellishment list to be sure that it is displayed properly
        mEmbellishments = embellishments;
        Collections.sort(mEmbellishments, new StashEmbellishmentComparator(context));
        mPattern = pattern;

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        // target fragment set when dialog initially created, persists through rotation/sleep, managed
        // by fragmentmanager
        mSelectEmbellishmentQuantityDialogCallback = (SelectEmbellishmentQuantityDialogListener) getTargetFragment();

        switch (which) {
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();

                // call back to let the pattern know to update the displayed embellishments
                mSelectEmbellishmentQuantityDialogCallback.onEmbellishmentQuantitiesUpdate();
                break;
            case Dialog.BUTTON_NEUTRAL:
                // call back to let the pattern know to update the displayed embellishments (in case
                // changes made before selecting "add new")
                mSelectEmbellishmentQuantityDialogCallback.onEmbellishmentQuantitiesUpdate();

                // create a new embellishment
                StashEmbellishment embellishment = new StashEmbellishment(getActivity());
                StashData.get(getActivity()).addEmbellishment(embellishment);

                // embellishment is presumably associated with this pattern
                mPattern.increaseQuantity(embellishment);

                // start StashEmbellishmentFragment with the new embellishment
                Intent i = new Intent(getActivity(), StashEmbellishmentPagerActivity.class);
                i.putExtra(StashEmbellishmentFragment.EXTRA_EMBELLISHMENT_ID, embellishment.getId());
                i.putExtra(StashEmbellishmentFragment.EXTRA_TAB_ID, StashConstants.MASTER_TAB);
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
        mAdapter = new QuantityAdapter(mEmbellishments, mPattern);

        builder.setTitle(R.string.embellishment_selectQuantity);
        builder.setAdapter(mAdapter, this);
        builder.setPositiveButton(R.string.ok, this);
        builder.setNeutralButton(R.string.add_new, this);

        return builder.create();
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {

        private final StashPattern mPattern;

        public QuantityAdapter(ArrayList<UUID> embellishments, StashPattern pattern) {
            super(getActivity(), StashConstants.NO_RESOURCE, embellishments);

            mPattern = pattern;
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
                vh.patternRef = mPattern;
                convertView.setTag(vh);
            }

            // configure view for this thread - keep in mind view may be recycled and all fields must
            // be initialized again
            ViewHolder vh = (ViewHolder)convertView.getTag();
            StashEmbellishment embellishment = StashData.get(getActivity()).getEmbellishment(getItem(position));
            vh.embellishmentRef = embellishment;

            vh.embellishmentInfo.setText(embellishment.toString());

            vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(embellishment)));

            // decrease the quantity of the embellishment called for by the pattern when clicked
            vh.decreaseButton.setTag(vh);
            vh.decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button decreaseButton = (Button) v;
                    ViewHolder vh = (ViewHolder) decreaseButton.getTag();

                    vh.patternRef.decreaseQuantity(vh.embellishmentRef);

                    // change the displayed quantity
                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.embellishmentRef)));
                }
            });

            // increase the quantity of the embellishment called for by the pattern when clicked
            vh.increaseButton.setTag(vh);
            vh.increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button increaseButton = (Button)v;
                    ViewHolder vh = (ViewHolder)increaseButton.getTag();

                    vh.patternRef.increaseQuantity(vh.embellishmentRef);

                    // change the displayed quantity
                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.embellishmentRef)));
                }
            });

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView embellishmentInfo;
        public TextView quantity;
        public Button decreaseButton;
        public Button increaseButton;
        public StashEmbellishment embellishmentRef;
        public StashPattern patternRef;
    }

}
