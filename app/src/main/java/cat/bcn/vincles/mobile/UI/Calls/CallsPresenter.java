package cat.bcn.vincles.mobile.UI.Calls;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import cat.bcn.vincles.mobile.Client.Business.NotificationProcessor;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.ErrorVideoconferenceRequest;
import cat.bcn.vincles.mobile.Client.Requests.StartVideoconferenceRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Login.LoginActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;

import static cat.bcn.vincles.mobile.UI.Calls.CallsActivityView.ONGOING_CALL;

public class CallsPresenter extends Fragment implements CallsPresenterContract, StartVideoconferenceRequest.OnResponse, ErrorVideoconferenceRequest.OnResponse {


    CallsActivityView view;

    private int callMode, userId;
    private boolean userIsVincles;
    private String userName;
    private String path;
    boolean varsInit = false;
    UserPreferences userPreferences;

    boolean activityCreated = false, presenterCreated = false;

    Handler callTimeoutHandler;

    boolean executeCallTimeout = false;

    boolean isIncomingCall;

    long callStartTime;
    private boolean missedCallNotificationSaved = false;


    public CallsPresenter() {
    }

    public static CallsPresenter newInstance (CallsActivityView view, Bundle savedInstanceState,
                                              int callMode, int userId, String path,
                                              String userName, boolean userIsVincles) {
        Log.d("callvid", "presenter newInstance callmode:"+callMode);
        CallsPresenter fragment = new CallsPresenter();
        fragment.setExternalVars(view, savedInstanceState);
        Bundle args = new Bundle();
        args.putInt("callMode", callMode);
        args.putInt("userId", userId);
        args.putString("path", path);
        args.putString("userName", userName);
        args.putBoolean("userIsVincles", userIsVincles);
        fragment.setArguments(args);

        return fragment;
    }


    public void setExternalVars(CallsActivityView view,
                                Bundle savedInstanceState) {
        this.view = view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("callvid", "presenter onCreate");
        super.onCreate(savedInstanceState);
        callTimeoutHandler = new Handler();

        if (userPreferences == null)
            userPreferences = new UserPreferences(MyApplication.getAppContext());
        //setRetainInstance(true);

        Bundle args = getArguments();
        callMode = args.getInt("callMode");
        Log.d("callvid", "presenter onCreate callmode:"+callMode);
        userId = args.getInt("userId");
        path = args.getString("path");
        userName = args.getString("userName");
        userIsVincles = args.getBoolean("userIsVincles");

        if (path == null || path.length() == 0) {
            //TODO request photo?
        }

        if (!varsInit && view != null) {
            varsInit = true;
            view.setMode(callMode);
        }

        presenterCreated = true;
        Log.d("callvid", "presenterCreated " + String.valueOf(presenterCreated));
        Log.d("callvid", "activityCreated " + String.valueOf(activityCreated));

        if (presenterCreated && activityCreated) doSetup();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        view = null;
    }

    private void doSetup() {
        Log.d("callvid", "doSetup");

        if (varsInit) view.setMode(callMode);
        if (callMode == CallsActivityView.RECEIVING_CALL) {
            isIncomingCall = true;
            Log.d("callvid", "presenter setup RECEIVING");
            int callerId = userId;
            String callerName = userName;
            boolean callerIsVincles = userIsVincles;
            int calleeId = userPreferences.getUserID();
            String calleeName = userPreferences.getName();
            boolean calleeIsVincles = userPreferences.getIsUserSenior();
            CallsWebRTCatManager callsWebRTCatManager = new CallsWebRTCatManager(
                    callerId, calleeId, callerName, calleeName, callerIsVincles,
                    calleeIsVincles, isIncomingCall);
            view.setupCallManager(callsWebRTCatManager);
            view.startRingSound();

            callStartTime = System.currentTimeMillis();
            executeCallTimeout = true;
            callTimeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d("callvid", "execute callTimeoutHandler1");
                    if (executeCallTimeout) {
                        addMissedCallNotification();
                        Log.d("incalock","finish call activity TIMETOUT");
                        if (view != null) view.finishCallActivity();
                    }
                }
            }, MyApplication.getAppContext().getResources().getInteger(R.integer.seconds_timeout_calls)*1000);
        } else if (callMode == CallsActivityView.MAKING_CALL) {
            isIncomingCall = false;
            Log.d("callvid", "presenter setup MAKING");
            int callerId = userPreferences.getUserID();
            String callerName = userPreferences.getName();
            boolean callerIsVincles = userPreferences.getIsUserSenior();
            int calleeId = userId;
            String calleeName = userName;
            boolean calleeIsVincles = userIsVincles;
            CallsWebRTCatManager callsWebRTCatManager = new CallsWebRTCatManager(
                    callerId, calleeId, callerName, calleeName, callerIsVincles,
                    calleeIsVincles, isIncomingCall);
            view.setupCallManager(callsWebRTCatManager);
        }
    }

    private void addMissedCallNotification() {
        if (!missedCallNotificationSaved) {
            missedCallNotificationSaved = true;
            new NotificationsDb(MyApplication.getAppContext()).saveMissedCallNotification(
                    callStartTime, userId);
            new UsersDb(MyApplication.getAppContext()).setMessagesInfo(userId,
                    new UserMessageDb(MyApplication.getAppContext())
                            .getNumberUnreadMessagesReceived(new UserPreferences().getUserID(),
                                    userId),
                    new NotificationsDb(MyApplication.getAppContext())
                            .getNumberUnreadMissedCallNotifications(userId),
                    Math.max(new UserMessageDb(MyApplication.getAppContext())
                            .getLastMessage(new UserPreferences().getUserID(), userId),
                            new NotificationsDb(MyApplication.getAppContext()).getLastMissedCallTime(userId)));
            NotificationProcessor.buildMissedCallNotification(getActivity() != null ?
                    getResources() : MyApplication.getAppContext().getResources(), userId);

            MyApplication.setPendingMissedCallBroadcast(true);
        }
    }

    @Override
    public void onCreateActivity() {
        activityCreated = true;
        Log.d("callvid", "presenterCreated2 " + String.valueOf(presenterCreated));
        Log.d("callvid", "activityCreated2 " + String.valueOf(activityCreated));
        if (activityCreated && presenterCreated) doSetup();
    }

    @Override
    public void onButtonClicked(int which) {
        switch (which) {
            case ANSWER_CALL_BUTTON:
                view.setMode(ONGOING_CALL);
                break;
            case RETRY_BUTTON:
                Log.d("callvid", "RETRY_BUTTON");
                callMode = CallsActivityView.MAKING_CALL;
                doSetup();
                break;
        }
    }

    @Override
    public void onOutgoingCallConnected() {
        Log.d("callvid", "cancel callTimeoutHandler");
        executeCallTimeout = false;
        if (view != null) view.setMode(ONGOING_CALL);
        callTimeoutHandler.removeCallbacks(null);
    }

    @Override
    public void onIncomingCallConnected() {
        executeCallTimeout = false;
        if (view != null) view.setMode(ONGOING_CALL);
    }

    @Override
    public void onIncomingCallCancelled() {
        executeCallTimeout = false;
        addMissedCallNotification();
    }

    @Override
    public void onIncomingCallRejected() {
        executeCallTimeout = false;
    }

    @Override
    public void onStopActivity() {
        if (isIncomingCall && callMode == ONGOING_CALL ) {
            addMissedCallNotification();
        }
    }

    @Override
    public void onError() {
        executeCallTimeout = false;
        /*if (isIncomingCall) {
            if (view != null) view.setMode(CallsActivityView.CALL_NO_ANSWER);
        } else {
            if (view != null) view.setMode(CallsActivityView.INCOMING_CALL_ERROR);
        }*/
        if (view != null) view.setMode(CallsActivityView.INCOMING_CALL_ERROR);
    }

    @Override
    public void onOutgoingCallError() {
        executeCallTimeout = false;
        if (view != null) view.setMode(CallsActivityView.OUTGOING_CALL_ERROR);
    }

    @Override
    public void onNotifyError(int idUser, String idRoom) {
        Log.d("cllerr", "onNotifyError idUser:"+idUser+" myUser:"+userPreferences.getUserID());
        ErrorVideoconferenceRequest errorVideoconferenceRequest = new ErrorVideoconferenceRequest(
                null,idUser, idRoom);
        errorVideoconferenceRequest.addOnOnResponse(this);
        errorVideoconferenceRequest.doRequest(userPreferences.getAccessToken());
    }

    @Override
    public void sendCallNotification(int idUser, String idRoom) {
        StartVideoconferenceRequest startVideoconferenceRequest = new StartVideoconferenceRequest(
                null,idUser, idRoom);
        startVideoconferenceRequest.addOnOnResponse(this);
        startVideoconferenceRequest.doRequest(userPreferences.getAccessToken());
        Log.d("callvid", "init callTimeoutHandler");
        executeCallTimeout = true;
        callTimeoutHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
               timeOutExecuted();
            }
        }, MyApplication.getAppContext().getResources().getInteger(R.integer.seconds_timeout_calls)*1000);


    }


    public void timeOutExecuted(){
        Log.d("callvid", "execute callTimeoutHandler2");
        if (executeCallTimeout) {
            callMode = CallsActivityView.CALL_NO_ANSWER;
            if (view != null) view.setMode(callMode);
            if (view != null) view.onCallTimeout();
            executeCallTimeout = false;

        }
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {

    }


    @Override
    public void onResponseStartVideoconference(int idUser, String idRoom) {
        Log.d("callvid", "onResponseStartVideoconference");
        //todo do something on success?
    }

    @Override
    public void onFailureStartVideoconference(Object error) {
        Log.d("callvid", "onFailureStartVideoconference");
        if (view != null) view.onError(MyApplication.getAppContext().getResources()
                .getString(R.string.calls_error_before_call));
    }

    @Override
    public void onResponseErrorVideoconference(int idUser, String idRoom) {
        Log.d("cllerr", "onResponseErrorVideoconference, idRoom:"+idRoom);
    }

    @Override
    public void onFailureErrorVideoconference(Object error) {
        Log.d("cllerr", "onFailureErrorVideoconference:"+error);
    }
}
