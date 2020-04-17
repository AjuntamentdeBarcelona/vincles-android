package cat.bcn.vincles.mobile.UI.Compound;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.mobile.R;

public class CalendarCompoundViewAdapter extends ArrayAdapter {

    private LayoutInflater mInflater;
    private List<Date> monthlyDates;
    private Calendar currentDate;
    private List<Date> allEvents;

    private int cellHeight = -1;

    public CalendarCompoundViewAdapter(@NonNull Context context, List<Date> monthlyDates,
                                       Calendar currentDate, List<Date> allEvents) {
        super(context, R.layout.calendar_cell_layout);
        this.monthlyDates = monthlyDates;
        this.currentDate = currentDate;
        this.allEvents = allEvents;
        mInflater = LayoutInflater.from(context);

    }

    public void setMonthlyDates(List<Date> monthlyDates) {
        this.monthlyDates = monthlyDates;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position==0)Log.d("vwhgt","cellHeight:"+cellHeight);
        Date mDate = monthlyDates.get(position);
        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(mDate);
        int dayValue = dateCal.get(Calendar.DAY_OF_MONTH);
        int displayMonth = dateCal.get(Calendar.MONTH);
        int displayYear = dateCal.get(Calendar.YEAR);
        int currentMonth = currentDate.get(Calendar.MONTH);
        int currentYear = currentDate.get(Calendar.YEAR);
        View view = convertView;
        if(view == null){
            view = mInflater.inflate(R.layout.calendar_cell_layout, parent, false);
        }
        TextView cellNumber = view.findViewById(R.id.day_tv);
        if (cellHeight != -1) {
            Log.d("cellHeight","compountView setting height");
            cellNumber.setMinHeight(cellHeight);
            cellNumber.setHeight(cellHeight);
        }

        if(displayMonth == currentMonth && displayYear == currentYear){
            //view.setBackgroundColor(Color.parseColor("#FF5733"));
            cellNumber.setVisibility(View.VISIBLE);
        } else {
            //view.setBackgroundColor(Color.parseColor("#cccccc"));
            cellNumber.setVisibility(View.INVISIBLE);
        }

        Calendar today = Calendar.getInstance();
        boolean isToday = displayMonth == today.get(Calendar.MONTH) && displayYear == today.get(Calendar.YEAR)
                && dayValue == today.get(Calendar.DAY_OF_MONTH);
        if (isToday) {
            cellNumber.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
        } else {
            cellNumber.setTextColor(getContext().getResources().getColor(R.color.darkGrayGeneric));
        }
        if (position % 2 == 0) {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.grayClear2));
        } else {
            view.setBackgroundColor(getContext().getResources().getColor(R.color.colorWhite));
        }
        if (!isToday && (dateCal.get(Calendar.DAY_OF_WEEK) == 1
                || dateCal.get(Calendar.DAY_OF_WEEK) == 7)) {
            Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.akkurat_bold);
            cellNumber.setTypeface(typeface);
            cellNumber.setTextColor(getContext().getResources().getColor(R.color.colorBlack));
        }

        //Add day to calendar
        cellNumber.setText(String.valueOf(dayValue));

        //Add events to the calendar
        View eventIndicator = view.findViewById(R.id.event_bullet);
        Calendar eventCalendar = Calendar.getInstance();
        boolean setVisible = false;
        for(int i = 0; i < allEvents.size(); i++){
            eventCalendar.setTime(allEvents.get(i));
            if(dayValue == eventCalendar.get(Calendar.DAY_OF_MONTH)
                    && displayMonth == eventCalendar.get(Calendar.MONTH)
                    && displayYear == eventCalendar.get(Calendar.YEAR)){
                eventIndicator.setVisibility(View.VISIBLE);
                setVisible = true;
                break;
            }
        }
        if (!setVisible) {
            eventIndicator.setVisibility(View.GONE);
        }
        return view;
    }
    @Override
    public int getCount() {
        return monthlyDates.size();
    }
    @Nullable
    @Override
    public Date getItem(int position) {
        return monthlyDates.get(position);
    }
    @Override
    public int getPosition(Object item) {
        return monthlyDates.indexOf(item);
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }
}
