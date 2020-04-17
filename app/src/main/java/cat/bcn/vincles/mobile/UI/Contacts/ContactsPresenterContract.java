package cat.bcn.vincles.mobile.UI.Contacts;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public interface ContactsPresenterContract  {

    int FILTER_NOT_INIT = -1;
    int FILTER_ALL_CONTACTS = 0;
    int FILTER_FAMILY = 1;
    int FILTER_GROUPS = 2;
    int FILTER_DYNAM = 3;
    int FILTER_ALL_CONTACTS_BUT_GROUPS = 4;

    void getContacts();
    int getFilterKind();
    void onFilterClicked(int whichFilter);
    void getContactPicture(int contactId, int contactType);
    void deleteCircle(int idUserToUnlink);
    void refreshContactList();
    void shareMedia(ArrayList<Integer> mediaIdArrayList);
    List<Integer> getSelectedContactIds();
    List<Integer> getSelectedChatIds();
    List<Integer> getSelectedContactAndChatIds();
    void contactSelected(Contact contact);
    void contactUnselected(Contact contact);
    void notificationToProcess(Bundle data);
}
