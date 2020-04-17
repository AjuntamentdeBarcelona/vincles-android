package cat.bcn.vincles.mobile.UI.Calendar;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Business.AlertsManager;
import cat.bcn.vincles.mobile.Client.Errors.ErrorHandler;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Alert.AlertNonDismissable;
import cat.bcn.vincles.mobile.UI.Chats.ChatFragment;
import cat.bcn.vincles.mobile.UI.Common.BaseFragment;
import cat.bcn.vincles.mobile.UI.Compound.ActionCompoundView;
import cat.bcn.vincles.mobile.UI.Compound.CalendarCompoundView;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmResults;

public class CalendarFragment extends BaseFragment implements CalendarFragmentView, View.OnClickListener, CalendarAdapter.OnItemClicked, AlertMessage.AlertMessageInterface, AlertMessage.CancelMessageInterface, AlertConfirmOrCancel.AlertConfirmOrCancelInterface {

    private static final String PRESENTER_FRAGMENT_TAG = "presenter_fragment_tag";


    View rootView;
    OnFragmentInteractionListener interactionListener;
    CalendarPresenterContract presenter;

    TextView title, dayTv, dateTv;
    TextView noMeetingsTv;
    ActionCompoundView actionButton;
    View backButton, todayButton, tomorrowButton, monthButton;

    CalendarCompoundView calendarCompoundView;

    RecyclerView recyclerView;
    CalendarAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<MeetingRealm> currentMeetings;
    RealmResults<MeetingRealm> allMeetings;

    AlertNonDismissable alertNonDismissable;
    Realm realm;

    long startDay = -1;

    public CalendarFragment(){}

    public static CalendarFragment newInstance(FragmentResumed listener) {
        CalendarFragment fragment = new CalendarFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CALENDAR);
        Bundle arguments = new Bundle();
        arguments.putBoolean("hasStartDay", false);
        fragment.setArguments(arguments);
        return fragment;
    }

    public static CalendarFragment newInstance(FragmentResumed listener, long startDay) {
        CalendarFragment fragment = new CalendarFragment();
        fragment.setListener(listener, FragmentResumed.FRAGMENT_CALENDAR);
        Bundle arguments = new Bundle();
        arguments.putBoolean("hasStartDay", true);
        arguments.putLong("startDay", startDay);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();

        OtherUtils.sendAnalyticsView(getActivity(),
                getResources().getString(R.string.tracking_calendar));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.realm = Realm.getDefaultInstance();
        alertNonDismissable = new AlertNonDismissable(getResources().getString(R.string.login_sending_data), true);

        if (getArguments() != null && getArguments().getBoolean("hasStartDay")) {
            startDay = getArguments().getLong("startDay");
        }

        CalendarPresenter presenterFragment = (CalendarPresenter) getFragmentManager().findFragmentByTag(PRESENTER_FRAGMENT_TAG);
        if (presenterFragment != null && savedInstanceState == null) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.remove(presenterFragment).commit();
            presenterFragment = null;
        }
        if (presenterFragment == null) {
            presenterFragment = CalendarPresenter.newInstance((BaseRequest.RenewTokenFailed)
                    getActivity(), this, realm, savedInstanceState);
            presenterFragment.setStartDay(startDay);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(presenterFragment, PRESENTER_FRAGMENT_TAG).commit();
        } else {
            presenterFragment.setExternalVars((BaseRequest.RenewTokenFailed)
                    getActivity(), this, savedInstanceState,realm);
        }
        presenter = presenterFragment;


    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        presenter.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_calendar, container, false);

        backButton = rootView.findViewById(R.id.back);
        todayButton = rootView.findViewById(R.id.see_today);
        tomorrowButton = rootView.findViewById(R.id.see_tomorrow);
        monthButton = rootView.findViewById(R.id.see_month);
        actionButton = rootView.findViewById(R.id.action);
        dateTv = rootView.findViewById(R.id.date_tv);
        dayTv = rootView.findViewById(R.id.day_tv);
        title = rootView.findViewById(R.id.calendar_title);
        noMeetingsTv = rootView.findViewById(R.id.no_meetings_tv);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        int rootViewLocation[] = new int[2];
        rootView.getLocationOnScreen(rootViewLocation);
        Log.d("cellHeight","location1:"+rootViewLocation[0]+", loc2:"+rootViewLocation[1]);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int calendarAvailableHeight = (int) (dm.heightPixels - getStatusbarHeight() -
                            ( getResources().getDimension(R.dimen.gallery_back_button_size)
                            + getResources().getDimension(R.dimen.calendar_month_toolbar_extra_margin)
                            + getResources().getDimension(R.dimen.gallery_bottom_buttons_height)
                            + getResources().getDimension(R.dimen.calendar_month_month_bar_height)
                            + getResources().getDimension(R.dimen.calendar_month_days_bar_height)
                            + 2*getResources().getDimension(R.dimen.calendar_month_margin_top)
                            + 2*getResources().getDimension(R.dimen.gallery_padding_top_bottom)
                            + getResources().getDimension(R.dimen.general_toolbar_logo_size)));

        actionButton.setText(getString(R.string.calendar_create_new_date));
        actionButton.setImageDrawable(getResources().getDrawable(R.drawable.selector_new_date_background));

        backButton.setOnClickListener(this);
        todayButton.setOnClickListener(this);
        tomorrowButton.setOnClickListener(this);
        monthButton.setOnClickListener(this);
        actionButton.setOnClickListener(this);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.meeting_list_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        currentMeetings = presenter.getCurrentMeetings();
        allMeetings = presenter.getAllMeetings();
        //users = presenter.getUsersInMeetings();
        adapter = new CalendarAdapter(getContext(), currentMeetings, this);
        recyclerView.setAdapter(adapter);
        if (currentMeetings.size() == 0) noMeetingsTv.setVisibility(View.VISIBLE);
        else noMeetingsTv.setVisibility(View.GONE);

        calendarCompoundView = rootView.findViewById(R.id.calendar_view);
        calendarCompoundView.setOnCalendarEventListener(presenter);
        if (allMeetings.size() > 0) calendarCompoundView.setEvents(getCalendarEvents());
        if (dm.heightPixels < dm.widthPixels)
            calendarCompoundView.setGridAvailableHeight(calendarAvailableHeight);

        presenter.onCreateView();

        return rootView;
    }


    private int getStatusbarHeight() {
        /*Rect rectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;*/
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private List<Long> getCalendarEvents() {
        ArrayList<Long> events = new ArrayList<>();
        for (MeetingRealm meetingRealm : allMeetings) {
            events.add(meetingRealm.getDate());
        }
        return events;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ChatFragment.OnFragmentInteractionListener) {
            interactionListener = (CalendarFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                getFragmentManager().popBackStack();
                break;
            case R.id.see_today:
                presenter.seeToday();
                break;
            case R.id.see_tomorrow:
                presenter.seeTomorrow();
                break;
            case R.id.see_month:
                presenter.seeMonth();
                break;
            case R.id.action:
                interactionListener.onCreateDate(presenter.getShownDay());
                break;
        }
    }

    @Override
    public void setButtonSelected(int which) {
        switch (which) {
            case BUTTON_NONE:
                todayButton.setSelected(false);
                tomorrowButton.setSelected(false);
                monthButton.setSelected(false);
                break;
            case BUTTON_TODAY:
                todayButton.setSelected(true);
                tomorrowButton.setSelected(false);
                monthButton.setSelected(false);
                break;
            case BUTTON_TOMORROW:
                todayButton.setSelected(false);
                tomorrowButton.setSelected(true);
                monthButton.setSelected(false);
                break;
            case BUTTON_MONTH:
                todayButton.setSelected(false);
                tomorrowButton.setSelected(false);
                monthButton.setSelected(true);
                break;
        }
    }

    @Override
    public void setDate(long millis, int whatDay) {
        Locale current = getResources().getConfiguration().locale;
        switch (whatDay) {
            case TODAY:
                dayTv.setText(getResources().getString(R.string.calendar_today_date,
                        DateUtils.getCalendarDay(millis, current)));
                break;
            case TOMORROW:
                dayTv.setText(getResources().getString(R.string.calendar_tomorrow_date,
                        DateUtils.getCalendarDay(millis, current)));
                break;
            case OTHER_DAY:
                String date = DateUtils.getCalendarDay(millis, current);
                String text = date.substring(0,1).toUpperCase() + date.substring(1);
                dayTv.setText(text);
                break;
        }
        dateTv.setText(DateUtils.getCalendarDate(millis, current));
    }

    @Override
    public void setDayView() {
        calendarCompoundView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        dayTv.setVisibility(View.VISIBLE);
        dateTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void setMonthView(Calendar month) {
        calendarCompoundView.setCurrentMonth(month);
        noMeetingsTv.setVisibility(View.GONE);
        calendarCompoundView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        dayTv.setVisibility(View.GONE);
        dateTv.setVisibility(View.GONE);
    }

    @Override
    public void editDate(int id) {
        if (interactionListener != null) interactionListener.onEditDate(id);
    }

    @Override
    public void showWaitDialog() {
        if (getActivity()!=null && AlertsManager.isNetworkAvailable(getActivity()))
        alertNonDismissable.showMessage(getActivity());
    }

    @Override
    public void hideWaitDialog() {
        if (alertNonDismissable != null && getActivity() != null && isAdded()) {
            alertNonDismissable.dismissSafely();
        }
    }

    @Override
    public void showError(Object error) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_ERROR);
        alertMessage.setCancelMessageInterface(this);
        String errorMsg = ErrorHandler.getErrorByCode(getContext(), error);
        alertMessage.showMessage(getActivity(),errorMsg, "");

    }

    @Override
    public void showConfirmationDialog(boolean isCancel) {
        int stringId = isCancel ? R.string.calendar_cancel_confirmation
                : R.string.calendar_reject_confirmation;
        int titleId = isCancel ? R.string.cancel : R.string.calendar_reject_date;
        new AlertConfirmOrCancel(getActivity(),this)
                .showMessage(getString(stringId), getString(titleId),
                        AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.dismissSafely();
        presenter.onConfirmationDialogAccepted();
    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.dismissSafely();
        presenter.onConfirmationDialogCanceled();
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        alertMessage.dismissSafely();
        presenter.stopedShowingErrorDialog();
    }

    @Override
    public void onCancelAlertMessage() {
        presenter.stopedShowingErrorDialog();
    }

    @Override
    public void onListsUpdated() {
        if (adapter!=null)adapter.notifyDataSetChanged();
        if (currentMeetings!=null && currentMeetings.size() == 0) noMeetingsTv.setVisibility(View.VISIBLE);
        else noMeetingsTv.setVisibility(View.GONE);
        if (allMeetings!=null && allMeetings.size() > 0) calendarCompoundView.setEvents(getCalendarEvents());
    }

    @Override
    public void onItemButtonClicked(int whatButton, int meetingId) {
        presenter.onItemButtonClicked(whatButton, meetingId);
    }

    @Override
    public void onItemClicked(int meetingId) {
        if (interactionListener != null) interactionListener.onViewDateDetail(meetingId);
    }

    /**
     *
     * Methods for showing help guide
     *
     */
    @Override
    protected boolean shouldShowMenu() {
        return true;
    }

    @Override
    protected String getTextForPage(int page) {
        switch (page) {
            case 0:
                return getString(R.string.context_help_calendar_today);
            case 1:
                return getString(R.string.context_help_calendar_tomorrow);
            case 2:
                return getString(R.string.context_help_calendar_month);
            case 3:
                return getString(R.string.context_help_calendar_create);
            default:
                return null;
        }
    }

    @Override
    protected View getViewForPage(int page) {
        switch (page) {
            case 0:
                return rootView.findViewById(R.id.see_today);
            case 1:
                return rootView.findViewById(R.id.see_tomorrow);
            case 2:
                return rootView.findViewById(R.id.see_month);
            case 3:
                return rootView.findViewById(R.id.action);
            default:
                return null;
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (recyclerView!=null)recyclerView.setAdapter(null);
        if (realm!=null)realm.close();
    }

    public interface OnFragmentInteractionListener {
        void onCreateDate(long shownDay);
        void onEditDate(int id);
        void onViewDateDetail(int id);
    }


}
