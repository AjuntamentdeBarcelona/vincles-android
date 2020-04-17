package cat.bcn.vincles.mobile.UI.Notifications;


import android.os.Bundle;

import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Model.NotificationAdapterModel;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import io.realm.RealmResults;

public interface NotificationsPresenterContract {

    ArrayList<NotificationAdapterModel> getNotificationsList();
    void deleteNotification(int notificationId);
    void notificationActionClicked(NotificationAdapterModel notificationRest);
    void onSaveInstanceState(Bundle outState);

}
