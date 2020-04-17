package cat.bcn.vincles.mobile.UI.Home;

import java.util.List;

import cat.bcn.vincles.mobile.UI.Contacts.Contact;

public interface HomeFragmentView {

    void onContactsReady(List<Contact> contacts);
    void onUserPictureLoaded(int id, String path);
    void showNoContactsError();
    void hideNoContactsError();
    void setCalendarNumber(int number);
    void setNotificationsNumber(int number);

}
