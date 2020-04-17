package cat.bcn.vincles.mobile.UI.Calendar;


import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;

public interface CalendarDateDetailFragmentView {

    void showMeeting(MeetingRealm meeting, List<Contact> contacts, String hostName,
                     String hostPath);

    void updateHostImage(String path);

    void showError(Object error);

    void notifyContactChange();

}
