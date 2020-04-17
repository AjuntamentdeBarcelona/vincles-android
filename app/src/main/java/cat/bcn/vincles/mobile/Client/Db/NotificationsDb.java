package cat.bcn.vincles.mobile.Client.Db;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.MeetingRealm;
import cat.bcn.vincles.mobile.Client.Model.MeetingUserInfoRest;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class NotificationsDb extends BaseDb {

    public final static String MEETING_REMINDER_NOTIFICATION_TYPE = "MEETING_REMINDER_NOTIFICATION_TYPE";
    public final static String MISSED_CALL_NOTIFICATION_TYPE = "MISSED_CALL_NOTIFICATION_TYPE";

    public NotificationsDb(Context context) {
        super(context);
    }

    @Override
    public void dropTable() {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.where(NotificationRest.class).findAll().deleteAllFromRealm();
                }
            });
        }
    }

    public ArrayList<NotificationRest> findAllUnProcessedNotifications() {

        try (Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<NotificationRest> notificationsList = realmInstance.where(NotificationRest.class)
                    .equalTo("processed",false)
                    .sort("creationTime", Sort.ASCENDING)
                    .findAll();
            if (notificationsList==null) return new ArrayList<>();
            return new ArrayList<>(realmInstance.copyFromRealm(notificationsList));
        }

    }
    //Closed on fragment/activity
    public RealmResults<NotificationRest> findShownNotificationsAsync(Realm realm) {
        return realm.where(NotificationRest.class)
                .equalTo("processed",true)
                .equalTo("shouldBeShown",true)
                .sort("creationTime", Sort.DESCENDING)
                //.findAll();
                .findAllAsync();
    }

    public int findUnwatchedNotificationsNumber() {
        try (Realm realmInstance = Realm.getDefaultInstance()){

            RealmResults<NotificationRest> notificationRests = realmInstance
                    .where(NotificationRest.class)
                    .equalTo("processed",true)
                    .equalTo("shouldBeShown",true)
                    .equalTo("watched",false)
                    .findAll();
            if (notificationRests==null || notificationRests.size()==0)return 0;
            return realmInstance.copyFromRealm(notificationRests).size();

        }

    }

    public void setMessageNotificationWatched(final long idMessage) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest noti = realm
                            .where(NotificationRest.class)
                            .equalTo("idMessage",idMessage)
                            .findFirst();
                    if(noti != null){
                        noti.setWatched(true);
                        noti.setShouldBeShown(false);
                    }

                }
            });
        }
    }

    public void saveNotificationRestList(final List<NotificationRest> notifications) {
        try (Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(notifications);
                }
            });
        }
    }

    public void saveNotification(final NotificationRest notificationRest) {
        try (Realm realmInstance = Realm.getDefaultInstance()) {
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.copyToRealmOrUpdate(notificationRest);
                }
            });
        }
    }

    public void saveMissedCallNotification(long notificationTime, int idUser) {
        NotificationRest notificationRest = new NotificationRest(
                new UserPreferences().notificationUpperIdGetAndSubtract(),
                NotificationsDb.MISSED_CALL_NOTIFICATION_TYPE, notificationTime,
                true, idUser, -1, -1,
                -1, -1, -1, "","", -1);
        notificationRest.setShouldBeShown(true);
        notificationRest.setWatched(false);

        saveNotification(notificationRest);
    }

    public int getNumberUnreadMissedCallNotifications(int userId) {

        try (Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<NotificationRest> notificationRest = realmInstance.where(NotificationRest.class)
                    .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                    .equalTo("idUser", userId)
                    .equalTo("watched", false)
                    .findAll();
            if (notificationRest==null) return 0;
            return realmInstance.copyFromRealm(notificationRest).size();
        }
    }

    public ArrayList<NotificationRest> getUnreadMissedCallNotifications() {
        try (Realm realmInstance = Realm.getDefaultInstance()){
           RealmResults<NotificationRest> notificationRests = realmInstance.where(NotificationRest.class)
                   .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                   .equalTo("watched", false)
                   .findAll();
            if (notificationRests==null)return new ArrayList<>();
            return new ArrayList<>(realmInstance.copyFromRealm(notificationRests));

        }
    }

    public ArrayList<NotificationRest> getMissedCallNotifications(int userId) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            RealmResults<NotificationRest> notificationRests = realmInstance.where(NotificationRest.class)
                    .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                    .equalTo("idUser", userId)
                    .findAll();
            if (notificationRests==null)return new ArrayList<>();
            return new ArrayList<>(realmInstance.copyFromRealm(notificationRests));
        }
    }

    public long getLastMissedCallTime(int userId) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            NotificationRest call = realmInstance.where(NotificationRest.class)
                    .equalTo("type", MISSED_CALL_NOTIFICATION_TYPE)
                    .equalTo("idUser", userId)
                    .sort("creationTime", Sort.DESCENDING)
                    .findFirst();
            if (call==null) return 0;
            return realmInstance.copyFromRealm(call).getCreationTime();
        }
    }

    public void saveNotificationCloseMeetingAlert(MeetingRealm meetingRealm) {
        NotificationRest notificationRest = new NotificationRest(
                new UserPreferences().notificationUpperIdGetAndSubtract(),
                MEETING_REMINDER_NOTIFICATION_TYPE, meetingRealm.getDate() - 60*60*1000,
                true, meetingRealm.getHostId(), -1, -1,
                -1, -1, meetingRealm.getId(), "","", -1);
        notificationRest.setShouldBeShown(true);
        notificationRest.setWatched(false);

        saveNotification(notificationRest);
    }

    public void setNotificationToProcessedShown(final int notificationId, final boolean setShown) {

        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setProcessed(true);
                    notification.setShouldBeShown(setShown);
                }
            });
        }
    }

    public void setNotificationUserName(final int notificationId, final String name) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setUserName(name);
                }
            });
        }
    }

    public void setNotificationShouldBeShown(final int notificationId, final boolean shouldBeShown) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setShouldBeShown(shouldBeShown);
                }
            });
        }
    }

    public void setNotificationUserUserName(final int notificationId, final int idUser, final String userName) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setIdUser(idUser);
                    notification.setUserName(userName);
                }
            });
        }
    }

    public void setNotificationUserId(final int notificationId, final int idUser) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setIdUser(idUser);
                }
            });
        }
    }

    public void setNotificationChatInfo(final int notificationId, final int idChat, final String chatName) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setIdChat(idChat);
                    notification.setUserName(chatName);
                }
            });
        }
    }

    public void setNotificationChatInfoGroup(final int notificationId, final int idChat, final String chatName) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification =  realm.where(NotificationRest.class).equalTo("id", notificationId).findFirst();
                    if (notification==null)return;
                    notification.setIdChat(idChat);
                    notification.setUserName(chatName);
                }
            });
        }
    }


    /**
     * Of all notifications of new message from a user, only the last one has to be shown.
     * Method to set all newMessage notifications to hidden except the one with id:notificationId
     *
     * @param notificationId    notification that will be set to shown
     * @param idChat            user sender of the message
     */
    public void setMessageNotificationsNotShownExceptId(final String type, final int notificationId, final int idChat) {

        final String idChatField = type.equals("NEW_MESSAGE") ? "idUser" : "idChat";

        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<NotificationRest> notifications = realm.where(NotificationRest.class)
                            .equalTo("type", type)
                            .equalTo(idChatField, idChat)
                            .findAll();
                    if (notifications == null || notifications.size() == 0) return;
                    for (NotificationRest notification : notifications) {
                        if (notification.getId() == notificationId) {
                            notification.setShouldBeShown(true);
                        } else {
                            notification.setShouldBeShown(false);
                        }
                    }
                }
            });
        }
    }

    public long getLastNotificationTime() {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            NotificationRest call = realmInstance.where(NotificationRest.class)
                    .sort("creationTime", Sort.DESCENDING)
                    .findFirst();
            if (call==null) return 0;
            return realmInstance.copyFromRealm(call).getCreationTime();
        }
    }

    public void markAllAsRead() {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<NotificationRest> notificationsList = realm.where(NotificationRest.class)
                            .findAll();
                    for (NotificationRest notificationRest : notificationsList) {
                        notificationRest.setWatched(true);
                    }
                }
            });
        }
    }


    public void setNotificationWatched(final ChatMessage chatMessage) {
        try (Realm realmInstance = Realm.getDefaultInstance()){
            realmInstance.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    NotificationRest notification = realm.where(NotificationRest.class).equalTo("id", chatMessage.getNotificationId()).findFirst();
                    if (notification != null) {
                        notification.setWatched(true);
                    }
                    chatMessage.setNotificationId(-1);
                }
            });
        }
    }

    public ArrayList<NotificationRest> findAllProcessedNotifications() {
        // TODO CLOSE
        Realm realm = Realm.getDefaultInstance();
        RealmResults<NotificationRest> notificationsList = realm.where(NotificationRest.class)
                .equalTo("processed",true)
                .sort("creationTime", Sort.ASCENDING)
                .findAll();
        return new ArrayList<>(notificationsList);
    }
}
