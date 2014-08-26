package edu.ncsu.mas.platys.android.ui.fragment;

import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment implements
    TimePickerDialog.OnTimeSetListener {

  OnTimeSetPlatysListener mCallback;

  public interface OnTimeSetPlatysListener {
    public void onTimeSet(int hourOfDay, int minute);
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      mCallback = (OnTimeSetPlatysListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement OnHeadlineSelectedListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);

    return new TimePickerDialog(getActivity(), this, hour, minute,
        DateFormat.is24HourFormat(getActivity()));
  }
  
  @Override
  public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
    mCallback.onTimeSet(hourOfDay, minute);
  }
  
}
