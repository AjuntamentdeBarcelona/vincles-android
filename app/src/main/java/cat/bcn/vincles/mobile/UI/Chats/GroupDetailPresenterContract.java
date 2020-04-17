package cat.bcn.vincles.mobile.UI.Chats;


import android.os.Bundle;

public interface GroupDetailPresenterContract {


    void onCreateView();
    void onSaveInstanceState(Bundle outState);

    void loadData();
    void loadContactPicture(int contactId);

    void stoppedShowingErrorDialog();
    void stoppedShowingMessageDialog();

    void clickedInvite(int id, int type);

}
