package cat.bcn.vincles.mobile.UI.Chats;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;

import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;

public interface ChatFragmentView {

    boolean isLanguageCatalan();

    void showMessages(List<ChatElement> elementsList, SparseArray<Contact> users);
    void updateUsers(SparseArray<Contact> users);
    void reloadMessagesAdapter();
    void setChatInfo(String name, String photo);
    void setChatDynamizer(int id, String photo);

    void showLoadingMessages();
    void hideLoadingMessages();

    void showWaitDialog();
    boolean isShowingImageErrorDialog();
    void showImageErrorDialog();
    boolean isShowingWaitDialog();
    void hideWaitDialog();
    void showRetryDialog();
    void hideSendAgainDialog();

    void showBottomBar();
    void showWritingBottomBar();
    void showAudioBottomBar();

    void setAction(Drawable drawable);
    void launchPickVideoOrPhoto();

    void setAudioProgress(int progress, String time);

    Resources getResources();
}
