package cat.bcn.vincles.mobile.UI.Calls;


import android.os.Bundle;
import android.util.SparseArray;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.UI.Compound.CalendarCompoundView;
import io.realm.RealmResults;

public interface CallsPresenterContract {

    static final int CANCEL_CALL_BUTTON = 0;
    static final int ANSWER_CALL_BUTTON = 1;
    static final int HUNG_UP_BUTTON = 2;
    static final int SEND_MESSAGE_BUTTON = 3;
    static final int RETRY_BUTTON = 4;

    void onCreateActivity();

    void onButtonClicked(int which);
    void onOutgoingCallConnected();
    void onIncomingCallConnected();
    void onIncomingCallCancelled();
    void onIncomingCallRejected();

    void timeOutExecuted();

    void onStopActivity();

    void onError();
    void onOutgoingCallError();
    void onNotifyError(int idUser, String idRoom);

    void sendCallNotification(int idUser, String idRoom);

}
