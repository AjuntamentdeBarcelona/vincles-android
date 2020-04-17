package cat.bcn.vincles.mobile.UI.Notifications;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cat.bcn.vincles.mobile.Client.Db.MeetingsDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.NotificationAdapterModel;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class NotificationsPresenter implements NotificationsPresenterContract,
        NotificationsRepository.Callback {


    MeetingsDb meetingsDb;
    UsersDb usersDb;
    UserGroupsDb groupsDb;

    private NotificationsFragmentView view;
    RealmResults<NotificationRest> notificationsRealm;
    ArrayList<NotificationAdapterModel> notifications;
    SparseArray<Contact> users = new SparseArray<>();
    SparseArray<GroupRealm> groups = new SparseArray<>();
    SparseArray<Dynamizer> dynamizers = new SparseArray<>();
    NotificationsRepository repository;
    RealmChangeListener<RealmResults<NotificationRest>> notificationsChangeListener;
    Realm realm;

    public NotificationsPresenter(BaseRequest.RenewTokenFailed listener, NotificationsFragmentView view,
                                  Bundle savedInstanceState, Realm realm) {
        this.view = view;
        this.realm = realm;
        repository = new NotificationsRepository(this, listener, this.realm);
        notifications = new ArrayList<>();
        meetingsDb = new MeetingsDb(MyApplication.getAppContext());
        usersDb = new UsersDb(MyApplication.getAppContext());
        groupsDb = new UserGroupsDb(MyApplication.getAppContext());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        notificationsRealm.removeAllChangeListeners();
    }

    public void onPause() {
        notificationsRealm.removeChangeListener(notificationsChangeListener);
        //notificationsRealm.removeAllChangeListeners();
    }

    public void onResume() {
        if (notificationsRealm != null && notificationsChangeListener != null) {
            notificationsRealm.addChangeListener(notificationsChangeListener);
        }
        onNotificationListUpdated();
    }

    @Override
    public ArrayList<NotificationAdapterModel> getNotificationsList() {
        if (notificationsRealm == null) {
            notificationsRealm = repository.getNotificationsList();
            notificationsChangeListener = new RealmChangeListener<RealmResults<NotificationRest>>() {
                @Override
                public void onChange(@NonNull RealmResults<NotificationRest> notificationRests) {
                    onNotificationListUpdated();
                }
            };
            notificationsRealm.addChangeListener(notificationsChangeListener);
        }
        return notifications;
    }

    private void onNotificationListUpdated() {
        notifications.clear();

        for (NotificationRest notificationRest : notificationsRealm) {
            NotificationAdapterModel notification = new NotificationAdapterModel(notificationRest);
            boolean addNotification = true;
            switch (notificationRest.getType()) {

                //user, meeting time
                case "MEETING_INVITATION_EVENT":
                case "MEETING_ACCEPTED_EVENT":
                case "MEETING_REJECTED_EVENT":
                case "MEETING_INVITATION_REVOKE_EVENT":
                case "MEETING_DELETED_EVENT":
                case NotificationsDb.MEETING_REMINDER_NOTIFICATION_TYPE:
                case "MEETING_CHANGED_EVENT": //time not needed
                    MeetingRealm meetingRealm = meetingsDb.findMeeting(
                            notificationRest.getIdMeeting());
                    if (notificationRest.getType().equals(
                            NotificationsDb.MEETING_REMINDER_NOTIFICATION_TYPE)) {
                        Log.d("dfg", "df");
                        Resources resources = view.getResources();
                        Locale locale = resources.getConfiguration().locale;
                        RealmList<Integer> guestIds = meetingRealm.getGuestIDs();
                        notification.setMeetingReminderText(meetingRealm.getDescription() + "\n"
                                + DateUtils.getNotificationFormatedTime(meetingRealm.getDate(), locale) + "\n"
                                + OtherUtils.getDuration(meetingRealm.getDuration(), resources) + "\n"
                                + getListOfUsers(guestIds, getUserList(guestIds), resources));

                    }
                    notification.setMeetingDate(meetingRealm.getDate());
                    notification.setShouldShowButton(meetingRealm.isShouldShow() ||
                            notificationRest.getType().equals("MEETING_REJECTED_EVENT")
                            || notificationRest.getType().equals("MEETING_DELETED_EVENT"));

                    //user
                case "USER_LINKED":
                case "USER_UNLINKED":
                case "USER_LEFT_CIRCLE":
                case "NEW_MESSAGE":
                    Dynamizer dynamizer = groupsDb.findDynamizerUnmanaged(notificationRest.getIdUser());
                    Contact contact;
                    if (dynamizer == null) {
                        GetUser getUser = usersDb.findUserUnmanaged(notificationRest.getIdUser());
                        contact = createContactFromGetUser(getUser);
                        if (contact != null) {
                            users.put(getUser.getId(), contact);

                        }
                    } else {
                        contact = createContactFromDynamizer(dynamizer);
                        if (contact != null) {
                            users.put(dynamizer.getId(), contact);
                        }
                    }
                    if (contact != null) {
                        notification.setIdUser(contact.getId());
                    }
                    if ("NEW_MESSAGE".equals(notificationRest.getType())) {
                        if (contact != null) {
                            notification.setNumberUnreadMessages(contact.getNumberNotifications());
                        }
                        if (notification.getNumberUnreadMessages() == 0) addNotification = false;
                    }
                    if ("NEW_MESSAGE".equals(notificationRest.getType())
                            || "USER_LINKED".equals(notificationRest.getType())) {
                        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
                        if (contact != null) {
                            notification.setShouldShowButton(usersDb.userCircleORCircleUserAreNOTnull(contact.getId()));
                        }

                    }
                    break;

                //group
                case "ADDED_TO_GROUP":
                case "NEW_CHAT_MESSAGE": //tambe num missatges
                case "REMOVED_FROM_GROUP":
                    GroupRealm group = groupsDb.getGroupFromIdChatUnmanaged(notification.getIdChat());
                    Dynamizer dynamizer1 = null;
                    if (group != null) {
                        notification.setIdUser(group.getIdGroup());
                        groups.put(group.getIdGroup(), group);
                    } else {
                        dynamizer1 = groupsDb.findDynamizerFromChatIdUnmanaged(notification.getIdChat());
                        if (dynamizer1 != null) {
                            notification.setIdUser(dynamizer1.getId());
                            users.put(dynamizer1.getId(), createContactFromDynamizer(dynamizer1));
                        }

                    }
                    if ("NEW_CHAT_MESSAGE".equals(notificationRest.getType())) {
                        if (group != null) {
                            notification.setNumberUnreadMessages(group.getNumberUnreadMessages());
                            notification.setShouldShowButton(group.isShouldShow());
                            if (notification.getNumberUnreadMessages() == 0)
                                addNotification = false;
                        } else {
                            if (dynamizer1 != null) {
                                notification.setNumberUnreadMessages(dynamizer1.getNumberUnreadMessages());
                                notification.setShouldShowButton(dynamizer1.isShouldShow());
                            }
                            if (notification.getNumberUnreadMessages() == 0)
                                addNotification = false;
                        }
                    } else if ("ADDED_TO_GROUP".equals(notificationRest.getType())
                            && group != null) {
                        notification.setShouldShowButton(group.isShouldShow());
                    }
                    break;

                case "GROUP_USER_INVITATION_CIRCLE":
                case NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE:
                    GetUser getUser = usersDb.findUserUnmanaged(notificationRest.getIdUser());
                    int theId = -1;
                    Contact con = null;
                    if (getUser == null) {
                        Dynamizer dyn = usersDb.findDynamizerUnmanaged(notificationRest.getIdUser());
                        if (dyn != null) {
                            theId = dyn.getId();
                            con = createContactFromDynamizer(dyn);
                        }

                    } else {
                        theId = getUser.getId();
                        con = createContactFromGetUser(getUser);
                    }
                    if (theId != -1 && con != null)
                        users.put(theId, con);


                    if (NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE.equals(notificationRest.getType()) && theId != -1) {
                        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
                        notification.setShouldShowButton(usersDb.userCircleORCircleUserAreNOTnull(theId));
                    }

                    break;


            }
            if (addNotification) notifications.add(notification);
        }

        view.updateList();
    }

    private Contact createContactFromGetUser(GetUser getUser) {
        if (getUser == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.setId(getUser.getId());
        contact.setName(getUser.getName());
        contact.setLastname(getUser.getLastname());
        contact.setPath(getUser.getPhoto());
        contact.setNumberNotifications(getUser.getNumberUnreadMessages());
        Log.d("dynpht", "noti presenter, createContactFromGetUser photo:" + contact.getPath() + "name:" + getUser.getName());
        return contact;
    }

    private Contact createContactFromDynamizer(Dynamizer dynamizer) {
        if (dynamizer == null) {
            return null;
        }
        Contact contact = new Contact();
        contact.setId(dynamizer.getId());
        contact.setName(dynamizer.getName());
        contact.setLastname(dynamizer.getLastname());
        contact.setPath(dynamizer.getPhoto());
        Log.d("dynpht", "noti presenter, createContactFromDynamizer photo:" + contact.getPath());
        contact.setNumberNotifications(dynamizer.getNumberUnreadMessages());
        return contact;
    }

    private String getListOfUsers(List<Integer> userIds, SparseArray<GetUser> usersList, Resources resources) {
        if (userIds.size() == 0) return "";
        int userMe = new UserPreferences(MyApplication.getAppContext()).getUserID();
        StringBuilder usersString = new StringBuilder();
        boolean putComma = false;
        for (Integer userId : userIds) {
            if (putComma) {
                usersString.append(", ");
            } else {
                putComma = true;
            }
            if (userMe == userId) {
                usersString.append(resources.getString(R.string.chat_username_you));
            } else {
                usersString.append(usersList.get(userId).getName());
            }
        }

        if (usersString.length() > 0) return resources.getString(R.string.calendar_date_guests,
                (usersString + "."));
        return "";
    }

    private SparseArray<GetUser> getUserList(List<Integer> userIds) {
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        SparseArray<GetUser> list = new SparseArray<>();
        for (int id : userIds) {
            list.put(id, usersDb.findUserUnmanaged(id));
        }
        return list;
    }

    @Override
    public void deleteNotification(int notificationId) {
        repository.deleteNotification(notificationId);
    }

    @Override
    public void notificationActionClicked(NotificationAdapterModel notificationRest) {
        if (view == null) return;
        switch (notificationRest.getType()) {
            case "USER_LINKED":
                view.openUserChat(notificationRest.getIdUser());
                break;
            case "USER_UNLINKED":
                view.openContacts();
                break;
            case "USER_LEFT_CIRCLE":
                view.openContacts();
                break;
            case "NEW_MESSAGE":
                view.openUserChat(notificationRest.getIdUser());
                break;
            case "MEETING_INVITATION_EVENT":
                view.openMeeting(notificationRest.getIdMeeting());
                break;
            case NotificationsDb.MEETING_REMINDER_NOTIFICATION_TYPE:
                view.openMeeting(notificationRest.getIdMeeting());
                break;
            case "MEETING_CHANGED_EVENT":
                view.openMeeting(notificationRest.getIdMeeting());
                break;
            case "MEETING_ACCEPTED_EVENT":
                view.openMeeting(notificationRest.getIdMeeting());
                break;
            case "MEETING_REJECTED_EVENT":
                view.openMeeting(notificationRest.getIdMeeting());
                break;
            case "MEETING_INVITATION_REVOKE_EVENT":
                view.openCalendarDay(notificationRest.getMeetingDate());
                break;
            case "MEETING_DELETED_EVENT":
                view.openCalendarDay(notificationRest.getMeetingDate());
                break;
            case "ADDED_TO_GROUP":
                view.openGroupChat(notificationRest.getIdChat());
                break;
            case "NEW_CHAT_MESSAGE":
                view.openGroupChat(notificationRest.getIdChat());
                break;
            case "REMOVED_FROM_GROUP":
                view.openContactsGroups();
                break;
            case NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE:
                view.openUserChat(notificationRest.getIdUser());
                break;
            case "GROUP_USER_INVITATION_CIRCLE":
                view.openAddToCircles(notificationRest.getCode());
                break;

        }

    }

    public SparseArray<Contact> getUsers() {
        return users;
    }

    public SparseArray<GroupRealm> getGroups() {
        return groups;
    }

    public SparseArray<Dynamizer> getDynamizers() {
        return dynamizers;
    }

    void deleteListeners() {
        if (notificationsRealm != null)
            notificationsRealm.removeAllChangeListeners();
    }
}
