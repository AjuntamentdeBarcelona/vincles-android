package cat.bcn.vincles.mobile.UI.FragmentManager;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageMulticastRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.ChatMessagesSentResponse;
import cat.bcn.vincles.mobile.Client.Model.CircleUser;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Model.Serializers.AddUser;
import cat.bcn.vincles.mobile.Client.Model.UserCircle;
import cat.bcn.vincles.mobile.Client.Model.UserGroup;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.AddUserRequest;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.DeleteCircleRequest;
import cat.bcn.vincles.mobile.Client.Requests.ExitCircleRequest;
import cat.bcn.vincles.mobile.Client.Requests.GenerateUserCodeRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetCircleUserRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserCircleRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserGroupsRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.SendMessageMulticastRequest;
import cat.bcn.vincles.mobile.Client.Requests.SendMessageRequest;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsPresenterContract;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.RealmList;

import static cat.bcn.vincles.mobile.UI.Contacts.Contact.TYPE_CIRCLE_USER;
import static cat.bcn.vincles.mobile.UI.Contacts.Contact.TYPE_USER_CIRCLE;
import static cat.bcn.vincles.mobile.UI.Contacts.ContactsPresenterContract.FILTER_ALL_CONTACTS;
import static cat.bcn.vincles.mobile.Utils.OtherUtils.convertIntegers;

public class ContactsRepository implements GetCircleUserRequest.OnResponse, GetUserPhotoRequest.OnResponse,
        GetUserCircleRequest.OnResponse, GetUserGroupsRequest.OnResponse, DeleteCircleRequest.OnResponse, GetGroupPhotoRequest.OnResponse,
        GenerateUserCodeRequest.OnResponse, AddUserRequest.OnResponse, SendMessageRequest.OnResponse, SendMessageMulticastRequest.OnResponse, ExitCircleRequest.OnResponse, GetMessageRequest.OnResponse, GetGroupMessageRequest.OnResponse {

    private Callback listener;
    private List<Contact> filterContactList;
    private List<Contact> allCircleUsersList;
    private List<Contact> familyContactsList;
    private List<Contact> groupsList;
    private List<Contact> dynamizersList;
    private UserGroupsDb userGroupsDb;
    private UsersDb usersDb;
    private int filterKind = FILTER_ALL_CONTACTS;
    private List<Integer> photoRequest;
    private boolean loadingUserAdded;
    private Contact userAddedContact;
    BaseRequest.RenewTokenFailed renewTokenListener;
    private int contactsListSize;
    public static ContactsRepository shared;

    public ContactsRepository(Callback listener, BaseRequest.RenewTokenFailed renewTokenListener) {
        this.listener = listener;
        this.renewTokenListener = renewTokenListener;
        filterContactList = new ArrayList<>();
        photoRequest = new ArrayList<>();
        usersDb = new UsersDb(MyApplication.getAppContext());
        contactsListSize = 0;
        ContactsRepository.shared = this;
    }

    public void setFilterKind(int filterKind) {
        this.filterKind = filterKind;
    }

    public void onFilterChanged(int filterKind) {
        setFilterKind(filterKind);
        onFinishedLoadingContacts(true);
    }

    public void loadLocalContacts() {
        ArrayList<GetUser> circleUserArrayList = usersDb.findAllCircleUser();
        allCircleUsersList = new ArrayList<>();
        for (int i = 0; i < circleUserArrayList.size(); i++) {
            allCircleUsersList.add(createContact(circleUserArrayList.get(i), -1));
        }
        ArrayList<GetUser> userCircleArrayList = usersDb.findAllUserCircle();
        familyContactsList = new ArrayList<>();
        for (int i = 0; i < userCircleArrayList.size(); i++) {
            familyContactsList.add(createContact(userCircleArrayList.get(i), TYPE_USER_CIRCLE));
        }
        ArrayList<GroupRealm> groupRealmArrayList = new UserGroupsDb(MyApplication.getAppContext()).findAllGroupRealm();
        groupsList = new ArrayList<>();
        for (int i = 0; i < groupRealmArrayList.size(); i++) {
            groupsList.add(createContactFromUserGroup(groupRealmArrayList.get(i)));
        }
        ArrayList<Dynamizer> dynamizerArrayList = new UserGroupsDb(MyApplication.getAppContext()).findAllDynamizer();
        dynamizersList = new ArrayList<>();
        for (int i = 0; i < dynamizerArrayList.size(); i++) {
            dynamizersList.add(createContactFromDynamizer(dynamizerArrayList.get(i)));
        }
        onFinishedLoadingContacts(true);
    }

    public void loadCircleUsers() {
        Log.d("contRepo","loadCircleUsers");
        String token = new UserPreferences().getAccessToken();
        GetCircleUserRequest getCircleUserRequest = new GetCircleUserRequest(renewTokenListener);
        getCircleUserRequest.addOnOnResponse(this);
        getCircleUserRequest.doRequest(token);
    }

    public void loadUserCircles() {
        Log.d("contRepo","loadUserCircles");
        String token = new UserPreferences().getAccessToken();
        GetUserCircleRequest getUserCircleRequest = new GetUserCircleRequest(renewTokenListener, token);
        getUserCircleRequest.addOnOnResponse(this);
        getUserCircleRequest.doRequest();
    }

    public void loadGroups() {
        Log.d("contRepo","loadGroups");
        String token = new UserPreferences().getAccessToken();
        GetUserGroupsRequest getUserGroupsRequest = new GetUserGroupsRequest(renewTokenListener, token);
        getUserGroupsRequest.addOnOnResponse(this);
        getUserGroupsRequest.doRequest();
    }

    public void deleteCircle(int idUserToUnlink) {
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        GetUser getUser = usersDb.findUserUnmanaged(idUserToUnlink);
        if (getUser != null) {
            String token = new UserPreferences().getAccessToken();
            if (getUser.getIdCircle() != null && getUser.getIdCircle() != -1) {
                ExitCircleRequest exitCircleRequest = new ExitCircleRequest(
                        renewTokenListener, token, idUserToUnlink, getUser.getIdCircle());
                exitCircleRequest.addOnOnResponse(this);
                exitCircleRequest.doRequest();


            } else {
                DeleteCircleRequest deleteCircleRequest = new DeleteCircleRequest(
                        renewTokenListener, token, idUserToUnlink);
                deleteCircleRequest.addOnOnResponse(this);
                deleteCircleRequest.doRequest();
            }
        } else {
            if (listener != null) {
                listener.onRemoveContact(false);
            }
        }


    }

    public void requestCode() {
        GenerateUserCodeRequest generateUserCodeRequest = new GenerateUserCodeRequest(renewTokenListener);
        generateUserCodeRequest.addOnOnResponse(this);
        generateUserCodeRequest.doRequest(new UserPreferences().getAccessToken());
    }

    public void addContact(String code, String relationship) {
        AddUserRequest addUserRequest = new AddUserRequest(renewTokenListener, code, relationship);
        addUserRequest.addOnOnResponse(this);
        addUserRequest.doRequest(new UserPreferences().getAccessToken());
    }

    public void onUserAdded(int idUser) {
        loadLocalContacts();
    }

    public void onUserUpdated(int idUser) {
        loadLocalContacts();
    }

    public void onReloadList() {
        loadLocalContacts();
    }

    public void onUserRemoved(int idUser) {
        for (Contact contact : allCircleUsersList) {
            if (idUser == contact.getId()) {
                allCircleUsersList.remove(contact);
                break;
            }
        }
        for (Contact contact : familyContactsList) {
            if (idUser == contact.getId()) {
                familyContactsList.remove(contact);
                break;
            }
        }
        onFinishedLoadingContacts(true);
    }

    public void onFinishedLoadingContacts(boolean local) {
        Log.d("contRepo","onFinishedLoadingContacts");
        if ((allCircleUsersList.size() + familyContactsList.size() + groupsList.size() + dynamizersList.size()) != contactsListSize || local) {
            filterContactList.clear();
            if (allCircleUsersList != null && (
                    (filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS)
                            || filterKind == ContactsPresenterContract.FILTER_FAMILY
                            || filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS_BUT_GROUPS)) {
                filterContactList.addAll(allCircleUsersList);
            }
            if (familyContactsList != null && (
                    (filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS)
                            || filterKind == ContactsPresenterContract.FILTER_FAMILY
                            || filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS_BUT_GROUPS)) {
                filterContactList.addAll(familyContactsList);
            }
            if (groupsList != null && (
                    (filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS)
                            || filterKind == ContactsPresenterContract.FILTER_GROUPS)) {
                filterContactList.addAll(groupsList);
            }
            if (dynamizersList != null && (
                    (filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS)
                            || filterKind == ContactsPresenterContract.FILTER_DYNAM
                            || filterKind == ContactsPresenterContract.FILTER_ALL_CONTACTS_BUT_GROUPS)) {
                filterContactList.addAll(dynamizersList);
            }

            Collections.sort(filterContactList);

            /*String unread = "";
            String intera = "";
            for (int i = 0; i < 10; i++) {
                unread = unread + ", " + filterContactList.get(i).getNumberNotifications();
                intera = intera + ", " + filterContactList.get(i).getLastInteraction();
            }
            Log.d("unrd","Numbers unread:"+unread+" \n Numbers intera:"+intera);*/

            String a = "";
            for (int i = 0; i < filterContactList.size(); i++) {
                a = a + "contactId:"+filterContactList.get(i).getId()+", numUnread:"
                        +filterContactList.get(i).getNumberNotifications()+", numIntera:"
                        +filterContactList.get(i).getNumberInteractions()+"\n";
            }
            Log.d("unrd","contactsRepo, info:\n"+a);


            contactsListSize = allCircleUsersList.size() + familyContactsList.size() + groupsList.size() + dynamizersList.size();
            listener.onCirclesLoaded(filterContactList);
        }

        if(filterKind == ContactsPresenterContract.FILTER_GROUPS){
            listener.setEmptyText(filterKind);
        }
    }

    public void loadContactPicture(int contactId, int contactType) {
        Log.d("homecnt", "loadGroupPicture repo");
        String accessToken = new UserPreferences().getAccessToken();

        photoRequest.add(new Integer(0));
        if (contactType == Contact.TYPE_GROUP) {
            GetGroupPhotoRequest getGroupPhotoRequest = new GetGroupPhotoRequest(renewTokenListener,
                    String.valueOf(contactId));
            getGroupPhotoRequest.addOnOnResponse(this);
            getGroupPhotoRequest.doRequest(accessToken);
        } else {
            GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(renewTokenListener, String.valueOf(contactId));
            getUserPhotoRequest.setContactType(contactType);
            getUserPhotoRequest.addOnOnResponse(this);
            getUserPhotoRequest.doRequest(accessToken);
        }
    }

    @Override
    public void onResponseGetCircleUserRequest(ArrayList<CircleUser> circleUsers) {

        allCircleUsersList = new ArrayList<>();
        for (CircleUser circleUser : circleUsers) {
            Contact contact = createContact(circleUser.getUser(), -1);

            String path = usersDb.getUserAvatarPath(circleUser.getUser().getId());
            contact.setPath(path != null ? path : "");
            circleUser.getUser().setPhoto(path);

            allCircleUsersList.add(contact);
        }
        usersDb.saveCircleUsers(circleUsers);
        loadUserCircles();
    }

    private Contact createContact(GetUser getUser, int type) {
        Contact contact = new Contact();
        contact.setId(getUser.getId());
        contact.setName(getUser.getName());
        contact.setLastname(getUser.getLastname());
        contact.setIdContentPhoto(getUser.getIdContentPhoto() == null ? -1 : getUser.getIdContentPhoto());
        contact.setPath(getUser.getPhoto());
        contact.setNumberInteractions(getUser.getLastInteraction());
        contact.setNumberNotifications(getUser.getNumberUnreadMessages());

        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        UserPreferences userPreferences = new UserPreferences();

        long lastInteraction = Math.max(userMessageDb.getLastMessage(userPreferences.getUserID(), getUser.getId()),
                new NotificationsDb(MyApplication.getAppContext())
                        .getLastMissedCallTime(getUser.getId()));
        contact.setLastInteraction(lastInteraction);

        if (type != -1) {
            contact.setType(type);
        }
        return contact;
    }

    public void getUserAvatar(int userID) {
        String accessToken = new UserPreferences().getAccessToken();
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(renewTokenListener, String.valueOf(userID));
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(accessToken);
    }

    @Override
    public void onFailureGetCircleUserRequest(Object error) {
        Log.d("contRepo","onFailureGetCircleUserRequest");
        if ("1102".equals(error)) {
            Log.d("cureq", "onFailureGetCircleUserRequest error 1102");
            loadUserCircles();
        } else {
            Log.d("cureq", "onFailureGetCircleUserRequest error NOT 1102");
        }
    }

    @Override
    public void onResponseGetUserCircleRequest(ArrayList<UserCircle> userCircles) {
        Log.d("contRepo","onResponseGetUserCircleRequest");
        familyContactsList = new ArrayList<>();
        for (UserCircle userCircle : userCircles) {
            Contact contact = createContactFromUserCircle(userCircle);

            String path = usersDb.getUserAvatarPath(userCircle.getCircle().getUser().getId());
            contact.setPath(path != null ? path : "");
            userCircle.getCircle().getUser().setPhoto(path);

            familyContactsList.add(contact);
        }
        usersDb.saveUserCircles(userCircles);
        loadGroups();

    }

    private Contact createContactFromUserCircle(UserCircle userCircle) {
        Contact contact = new Contact();
        contact.setId(userCircle.getCircle().getUser().getId());
        contact.setName(userCircle.getCircle().getUser().getName());
        contact.setLastname(userCircle.getCircle().getUser().getLastname());
        contact.setIdContentPhoto(userCircle.getCircle().getUser().getIdContentPhoto());
        contact.setPath(userCircle.getCircle().getUser().getPhoto());
        contact.setType(TYPE_USER_CIRCLE);
        contact.setNumberInteractions(userCircle.getCircle().getUser().getLastInteraction());
        contact.setNumberNotifications(userCircle.getCircle().getUser().getNumberUnreadMessages());

        UserPreferences userPreferences = new UserPreferences();
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());

        long lastInteraction = Math.max(userMessageDb.getLastMessage(userPreferences.getUserID(), userCircle.getCircle().getUser().getId()),
                new NotificationsDb(MyApplication.getAppContext())
                        .getLastMissedCallTime(userCircle.getCircle().getUser().getId()));

        contact.setLastInteraction(lastInteraction);


        return contact;
    }

    @Override
    public void onFailureGetUserCircleRequest(Object error) {
        Log.d("contRepo","onFailureGetUserCircleRequest err:"+error);
        if ("1102".equals(error)) {
            Log.d("cureq", "onFailureGetUserCircleRequest error 1102");
            loadGroups();
        } else if ("1107".equals(error)) {
            //user type cannot load this data, proceed to next
            loadGroups();
        } else {
            Log.d("cureq", "onFailureGetUserCircleRequest error NOT 1102");
        }
    }

    @Override
    public void onResponseGetUserGroupsRequest(ArrayList<UserGroup> userGroups) {
        Log.d("contRepo","onResponseGetUserGroupsRequest");
        userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
        groupsList = new ArrayList<>();
        dynamizersList = new ArrayList<>();
        for (UserGroup userGroup: userGroups) {

            GroupRealm groupRealm = new GroupRealm(userGroup.getGroup().getIdGroup(), userGroup.getGroup().getName(), userGroup.getGroup().getTopic(), userGroup.getGroup().getDescription(),
                    userGroup.getGroup().getPhoto(), userGroup.getGroup().getDynamizer().getId(), userGroup.getGroup().getIdChat());
            Contact contact = createContactFromUserGroup(groupRealm);
            groupsList.add(contact);
            String path = userGroupsDb.getUserGroupAvatarPath(userGroup.getGroup().getIdGroup());
            userGroup.getGroup().setPhoto(path);
            contact.setPath(path);

            RealmList<Integer> userIds = null;
            GroupRealm currentGroupRealm = userGroupsDb.getGroupUnmanaged(userGroup.getGroup().getIdGroup());
            if (currentGroupRealm != null) {
                userIds = currentGroupRealm.getUsers();
            }
            if (userIds != null) {
                groupRealm.setUsers(userIds);
            }

            Contact contactDynamizer = createContactFromDynamizer(userGroup.getGroup().getDynamizer());
            boolean dynamizerAdded = false;
            for (Contact dynamizerContact : dynamizersList) {
                if (dynamizerContact.getId() == contactDynamizer.getId()) {
                    dynamizerAdded = true;
                    break;
                }
            }
            if (!dynamizerAdded) {
                dynamizersList.add(contactDynamizer);
            }
            path = userGroupsDb.getGroupDynamizerAvatarPath(userGroup.getGroup().getDynamizer().getId());
            userGroup.getGroup().getDynamizer().setPhoto(path);
            contactDynamizer.setPath(path);
        }
        userGroupsDb.saveCurrentUsersGroups(userGroups);
        onFinishedLoadingContacts(false);

    }

    @Override
    public void onResponseGenerateUserCode(String code) {
        if (listener != null && listener instanceof AddContactCallback) {
            ((AddContactCallback) listener).onResponseGenerateUserCode(code);
        }
    }

    @Override
    public void onFailureGenerateUserCode(Object error) {
        if (listener != null && listener instanceof AddContactCallback) {
            ((AddContactCallback) listener).onResponseGenerateUserCodeError(error);
        }
    }

    @Override
    public void onResponseAddUser(AddUser response) {
        new UsersDb(MyApplication.getAppContext()).addUser(response);
        createContactFromUserAdded(response);
    }

    @Override
    public void onFailureAddUser(Object error) {
        if (listener != null && listener instanceof AddContactCallback) {
            ((AddContactCallback) listener).onResponseAddUserError(error);
        }
    }

    private void createContactFromUserAdded(AddUser addUser) {
        this.loadingUserAdded = true;
        userAddedContact = new Contact();
        userAddedContact.setName(addUser.getUserVincles().getName());
        userAddedContact.setLastname(addUser.getUserVincles().getLastname());
        getUserAvatar(addUser.getUserVincles().getId());
    }

    private Contact createContactFromUserGroup(GroupRealm groupRealm) {
        Contact contact = new Contact();
        contact.setId(groupRealm.getIdGroup());
        contact.setIdChat(groupRealm.getIdChat());
        contact.setName(groupRealm.getName());
        contact.setLastname("");
        contact.setPath(groupRealm.getPhoto());
        contact.setType(Contact.TYPE_GROUP);
        contact.setNumberInteractions(groupRealm.getNumberInteractions());
        contact.setNumberNotifications(groupRealm.getNumberUnreadMessages());

        GroupMessageDb ch = new GroupMessageDb(MyApplication.getAppContext());

        long lastInteraction = ch.getLastMessageFromGroup(groupRealm.getIdChat());

        contact.setLastInteraction(lastInteraction);

        return contact;
    }

    private Contact createContactFromDynamizer(Dynamizer dynamizer) {
        Contact contact = new Contact();
        contact.setId(dynamizer.getId());
        contact.setIdChat(dynamizer.getIdChat());
        contact.setName(dynamizer.getName());
        contact.setLastname(dynamizer.getLastname());
        contact.setIdContentPhoto(dynamizer.getIdContentPhoto());
        contact.setPath(dynamizer.getPhoto());
        contact.setType(Contact.TYPE_DYNAMIZER);
        contact.setNumberInteractions(dynamizer.getNumberInteractions());
        contact.setNumberNotifications(dynamizer.getNumberUnreadMessages());

        GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());

        long lastInteraction = Math.max(groupMessageDb.getLastMessageFromGroup(dynamizer.getIdChat()),
                new NotificationsDb(MyApplication.getAppContext())
                        .getLastMissedCallTime(dynamizer.getId()));

        contact.setLastInteraction(lastInteraction);

        return contact;
    }

    @Override
    public void onFailureGetUserGroupsRequest(Object error) {
        Log.d("contRepo","onFailureGetUserGroupsRequest err:"+error);
        onFinishedLoadingContacts(false);
    }

    @Override
    public void onResponseGetUserPhotoRequest(final Uri photo, String userID, final int viewID, int contactType) {
        if (photoRequest != null && photoRequest.size() > 0) {
            photoRequest.remove(0);
        }
        processPhotoResponse(photo.getPath(), userID, contactType);
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        if (photoRequest != null && photoRequest.size() > 0) {
            photoRequest.remove(0);
        }
        processPhotoError(userID, contactType);
    }

    @Override
    public void onResponseGetGroupPhotoRequest(Uri photo, String userID, int viewID) {
        if (photoRequest != null && photoRequest.size() > 0) {
            photoRequest.remove(0);
        }
        processPhotoResponse(photo.getPath(), userID, Contact.TYPE_GROUP);
    }

    @Override
    public void onFailureGetGroupPhotoRequest(Object error, String userID) {
        if (photoRequest != null && photoRequest.size() > 0) {
            photoRequest.remove(0);
        }
        processPhotoError(userID, Contact.TYPE_GROUP);
    }

    private void processPhotoResponse(final String path, String userID, int contactType) {
        Log.d("homecnt", "processPhotoResponse repo");

        List<Contact> modifiedList;
        //store photo path in realm
        switch (contactType) {
            case TYPE_CIRCLE_USER:
            default:
                usersDb.setPathAvatarToUser(Integer.parseInt(userID), path);
                modifiedList = allCircleUsersList;
                break;
            case TYPE_USER_CIRCLE:
                usersDb.setPathAvatarToUser(Integer.parseInt(userID), path);
                modifiedList = familyContactsList;
                break;
            case Contact.TYPE_GROUP:
                UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                userGroupsDb.setUserGroupAvatarPath(Integer.parseInt(userID), path);
                modifiedList = groupsList;
                break;
            case Contact.TYPE_DYNAMIZER:
                userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                userGroupsDb.setGroupDynamizerAvatarPath(Integer.parseInt(userID), path);
                modifiedList = dynamizersList;
                break;
        }

        if (listener instanceof ContactsCallback) {
            for (Contact contact : modifiedList) {
                if (contact != null && String.valueOf(contact.getId()).equals(userID)) {
                    contact.setPath(path);
                }
            }
            if (photoRequest.size() == 0) {
                onFinishedLoadingContacts(true);
            }

        } else if (listener instanceof HomeCallback) {
            ((HomeCallback) listener).onUserPictureLoaded(Integer.parseInt(userID), path);
        } else if (listener instanceof AddContactCallback) {
            if(userAddedContact==null)return;
            userAddedContact.setPath(path);
            ((AddContactCallback) listener).onResponseAddUser(userAddedContact);
        }

    }

    private void processPhotoError(String userID, int contactType) {
        if (listener instanceof ContactsCallback) {
            List<Contact> modifiedList;
            switch (contactType) {
                case TYPE_CIRCLE_USER:
                default:
                    modifiedList = allCircleUsersList;
                    break;
                case TYPE_USER_CIRCLE:
                    modifiedList = familyContactsList;
                    break;
                case Contact.TYPE_GROUP:
                    modifiedList = groupsList;
                    break;
                case Contact.TYPE_DYNAMIZER:
                    modifiedList = dynamizersList;
                    break;
            }
            for (Contact contact : modifiedList) {
                if (String.valueOf(contact.getId()).equals(userID)) {
                    contact.setPath("placeholder");
                    break;
                }
            }
            if (photoRequest.size() == 0) {
                onFinishedLoadingContacts(false);
            }

        } else if (listener instanceof HomeCallback) {
            ((HomeCallback) listener).onUserPictureFail(Integer.parseInt(userID));
        }
    }

    public void shareMediaToOneContact(ArrayList<Integer> mediaIdArrayList, int contactId) {
        ArrayList<Integer> idContents = getIdContentArrayFromId(mediaIdArrayList);
        String metadataTipus = getMetadataTipusArrayFromId(mediaIdArrayList);

        UserPreferences userPreferences = new UserPreferences();
        ChatMessageRest chatMessageRest = new ChatMessageRest();

        chatMessageRest.setIdAdjuntContents(OtherUtils.convertIntegersToRealmList(idContents));
        chatMessageRest.setIdUserTo(contactId);
        chatMessageRest.setIdUserFrom(userPreferences.getUserID());
        chatMessageRest.setMetadataTipus(metadataTipus);

        SendMessageRequest sendMessageRequest = new SendMessageRequest(renewTokenListener, chatMessageRest);
        sendMessageRequest.addOnOnResponse(this);
        sendMessageRequest.doRequest(userPreferences.getAccessToken());
    }

    public void shareMediaToManyContacts(ArrayList<Integer> mediaIdArrayList,
                                         List<Integer> contactIdArrayList,
                                         List<Integer> contactIdChatArrayList) {
        ArrayList<Integer> idContents = getIdContentArrayFromId(mediaIdArrayList);
        String metadataTipus = getMetadataTipusArrayFromId(mediaIdArrayList);

        UserPreferences userPreferences = new UserPreferences();
        ChatMessageMulticastRest chatMessageMulticastRest = new ChatMessageMulticastRest();
        chatMessageMulticastRest.setIdAdjuntContents(convertIntegers(idContents));
        chatMessageMulticastRest.setIdUserToList(contactIdArrayList != null ?
                convertIntegers(contactIdArrayList) : new int[0]);
        chatMessageMulticastRest.setIdChatToList(contactIdChatArrayList != null ?
                convertIntegers(contactIdChatArrayList) : new int[0]);
        chatMessageMulticastRest.setIdUserFrom(userPreferences.getUserID());
        chatMessageMulticastRest.setMetadataTipus(metadataTipus);
        chatMessageMulticastRest.setText("");
        SendMessageMulticastRequest sendMessageMulticastRequest = new SendMessageMulticastRequest(
                renewTokenListener, chatMessageMulticastRest);
        sendMessageMulticastRequest.addOnOnResponse(this);
        sendMessageMulticastRequest.doRequest(userPreferences.getAccessToken());
    }

    private ArrayList<Integer> getIdContentArrayFromId(ArrayList<Integer> idArray) {
        GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
        Integer[] arr = idArray.toArray(new Integer[0]);
        ArrayList<Integer> arrayIds = galleryDb.getIdContentFromIds(arr);
        return new ArrayList<>(arrayIds);
    }

    private String getMetadataTipusArrayFromId(ArrayList<Integer> idArray) {
        GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
        Integer[] arr = (Integer[]) idArray.toArray(new Integer[0]);
        ArrayList<String> metadataTipus = new ArrayList<>(galleryDb.getMetadataTipusFromArray(arr));
        return android.text.TextUtils.join(",", metadataTipus);
    }


    @Override
    public void onResponseDeleteCircleRequest(int idUserToUnlink) {
        for (Contact contact : allCircleUsersList) {
            if (idUserToUnlink == contact.getId()) {
                allCircleUsersList.remove(contact);
                break;
            }
        }
        for (Contact contact : familyContactsList) {
            if (idUserToUnlink == contact.getId()) {
                familyContactsList.remove(contact);
                break;
            }
        }

        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.deleteCircleUser(idUserToUnlink);

        if (listener != null) {
            listener.onRemoveContact(true);
        }
    }

    @Override
    public void onFailureDeleteCircleRequest(Object error, int idUserToUnlink) {
        if (listener != null) {
            listener.onRemoveContact(false);
        }
    }

    @Override
    public void onResponseExitCircleRequest(int idUserToUnlink, int circleId) {

        for (Contact contact : allCircleUsersList) {
            if (idUserToUnlink == contact.getId()) {
                allCircleUsersList.remove(contact);
                break;
            }
        }
        for (Contact contact : familyContactsList) {
            if (idUserToUnlink == contact.getId()) {
                familyContactsList.remove(contact);
                break;
            }
        }

        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.deleteUserCircle(circleId);
        usersDb.deleteCircleUser(idUserToUnlink);


        if (listener != null) {
            listener.onRemoveContact(true);
        }
    }

    @Override
    public void onFailureExitCircleRequest(Object error, int idUserToUnlink, int circleId) {
        if (listener != null) {
            listener.onRemoveContact(false);
        }
    }

    @Override
    public void onResponseSendMessageRequest(ChatMessagesSentResponse responseBody) {
        if (listener != null && listener instanceof ContactsCallback) {
            ((ContactsCallback) listener).onMediaShared(true);
        }
        for (int userMessageId : responseBody.getUserMessageId()) {
            GetMessageRequest getMessageRequest = new GetMessageRequest(renewTokenListener,
                    String.valueOf(userMessageId));
            getMessageRequest.addOnOnResponse(this);
            getMessageRequest.doRequest(new UserPreferences().getAccessToken());
        }

        int i = 0;
        for (int chatMessageId : responseBody.getChatMessageId()) {
            GetGroupMessageRequest getGroupMessageRequest = new GetGroupMessageRequest(renewTokenListener,
                    String.valueOf(responseBody.getChatId().get(i)), String.valueOf(chatMessageId));
            getGroupMessageRequest.addOnOnResponse(this);
            getGroupMessageRequest.doRequest(new UserPreferences().getAccessToken());
            i++;
        }
    }

    @Override
    public void onResponseSendMessageRequest(ChatMessageSentResponse responseBody) {
        if (listener != null && listener instanceof ContactsCallback) {
            ((ContactsCallback) listener).onMediaShared(true);
        }
        GetMessageRequest getMessageRequest = new GetMessageRequest(renewTokenListener, String.valueOf(responseBody.getId()));
        getMessageRequest.addOnOnResponse(this);
        getMessageRequest.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void onFailureSendMessageRequest(Object error) {
        if (listener != null && listener instanceof ContactsCallback) {
            ((ContactsCallback) listener).onMediaShared(false);
        }
    }

    @Override
    public void onResponseGetMessageRequest(ChatMessageRest chatMessageRest) {

        if (chatMessageRest.getMetadataTipus().toLowerCase().contains("video") && chatMessageRest.getMetadataAdjuntContents().isEmpty()){
            ArrayList<String> x = new ArrayList<>();
            x.add("video");
            chatMessageRest.setMetadataAdjuntContents(x);

        }
        new UserMessageDb(MyApplication.getAppContext()).saveChatMessageRest(chatMessageRest);
    }

    @Override
    public void onFailureGetMessageRequest(Object error) {

    }

    @Override
    public void onResponseGetGroupMessageRequest(GroupMessageRest groupMessageRest) {
        new GroupMessageDb(MyApplication.getAppContext()).saveGroupMessageRest(groupMessageRest);
    }

    @Override
    public void onFailureGetGroupMessageRequest(Object error) {

    }

    public interface Callback {
        void onCirclesLoaded(List<Contact> contactList);
        void onRemoveContact(boolean ok);
        void setEmptyText(int filterKind);
    }

    public interface ContactsCallback extends Callback {
        void onUserPictureLoaded();
        void onUserPictureFail();
        void onMediaShared(boolean ok);
    }

    public interface HomeCallback extends Callback {
        void onUserPictureLoaded(int id, String path);
        void onUserPictureFail(int id);
    }

    public interface AddContactCallback extends Callback {
        void onResponseGenerateUserCode(String code);
        void onResponseGenerateUserCodeError(Object error);
        void onResponseAddUser(Contact contact);
        void onResponseAddUserError(Object error);
    }

}
