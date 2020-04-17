package cat.bcn.vincles.mobile.Utils;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cat.bcn.vincles.mobile.R;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    ArrayList<DatePickerFragmentInterface> datePickerFragmentInterfaceListeners = new ArrayList<DatePickerFragmentInterface>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        /*Date maxAge = new Date();
        maxAge.setYear(new Date().getYear() - 14);*/

        DatePickerDialog dialog = new DatePickerDialog(getActivity(),R.style.DatePickerTheme,this, year, month, day);
        //dialog.getDatePicker().setMaxDate(maxAge.getTime() - 86400000);

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String dateString = String.valueOf(day) + "/" + String.valueOf(month+1) + "/" + String.valueOf(year);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        long epochTime = c.getTimeInMillis();
        for (DatePickerFragmentInterface d: datePickerFragmentInterfaceListeners ) {
            d.onDataSet(dateString,epochTime);
        }
    }

    public void addDatePickerFragmentInterface (DatePickerFragmentInterface datePickerFragmentInterface) {
        datePickerFragmentInterfaceListeners.add(datePickerFragmentInterface);
    }

    public interface DatePickerFragmentInterface {
        void onDataSet(String date, long epochTime);
    }
}
