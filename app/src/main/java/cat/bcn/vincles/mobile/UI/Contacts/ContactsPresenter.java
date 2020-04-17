package cat.bcn.vincles.mobile.UI.Contacts;


import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.UI.FragmentManager.ContactsRepository;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class ContactsPresenter implements ContactsPresenterContract, ContactsRepository.ContactsCallback {

    List<Contact> contactList;
    private ContactsRepository contactsRepository;
    private ContactsFragment contactsFragment;
    private int filterKind = ContactsPresenterContract.FILTER_ALL_CONTACTS;
    // contactId of the selected contacts of type "individual" (for the presenter to eventually perform
    // the corresponding action: share content, invite to events, etc.)
    private List<Integer> selectedContactIds = new ArrayList<>();
    // chatId of the selected contacts of type "group" (for the presenter to eventually perform
    // the corresponding action: share content, invite to events, etc.)
    private List<Integer> selectedChatIds = new ArrayList<>();
    // contactId of the selected contacts of both type "individual" and "group" (for selection purposes)
    private List<Integer> selectedContactAndChatIds = new ArrayList<>();

    public ContactsPresenter(BaseRequest.RenewTokenFailed listener, ContactsFragment contactsFragment, int filterKind,
                             List<Integer> selectedContacts, List<Integer> selectedChats, List<Integer> selectedContactsAndChats) {
        contactsRepository = new ContactsRepository(this, listener);
        this.contactsFragment = contactsFragment;
        if (filterKind != ContactsPresenterContract.FILTER_NOT_INIT) {
            this.filterKind = filterKind;
            contactsRepository.setFilterKind(filterKind);
        }
        if (selectedContacts == null) {
            selectedContacts = new ArrayList<>();
        }
        selectedContactIds.addAll(Collections.unmodifiableList(selectedContacts));
        if (selectedChats == null) {
            selectedChats = new ArrayList<>();
        }
        selectedChatIds.addAll(Collections.unmodifiableList(selectedChats));
        if (selectedContactsAndChats == null) {
            selectedContactsAndChats = new ArrayList<>();
        }
        selectedContactAndChatIds.addAll(Collections.unmodifiableList(selectedContactsAndChats));
    }

    @Override
    public void getContacts() {
        contactsRepository.loadLocalContacts();
        //contactsRepository.loadCircleUsers();
    }

    @Override
    public int getFilterKind() {
        return filterKind;
    }

    @Override
    public void onFilterClicked(int whichFilter) {
        filterKind = whichFilter;
        contactsRepository.onFilterChanged(filterKind);
        setEmptyText(filterKind);
    }


    @Override
    public void getContactPicture(int contactId, int contactType) {
        contactsRepository.loadContactPicture(contactId, contactType);
    }

    @Override
    public void deleteCircle(int idUserToUnlink) {
        contactsFragment.showWaitDialog();
        contactsRepository.deleteCircle(idUserToUnlink);
    }

    @Override
    public void refreshContactList() {
        contactsRepository.onFinishedLoadingContacts(false);
    }

    @Override
    public void shareMedia(ArrayList<Integer> mediaIdArrayList) {
        if ((selectedContactIds != null && selectedContactIds.size() > 0) || (selectedChatIds != null
                && selectedChatIds.size() > 0)) {
            contactsFragment.showWaitDialog();
            if (selectedContactIds !=null && selectedContactIds.size()==1 && mediaIdArrayList.size() == 1 && (selectedChatIds == null
                    || selectedChatIds.size() == 0)) {
                contactsRepository.shareMediaToOneContact(mediaIdArrayList, selectedContactIds.get(0));
            } else {
                contactsRepository.shareMediaToManyContacts(mediaIdArrayList, selectedContactIds,
                        selectedChatIds);
            }
        }
    }

    @Override
    public List<Integer> getSelectedContactIds() {
        return selectedContactIds;
    }

    @Override
    public List<Integer> getSelectedChatIds() {
        return selectedChatIds;
    }

    @Override
    public List<Integer> getSelectedContactAndChatIds() {
        return selectedContactAndChatIds;
    }

    @Override
    public void onCirclesLoaded(List<Contact> contactList) {
        this.contactList = contactList;
        if (contactList.size() == 0) {
            contactsFragment.showNoContactsError();
        } else {
            contactsFragment.hideNoContactsError();
            contactsFragment.loadContacts(contactList);
        }
    }

    @Override
    public void onRemoveContact(boolean ok) {
        contactsFragment.hideWaitDialog();
        contactsFragment.showContactDeleteAlert(ok);
    }

    @Override
    public void setEmptyText(int filterKind) {
        contactsFragment.setEmptyText(filterKind);
    }

    @Override
    public void onUserPictureLoaded() {
        contactsFragment.reloadContactAdapter();
    }

    @Override
    public void onUserPictureFail() {
    }

    @Override
    public void onMediaShared(boolean ok) {
        contactsFragment.hideWaitDialog();
        contactsFragment.showSharedMediaAlert(ok);
    }

    @Override
    public void contactSelected(Contact contact) {
        if (contact.getType() <= Contact.TYPE_USER_CIRCLE) {
            if (selectedContactIds == null) {
                selectedContactIds = new ArrayList<>();
            }

            selectedContactIds.add(contact.getId());
            OtherUtils.removeDuplicates(selectedContactIds);

        } else {
            if (selectedChatIds == null) {
                selectedChatIds = new ArrayList<>();
            }

            selectedChatIds.add(contact.getIdChat());
            OtherUtils.removeDuplicates(selectedChatIds);
        }
        if (selectedContactAndChatIds == null) {
            selectedContactAndChatIds = new ArrayList<>();
        }

        selectedContactAndChatIds.add(contact.getId());
        OtherUtils.removeDuplicates(selectedContactAndChatIds);
    }

    @Override
    public void contactUnselected(Contact contact) {
        if (contact.getType() <= Contact.TYPE_USER_CIRCLE) {
            if (selectedContactIds == null) {
                selectedContactIds = new ArrayList<>();
            }

            selectedContactIds.removeAll(Collections.singletonList(contact.getId()));

        } else {
            if (selectedChatIds == null) {
                selectedChatIds = new ArrayList<>();
            }

            selectedChatIds.removeAll(Collections.singletonList(contact.getIdChat()));
        }
        if (selectedContactAndChatIds == null) {
            selectedContactAndChatIds = new ArrayList<>();
        }

        selectedContactAndChatIds.removeAll(Collections.singletonList(contact.getId()));
    }

    @Override
    public void notificationToProcess(Bundle data) {
        String type = data.getString("type");
        switch (type) {
            case "USER_UPDATED":
            case "USER_LINKED":
            case "ADDED_TO_GROUP":
            case "GROUP_UPDATED":
            case "REMOVED_FROM_GROUP":
            case "NEW_MESSAGE":
            case "NEW_CHAT_MESSAGE":
                contactsRepository.onReloadList();
                break;
            case "USER_UNLINKED":
            case "USER_LEFT_CIRCLE":
                int idUser = data.getInt("idUser");
                contactsRepository.onUserRemoved(idUser);
                break;
        }
    }
}
