package cat.bcn.vincles.mobile.UI.Chats;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRepositoryModel;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageSentResponse;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.AddContentInTheGallery;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GalleryAddContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserMessagesRequest;
import cat.bcn.vincles.mobile.Client.Requests.SendMessageRequest;
import cat.bcn.vincles.mobile.Client.Requests.SetMessageWatchedRequest;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ChatRepositoryUser extends ChatRepository implements
        GetUserMessagesRequest.OnResponse,
        GetMessageRequest.OnResponse, SendMessageRequest.OnResponse, GalleryAddContentRequest.OnResponse, AddContentInTheGallery.OnResponse {

    private int fileId;
    private String mediaType;
    ArrayList<GetUser> chatUserList;
    RealmChangeListener chatUserChangeListener;

    ArrayList<String> sendingPaths;
    ArrayList<String> sendingMetadataList;

    public ChatRepositoryUser() {
        chatUserChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(@NonNull Object o) {
                notifyDataIfReady();
            }
        };
    }

    @SuppressLint("ValidFragment")
    public ChatRepositoryUser(final Callback listener, BaseRequest.RenewTokenFailed renewTokenListener,
                              int userId, final int chatId, Realm realm) {
        super(listener, renewTokenListener, userId, chatId, realm);

        chatUserChangeListener = new RealmChangeListener() {
            @Override
            public void onChange(@NonNull Object o) {
                notifyDataIfReady();
            }
        };
    }

    @Override
    protected void notifyDataIfReady() {
        if (listener != null && chatUserList != null) {
            GetUser chatUser = chatUserList.get(0);
            if (chatUser.isValid()) {
                listener.onChatInfoUpdated(chatUser.getName(), chatUser.getLastname(),
                        chatUser.getPhoto());
                listener.onUserListUpdated();
            }
        }
    }


    @Override
    public void onDestroy() {
        Log.d("lifecycleList", "ChatRepositpryUser onDestroy");

        if (chatUserList != null){
            for(GetUser user: chatUserList){
                user.removeAllChangeListeners();
            }
        }
        chatUserChangeListener = null;
        super.onDestroy();
    }

    @Override
    public ArrayList<GetUser> getUserListData(Realm realm) {
        if (chatUserList != null) return chatUserList;

        chatUserList = new ArrayList<>();
        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        GetUser chatUser = usersDb.findUser(chatId, realm);
        chatUserList.add(chatUser);
        chatUser.addChangeListener(chatUserChangeListener);

        return chatUserList;
    }

    public void getMessages(long timeStampStart, long timeStampEnd) {
        if (timeStampStart == 0 && timeStampEnd == 0) {
            UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
            ArrayList<ChatMessageRest> messages = userMessageDb.findAllMessagesBetween(userId, chatId);
            if (messages != null && messages.size() > 0) {
                handleObtainedUserMessages(messages);
            } else {
                GetUserMessagesRequest getUserMessagesRequest = new GetUserMessagesRequest(renewTokenListener, String.valueOf(chatId), timeStampStart, timeStampEnd);
                getUserMessagesRequest.addOnOnResponse(this);
                getUserMessagesRequest.doRequest(new UserPreferences().getAccessToken());
            }
        } else {
            GetUserMessagesRequest getUserMessagesRequest = new GetUserMessagesRequest(renewTokenListener, String.valueOf(chatId), timeStampStart, timeStampEnd);
            getUserMessagesRequest.addOnOnResponse(this);
            getUserMessagesRequest.doRequest(new UserPreferences().getAccessToken());
        }

    }

    public void getMessage(String idMessage) {
        GetMessageRequest getMessageRequest = new GetMessageRequest(renewTokenListener, idMessage);
        getMessageRequest.addOnOnResponse(this);
        getMessageRequest.doRequest(new UserPreferences().getAccessToken());
    }

    public void getLocalMessage(Long idMessage) {
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        ChatMessageRest chatMessageRest = userMessageDb.findMessage(idMessage);
        onLocalMessageReady(new ChatMessageRepositoryModel(chatMessageRest));
    }

    @Override
    public void sendMessage(ChatMessageRepositoryModel message) {
        ChatMessageRest chatMessageRest = new ChatMessageRest();
        chatMessageRest.setIdUserTo(message.getIdChat());
        chatMessageRest.setIdUserFrom(message.getIdSender());
        chatMessageRest.setText(message.getText());
        chatMessageRest.setIdAdjuntContents(OtherUtils.convertIntegersToRealmList(message.getIdAdjuntContents()));
        chatMessageRest.setMetadataTipus(message.getMetadataTipus());
        sendingPaths = message.getPathsAdjuntContents();
        sendingMetadataList = message.getMetadataAdjuntContents();

        SendMessageRequest sendMessageRequest = new SendMessageRequest(renewTokenListener,
                chatMessageRest);
        sendMessageRequest.addOnOnResponse(this);
        sendMessageRequest.doRequest(new UserPreferences().getAccessToken());
    }

    @Override
    public void saveFileToGallery(File file, String mediaType) {
        this.mediaType = mediaType;
        String imageFileName = file.getName();

        RequestBody fileB = RequestBody.create(MediaType.parse(mediaType), file);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFileName, fileB);

        String accessToken = new UserPreferences().getAccessToken();
        GalleryAddContentRequest galleryAddContentRequest = new GalleryAddContentRequest(
                renewTokenListener, body);
        galleryAddContentRequest.addOnOnResponse(this);
        galleryAddContentRequest.doRequest(accessToken);
    }

    @Override
    public void setMessageAsWatched(long messageId) {

        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        userMessageDb.setMessageWatched(messageId);
        Log.d("qwe","repo setMessageAsWatched");
        SetMessageWatchedRequest setMessageWatchedRequest = new SetMessageWatchedRequest(
                renewTokenListener, messageId);
        setMessageWatchedRequest.doRequest(new UserPreferences().getAccessToken());

        UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.setMessagesInfo(chatId, 0, new NotificationsDb(MyApplication.getAppContext())
                .getNumberUnreadMissedCallNotifications(chatId),
                Math.max(userMessageDb.getLastMessage(new UserPreferences().getUserID(), chatId),
                        new NotificationsDb(MyApplication.getAppContext())
                                .getLastMissedCallTime(chatId)));
        Log.d("unrd","ChatRepoUser, user:"+chatId+" totalNum:"
                +new UserMessageDb(MyApplication.getAppContext())
                .getTotalNumberMessages(userId, chatId));
    }

    @Override
    public void onResponseGetUserMessagesRequest(ArrayList<ChatMessageRest> messages,
                                                 String idUserSender) {
        if (messages != null && messages.size() > 0) {
            UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
            userMessageDb.saveChatMessageRestList(messages);
        }

        handleObtainedUserMessages(messages);
    }

    private void handleObtainedUserMessages(List<ChatMessageRest> messages) {
        if (listener != null) {
            ArrayList<ChatMessageRepositoryModel> chatMessageList = new ArrayList<>();
            for (ChatMessageRest chatMessageRest : messages) {
                chatMessageList.add(new ChatMessageRepositoryModel(chatMessageRest));
            }
            listener.messagesReceived(chatMessageList);
        }
    }

    @Override
    public void onFailureGetUserMessagesRequest(Object error, String idUserSender) {
        Log.d("mesr","onFailureGetUserMessagesRequest!!!!!");
    }

    @Override
    public void onResponseGetMessageRequest(ChatMessageRest chatMessageRest) {
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        chatMessageRest.setPathsAdjuntContents(sendingPaths);
        chatMessageRest.setMetadataAdjuntContents(sendingMetadataList);
        userMessageDb.saveChatMessageRest(chatMessageRest);
        onMessageReceived(new ChatMessageRepositoryModel(chatMessageRest));
    }

    @Override
    public void onFailureGetMessageRequest(Object error) {

    }

    @Override
    public void onResponseSendMessageRequest(ChatMessageSentResponse responseBody) {
        onMessageSent(responseBody.getId());
    }

    @Override
    public void onFailureSendMessageRequest(Object error) {
        onMessageError();
    }

    @Override
    public void onResponseGalleryAddContentRequest(JsonObject galleryAddedContent) {
        fileId = galleryAddedContent.getAsJsonObject().get("id").getAsInt();

        if (mediaType.toLowerCase().contains("audio")) {
            onFileSaved(-1, fileId, mediaType);
        } else {
            AddContentInTheGallery addContentInTheGallery = new AddContentInTheGallery(
                    renewTokenListener, fileId);
            addContentInTheGallery.addOnOnResponse(this);
            addContentInTheGallery.doRequest(new UserPreferences().getAccessToken());
        }

    }

    @Override
    public void onFailureGalleryAddContentRequest(Object error) {
        listener.onFileSaveToGalleryError();
    }

    @Override
    public void onResponseAddContentInTheGallery(int contentId) {
        onFileSaved(contentId, fileId, mediaType);
    }

    @Override
    public void onFailureAddContentInTheGallery(Object error) {
        listener.onFileSaveToGalleryError();
        //TODO en cas dérror enviar si falla la primera o la segona!
    }
}
