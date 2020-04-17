package cat.bcn.vincles.mobile.UI.Notifications;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRepositoryModel;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.UI.Chats.ChatRepositoryGroup;
import cat.bcn.vincles.mobile.UI.Chats.ChatRepositoryUser;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import io.realm.Realm;
import io.realm.RealmResults;

public class NotificationsRepository {

    protected Callback listener;
    BaseRequest.RenewTokenFailed renewTokenListener;
    NotificationsDb notificationsDb;
    Realm realm;

    public NotificationsRepository(Callback listener, BaseRequest.RenewTokenFailed renewTokenListener, Realm realm) {
        this.listener = listener;
        this.renewTokenListener = renewTokenListener;
        notificationsDb = new NotificationsDb(MyApplication.getAppContext());
        this.realm = realm;
    }

    RealmResults<NotificationRest> getNotificationsList() {
        return notificationsDb.findShownNotificationsAsync(realm);
    }

    void deleteNotification(int notificationId) {
        notificationsDb.setNotificationShouldBeShown(notificationId, false);
    }


    public interface Callback {

    }
}
