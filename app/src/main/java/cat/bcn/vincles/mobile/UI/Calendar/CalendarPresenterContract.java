package cat.bcn.vincles.mobile.UI.Calendar;


import android.os.Bundle;
import android.util.SparseArray;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.UI.Compound.CalendarCompoundView;
import io.realm.RealmResults;

public interface CalendarPresenterContract extends CalendarCompoundView.OnCalendarEventListener {

    void loadData();

    void onCreateView();
    void onSaveInstanceState(Bundle outState);

    void seeToday();
    void seeTomorrow();
    void seeMonth();

    long getShownDay();

    void stopedShowingErrorDialog();

    ArrayList<MeetingRealm> getCurrentMeetings();
    RealmResults<MeetingRealm> getAllMeetings();
    //SparseArray<GetUser> getUsersInMeetings();

    void onItemButtonClicked(int whatButton, int meetingId);

    void onConfirmationDialogAccepted();
    void onConfirmationDialogCanceled();

}
