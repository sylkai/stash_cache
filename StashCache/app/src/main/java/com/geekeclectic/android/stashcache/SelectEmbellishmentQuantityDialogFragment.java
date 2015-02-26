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
public class SelectEmbellishmentQuantityDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private static ArrayList<UUID> mEmbellishments;
    private static StashPattern mPattern;
    private QuantityAdapter mAdapter;
    private SelectEmbellishmentQuantityDialogListener mSelectEmbellishmentQuantityDialogCallback;

    public interface SelectEmbellishmentQuantityDialogListener {
        public void onEmbellishmentQuantitiesUpdate();
    }

    public static SelectEmbellishmentQuantityDialogFragment newInstance(ArrayList<UUID> embellishments, StashPattern pattern, Context context) {
        final SelectEmbellishmentQuantityDialogFragment dialog = new SelectEmbellishmentQuantityDialogFragment();

        mEmbellishments = embellishments;
        Collections.sort(mEmbellishments, new StashEmbellishmentComparator(context));
        mPattern = pattern;

        return dialog;
    }

    public void setSelectEmbellishmentQuantityDialogCallback(SelectEmbellishmentQuantityDialogListener listener) {
        mSelectEmbellishmentQuantityDialogCallback = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case Dialog.BUTTON_NEGATIVE:
                dialog.cancel();
                break;
            case Dialog.BUTTON_POSITIVE:
                dialog.dismiss();
                mSelectEmbellishmentQuantityDialogCallback.onEmbellishmentQuantitiesUpdate();
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
        builder.setNegativeButton(R.string.cancel, this);

        return builder.create();
    }

    private class QuantityAdapter extends ArrayAdapter<UUID> {

        final StashPattern mPattern;

        public QuantityAdapter(ArrayList<UUID> embellishments, StashPattern pattern) {
            super(getActivity(), 0, embellishments);

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

            vh.decreaseButton.setTag(vh);
            vh.decreaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button decreaseButton = (Button) v;
                    ViewHolder vh = (ViewHolder) decreaseButton.getTag();

                    if (vh.patternRef.getQuantity(vh.embellishmentRef) != 0) {
                        vh.patternRef.decreaseQuantity(vh.embellishmentRef);
                    }

                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.embellishmentRef)));
                }
            });

            vh.increaseButton.setTag(vh);
            vh.increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button increaseButton = (Button)v;
                    ViewHolder vh = (ViewHolder)increaseButton.getTag();

                    vh.patternRef.increaseQuantity(vh.embellishmentRef);
                    vh.quantity.setText(Integer.toString(vh.patternRef.getQuantity(vh.embellishmentRef)));
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
        StashPattern patternRef;
    }

}
