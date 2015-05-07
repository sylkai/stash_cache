package com.geekeclectic.android.stashcache;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;

import java.util.Calendar;

/**
 * DatePickerDialog, implemented based in part on example from the Big Nerd Ranch book and part
 * individual work.  Allows the user to set the day, month, and year, and communicates it back
 * to the user as a calendar item.  Used to set start and end dates for projects (so hour+ timing
 * is not required).
 */
public class DatePickerDialogFragment extends DialogFragment {

    public static final String EXTRA_CALENDAR = "com.geekeclectic.android.stashcache.date_picker_calendar";

    private Calendar mCalendar;
    private static DatePickerDialogListener mDatePickerDialogCallback;

    // send the calendar object back to the calling fragment to allow it to be saved
    public interface DatePickerDialogListener {
        void onDateSet(Calendar calendar);
    }

    // the calendar is created and passed in to the datepicker by the host fragment
    public static DatePickerDialogFragment newInstance(Calendar calendar) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_CALENDAR, calendar);

        DatePickerDialogFragment fragment = new DatePickerDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    // set the listener to communicate the results back to (called by the host after creating the
    // fragment)
    public void setDatePickerDialogListener(DatePickerDialogListener listener) {
        mDatePickerDialogCallback = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCalendar = (Calendar)getArguments().getSerializable(EXTRA_CALENDAR);

        // set the year/month/day based on the calendar
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int day = mCalendar.get(Calendar.DAY_OF_MONTH);

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_date, null);

        DatePicker datePicker = (DatePicker)v.findViewById(R.id.dialog_date_datePicker);
        datePicker.init(year, month, day, new DatePicker.OnDateChangedListener() {
            public void onDateChanged(DatePicker view, int year, int month, int day) {
                // create a new calendar (to avoid modifying the existing one if the user cancels)
                mCalendar = Calendar.getInstance();
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, day);

                // store the new calendar to be returned when the user hits ok
                getArguments().putSerializable(EXTRA_CALENDAR, mCalendar);
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.date_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // return the calendar to the user
                        mDatePickerDialogCallback.onDateSet(mCalendar);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }
}
