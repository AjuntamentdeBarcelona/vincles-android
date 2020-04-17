package cat.bcn.vincles.mobile.UI.Chats;


import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;

public interface GroupDetailFragmentView {

    void showError(Object error);
    void showInvitationSent();
    void showSendingData();
    void hideSendingData();

    void notifyContactChange();

    void updateAvatar(String path);
    void updateGroupName(String name);
    void updateDescription(String description);
    void setContacts(ArrayList<Contact> contacts);


}
