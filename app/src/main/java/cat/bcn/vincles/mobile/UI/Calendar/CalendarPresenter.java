package cat.bcn.vincles.mobile.UI.Calendar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GuestRest;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMeetingsRequest;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class CalendarPresenter extends Fragment implements CalendarPresenterContract, CalendarRepository.Callback, GetMeetingsRequest.OnResponse, AlertMessage.AlertMessageInterface {

    private static final int STATE_NO_REQUEST = -1;
    private static final int STATE_REQUEST_FAILED = -2;
    private static final int CONFIRMATION_DIALOG_NONE = 0;
    private static final int CONFIRMATION_DIALOG_REJECT = 1;
    private static final int CONFIRMATION_DIALOG_CANCEL = 2;

    long startDay = -1;
    long shownDay = -1;

    BaseRequest.RenewTokenFailed listener;
    CalendarFragmentView view;
    RealmResults<MeetingRealm> allMeetings;
    ArrayList<MeetingRealm> currentMeetings = new ArrayList<>();
    CalendarRepository repository;
    RealmChangeListener meetingsChangeListener;

    int stateRequest = STATE_NO_REQUEST;
    boolean showingWaitDialog;
    Object error;

    int confirmationDialogState = CONFIRMATION_DIALOG_NONE;
    int pendingRequestMeetingId = -1;
    int pendingRequestWhatButton = -1;

    private boolean isMonthView = false;
    private Calendar currentMonth;
    private UserPreferences userPreferences;
    Realm realm;


    public CalendarPresenter() {
        if (realm==null)realm = Realm.getDefaultInstance();
        repository = new CalendarRepository(listener, realm, this);
        meetingsChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(Object o) {
                onMeetingsListUpdated();
            }
        };
        shownDay = System.currentTimeMillis();

        userPreferences = new UserPreferences();
    }

    public static CalendarPresenter newInstance (BaseRequest.RenewTokenFailed listener,
                                                 CalendarFragmentView view,
                                                Realm realm,
                                                Bundle savedInstanceState) {
        CalendarPresenter fragment = new CalendarPresenter();
        fragment.setExternalVars(listener, view, savedInstanceState, realm);

        return fragment;
    }


    public void setExternalVars(BaseRequest.RenewTokenFailed listener, CalendarFragmentView view,
                                Bundle savedInstanceState, Realm realm) {
        this.listener = listener;
        this.view = view;
        this.realm = realm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        view = null;
    }


    @Override
    public void onCreateView() {
        if (view == null)return;
        if (shownDay != -1) { //fragment retained
            if (isMonthView) {
                view.setButtonSelected(CalendarFragmentView.BUTTON_MONTH);
                view.setMonthView(currentMonth);
            } else {
                if (isShownDayToday()) {
                    view.setDate(shownDay, view.TODAY);
                    view.setButtonSelected(CalendarFragmentView.BUTTON_TODAY);
                } else if (isShownDayTomorrow()) {
                    view.setDate(shownDay, view.TOMORROW);
                    view.setButtonSelected(CalendarFragmentView.BUTTON_TOMORROW);
                } else {
                    view.setDate(shownDay, view.OTHER_DAY);
                    view.setButtonSelected(CalendarFragmentView.BUTTON_NONE);
                }
            }
        } else {
            currentMonth = Calendar.getInstance();
            if (view != null) view.setButtonSelected(CalendarFragmentView.BUTTON_TODAY);
            Calendar calendar = Calendar.getInstance();
            if (startDay != -1) calendar.setTime(new Date(startDay));
            shownDay = calendar.getTimeInMillis();
            if (view != null) view.setDate(shownDay, view.TODAY);
        }


        if (view != null) {
            if (stateRequest == STATE_REQUEST_FAILED) {
                view.showError(error);
            } else if (showingWaitDialog) {
                view.showWaitDialog();
            }
        }


        if (confirmationDialogState == CONFIRMATION_DIALOG_CANCEL) {
            view.showConfirmationDialog(true);
        } else if (confirmationDialogState == CONFIRMATION_DIALOG_REJECT) {
            view.showConfirmationDialog(false);
        }

        refreshCurrentMeetings();

    }

    private boolean isShownDayToday() {
        Calendar today = Calendar.getInstance();
        Calendar shownDay = Calendar.getInstance();
        shownDay.setTime(new Date(this.shownDay));
        return today.get(Calendar.DAY_OF_MONTH) == shownDay.get(Calendar.DAY_OF_MONTH)
                && today.get(Calendar.MONTH) == shownDay.get(Calendar.MONTH)
                && today.get(Calendar.YEAR) == shownDay.get(Calendar.YEAR);
    }

    private boolean isShownDayTomorrow() {
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar shownDay = Calendar.getInstance();
        shownDay.setTime(new Date(this.shownDay));
        return tomorrow.get(Calendar.DAY_OF_MONTH) == shownDay.get(Calendar.DAY_OF_MONTH)
                && tomorrow.get(Calendar.MONTH) == shownDay.get(Calendar.MONTH)
                && tomorrow.get(Calendar.YEAR) == shownDay.get(Calendar.YEAR);
    }

    @Override
    public void loadData() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void seeToday() {
        seeDay(System.currentTimeMillis());
        if (view != null) {
            view.setDate(shownDay, CalendarFragmentView.TODAY);
            view.setButtonSelected(CalendarFragmentView.BUTTON_TODAY);
        }
    }

    @Override
    public void seeTomorrow() {
        seeDay(System.currentTimeMillis() + 24*3600*1000);
        if (view != null) {
            view.setDate(shownDay, CalendarFragmentView.TOMORROW);
            view.setButtonSelected(CalendarFragmentView.BUTTON_TOMORROW
            );
        }
    }

    @Override
    public void onDateClicked(Date date) {
        seeDay(date.getTime());
    }

    @Override
    public void onMonthChanged(Calendar currentMonth) {
        this.currentMonth = currentMonth;
        if (isMonthView){

            long getFirstDayOfMonth = getFirstDayOfMonth();
            long oldestMeetingDate = getOldestMeetingDate();
            if (oldestMeetingDate > getFirstDayOfMonth){
                Log.d("getMeetReq", "GET MORE MEETINGS");
                view.showWaitDialog();
                getMoreMeetings(oldestMeetingDate);
            }
            else{
                Log.d("getMeetReq", "DON'T GET MORE MEETINGS");
            }
        }
    }

    private void getMoreMeetings(long oldestMeetingDate) {
        GetMeetingsRequest getMeetingsRequest = new GetMeetingsRequest(null,
                0, oldestMeetingDate);
        getMeetingsRequest.addOnOnResponse(this);
        getMeetingsRequest.doRequest(userPreferences.getAccessToken());
    }

    private long getFirstDayOfMonth() {
        Calendar c = currentMonth;
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }


    public void seeDay(long dayInMillis) {
        isMonthView = false;
        shownDay = dayInMillis;
        refreshCurrentMeetings();
        if (view != null) {
            view.setDate(shownDay, CalendarFragmentView.OTHER_DAY);
            view.setButtonSelected(CalendarFragmentView.BUTTON_NONE);
            view.setDayView();
        }
    }

    @Override
    public void seeMonth() {
        isMonthView = true;
        refreshCurrentMeetings();
        if (view != null) {
            currentMonth = Calendar.getInstance();
            view.setButtonSelected(CalendarFragmentView.BUTTON_MONTH);
            view.setMonthView(currentMonth);
        }
    }

    private long getOldestMeetingDate() {
        MeetingRealm meetingRealm =  allMeetings.where()
                .sort("date", Sort.ASCENDING)
                 .findFirst();
        if (meetingRealm != null) {
            return meetingRealm.getDate();
        }
        return 0;
    }

    @Override
    public long getShownDay() {
        return shownDay;
    }

    @Override
    public void stopedShowingErrorDialog() {
        stateRequest = STATE_NO_REQUEST;
    }

    @Override
    public RealmResults<MeetingRealm> getAllMeetings() {
        if (allMeetings == null) {
            allMeetings = repository.getAllMeetings();
            allMeetings.removeAllChangeListeners();
            allMeetings.addChangeListener(meetingsChangeListener);
        }
        return allMeetings;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (allMeetings !=null){
            allMeetings.removeAllChangeListeners();
        }

    }

    @Override
    public ArrayList<MeetingRealm> getCurrentMeetings() {
        if (allMeetings == null) {
            allMeetings = repository.getAllMeetings();
            allMeetings.removeAllChangeListeners();
            allMeetings.addChangeListener(meetingsChangeListener);
        }
        return currentMeetings;
    }

    private void onMeetingsListUpdated() {
        refreshCurrentMeetings();
    }

    private void refreshCurrentMeetings() {
        currentMeetings.clear();
        //currentMeetings.addAll(allMeetings);
        currentMeetings.addAll(getDayMeetings());
        Log.d("evntscal","presenter currntMeet size:"+currentMeetings.size());
        if (view != null) view.onListsUpdated();
    }

    private RealmResults<MeetingRealm> getDayMeetings() {
        Calendar firstTime = Calendar.getInstance();
        firstTime.setTime(new Date(shownDay));
        firstTime.set(Calendar.HOUR_OF_DAY, 0);
        firstTime.set(Calendar.MINUTE, 0);
        firstTime.set(Calendar.SECOND, 0);
        firstTime.set(Calendar.MILLISECOND, 0);
        Calendar lastTime = Calendar.getInstance();
        lastTime.setTime(new Date(shownDay));
        lastTime.set(Calendar.HOUR_OF_DAY, 23);
        lastTime.set(Calendar.MINUTE, 59);
        lastTime.set(Calendar.SECOND, 59);
        lastTime.set(Calendar.MILLISECOND, 999);

        return allMeetings.where()
                .sort("date", Sort.ASCENDING)
                .between("date", firstTime.getTimeInMillis(), lastTime.getTimeInMillis())
                .findAll();
    }

    @Override
    public void onItemButtonClicked(int whatButton, int meetingId) {
        pendingRequestWhatButton = whatButton;
        switch (whatButton) {
            case CalendarAdapter.OnItemClicked.CANCEL_OWN:
                confirmationDialogState = CONFIRMATION_DIALOG_CANCEL;
                pendingRequestMeetingId = meetingId;
                view.showConfirmationDialog(true);
                break;
            case CalendarAdapter.OnItemClicked.EDIT:
                if (view != null) view.editDate(meetingId);
                break;
            case CalendarAdapter.OnItemClicked.REJECT:
                confirmationDialogState = CONFIRMATION_DIALOG_REJECT;
                pendingRequestMeetingId = meetingId;
                view.showConfirmationDialog(false);
                break;
            case CalendarAdapter.OnItemClicked.ACCEPT:
                showingWaitDialog = true;
                view.showWaitDialog();
                repository.acceptRejectMeeting(meetingId, true);
                stateRequest = whatButton;
                break;
            case CalendarAdapter.OnItemClicked.CANCEL:
                confirmationDialogState = CONFIRMATION_DIALOG_CANCEL;
                pendingRequestMeetingId = meetingId;
                view.showConfirmationDialog(true);
                break;
        }
    }

    @Override
    public void onConfirmationDialogAccepted() {
        stateRequest = pendingRequestWhatButton;
        confirmationDialogState = CONFIRMATION_DIALOG_NONE;
        doPendingCancelOrReject(pendingRequestMeetingId);
    }

    @Override
    public void onConfirmationDialogCanceled() {
        confirmationDialogState = CONFIRMATION_DIALOG_NONE;
    }

    private void doPendingCancelOrReject(int meetingId) {
        showingWaitDialog = true;
        view.showWaitDialog();
        if (stateRequest == CalendarAdapter.OnItemClicked.CANCEL_OWN) {
            repository.deleteMeeting(meetingId);
        } else {
            repository.acceptRejectMeeting(meetingId, false);
        }
    }

    @Override
    public void onFailureRequest(Object error) {
        this.error = error;
        stateRequest = STATE_REQUEST_FAILED;
        showingWaitDialog = false;
        view.hideWaitDialog();
        view.showError(error);
    }

    @Override
    public void onResponseDeleteMeeting(int idMeeting) {
        stateRequest = STATE_NO_REQUEST;
        repository.setMeetingNotShown(idMeeting);

        showingWaitDialog = false;
        view.hideWaitDialog();
    }

    @Override
    public void onResponseAcceptDeclineMeeting(int idMeeting, boolean attendance) {
        stateRequest = STATE_NO_REQUEST;

        if (attendance) {
            repository.onMeetingAccepted(idMeeting);
        } else {
            repository.setMeetingNotShown(idMeeting);
        }

        showingWaitDialog = false;
        view.hideWaitDialog();
    }

    public void setStartDay(long startDay) {
        this.startDay = startDay;
    }

    @Override
    public void onResponseGetMeetingsRequest(ArrayList<MeetingRest> meetingRestList) {
        if (meetingRestList != null) {
            UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
           // List<MeetingRest> meetingRests = meetingRestList.getChatMessageRestList();
            for (MeetingRest meetingRest : meetingRestList) {
                //save users missing in db
                usersDb.saveGetUserIfNotExists(meetingRest.getHost());
                if (meetingRest.getGuests() != null) {
                    for (GuestRest guest : meetingRest.getGuests()) {
                        usersDb.saveGetUserIfNotExists(guest.getUser());
                    }
                }
            }

            MeetingsDb meetingsDb = new MeetingsDb(MyApplication.getAppContext());
            meetingsDb.saveMeetingsRestList(meetingRestList,
                    userPreferences.getIsSyncCalendars() ? userPreferences.getCalendarId() : -1);

            //Check if meetings list is empty
            if (meetingRestList.size() == 0){
                refreshAllMeetings();
                return;
            }

            //Get long of last meeting
           long timeOfLastMeeting = meetingRestList.get(meetingRestList.size()-1).getDate();

            //Check if is the last one or get more
            if (getFirstDayOfMonth() < timeOfLastMeeting){
                getMoreMeetings(timeOfLastMeeting);
            }
            else{
                refreshAllMeetings();
            }


        }
    }

    @Override
    public void onFailureGetMeetingsRequest(Object error) {
        Log.d("logdown","onFailureGetMeetingsRequest ERROR: "+error);
        view.showError(error);
    }

    private void refreshAllMeetings() {
        view.hideWaitDialog();
        currentMeetings.clear();
        //currentMeetings.addAll(allMeetings);
        currentMeetings.addAll(getAllMeetings());
        Log.d("evntscal","presenter currntMeet size:"+currentMeetings.size());
        if (view != null) view.onListsUpdated();
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {

    }
}
