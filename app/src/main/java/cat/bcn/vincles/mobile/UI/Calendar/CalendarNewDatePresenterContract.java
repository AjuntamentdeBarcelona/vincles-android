package cat.bcn.vincles.mobile.UI.Calendar;


import android.os.Bundle;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.MeetingRest;

public interface CalendarNewDatePresenterContract {


    void onCreateView();
    void onSaveInstanceState(Bundle outState);

    void loadMeeting(int meetingId);

    void onSaveClicked(String description, long startDate, int lengthDate);

    void stopedShowingErrorDialog();

    void addContacts(ArrayList<Integer> contactIds);

    ArrayList<Integer> getContactIds();
    void removeContact(int id);

    void loadContactPicture(int contactId);


}
