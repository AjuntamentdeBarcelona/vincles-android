package cat.bcn.vincles.mobile.UI.Compound;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.DateUtils;

public class CalendarCompoundView extends LinearLayout implements View.OnClickListener, AdapterView.OnItemClickListener {

    private CalendarCompoundViewAdapter adapter;
    private GridView gridView;
    private ViewGroup rootView;
    private View previousMonth, nextMonth;
    private TextView monthTv;

    private Context context;
    private List<Long> events = new ArrayList<>();
    private List<Date> monthEvents = new ArrayList<>();
    private Calendar currentMonth;
    private long lastMonthTime;

    private int gridAvailableHeight = -1;

    private OnCalendarEventListener onCalendarEventListener;

    public CalendarCompoundView(Context context) {
        super(context);
        initializeViews(context);
    }

    public CalendarCompoundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initializeViews(context);
    }

    public CalendarCompoundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeViews(context);
    }

    private void initializeViews(Context context) {
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = (ViewGroup) inflater.inflate(R.layout.calendar_compound_view_layout, this);
        gridView = rootView.findViewById(R.id.calendar_grid);
        previousMonth = rootView.findViewById(R.id.previous_month);
        nextMonth = rootView.findViewById(R.id.next_month);
        monthTv = rootView.findViewById(R.id.month_tv);
        previousMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);

        gridView.setOnItemClickListener(this);

        currentMonth = Calendar.getInstance();
        setupCurrentMonth();
        setupAdapter();
    }

    private void setupCurrentMonth() {
        currentMonth.set(Calendar.DAY_OF_MONTH, 1);
        currentMonth.set(Calendar.HOUR, 0);
        currentMonth.set(Calendar.MINUTE, 0);
        currentMonth.set(Calendar.SECOND, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentMonth.getTimeInMillis()));
        calendar.set(Calendar.DAY_OF_MONTH, 28);
        int month = calendar.get(Calendar.MONTH);
        while (calendar.get(Calendar.MONTH) == month) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        lastMonthTime = calendar.getTimeInMillis();

        monthTv.setText(DateUtils.getCalendarMonthYear(lastMonthTime, getResources()));
    }


    private List<Date> getMonthDaysList() {
        List<Date> monthDaysList = new ArrayList<Date>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(currentMonth.getTimeInMillis()));
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        int weeksInMonth = getNumberOfWeeksInMonth(calendar.getTimeInMillis());
        int firstDayOfTheMonth = (calendar.get(Calendar.DAY_OF_WEEK)+5)%7;
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while(monthDaysList.size() < 7*weeksInMonth){
            monthDaysList.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        /*int firstDayOfTheMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.add(Calendar.DAY_OF_MONTH, -firstDayOfTheMonth);
        while(monthDaysList.size() < MAX_CALENDAR_POSITIONS){
            monthDaysList.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }*/

        return monthDaysList;
    }

    private void setupAdapter() {

        List<Date> monthDaysList = getMonthDaysList();
        adapter = new CalendarCompoundViewAdapter(context, monthDaysList, currentMonth,
                monthEvents);
        if (gridAvailableHeight != -1) {
            adapter.setCellHeight(gridAvailableHeight / (monthDaysList.size()/7));
        }
        gridView.setAdapter(adapter);
    }

    private int getNumberOfWeeksInMonth(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(millis));

        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int weekOfFirstDay = calendar.get(Calendar.WEEK_OF_MONTH);

        if (calendar.get(Calendar.MONTH) == 1) {
            calendar.set(Calendar.DAY_OF_MONTH, 28);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            if (calendar.get(Calendar.MONTH) == 2) calendar.add(Calendar.DAY_OF_MONTH, -1);
            return calendar.get(Calendar.WEEK_OF_MONTH) - weekOfFirstDay + 1;
        }
        calendar.set(Calendar.DAY_OF_MONTH, 30);
        int currentMonth = calendar.get(Calendar.MONTH);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        if (calendar.get(Calendar.MONTH) != currentMonth) calendar.add(Calendar.DAY_OF_MONTH, -1);

        return calendar.get(Calendar.WEEK_OF_MONTH) - weekOfFirstDay + 1;
    }

    public void setEvents(List<Long> events) {
        this.events.clear();
        this.events.addAll(events);
        setMonthEvents();
    }

    private void setMonthEvents() {
        monthEvents.clear();
        Long firstTimeOfMonth = currentMonth.getTimeInMillis();
        for (Long event : events) {
            if (event > firstTimeOfMonth && event < lastMonthTime) {
                monthEvents.add(new Date(event));
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_month:
            case R.id.next_month:
                currentMonth.add(Calendar.MONTH, (v.getId() == R.id.previous_month) ? -1 : 1);
                setupCurrentMonth();
                List<Date> monthDaysList = getMonthDaysList();
                if (gridAvailableHeight != -1) {
                    adapter.setCellHeight(gridAvailableHeight / (monthDaysList.size()/7));
                }
                adapter.setMonthlyDates(monthDaysList);
                setMonthEvents();
                if (onCalendarEventListener!=null) onCalendarEventListener.onMonthChanged(currentMonth);
                break;
        }
    }

    public void setCurrentMonth(Calendar currentMonth) {
        this.currentMonth.setTime(currentMonth.getTime());
        setupCurrentMonth();
        adapter.setMonthlyDates(getMonthDaysList());
        setMonthEvents();
        if (onCalendarEventListener!=null) onCalendarEventListener.onMonthChanged(currentMonth);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (onCalendarEventListener != null)
            onCalendarEventListener.onDateClicked(adapter.getItem(position));
    }

    public void setOnCalendarEventListener(OnCalendarEventListener onCalendarEventListener) {
        this.onCalendarEventListener = onCalendarEventListener;
    }

    public void setGridAvailableHeight(int gridAvailableHeight) {
        this.gridAvailableHeight = gridAvailableHeight;
        adapter.setCellHeight(gridAvailableHeight / (getMonthDaysList().size()/7));
    }

    public interface OnCalendarEventListener {
        void onDateClicked(Date date);
        void onMonthChanged(Calendar currentMonth);
    }
}
