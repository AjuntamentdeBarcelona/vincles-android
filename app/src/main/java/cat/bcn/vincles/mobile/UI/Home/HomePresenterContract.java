package cat.bcn.vincles.mobile.UI.Home;

import android.os.Bundle;

public interface HomePresenterContract {

    void getContacts(boolean needCallRest);
    void getContactPicture(int id, int type);
    void onCreateView();
    void updateNotificationsNumber();
}
