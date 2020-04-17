package cat.bcn.vincles.mobile.UI.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerDialogFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    TimePicked listener;
    int hourOfDay, minute;
    boolean pendingResult = false;


    public void setListener(TimePicked listener) {
        this.listener = listener;
        if (pendingResult) {
            listener.onTimePicked(hourOfDay, minute);
            pendingResult = false;
        }
    }

    public void setHourMinute(int hour, int minute) {
        hourOfDay = hour;
        this.minute = minute;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new TimePickerDialog(getActivity(), this, hourOfDay, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (listener != null) {
            listener.onTimePicked(hourOfDay, minute);
            pendingResult = false;
        } else {
            this.hourOfDay = hourOfDay;
            this.minute = minute;
            pendingResult = true;
        }
    }

    public interface TimePicked {
        void onTimePicked(int hourOfDay, int minute);
    }

}
