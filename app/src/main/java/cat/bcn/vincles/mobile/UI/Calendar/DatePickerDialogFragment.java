package cat.bcn.vincles.mobile.UI.Calendar;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.DatePicker;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    long initialDate = -1;
    DatePicked listener;
    int y,m,d;
    boolean pendingResult = false;

    public void setListener(DatePicked listener) {
        this.listener = listener;
        if (pendingResult) {
            listener.onDatePicked(y,m,d);
            pendingResult = false;
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        if (initialDate != -1) {
            calendar.setTime(new Date(initialDate));
        }
        int yy = calendar.get(Calendar.YEAR);
        int mm = calendar.get(Calendar.MONTH);
        int dd = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, yy, mm, dd);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis()-1000);
        return datePickerDialog;
    }

    public void setInitialDate(long initialDate) {
        this.initialDate = initialDate;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d("datest","populateSetDate y:"+year+" m:"+month+" d:"+day);
        if (listener != null) {
            listener.onDatePicked(year, month, day);
            pendingResult = false;
        } else {
            y = year;
            m = month;
            d = day;
            pendingResult = true;
        }
    }


    public interface DatePicked {
        void onDatePicked(int year, int month, int day);
    }

}
