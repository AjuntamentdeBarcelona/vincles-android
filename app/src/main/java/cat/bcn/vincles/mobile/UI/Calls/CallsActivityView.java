package cat.bcn.vincles.mobile.UI.Calls;


import java.util.Calendar;

public interface CallsActivityView {

    static final int RECEIVING_CALL = 0;
    static final int MAKING_CALL = 1;
    static final int CALL_NO_ANSWER = 2;
    static final int ONGOING_CALL = 3;
    static final int INCOMING_CALL_ERROR = 4;
    static final int OUTGOING_CALL_ERROR = 5;


    void setMode(int mode);
    void setupCallManager(CallsWebRTCatManager callsWebRTCatManager);
    void setPathAvatar(String path);
    void startRingSound();
    void stopRingSound();

    void onError(String error);

    void finishCallActivity();

    void onCallTimeout();

}
