package cat.bcn.vincles.mobile.UI.Calendar;


import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;

public interface CalendarNewDateFragmentView {

    void showMeeting(MeetingRealm meeting, List<Contact> contacts);
    void showContacts(List<Contact> contacts);

    void goBack();

    void showWaitDialog();
    void hideWaitDialog();
    void showError(Object error);
    void showEmptyTitleError();

    void notifyContactChange();

    void onMeetingCreatedOrUpdated();

}
