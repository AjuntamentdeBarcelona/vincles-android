package cat.bcn.vincles.mobile.UI.Chats;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRepositoryModel;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetContentInMyGallery;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import io.realm.Realm;

public abstract class ChatRepository extends Fragment implements GetUserPhotoRequest.OnResponse, GetContentInMyGallery.OnResponse {

    protected Callback listener;
    BaseRequest.RenewTokenFailed renewTokenListener;
    int userId, chatId;

    public Realm realm;

    public ChatRepository(){}

    public ChatRepository(Callback listener, BaseRequest.RenewTokenFailed renewTokenListener,
                          int userId, int chatId, Realm realm) {
        this.userId = userId;
        this.chatId = chatId;
        this.listener = listener;
        this.renewTokenListener = renewTokenListener;
        this.realm = realm;
        Bundle arguments = new Bundle();
        arguments.putInt("chatId", chatId);
        arguments.putInt("userId", userId);
        setArguments(arguments);
    }

    public static ChatRepository newInstance(Callback listener,
                                             BaseRequest.RenewTokenFailed renewTokenListener,
                                             int userId, int chatId, boolean isGroupChat, boolean isDynamizer, Realm realm) {
        if (isGroupChat) return new ChatRepositoryGroup(listener, renewTokenListener, userId, chatId, isDynamizer, realm);
        return new ChatRepositoryUser(listener, renewTokenListener, userId, chatId, realm);
    }

    public void setListeners(Callback listener, BaseRequest.RenewTokenFailed renewTokenListener) {
        this.listener = listener;
        this.renewTokenListener = renewTokenListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getArguments() != null) {
            chatId = getArguments().getInt("chatId");
            userId = getArguments().getInt("userId");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public abstract ArrayList<GetUser> getUserListData(Realm realm);

    public abstract void getMessages(long timeStampStart, long timeStampEnd);

    public abstract void getMessage(String idMessage);

    public abstract void getLocalMessage(Long idMessage);

   // public abstract void getMediaFiles(long messageId, int[] mediaId);

   // public abstract void getMediaFile(long messageId, int position);

    public abstract void sendMessage(ChatMessageRepositoryModel message);

    public abstract void saveFileToGallery(File file, String mediaType);

    public abstract void setMessageAsWatched(long messageId);

    public void setMessagesAsWatched(ArrayList<ChatMessage> messages) {
        for (ChatMessage message : messages) {
            setMessageAsWatched(message.getId());
        }
    }

    protected void onMessagesReceived(ArrayList<ChatMessageRepositoryModel> chatMessageList) {
        if (listener != null) listener.messagesReceived(chatMessageList);
    }

    protected void onMessageReceived(ChatMessageRepositoryModel chatMessage) {
        if (listener != null) listener.messageReceived(chatMessage);
    }

    protected void onLocalMessageReady(ChatMessageRepositoryModel chatMessage) {
        if (listener != null) listener.onLocalMessageReady(chatMessage);
    }

    protected void onMessageSent(int messageId) {
        if (listener != null) listener.onMessageSent(messageId);
    }

    protected void onMessageError() {
        if (listener != null) listener.onSendMessageError();
    }

    protected void onFailureContentRequest(Object error) {
        if (listener != null) listener.onFileError(error);
    }

    protected void onFileSaved(int galleryId, int contentId, String mediaType) {
        if (listener != null) listener.onFileSavedToGallery(galleryId, contentId, mediaType);
    }

    protected void onFileReceived(long messageID, int contentID, String filePath, String mediaType) {
        if (listener != null) listener.onFileReceived(messageID, contentID, filePath, mediaType);
    }

    protected void onUserListUpdated() {
        if (listener != null) listener.onUserListUpdated();
    }

    protected void notifyDataIfReady(){}

    protected int getDynamizerId() {
        return -1;
    }

    protected Dynamizer getDynamizer() {
        return null;
    }

    public void getUserMeData() {
        if (listener != null) {
            notifyDataIfReady();
            GetUser userMe = new GetUser();
            UserPreferences preferences = new UserPreferences();
            userMe.setName(preferences.getName());
            userMe.setLastname(preferences.getLastName());
            userMe.setPhoto(preferences.getUserAvatar());

            listener.onUserMeReceived(userMe);
        }
    }
    public void loadUserPhoto(int contactId) {
        Log.d("usph", "repo loadUserPhoto");
        String accessToken = new UserPreferences().getAccessToken();
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(renewTokenListener, String.valueOf(contactId));
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(accessToken);
    }

    @Override
    public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
        Log.d("usph", "repo onResponseGetUserPhotoRequest usID:"+userID);
        String path = photo.getPath();
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.setPathAvatarToUser(Integer.parseInt(userID), path);
        if (listener != null) {
            listener.onUserPhotoDownloaded();
        }
    }

    public void saveFileToRealmGallery(int contentId) {
        GetContentInMyGallery getContentInMyGallery = new GetContentInMyGallery(
                null, contentId);
        getContentInMyGallery.addOnOnResponse(this);
        getContentInMyGallery.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {

    }

    @Override
    public void onResponseGetContentInMyGallery(GalleryContentRealm galleryContent) {
        GalleryDb galleryDb = new GalleryDb(MyApplication.getAppContext());
        galleryDb.insertContent(new GalleryContentRealm(galleryContent.getId(),
                galleryContent.getIdContent(), galleryContent.getMimeType(),
                new UserPreferences().getUserID(), galleryContent.getInclusionTime()));
    }

    @Override
    public void onFailureGetContentInMyGallery(Object error) {

    }

    public void onLogout() {}


    public interface Callback {
        void messagesReceived(ArrayList<ChatMessageRepositoryModel> repositoryMessageList);
        void messageReceived(ChatMessageRepositoryModel message);
        void onLocalMessageReady(ChatMessageRepositoryModel message);
        void onFileReceived(String messageID, String filePath);
        void onFileError(Object error);
        void onMessageSent(int messageId);
        void onSendMessageError();
        void onFileSavedToGallery(int galleryId, int contentId, String mediaType);
        void onFileReceived(long messageID, int contentID, String filePath, String mediaType);
        void onFileSaveToGalleryError();
        void onUserMeReceived(GetUser userMe);
        void onUserListUpdated();
        void onChatInfoUpdated(String name, String lastname, String photo);
        void onChatDynamizerUpdated(int id, String photo);
        void onUserPhotoDownloaded();
    }
}
