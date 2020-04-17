package cat.bcn.vincles.mobile.UI.Contacts;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.UI.FragmentManager.ContactsRepository;

public class AddContactPresenter implements AddContactPresenterContract, ContactsRepository.AddContactCallback {

    private AddContactView addContactView;
    private ContactsRepository contactsRepository;

    AddContactPresenter(BaseRequest.RenewTokenFailed listener, AddContactView addContactView, UserPreferences userPreferences) {
        this.addContactView = addContactView;
        this.contactsRepository = new ContactsRepository(this, listener);
    }

    //SEE CODE
    @Override
    public void onSeeCodeClicked() {
        contactsRepository.requestCode();
        addContactView.showWaitDialog();
    }

    @Override
    public void onAddContactClicked(String code, String relationship) {
        if (code == null || !isValid(code)) {
            addContactView.codeShouldNotBeEmpty();
        } else {
            contactsRepository.addContact(code, relationship);
            addContactView.showWaitDialog();
        }
    }

    @Override
    public void onResponseGenerateUserCode(String code) {
        addContactView.showCode(code);
        addContactView.hideWaitDialog();
    }

    @Override
    public void onResponseGenerateUserCodeError(Object error) {
        addContactView.hideWaitDialog();
        addContactView.showErrorMessage(error);
    }

    @Override
    public void onResponseAddUser(Contact contact) {
        addContactView.hideWaitDialog();
        addContactView.showContactAdded(contact);
    }

    @Override
    public void onResponseAddUserError(Object error) {
        addContactView.hideWaitDialog();
        addContactView.showErrorMessage(error);
    }

    private boolean isValid(String code) {
        return  code.length() > 0;
    }

    @Override
    public void onCirclesLoaded(List<Contact> contactList) {

    }

    @Override
    public void onRemoveContact(boolean ok) {
        //Not used
    }

    @Override
    public void setEmptyText(int filterKind) {

    }
}
