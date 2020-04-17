package cat.bcn.vincles.mobile.UI.Chats;


import android.net.Uri;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;

public interface ChatPresenterContract {

    void loadData();
    void onScrolledToTop();

    void onCreateView();

    void onNewMessageReceived(Long messageId);

    void onClickText();
    void onClickSendMessage(String text);
    void onClickCancelMessage();
    void onClickAudio();
    void onClickSendAudio();
    void onClickFileShare();
    void onSendSystemFile(String path, String mimeType);

    void onLogout();

    GetUser getOtherUserInfoIfNotGroup();

    int getDynamizerChatId();

    void retrySendMessage();
    void cancelRetrySendMessage();

    void onSaveMediaFile(String path, int type);

    void onSaveInstanceState(Bundle outState);
    void sendFileMessage(int[] idAdjuntContents, ArrayList<String> paths,
                         ArrayList<String> metadatas, boolean isAudio);
}
