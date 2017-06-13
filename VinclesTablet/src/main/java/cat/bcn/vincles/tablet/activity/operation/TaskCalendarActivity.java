/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.adapter.TaskAdapter;

public class TaskCalendarActivity extends TaskActivity {
    private static final String TAG = "TaskCalendarActivity";
    private static final int GOSHT_TAB_INDEX = 3;

    private List<Task> items;
    private CaldroidFragment caldroidFragment;
    private ListView lisTask;
    private View calContainer, liPagination, tabContent;
    private TaskAdapter adapter;
    private TabHost tabs;
    private TextView txTaskResul, txTaskTitle;
    private Calendar calendar;
    private int listHeight = 0;
    private int month, year;

    public static final int MAX_ITEM = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_calendar);
        calContainer = findViewById(R.id.calContainer);
        liPagination = findViewById(R.id.lisPagination);
        tabContent = findViewById(android.R.id.tabcontent);
        tabs = (TabHost) findViewById(android.R.id.tabhost);
        txTaskResul = (TextView) findViewById(R.id.txTaskResul);
        txTaskTitle = (TextView) findViewById(R.id.txTaskTitle);
        txTaskTitle.setText(VinclesConstants.getDateString(taskModel.selectedDate, getString(R.string.dateLargeformat), mainModel.locale));

        calendar = Calendar.getInstance();
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        final TabHost tabs = (TabHost) findViewById(android.R.id.tabhost);
        tabs.setup();

        View tab1View = getLayoutInflater().inflate(R.layout.component_agenda_tab, null);
        ((TextView)tab1View.findViewById(R.id.tabText)).setText(getString(R.string.today));
        TabHost.TabSpec spec = tabs.newTabSpec("tab1");
        spec.setContent(R.id.tab1);
        spec.setIndicator(tab1View);
        tabs.addTab(spec);

        View tab2View = getLayoutInflater().inflate(R.layout.component_agenda_tab, null);
        ((TextView)tab2View.findViewById(R.id.tabText)).setText(getString(R.string.tomorrow));
        spec = tabs.newTabSpec("tab2");
        spec.setContent(R.id.tab2);
        spec.setIndicator(tab2View);
        tabs.addTab(spec);

        View tab3View = getLayoutInflater().inflate(R.layout.component_agenda_tab, null);
        ((TextView)tab3View.findViewById(R.id.tabText)).setText(getString(R.string.month));
        spec = tabs.newTabSpec("tab3");
        spec.setContent(R.id.tab3);
        spec.setIndicator(tab3View);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("ghostTab");
        spec.setContent(R.id.ghostTab);
        spec.setIndicator("");
        tabs.addTab(spec);
        tabs.getTabWidget().getChildTabViewAt(3).setVisibility(View.GONE);


        items = taskModel.getTodayTaskList();
        initializeCalendar();
        tabs.setCurrentTab(0);

        lisTask = (ListView) findViewById(R.id.lisTask);

        showTextResult(items.size() == 0);
        getTodayTaskServerList();

        tabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (adapter != null) adapter.reset();
                int selectedTab = tabs.getCurrentTab();
                calContainer.setVisibility(View.GONE);
                lisTask.setVisibility(View.VISIBLE);
                txTaskTitle.setVisibility(View.VISIBLE);
                liPagination.setVisibility(View.VISIBLE);
                if (selectedTab == 0) {
                    // Refresh adapter
                    items = taskModel.getTodayTaskList();
                    getTodayTaskServerList();

                    txTaskTitle.setText(VinclesConstants.getDateString(taskModel.selectedDate, getString(R.string.dateLargeformat), mainModel.locale));
                    showTextResult(items.size() == 0);
                }
                if (selectedTab == 1) {
                    // Refresh adapter
                    items = taskModel.getTomorrowTaskList();

                    taskModel.getTomorrowTaskServerList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            Log.i(TAG, "getTomorrowTaskServerList() - result");

                            List<Task> items = taskModel.getTomorrowTaskList();
                            adapter.clear();
                            adapter.addAll(items);
                            adapter.notifyDataSetChanged();
                            showTextResult(items.size() == 0);
                            refreshButtons();
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.e(TAG, "getTomorrowTaskServerList() - error: " + error);

                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                            toast.show();
                            adapter.notifyDataSetChanged();
                        }
                    });
                    refreshButtons();

                    Calendar calDate = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
                    calDate.add(Calendar.DATE, 1);

                    txTaskTitle.setText(VinclesConstants.getDateString(calDate.getTime(), getString(R.string.dateLargeformat), mainModel.locale));
                    showTextResult(items.size() == 0);
                }
                if (selectedTab == 2) {
                    caldroidFragment.refreshView();
                    lisTask.setVisibility(View.GONE);
                    liPagination.setVisibility(View.GONE);
                    txTaskTitle.setVisibility(View.GONE);
                    fillMonthCalendar(month, year);
                    calContainer.setVisibility(View.VISIBLE);
                    showTextResult(false);
                }

                adapter.clear();
                adapter.addAll(items);
                adapter.notifyDataSetChanged();
            }
        });


        tabContent.post(new Runnable() {
            @Override
            public void run() {
                // SET SIZES:
                ViewGroup.LayoutParams lp = calContainer.getLayoutParams();
                lp.width = tabContent.getMeasuredHeight();
                calContainer.setLayoutParams(lp);
                calContainer.invalidate();
                calContainer.requestLayout();
            }
        });

        updateViews();
        if (getIntent().hasExtra("date")) {
            Date d = new Date();
            d.setTime(getIntent().getLongExtra("date", -1));
            goToDate(d);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        lisTask.post(new Runnable() {
            @Override
            public void run() {
                listHeight = lisTask.getMeasuredHeight() / MAX_ITEM;
                updateViews();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        final CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                Log.i(TAG, "onSelectDate() - date: " + date);
                goToDate(date);
            }

            @Override
            public void onChangeMonth(int month, int year) {
                Log.i(TAG, "onChangeMonth()");
                TaskCalendarActivity.this.month = month -1;
                TaskCalendarActivity.this.year = year;
                fillMonthCalendar(month, year);
            }

            @Override
            public void onLongClickDate(Date date, View view) {
                Log.i(TAG, "onLongClickDate()");
            }

            @Override
            public void onCaldroidViewCreated() {
                Log.i(TAG, "onCaldroidViewCreated()");
            }

        };
        caldroidFragment.setCaldroidListener(listener);
    }

    private void updateViews() {
        adapter = new TaskAdapter(this, 0, items, listHeight, MAX_ITEM);
        lisTask.setAdapter(adapter);
        refreshButtons();
    }

    private void fillMonthCalendar(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        int monthDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Clear backgrounds to update view
        for (int i = 1; i <= monthDays; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            Date dt = cal.getTime();
            caldroidFragment.clearBackgroundDrawableForDate(dt);
        }

        items = taskModel.getTasksBetweenMonths(month, year, month+2, (month == 11 ? year+1 : year));
        refreshCalendar(items);

        taskModel.getMonthTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getMonthTaskServerList() - result");
                refreshCalendar((List<Task>) result);
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getMonthTaskServerList() - error: " + error);

                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                toast.show();
                adapter.notifyDataSetChanged();
            }
        }, month, year);
    }

    private void initializeCalendar() {
        Locale locale = new Locale(getResources().getString(R.string.locale_language));
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getResources().updateConfiguration(config,
                getResources().getDisplayMetrics());

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, true);
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        args.putBoolean(CaldroidFragment.SHOW_NAVIGATION_ARROWS, false);
        args.putBoolean(CaldroidFragment.ENABLE_SWIPE, false);
        args.putInt(CaldroidFragment.THEME_RESOURCE, R.style.CaldroidVincles);
        caldroidFragment.setArguments(args);

        caldroidFragment.setMinDate(cal.getTime());
        caldroidFragment.setTextColorForDate(R.color.white, cal.getTime());


        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calContainer, caldroidFragment);
        t.commit();

        calendar = Calendar.getInstance();
        refreshCalendar(items);
    }

    private void goToDate(final Date date) {
        // Refresh adapter
        items = taskModel.getSelectedTaskList(date);
        adapter.addAll(items);
        adapter.notifyDataSetChanged();
        lisTask.setVisibility(View.VISIBLE);
        calContainer.setVisibility(View.GONE);
        // Deselect all tabs
        tabs.setCurrentTab(GOSHT_TAB_INDEX);

        taskModel.getSelectedTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getTaskServerList() - result");

                List<Task> items = taskModel.getSelectedTaskList(date);
                adapter.clear();
                adapter.addAll(items);
                adapter.notifyDataSetChanged();
                showTextResult(items.size() == 0);
                refreshButtons();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getTaskServerList() - error: " + error);

                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                toast.show();
                adapter.notifyDataSetChanged();
            }
        }, date);

        txTaskTitle.setText(VinclesConstants.getDateString(date, getString(R.string.dateLargeformat), mainModel.locale));
    }

    private void refreshCalendar(List<Task> items) {
        for (Task it : items) {
            caldroidFragment.setSelectedDate(it.getDate());
            caldroidFragment.setTextColorForDate(R.color.white, it.getDate());
        }
        caldroidFragment.refreshView();
    }

    private void showTextResult(boolean size) {
        if (size == true) {
            txTaskResul.setVisibility(View.VISIBLE);
        } else {
            txTaskResul.setVisibility(View.GONE);
        }
    }

    public void previousTask(View view) {
        adapter.showLess();
        refreshButtons();
    }

    public void nextTask(View view) {
        adapter.showMore();
        refreshButtons();
    }

    public void refreshButtons() {
        if (adapter == null) {
            findViewById(R.id.ll_next).setEnabled(false);
            findViewById(R.id.ll_prev).setEnabled(false);
            return;
        }

        if (adapter.isMore())
            findViewById(R.id.ll_next).setEnabled(true);
        else
            findViewById(R.id.ll_next).setEnabled(false);

        if (adapter.isLess())
            findViewById(R.id.ll_prev).setEnabled(true);
        else
            findViewById(R.id.ll_prev).setEnabled(false);
    }

    private void getTodayTaskServerList() {
        taskModel.getTodayTaskServerList(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "getTodayTaskServerList() - result");

                // Now, load from local user list updated!!!
                List<Task> items = taskModel.getTodayTaskList();
                if (adapter != null) {
                    adapter.clear();
                    adapter.addAll(items);
                    adapter.notifyDataSetChanged();
                }
                showTextResult(items.size() == 0);
                refreshButtons();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getTodayTaskServerList() - error: " + error);
                Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                toast.show();

                if (adapter != null) adapter.notifyDataSetChanged();
            }
        });
        refreshButtons();
    }

    public void prevMonth(View v) {
        caldroidFragment.prevMonth();
    }

    public void nextMonth(View v) {
        caldroidFragment.nextMonth();
    }

    @Override
    protected void onPause() {
        // CAUTION: Restore fontSize applied!!!
        mainModel.updateFontSize(mainModel.fontSize);
        super.onPause();
    }
}