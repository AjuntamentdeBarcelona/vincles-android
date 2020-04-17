package cat.bcn.vincles.mobile.UI.Home;


import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.FragmentManager.ContactsRepository;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class HomePresenter implements HomePresenterContract, ContactsRepository.HomeCallback {

    private ContactsRepository contactsRepository;
    private HomeFragmentView homeFragmentView;

    public HomePresenter(BaseRequest.RenewTokenFailed listener, HomeFragmentView homeFragmentView) {
        this.homeFragmentView = homeFragmentView;
        contactsRepository = new ContactsRepository(this, listener);
    }

    @Override
    public void getContacts(boolean needCallRest) {
        contactsRepository.loadLocalContacts();
        if (needCallRest) {
            contactsRepository.loadCircleUsers();
        }
    }

    @Override
    public void getContactPicture(int id, int type) {
        contactsRepository.loadContactPicture(id, type);
    }

    @Override
    public void onCreateView() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateNotificationsNumber();
            }
        }).start();
    }

    public void updateNotificationsNumber() {
        int calendarNumber = new MeetingsDb(MyApplication.getAppContext())
                .getNumberOfMeetingsPending();
        if (homeFragmentView != null) homeFragmentView.setCalendarNumber(calendarNumber);

        int notificationsNumber = new NotificationsDb(MyApplication.getAppContext())
                .findUnwatchedNotificationsNumber();
        if (homeFragmentView != null) homeFragmentView.setNotificationsNumber(notificationsNumber);
    }


    @Override
    public void onCirclesLoaded(List<Contact> contactList) {
        if (contactList.size() == 0) {
            homeFragmentView.showNoContactsError();
        } else {
            homeFragmentView.hideNoContactsError();
            homeFragmentView.onContactsReady(contactList);
        }
    }

    @Override
    public void onRemoveContact(boolean ok) {
        //Not needed
    }

    @Override
    public void setEmptyText(int filterKind) {

    }

    @Override
    public void onUserPictureLoaded(int id, String path) {
        homeFragmentView.onUserPictureLoaded(id, path);
    }

    @Override
    public void onUserPictureFail(int id) {
        homeFragmentView.onUserPictureLoaded(id,"placeholder");
    }
}
