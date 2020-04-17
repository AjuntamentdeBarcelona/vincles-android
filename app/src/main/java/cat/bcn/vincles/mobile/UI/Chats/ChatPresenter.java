package cat.bcn.vincles.mobile.UI.Chats;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.iceteck.silicompressorr.SiliCompressor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.NotificationsDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRepositoryModel;
import cat.bcn.vincles.mobile.Client.Model.Dynamizer;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.NotificationRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatElement;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessage;
import cat.bcn.vincles.mobile.UI.Chats.Model.ChatMessageMedia;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Notifications.NotificationsRepository;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;
import io.realm.Realm;
import io.realm.RealmResults;

public class ChatPresenter implements ChatPresenterContract, ChatRepository.Callback, GetUserPhotoRequest.OnResponse {

    static final int MEDIA_NOT_SAVING = -1;
    static final int MEDIA_PHOTO = 0;
    static final int MEDIA_VIDEO = 1;
    static final int MEDIA_AUDIO = 2;

    static final int RETRY_NOT_SHOWING = -1;
    static final int RETRY_MESSAGE = 0;
    static final int RETRY_FILE = 1;
    static final int SENDING_MESSAGE = 2;

    BaseRequest.RenewTokenFailed listener;
    private ChatRepository chatRepository;
    private ChatFragmentView view;
    private boolean isWritingMode = false;
    private boolean isAudioMode = false;
    private boolean isLoadingMoreMessages = false;
    private boolean isFirstTime;
    private int idUserMe;
    private boolean isAutodownload;
    private String idChat;
    private boolean isGroupChat, isDynamizer;
    private ArrayList<ChatElement> chatElements;
    private boolean olderMessagesExist = true;
    private ChatMessageRepositoryModel messageToSend;
    private int sendingStage = RETRY_NOT_SHOWING;
    private ArrayList<String> filePath, mediaType;
    private ArrayList<Integer> contentIDs;
    private String audioPath;

    private Dynamizer dynamizer;
    private ArrayList<GetUser> users;
    private ArrayList<Integer> requestedUserPhotos = new ArrayList<>();
    private ArrayList<Integer> usersPhotoRequested = new ArrayList<>();
    private boolean usersReady = false, messagesReady = false;
    private String otherUserUserName;
    private String otherUserGender;

    private ArrayList<NotificationRest> missedCallsRest = null;

    ChatAudioRecorderFragment audioRecorderFragment;

    Handler handler;

    private Realm realm;


    int numberFilesDownloading = 0;
    Map<String, String> downloadedFiles = new HashMap<>();

    public void putNotificationOnWatched(Context context, ChatMessage chatMessage) {
            NotificationsDb notificationsDb = new NotificationsDb(context);
            notificationsDb.setNotificationWatched(chatMessage);

    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;
        String mediaType;
        String filename;
        String baseFile;

        public VideoCompressAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = null;
            mediaType = paths[2];
            filename = paths[3];

            try {
                MediaExtractor mex = new MediaExtractor();
                try {
                    mex.setDataSource(paths[0]);// the adresss location of the sound on sdcard.
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                MediaFormat mf = mex.getTrackFormat(0);
                int width = 0;

                if(mf.containsKey(MediaFormat.KEY_WIDTH)){
                    width = mf.getInteger(MediaFormat.KEY_WIDTH);
                }

                int height = 0;

                if(mf.containsKey(MediaFormat.KEY_HEIGHT)){
                    height = mf.getInteger(MediaFormat.KEY_HEIGHT);
                }

                int newWidth = (int) (width * 0.75);
                int newHeight = (int) (height * 0.75);

                filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1], 0, 0, 1500000 );

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);

            if(compressedFilePath == null){
                view.showImageErrorDialog();
            }
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            String value;

            if (length >= 1024)
                value = length / 1024f + " MB";
            else
                value = length + " KB";

            Log.i("Silicompressor", "Path: " + compressedFilePath);
            Log.d("compressor", "size: " + imageFile.length());

            chatRepository.saveFileToGallery(imageFile, mediaType);



        }
    }

    public ChatPresenter(BaseRequest.RenewTokenFailed listener, ChatFragmentView view,
                         Bundle savedInstanceState, String idChat, UserPreferences userPreferences,
                         boolean isGroupChat, boolean isDynamizer, Realm realm) {
        this.realm = realm;
        this.listener = listener;
        handler = new Handler();
        this.view = view;
        this.isGroupChat = isGroupChat;
        this.isDynamizer = isDynamizer;
        this.idChat = idChat;
        users = new ArrayList<>();
        chatElements = new ArrayList<>();
        idUserMe = userPreferences.getUserID();
        isAutodownload = userPreferences.getIsAutodownload();
        loadSavedState(savedInstanceState);
        isFirstTime = true;
    }

    public void setChatRepository(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public void setAudioRecorderFragment(ChatAudioRecorderFragment audioRecorderFragment) {
        this.audioRecorderFragment = audioRecorderFragment;
    }

    @Override
    public void loadData() {
        ArrayList<GetUser> userList = chatRepository.getUserListData(realm);
        chatRepository.getUserMeData();
        if (isGroupChat) {
            dynamizer = chatRepository.getDynamizer();
        }
        users.addAll(userList);
        if (!isGroupChat) {
            GetUser otherUser = new UsersDb(MyApplication.getAppContext()).findUser(
                    Integer.parseInt(idChat), realm);
            otherUserUserName = otherUser.getName();
            otherUserGender = otherUser.getGender();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                chatRepository.getMessages(0, 0);
            }
        }).start();
    }

    private void notifyViewIfDataReady() {
        if (usersReady && messagesReady) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    view.showMessages(chatElements, getUsersSparseArray());
                }
            });
        }
    }

    private void requestMissingPictures(ArrayList<GetUser> users) {
        for (GetUser user : users) {
            if ((user.getPhoto() == null || user.getPhoto().length() == 0)
                    && !usersPhotoRequested.contains(user.getId())) {
                usersPhotoRequested.add(user.getId());
                loadContactPicture(user.getId());
            }
        }
    }

    public void loadContactPicture(int contactId) {
        GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(listener,
                String.valueOf(contactId));
        getUserPhotoRequest.addOnOnResponse(this);
        getUserPhotoRequest.doRequest(new UserPreferences(MyApplication.getAppContext()).getAccessToken());
    }

    @Override
    public void onResponseGetUserPhotoRequest(final Uri photo, final String userID, int viewID, int contactType) {
        final UsersDb usersDb = new UsersDb(MyApplication.getAppContext());
        usersDb.setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());
        handler.post(new Runnable() {
            @Override
            public void run() {
                for (final GetUser user : users) {
                    if (user.getId() == Integer.parseInt(userID) && view != null) {
                        users.remove(user);
                        users.add(usersDb.findUser(Integer.parseInt(userID), realm));
                        view.updateUsers(getUsersSparseArray());
                        return;
                    }
                }
            }
        });
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {

    }

    private SparseArray<Contact> getUsersSparseArray() {
        SparseArray<Contact> usersSparseArray = new SparseArray<>();
        Log.d("tpqne","getUsersSparseArray");
        Log.d("dynpht","getUsersSparseArray, dynamizer nul?"+(dynamizer==null));
        if (isGroupChat && dynamizer != null) {
            usersSparseArray.put(dynamizer.getId(), createContactFromDynamizer(dynamizer));
        }
        for (GetUser getUser : users) {
            Log.d("tpqne","getUsersSparseArray, us id:"+getUser.getId());
            if (dynamizer == null || dynamizer.getId() != getUser.getId()) {
                usersSparseArray.append(getUser.getId(), createContactFromGetUser(getUser));
            }
        }
        return usersSparseArray;
    }

    private Contact createContactFromDynamizer(Dynamizer dynamizer) {
        Contact contact = new Contact();
        contact.setId(dynamizer.getId());
        contact.setIdChat(dynamizer.getIdChat());
        contact.setName(dynamizer.getName());
        contact.setLastname(dynamizer.getLastname());
        contact.setIdContentPhoto(dynamizer.getIdContentPhoto());
        contact.setPath(dynamizer.getPhoto());
        contact.setType(Contact.TYPE_DYNAMIZER);
        contact.setNumberInteractions(dynamizer.getNumberInteractions());
        contact.setNumberNotifications(dynamizer.getNumberUnreadMessages());
        UserPreferences userPreferences = new UserPreferences();
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        contact.setLastInteraction(userMessageDb.getLastMessage(userPreferences.getUserID(), dynamizer.getId()));

        return contact;
    }

    private Contact createContactFromGetUser(GetUser getUser) {
        Contact contact = new Contact();
        contact.setId(getUser.getId());
        contact.setName(getUser.getName());
        contact.setLastname(getUser.getLastname());
        contact.setPath(getUser.getPhoto());
        contact.setNumberNotifications(getUser.getNumberUnreadMessages());
        UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
        UserPreferences userPreferences = new UserPreferences();
        contact.setLastInteraction(userMessageDb.getLastMessage(userPreferences.getUserID(), getUser.getId()));

        return contact;
    }

    @Override
    public void onScrolledToTop() {
        if (!isLoadingMoreMessages && olderMessagesExist) {
            Log.d("mesr","onScrolledToTop");
            isLoadingMoreMessages = true;
            view.showLoadingMessages();
            chatRepository.getMessages(0,getTimeOfLastMessage());

            /*new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isLoadingMoreMessages = false;
                    view.hideLoadingMessages();
                }
            },2000);*/
        }
    }

    @Override
    public void onCreateView() {
        if (isWritingMode) {
            view.showWritingBottomBar();
        } else if (isAudioMode) {
            view.showAudioBottomBar();
        } else {
            view.showBottomBar();
        }
        if (isGroupChat) {
            view.setAction(null);
        }
        if (sendingStage == SENDING_MESSAGE) {
            view.showWaitDialog();
        } else if (sendingStage != RETRY_NOT_SHOWING) {
            view.showRetryDialog();
        }
    }

    @Override
    public void onClickText() {
        isWritingMode = true;
        view.showWritingBottomBar();
    }

    @Override
    public void onClickAudio() {
        if (audioRecorderFragment != null) {
            if (!OtherUtils.checkIfMicrophoneIsBusy()){
                AlertMessage alertMessage = new AlertMessage(audioRecorderFragment, AlertMessage.TITTLE_ERROR);
                alertMessage.showMessage(audioRecorderFragment.getActivity(),audioRecorderFragment.getResources().getString(R.string.error_audio_busy), "");
            }
            else{
                isAudioMode = true;
                view.showAudioBottomBar();
                audioRecorderFragment.startRecording();
            }

        }
    }

    @Override
    public void onClickSendAudio() {
        try {
            isAudioMode = false;
            view.showBottomBar();
            if (audioRecorderFragment != null) {
                audioRecorderFragment.stopRecording(true, false);
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
        }
    }

    @Override
    public void onClickFileShare() {
        //view.launchGalleryInSelectMode();
        view.launchPickVideoOrPhoto();
    }

    @Override
    public void onSendSystemFile(String path, String mimeType) {



        this.filePath = new ArrayList<>();
        this.mediaType = new ArrayList<>();
        if (mimeType.equals("video/mp4")) {
            Context context = MyApplication.getAppContext();
            if (context == null){
                return;
            }
            String destPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Vincles/videos";

            File f = new File(destPath);
            File baseFile = new File(path);

            this.filePath.add(path);
            this.mediaType.add(mimeType);
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;

            if (f.mkdirs() || f.isDirectory())
                new ChatPresenter.VideoCompressAsyncTask(context).execute(baseFile.getPath(), destPath, mimeType, baseFile.getName());


            //    chatRepository.saveFileToGallery(new File(path), mediaType);
        } else{

            File newFile = ImageUtils.getResizedFile(new File(path));
            if (newFile == null){
                view.showImageErrorDialog();
                return;
            }

            this.filePath.add(newFile.getPath());
            this.mediaType.add(mimeType);
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;

            chatRepository.saveFileToGallery(newFile, mimeType);
        }




    }

    @Override
    public void onLogout() {
        if (chatRepository != null) chatRepository.onLogout();
    }

    @Override
    public GetUser getOtherUserInfoIfNotGroup() {
        if (isGroupChat) return null;
        for (GetUser user : users) {
            if (user.getId() == Integer.parseInt(idChat)) return user;
        }
        return null;
    }

    @Override
    public int getDynamizerChatId() {
        return chatRepository.getDynamizerId();
    }

    @Override
    public void onClickSendMessage(String inputText) {
        isWritingMode = false;
        view.showBottomBar();
        if (inputText != null && inputText.length() > 0) {
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;
            sendTextMessage(inputText);
        }

    }

    @Override
    public void onClickCancelMessage() {
        if (isWritingMode) {
            isWritingMode = false;
            view.showBottomBar();
        } else {
            isAudioMode = false;
            view.showBottomBar();
            if (audioRecorderFragment != null) {
                audioRecorderFragment.stopRecording(false, false);
            }
        }
    }

    @Override
    public void onFileReceived(String messageID, String filePath) {
        downloadedFiles.put(messageID, filePath);
        onFileResult();
    }

    @Override
    public void onFileError(Object error) {
        onFileResult();
    }

    @Override
    public void onMessageSent(int messageId) {
        chatRepository.getMessage(String.valueOf(messageId));
        setReceivedMessagesToWatched();
    }

    @Override
    public void messageReceived(ChatMessageRepositoryModel message) {

        boolean isFirstUser = false;
        if (chatElements.size() > 0) {
            ChatElement lastElement = chatElements.get(0);
            Calendar calendarLast = Calendar.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendarLast.setTimeInMillis(lastElement.getSendTime());
            calendar.setTimeInMillis(message.getSendTime());
            if (!areCalendarsSameDay(calendarLast, calendar)) {
                setMessageToTypeFirst(lastElement);
                chatElements.add(0, createDateAlert(message.getSendTime()));
                isFirstUser = true;
            }  else if (!(lastElement instanceof ChatMessage) || ((ChatMessage) lastElement)
                    .getIdUserFrom() != message.getIdSender()) {
                isFirstUser = true;
            }
        }
        if (chatElements.size() == 1) isFirstUser = true;

        ChatMessage chatMessage = createChatElementFromRepositoryModel(message, isFirstUser,
                isFirstUser);


        chatElements.add(0, chatMessage);

        view.hideWaitDialog();
        sendingStage = RETRY_NOT_SHOWING;
        view.reloadMessagesAdapter();
    }

    @Override
    public void onNewMessageReceived(Long messageId) {
        chatRepository.getLocalMessage(messageId);
    }


    @Override
    public void onLocalMessageReady(ChatMessageRepositoryModel message) {
        //This is not used anymore beacause of the MediaManager
        /*if (message.getIdAdjuntContents() != null && message.getIdAdjuntContents().size() > 0) {

            ArrayList<String> paths = message.getPathsAdjuntContents();
            if (paths != null && paths.size() > 0) {
                //request any path that is missing
                int i = 0;
                ArrayList<Integer> ids = new ArrayList<>();
                for (String path : paths) {
                    if (path == null || path.length() <= 0) {
                        ids.add(message.getIdAdjuntContents().get(i));
                    }
                    i++;
                }
                if (isAutodownload) {
                    numberFilesDownloading = numberFilesDownloading + ids.size();
                    chatRepository.getMediaFiles(message.getId(),
                            OtherUtils.convertIntegers(ids));
                }
            } else { //no paths, request all files
                if (isAutodownload) {
                    numberFilesDownloading = numberFilesDownloading
                            + message.getIdAdjuntContents().size();
                    chatRepository.getMediaFiles(message.getId(),
                            OtherUtils.convertIntegers(message.getIdAdjuntContents()));
                }
            }

            numberFilesDownloading = numberFilesDownloading
                    + message.getIdAdjuntContents().size();
            chatRepository.getMediaFiles(message.getId(),
                    OtherUtils.convertIntegers(message.getIdAdjuntContents()));
        }*/

        removeNotReadAlert();

        boolean isFirst = false;
        if (chatElements.size() > 0) {
            ChatElement lastElement = chatElements.get(0);
            Calendar calendarLast = Calendar.getInstance();
            Calendar calendar = Calendar.getInstance();
            calendarLast.setTimeInMillis(lastElement.getSendTime());
            calendar.setTimeInMillis(message.getSendTime());
            if (!areCalendarsSameDay(calendarLast, calendar)) {
                //setMessageToTypeFirst(lastElement);

                chatElements.add(createDateAlert(lastElement.getSendTime()));
                isFirst = true;
            } else if (!(lastElement instanceof ChatMessage) || ((ChatMessage) lastElement)
                    .getIdUserFrom() != message.getIdSender()) {
                isFirst = true;
            }
        }

        long id = message.getId();
        boolean add = true;
        for (ChatElement element:  chatElements){
            if (element instanceof ChatMessage){
                long idMessage = ((ChatMessage) element).getId();
                if(idMessage == id){
                    add = false;
                }

            }
        }
        if(add){
            chatElements.add(0, createChatElementFromRepositoryModel(message, isFirst,
                    isFirst));
        }
        else{
            Log.d("message", "message repeated");
        }


        putNotReadAlert();
        view.reloadMessagesAdapter();
    }

    private void setReceivedMessagesToWatched() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<ChatMessage> messages = new ArrayList<>();
                //copying list to avoid concurrent modification exception
                ArrayList<ChatElement> chatElements = new ArrayList<>(ChatPresenter.this.chatElements);
                for (ChatElement element : chatElements) {
                    if (element.getType() < ChatElement.TYPE_ALERT_NOT_READ && element instanceof ChatMessage
                            && !isMessageFromUserMe(((ChatMessage)element)) && !((ChatMessage) element).isWatched()) {
                        messages.add((ChatMessage) element);
                        ((ChatMessage) element).setWatched(true);
                    }
                }
                chatRepository.setMessagesAsWatched(messages);
            }
        }).start();
    }
    ArrayList<Long> getUnwatchedReceivedMessages() {
        ArrayList<Long> ids = new ArrayList<>();
        for (ChatElement element : chatElements) {
            if (element.getType() < ChatElement.TYPE_ALERT_MISSED_CALL && element instanceof ChatMessage
                    && !isMessageFromUserMe(((ChatMessage)element))) {
                Log.d("qwe","presenter setReceivedMessagesToWatched for elem");
                ids.add(((ChatMessage) element).getId());
            }
        }
        return ids;
    }

    @Override
    public void onSendMessageError() {
        sendingStage = RETRY_MESSAGE;
        view.showRetryDialog();
        view.hideWaitDialog();
    }

    @Override
    public void onFileSavedToGallery(int galleryId, int contentId, String mediaType) {
        if (!mediaType.toLowerCase().contains("audio")) {
            chatRepository.saveFileToRealmGallery(galleryId);
        }
        int[] contentID = new int[1];
        contentID[0] = contentId;
        sendFileMessage(contentID, null, null, mediaType.toLowerCase().contains("audio"));
    }

    @Override
    public void onFileReceived(long messageID, int contentID, String filePath, String mediaType) {
        for (ChatElement element : chatElements) {
            if (element instanceof ChatMessageMedia && ((ChatMessage) element).getId() == messageID) {
                ArrayList<String> files = ((ChatMessageMedia) element).getMediaFiles();
                ArrayList<Boolean> isVideo = ((ChatMessageMedia) element).getIsVideo();
                for (int i = 0; i<files.size(); i++) {

                    if (files.get(i) == null || files.get(i).length() == 0 || FilenameUtils.getExtension(files.get(i)) == ""){
                        files.remove(i);
                        files.add(i, filePath);
                        isVideo.remove(i);
                        isVideo.add(i, mediaType.toLowerCase().contains("video"));
                        view.reloadMessagesAdapter();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onFileSaveToGalleryError() {
        sendingStage = RETRY_FILE;
        view.showRetryDialog();
        view.hideWaitDialog();
    }

    @Override
    public void onUserMeReceived(GetUser userMe) {
        userMe.setId(ChatAdapter.USER_ME);
        users.add(userMe);
    }

    @Override
    public void onUserListUpdated() {
        for (int i = 0; i < users.size(); i++) {
            GetUser user = users.get(i);
            if ((user.getPhoto() == null || user.getPhoto().length() == 0) &&
                    !requestedUserPhotos.contains(user.getId())) {
                requestedUserPhotos.add(user.getId());
                chatRepository.loadUserPhoto(user.getId());
            }
        }
        usersReady = true;
        notifyViewIfDataReady();
        requestMissingPictures(users);
    }

    @Override
    public void onChatInfoUpdated(String name, String lastname, String photo) {
        String title = name;
        if (lastname != null && lastname.length() > 0) {
            title = title +" "+lastname;
        }
        view.setChatInfo(title, photo);

        if(isGroupChat){
            dynamizer = chatRepository.getDynamizer();

            if (dynamizer != null){
                view.setChatDynamizer(dynamizer.getId(), dynamizer.getPhoto());
            }
        }


    }

    @Override
    public void onChatDynamizerUpdated(int id, String photo) {
        view.setChatDynamizer(id, photo);
    }


    @Override
    public void onUserPhotoDownloaded() {
        view.reloadMessagesAdapter();
    }

    @Override
    public void retrySendMessage() {
        view.hideSendAgainDialog();
        view.showWaitDialog();
        //sendingStage = SENDING_MESSAGE;
        if (sendingStage == RETRY_FILE) {
            chatRepository.saveFileToGallery(new File(filePath.get(0)), mediaType.get(0));
        } else if (sendingStage == RETRY_MESSAGE) {
            chatRepository.sendMessage(messageToSend);
        }
        sendingStage = RETRY_NOT_SHOWING;
    }

    @Override
    public void cancelRetrySendMessage() {
        sendingStage = RETRY_NOT_SHOWING;
        view.hideSendAgainDialog();
        view.hideWaitDialog();
    }

    @Override
    public void onSaveMediaFile(String path, int type) {
        String mediaType = "image/jpeg";
        if (type == MEDIA_VIDEO) {
            mediaType = "video/mp4";
        } else if (type == MEDIA_AUDIO) {
            mediaType = "audio/aac";
        }

        this.filePath = new ArrayList<>();
        this.mediaType = new ArrayList<>();
        if (type == MEDIA_VIDEO) {
            Context context = MyApplication.getAppContext();
            String destPath =  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Vincles/videos";

            File f = new File(destPath);
            File baseFile = new File(path);

            this.filePath.add(path);
            this.mediaType.add(mediaType);
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;

            if (f.mkdirs() || f.isDirectory())
                new ChatPresenter.VideoCompressAsyncTask(context).execute(baseFile.getPath(), destPath, mediaType, baseFile.getName());


            //    chatRepository.saveFileToGallery(new File(path), mediaType);
        }
        else if (type == MEDIA_PHOTO) {
            File newFile = ImageUtils.getResizedFile(new File(path));

            this.filePath.add(newFile.getPath());
            this.mediaType.add(mediaType);
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;

            chatRepository.saveFileToGallery(newFile, mediaType);
        }
        else{

            File file = new File(path);

            this.filePath.add(path);
            this.mediaType.add(mediaType);
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;

            chatRepository.saveFileToGallery(file, mediaType);
        }


    }



    private void onFileResult() {
        numberFilesDownloading--;
        if (numberFilesDownloading == 0) {
            for (Map.Entry<String, String> entry : downloadedFiles.entrySet()) {
                String messageID = entry.getKey();
                String path = entry.getValue();
                setFileForContentID(messageID, path);
            }
            downloadedFiles.clear();
            view.reloadMessagesAdapter();
        }
    }

    private void setFileForContentID(String messageID, String filePath) {
        for (ChatElement chatElement : chatElements) {
            if (chatElement instanceof ChatMessageMedia && ((ChatMessageMedia) chatElement).getId()
                    == Integer.parseInt(messageID)) {
                ArrayList<String> paths = ((ChatMessageMedia) chatElement).getMediaFiles();
                for (String path : paths) {
                    if (path == null || path.length()==0) {
                        path = filePath;
                        break;
                    }
                }
                break;
            }
        }
    }

    private void sendTextMessage(String message) {
        messageToSend = new ChatMessageRepositoryModel(Integer.parseInt(idChat),
                idUserMe, message,null, "TEXT_MESSAGE");
        chatRepository.sendMessage(messageToSend);
    }

    public void sendFileMessage(int[] idAdjuntContents, ArrayList<String> paths,
                                ArrayList<String> metadatas, boolean isAudio) {
        if(idAdjuntContents == null){
            return;
        }
        if (!view.isShowingWaitDialog()) {
            view.showWaitDialog();
            sendingStage = SENDING_MESSAGE;
        }

        this.contentIDs = OtherUtils.convertIntegers(idAdjuntContents);
        if (paths != null) {
            this.filePath = paths;
            this.mediaType = metadatas;
        }

        messageToSend = new ChatMessageRepositoryModel(Integer.parseInt(idChat), idUserMe,
                null, OtherUtils.convertIntegers(idAdjuntContents), filePath, mediaType,
                isAudio ? "AUDIO_MESSAGE" : getMediaMessageMetadata(mediaType));
        chatRepository.sendMessage(messageToSend);
    }

    private String getMediaMessageMetadata(ArrayList<String> mediaTypes) {
        if (mediaTypes == null || mediaTypes.size() == 0) return "TEXT_MESSAGE";
        if (mediaTypes.size() > 1) return "MESSAGE_MULTI";
        if (mediaTypes.get(0).contains("video")) return "VIDEO_MESSAGE";
        return "IMAGES_MESSAGE";
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isWritingMode", isWritingMode);
        outState.putBoolean("isAudioMode", isAudioMode);
        outState.putInt("sendingStage", sendingStage);

        if (sendingStage == RETRY_MESSAGE) {
            if (messageToSend.getMetadataTipus().equals("TEXT_MESSAGE")) {
                outState.putString("retryMessage", messageToSend.getText());
            } else {
                ArrayList<Integer> ids = new ArrayList<>(messageToSend.getIdAdjuntContents());
                outState.putIntegerArrayList("retryContentIDs", ids);
            }
            outState.putString("retryMetadata", messageToSend.getMetadataTipus());
        }

        outState.putStringArrayList("filePath", filePath);
        outState.putStringArrayList("mediaType", mediaType);
    }

    private void loadSavedState(Bundle state) {
        if (state != null) {
            isWritingMode = state.getBoolean("isWritingMode");
            isAudioMode = state.getBoolean("isAudioMode");
            sendingStage = state.getInt("sendingStage");
            if (sendingStage == RETRY_MESSAGE) {
                String metadata = state.getString("retryMetadata");
                String message = null;
                int[] contentIDs = null;
                if (metadata.equals("TEXT_MESSAGE")) {
                    message = state.getString("retryMessage");
                } else {
                    ArrayList<Integer> list = state.getIntegerArrayList("retryContentIDs");
                    contentIDs = new int[list.size()];
                    for (int i = 0; i < contentIDs.length; i++) {
                        contentIDs[i] = list.get(i);
                    }
                }
                messageToSend = new ChatMessageRepositoryModel(Integer.parseInt(idChat),
                        idUserMe, message, contentIDs==null ? new ArrayList<Integer>() :
                        OtherUtils.convertIntegers(contentIDs), metadata);
            }

            filePath = state.getStringArrayList("filePath");
            mediaType = state.getStringArrayList("mediaType");
            //this.chatElements = state.getParcelableArrayList("chatElements");
        }
    }

    /**
     * Converting received messages to the View's message Model.
     *
     * Iteratively:
     * If the date changes day, add date alert
     * If an alert has been added or the new message changes user, set FIRST to previous message
     *
     * After the loop:
     * IF there is a non read message, put an alert after the last non read message, and change
     * first condition of the following message.
     * Also, add a date on top
     *
     * Finally, send list to view
     *
     * @param repositoryMessageList   received messages, from newest to oldest
     */
    @Override
    public void messagesReceived(ArrayList<ChatMessageRepositoryModel> repositoryMessageList) {
        isLoadingMoreMessages = false;

        olderMessagesExist = repositoryMessageList.size() >= 10;

        //downloading more messages when user scrolls to top
        //remove date on top if necessary, and adjust first/not first
        if (repositoryMessageList.size() > 0 && chatElements.size() > 1) {
            removeNotReadAlert();

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTimeInMillis(repositoryMessageList.get(0).getSendTime());
            c2.setTimeInMillis(chatElements.get(chatElements.size()-1).getSendTime());
            if (chatElements.size()-1 != -1){
                if (areCalendarsSameDay(c1,c2) && chatElements.get(chatElements.size()-1).getType() != ChatElement.TYPE_ALERT_DATE) {
                    chatElements.remove(chatElements.size()-1);

                    ChatElement lastMessage = chatElements.get(chatElements.size()-1);
                    if (lastMessage instanceof ChatMessage && ((ChatMessage) lastMessage).getIdUserFrom()
                            == repositoryMessageList.get(0).getIdSender()) {
                        setMessageToTypeNotFirst(lastMessage);
                    }
                }
            }
        }

        boolean addDateLabel = true;
        //processing all messages one by one
        for (ChatMessageRepositoryModel chatMessage : repositoryMessageList) {

            boolean containsFlag = false;
            ChatElement element = createChatElementFromRepositoryModel(chatMessage, false, false);
            for (int i = 0; i<this.chatElements.size(); i++){
                containsFlag = containsFlag || chatElements.get(i).getSendTime() == element.getSendTime();
            }
            addDateLabel = containsFlag;

            if (!containsFlag){
                if (chatElements.size() > 0) {
                    ChatElement lastElement = chatElements.get(chatElements.size()-1);
                    Calendar calendarLast = Calendar.getInstance();
                    Calendar calendar = Calendar.getInstance();
                    calendarLast.setTimeInMillis(lastElement.getSendTime());
                    calendar.setTimeInMillis(chatMessage.getSendTime());
                    if (!areCalendarsSameDay(calendarLast, calendar)) {
                        setMessageToTypeFirst(lastElement);
                        chatElements.add(createDateAlert(lastElement.getSendTime()));
                    } else {
                        if (lastElement instanceof ChatMessage && ((ChatMessage) lastElement).getIdUserFrom() != chatMessage.getIdSender()) {
                            setMessageToTypeFirst(lastElement);
                        }
                    }
                }
                chatElements.add(element);
            }
        }

        if (missedCallsRest == null && !isGroupChat) {
            NotificationsDb notificationsDb = new NotificationsDb(MyApplication.getAppContext());
            missedCallsRest = notificationsDb.getMissedCallNotifications(Integer.parseInt(idChat));
            if (missedCallsRest.size() > 0) {
                removeAndAddMissedCalls();
                Collections.sort(chatElements);
            }
        }

        if (repositoryMessageList.size() > 0 && chatElements.size() > 0) {
            setMessageToTypeFirst(chatElements.get(chatElements.size()-1));
            if (!addDateLabel){
                chatElements.add(createDateAlert(chatElements.get(chatElements.size()-1).getSendTime()));
            }
            putNotReadAlert();
        }

        messagesReady = true;
        usersReady = true;
        notifyViewIfDataReady();


        //update messages data
        Context context = MyApplication.getAppContext();
        if (isGroupChat) {
            int idMe = new UserPreferences().getUserID();
            int userId = Integer.parseInt(idChat);
            GroupMessageDb groupMessageDb = new GroupMessageDb(context);
            new UserGroupsDb(context).setMessagesInfo(Integer.parseInt(idChat), groupMessageDb.getNumberUnreadMessagesReceived(idMe, userId),
                    groupMessageDb.getTotalNumberMessages(userId));
            OtherUtils.updateGroupOrDynChatInfo(Integer.parseInt(idChat));

        } else {
            UsersDb usersDb = new UsersDb(context);
            usersDb.setMessagesInfo(Integer.parseInt(idChat), new UserMessageDb(context)
                            .getNumberUnreadMessagesReceived(new UserPreferences().getUserID(),
                                    Integer.parseInt(idChat)),
                    new NotificationsDb(MyApplication.getAppContext())
                            .getNumberUnreadMissedCallNotifications(Integer.parseInt(idChat)),
                    Math.max(new UserMessageDb(MyApplication.getAppContext())
                                    .getLastMessage(new UserPreferences().getUserID(),
                                            Integer.parseInt(idChat)),
                            new NotificationsDb(MyApplication.getAppContext())
                                    .getLastMissedCallTime(Integer.parseInt(idChat))));
            Log.d("unrd","ChatPresenter, user:"+idChat+" totalNum:"
                    +new UserMessageDb(context)
                    .getTotalNumberMessages(new UserPreferences().getUserID(), Integer.parseInt(idChat)));
        }

    }

    private void removeAndAddMissedCalls() {
        for (ChatElement chatElement : chatElements) {
            if (chatElement.getType() == ChatElement.TYPE_ALERT_MISSED_CALL) {
                chatElements.remove(chatElement);
            }
        }
        for (NotificationRest notificationRest : missedCallsRest) {
            chatElements.add(createMissedCallChatElement(notificationRest));
        }
    }

    private ChatElement createMissedCallChatElement(NotificationRest missedCall) {

        Resources resources = view.getResources();
        Locale locale = resources.getConfiguration().locale;
        String message = OtherUtils.getArticleBeforeName(locale,otherUserUserName, otherUserGender)
                + resources.getString(R.string.chat_alert_missed_call,
                otherUserUserName, DateUtils.getFormatedDate(locale, missedCall.getCreationTime()),
                DateUtils.getFormatedHourMinute(missedCall.getCreationTime()));
        return new ChatMessage(ChatElement.TYPE_ALERT_MISSED_CALL, missedCall.getCreationTime(),message,-1, missedCall.getIdUser(), "", true, missedCall.getId());
    }

    private void putNotReadAlert() {
        Log.d("nread","putNotReadAlert");
        int lastUnreadMessagePosition = -1;
        for (int i = chatElements.size()-1; i>=0; i--) {
            ChatElement chatElement = chatElements.get(i);
            if (chatElement instanceof ChatMessage && !((ChatMessage) chatElement).isWatched()
                    && chatElement.getType()>=ChatElement.TYPE_USER_TEXT
                    && chatElement.getType()<=ChatElement.TYPE_USER_AUDIO_FIRST) {
                lastUnreadMessagePosition = i;
                Log.d("nread","position:"+i);
                break;
            }
        }
        if (lastUnreadMessagePosition != -1) {
            setMessageToTypeFirst(chatElements.get(lastUnreadMessagePosition));

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(chatElements.get(lastUnreadMessagePosition).getSendTime());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            ChatElement chatElement = new ChatElement(ChatElement.TYPE_ALERT_NOT_READ,
                    calendar.getTimeInMillis(), MyApplication.getAppContext()
                    .getResources().getString(R.string.chat_alert_not_read));
            //chatElements.add(chatElement);
            chatElements.add(lastUnreadMessagePosition+1, chatElement);

        }
    }

    private void removeNotReadAlert() {
        ArrayList<ChatElement> elementsCopy = new ArrayList<>(chatElements);
        ArrayList<ChatElement> elementsToRemove = new ArrayList<>();

        int pos = 0;
        for (ChatElement element : elementsCopy) {
            if (element != null && element.getType() == ChatElement.TYPE_ALERT_NOT_READ) {

                //remove FIRST condition if necessary
                if (pos+1 < chatElements.size()) {
                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();
                    c1.setTimeInMillis(chatElements.get(pos-1).getSendTime());
                    c2.setTimeInMillis(chatElements.get(pos+1).getSendTime());
                    if (areCalendarsSameDay(c1,c2)) {
                        ChatElement lastMessage = chatElements.get(pos-1);
                        ChatElement nextMessage = chatElements.get(pos+1);
                        if (lastMessage instanceof ChatMessage && nextMessage instanceof ChatMessage
                                && ((ChatMessage) lastMessage).getIdUserFrom()
                                == ((ChatMessage)nextMessage).getIdUserFrom()) {
                            setMessageToTypeNotFirst(lastMessage);
                        }
                    }
                }

                elementsToRemove.add(element);

            }

            pos++;
        }

        for (ChatElement element: elementsToRemove){
            chatElements.remove(element);
        }

    }

    private long getTimeOfLastMessage() {
        for (int i=chatElements.size()-1; i>=0;i--) {
            if (chatElements.get(i).getType() < ChatElement.TYPE_ALERT_NOT_READ) {
                return chatElements.get(i).getSendTime();
            }
        }
        return 0;
    }

    private ChatElement createDateAlert(long timeOfDay) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeOfDay);
        String date = MyApplication.getAppContext().getResources().getString(R.string.chat_alert_today);

        date = DateUtils.getFormatedDate(view.isLanguageCatalan(),calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH),calendar.get(Calendar.YEAR));

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return new ChatElement(ChatElement.TYPE_ALERT_DATE, calendar.getTimeInMillis(), date);
    }

    private boolean areCalendarsSameDay(Calendar calendar1, Calendar calendar2) {
        return calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
                && calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR);
    }


    private boolean isMessageFromUserMe(int idUserFrom) {
        return idUserFrom == idUserMe;
    }

    private boolean isMessageFromUserMe(ChatMessage chatMessage) {
        return chatMessage.getType() >= ChatMessage.TYPE_ME_TEXT && chatMessage.getType()
                <= ChatMessage.TYPE_ME_AUDIO_FIRST;
    }

    private void setMessageToTypeFirst(ChatElement chatElement) {
        if (chatElement.getType() < ChatElement.TYPE_ALERT_NOT_READ && chatElement.getType()%2 == 0) {
            //if it is even, it is not first -> make first by adding 1
            chatElement.setType(chatElement.getType() + 1);
        }
    }

    private void setMessageToTypeNotFirst(ChatElement chatElement) {
        if (chatElement.getType() < ChatElement.TYPE_ALERT_NOT_READ && chatElement.getType()%2 == 1) {
            //if it is odd, it is first -> make not first by subtracting 1
            chatElement.setType(chatElement.getType() - 1);
        }
    }

    private ChatMessage createChatElementFromRepositoryModel(ChatMessageRepositoryModel chatMessage,
                                                             boolean isFirstUserSender,
                                                             boolean isFirstOtherUser) {
        int type = -1;

        if (chatMessage.getMetadataTipus() != null && chatMessage.getMetadataTipus().toLowerCase().contains("audio")) { //it is audio

            if (isMessageFromUserMe(chatMessage.getIdSender()) && isFirstUserSender) {
                type = ChatElement.TYPE_ME_AUDIO_FIRST;
            } else if (isMessageFromUserMe(chatMessage.getIdSender()) && !isFirstUserSender) {
                type = ChatElement.TYPE_ME_AUDIO;
            } else if (!isMessageFromUserMe(chatMessage.getIdSender()) && !isFirstOtherUser) {
                type = ChatElement.TYPE_USER_AUDIO;
            } else {
                type = ChatElement.TYPE_USER_AUDIO_FIRST;
            }
        } else if (chatMessage.getIdAdjuntContents().size() > 0) { //has image/video
            if (isMessageFromUserMe(chatMessage.getIdSender()) && !isFirstUserSender) {
                type = ChatElement.TYPE_ME_IMAGE;
            } else if (isMessageFromUserMe(chatMessage.getIdSender()) && isFirstUserSender) {
                type = ChatElement.TYPE_ME_IMAGE_FIRST;
            } else if (!isMessageFromUserMe(chatMessage.getIdSender()) && !isFirstOtherUser) {
                type = ChatElement.TYPE_USER_IMAGE;
            } else {
                type = ChatElement.TYPE_USER_IMAGE_FIRST;
            }
        }

        if (type != -1) {
            ArrayList<Boolean> isVideoList = new ArrayList<>();
            for (String metadata : chatMessage.getMetadataAdjuntContents()) {
                isVideoList.add(metadata.toLowerCase().contains("video"));
            }
            return new ChatMessageMedia(type, chatMessage.getSendTime(),
                    chatMessage.getText(), chatMessage.getId(),
                    chatMessage.getIdSender(), chatMessage.isWatched(),
                    chatMessage.getPathsAdjuntContents(), chatMessage.getFullNameUserSender(),
                    isVideoList);
        }

        //only text
        if (isMessageFromUserMe(chatMessage.getIdSender()) && !isFirstUserSender) {
            type = ChatElement.TYPE_ME_TEXT;
        } else if (isMessageFromUserMe(chatMessage.getIdSender()) && isFirstUserSender) {
            type = ChatElement.TYPE_ME_TEXT_FIRST;
        } else if (!isMessageFromUserMe(chatMessage.getIdSender()) && !isFirstOtherUser) {
            type = ChatElement.TYPE_USER_TEXT;
        } else {
            type = ChatElement.TYPE_USER_TEXT_FIRST;
        }
        return new ChatMessage(type, chatMessage.getSendTime(), chatMessage.getText(),
                chatMessage.getId(), chatMessage.getIdSender(), chatMessage.getFullNameUserSender(),
                chatMessage.isWatched());
    }


}

