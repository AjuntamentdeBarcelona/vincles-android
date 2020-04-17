package cat.bcn.vincles.mobile.Client.Business;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.HashMap;

import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Db.UserMessageDb;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.ChatMessageRest;
import cat.bcn.vincles.mobile.Client.Model.GroupMessageRest;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupMessageFileRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGroupPhotoRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Contacts.Contact;
import cat.bcn.vincles.mobile.UI.Contacts.ContactsAdapter;
import cat.bcn.vincles.mobile.UI.Gallery.GalleryAdapter;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.RequestsUtils;

import static android.content.Context.MODE_PRIVATE;

public class MediaManager {

    private static final String TAG = "MediaManager";
    private Context context;
    public UserPreferences userPreferences;

    public MediaManager(Context c) {
        this.context = c;
        this.userPreferences = new UserPreferences();
    }

    public void downloadContactItem(Object holder, int position, String filePath, int userId, int contactType, int idContent, boolean isUser, final MediaCallbacksGallery mediaCallbacks)
    {
        Log.d("downloadContactItem", "filePath: " + filePath);

        final HashMap<Integer, Object> returnHash = new HashMap<>();
        returnHash.put(0,holder);
        returnHash.put(1,filePath);
        returnHash.put(2,position);
        returnHash.put(3,userId);

        //If File Path Exists
        if (filePath != null && !"".equals(filePath)) {
            returnHash.put(1,filePath);
            mediaCallbacks.onSuccess(returnHash);
            return;
        }else{
            String idContentPath = checkContentIdExists(idContent, context);
            if(!idContentPath.equals("")){
                returnHash.put(1,idContentPath);
                mediaCallbacks.onSuccess(returnHash);
                return;
            }

        }


        //If Contact Type is GROUP
        Log.d("contactType", "contactType: " + String.valueOf(contactType));
        Log.d("contactType", "userId: " + String.valueOf(userId));

        Log.d("contactType", "Contact.TYPE_GROUP: " + String.valueOf(contactType == Contact.TYPE_GROUP));
        if (contactType == Contact.TYPE_GROUP && !isUser) {
            GetGroupPhotoRequest getGroupPhotoRequest = new GetGroupPhotoRequest(new BaseRequest.RenewTokenFailed() {
                @Override
                public void onRenewTokenFailed() {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.close_session)
                            .setMessage(R.string.close_session_token_message)
                            .setNegativeButton(android.R.string.ok, null)
                            .show();
                }
            },
                    String.valueOf(userId));
            getGroupPhotoRequest.addOnOnResponse(new GetGroupPhotoRequest.OnResponse() {
                @Override
                public void onResponseGetGroupPhotoRequest(Uri photo, String userID, int viewID) {
                    UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
                    userGroupsDb.setUserGroupAvatarPath(Integer.parseInt(userID), photo.getPath());
                    returnHash.put(1,photo.getPath());
                    mediaCallbacks.onSuccess(returnHash);
                }

                @Override
                public void onFailureGetGroupPhotoRequest(Object error, String userID) {
                    returnHash.put(1, UserPreferences.AUTO_DOWNLOAD);
                    mediaCallbacks.onFailure(returnHash);

                }
            });
            getGroupPhotoRequest.doRequest(this.userPreferences.getAccessToken());
        } else {
            GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(new BaseRequest.RenewTokenFailed() {
                @Override
                public void onRenewTokenFailed() {
                    new AlertDialog.Builder(context)
                            .setTitle(R.string.close_session)
                            .setMessage(R.string.close_session_token_message)
                            .setNegativeButton(android.R.string.ok, null)
                            .show();
                }
            }, String.valueOf(userId));
            getUserPhotoRequest.addOnOnResponse(new GetUserPhotoRequest.OnResponse() {
                @Override
                public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
                    new UserGroupsDb(MyApplication.getAppContext()).setGroupDynamizerAvatarPath(Integer.parseInt(userID), photo.getPath());
                    new UsersDb(MyApplication.getAppContext()).setPathAvatarToUser(Integer.parseInt(userID), photo.getPath());
                    returnHash.put(1,photo.getPath());
                    mediaCallbacks.onSuccess(returnHash);

                }

                @Override
                public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
                    returnHash.put(1, UserPreferences.AUTO_DOWNLOAD);
                    mediaCallbacks.onFailure(returnHash);
                }
            });
            getUserPhotoRequest.doRequest(this.userPreferences.getAccessToken());

         }

        }

        public String checkContentIdExists(int idContent, Context context){
            ContextWrapper wrapper = new ContextWrapper(context);

            String fileName = idContent + ".jpeg";

            File file = wrapper.getDir("VINCLES",MODE_PRIVATE);
            file = new File(file, fileName);
            String fullPath = file.getAbsolutePath();
            if(file.exists()){
                return fullPath;

            }

            fileName = idContent + ".png";

            file = wrapper.getDir("VINCLES",MODE_PRIVATE);
            file = new File(file, fileName);
            fullPath = file.getAbsolutePath();
            if(file.exists()){
                return fullPath;

            }

            fileName = idContent + ".mp4";

            file = wrapper.getDir("VINCLES",MODE_PRIVATE);
            file = new File(file, fileName);
            fullPath = file.getAbsolutePath();
            if(file.exists()){
                return fullPath;

            }

            fileName = idContent + ".aac";

            file = wrapper.getDir("VINCLES",MODE_PRIVATE);
            file = new File(file, fileName);
            fullPath = file.getAbsolutePath();
            if(file.exists()){
                return fullPath;

            }

            fileName = idContent + ".mp3";

            file = wrapper.getDir("VINCLES",MODE_PRIVATE);
            file = new File(file, fileName);
            fullPath = file.getAbsolutePath();
            if(file.exists()){
                return fullPath;

            }

            return "";
        }
    public void downloadGalleryItem(Object holder, int position, String filePath, int idContent, String mimeType, final MediaCallbacksGallery mediaCallbacks, boolean downloadClicked){

        final MediaManager manager = this;

        final HashMap<Integer, Object> returnHash = new HashMap<>();
        returnHash.put(0,holder);
        returnHash.put(1,filePath);
        returnHash.put(2,position);
        returnHash.put(3,idContent);


        //If filePath Exists
        if (filePath != null && !"".equals(filePath)) {
            returnHash.put(1,filePath);
            mediaCallbacks.onSuccess(returnHash);
            return;
        }
        else{
            String idContentPath = checkContentIdExists(idContent, context);
           if(idContentPath != ""){
               returnHash.put(1,idContentPath);
               mediaCallbacks.onSuccess(returnHash);
               return;
           }

        }

        //Another gallery item links to the same file so we reuse it instead of downloading
        final GalleryDb galleryDb = new GalleryDb(context);
        boolean exists = galleryDb.existsContentById(idContent);
        if(exists){
            String path = galleryDb.getPathFromIdContent(idContent);
            if(path != null){
                galleryDb.setPathFromIdContent(idContent, path);
                returnHash.put(1,path);
                mediaCallbacks.onSuccess(returnHash);
                return;
            }
        }


        //If User Settings is NOT autoDownload()
        if (!userPreferences.getIsAutodownload() && !downloadClicked) {
            returnHash.put(1, UserPreferences.AUTO_DOWNLOAD);
            mediaCallbacks.onFailure(returnHash);
            return;
        }

        //Download missing resource
        GetGalleryContentRequest getGalleryContentRequest = new GetGalleryContentRequest(new BaseRequest.RenewTokenFailed() {
            @Override
            public void onRenewTokenFailed() {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.close_session)
                        .setMessage(R.string.close_session_token_message)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            }
        }, context, String.valueOf(idContent), mimeType);
        getGalleryContentRequest.addOnOnResponse(new GetGalleryContentRequest.OnResponse() {
            @Override
            public void onResponseGetGalleryContentRequest(String contentID, final String filePath) {
                /*
                galleryDb.setPathFromIdContent(Integer.valueOf(contentID), filePath);

                //store on external SD if user wants to
                if (userPreferences.getIsCopyPhotos() && checkWriteExternalStoragePermission()) {
                    ImageUtils.safeCopyFileToExternalStorage(filePath, contentID);
                }

                returnHash.put(1,filePath);
                mediaCallbacks.onSuccess(returnHash);

*/

                DownloadGalleryTask asyncTask =new DownloadGalleryTask(new AsyncResponse() {

                    @Override
                    public void processFinish() {
                        Log.d("Response", "ee");
                        returnHash.put(1,filePath);
                        mediaCallbacks.onSuccess(returnHash);

                    }
                }, filePath, contentID, context, manager);
                asyncTask.execute(new Object[] { "Youe request to aynchronous task class is giving here.." });

            }

            @Override
            public void onFailureGetGalleryContentRequest(Object error) {
                Log.d("ContentRequest", error.toString());
                returnHash.put(1, UserPreferences.AUTO_DOWNLOAD);
                mediaCallbacks.onFailure(returnHash);
            }
        });
        getGalleryContentRequest.doRequest(userPreferences.getAccessToken());


    }
    public void downloadGalleryItemDetail(String filePath, int idContent, String mimeType, final MediaCallbacks mediaCallbacks, boolean downloadClicked){

        //If filePath Exists
        if (filePath != null && !"".equals(filePath)) {
            mediaCallbacks.onSuccess(filePath);
            return;
        }
        else{
            String idContentPath = checkContentIdExists(idContent, context);
            if(idContentPath != ""){
                mediaCallbacks.onSuccess(idContentPath);
                return;
            }

        }
        //Another gallery item links to the same file so we reuse it instead of downloading
        final GalleryDb galleryDb = new GalleryDb(context);
        boolean exists = galleryDb.existsContentById(idContent);
        if(exists){
            String path = galleryDb.getPathFromIdContent(idContent);
            if(path != null){
                galleryDb.setPathFromIdContent(idContent, path);
                mediaCallbacks.onSuccess(path);
                return;
            }
        }


        //If User Settings is NOT autoDownload()
        if (!userPreferences.getIsAutodownload() && !downloadClicked) {
            mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);
            return;
        }

        //Download missing resource
        GetGalleryContentRequest getGalleryContentRequest = new GetGalleryContentRequest(new BaseRequest.RenewTokenFailed() {
            @Override
            public void onRenewTokenFailed() {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.close_session)
                        .setMessage(R.string.close_session_token_message)
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
            }
        }, context, String.valueOf(idContent), mimeType);
        getGalleryContentRequest.addOnOnResponse(new GetGalleryContentRequest.OnResponse() {
            @Override
            public void onResponseGetGalleryContentRequest(String contentID, String filePath) {
                galleryDb.setPathFromIdContent(Integer.valueOf(contentID), filePath);

                //store on external SD if user wants to
                if (userPreferences.getIsCopyPhotos() && checkWriteExternalStoragePermission()) {
                    ImageUtils.safeCopyFileToExternalStorage(context,filePath, contentID);
                }

                mediaCallbacks.onSuccess(filePath);

            }

            @Override
            public void onFailureGetGalleryContentRequest(Object error) {
                Log.d("ContentRequest", error.toString());
                mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);
            }
        });
        getGalleryContentRequest.doRequest(userPreferences.getAccessToken());


    }

    public boolean checkWriteExternalStoragePermission() {
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void downloadMessageItem(String filePath, final int messageId, String idChat, int position, Boolean isGroupChat, final MediaCallbacks mediaCallbacks, Boolean fromDownload) {
        //If filePath Exists
        if (filePath != null && !"".equals(filePath)) {
            mediaCallbacks.onSuccess(filePath);
            return;
        }
        else {
            if (isGroupChat) {
                GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
                GroupMessageRest messageRest = groupMessageDb.findMessageUnmanaged(messageId);

                String idContentPath = checkContentIdExists(messageRest.getIdContent(), context);

                if(idContentPath != ""){
                    mediaCallbacks.onSuccess(idContentPath);
                    return;
                }
            }
            else{
                UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
                ChatMessageRest messageRest = userMessageDb.findMessage(messageId);
                if (messageRest==null){
                    mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);
                    return;
                }
                String idContentPath = checkContentIdExists(messageRest.getIdAdjuntContents().get(position), context);

                if(!idContentPath.equals("")){
                    mediaCallbacks.onSuccess(idContentPath);
                    return;
                }
            }
        }

        //If User Settings is NOT autoDownload()
        if (!userPreferences.getIsAutodownload() && !fromDownload) {
            mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);
            return;
        }

        if (isGroupChat){
            GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
            GroupMessageRest messageRest = groupMessageDb.findMessageUnmanaged(messageId);
            String fileId = String.valueOf(messageRest.getIdContent());

            String accessToken = new UserPreferences().getAccessToken();
            GetGroupMessageFileRequest getGroupMessageFileRequest = new GetGroupMessageFileRequest(
                    new BaseRequest.RenewTokenFailed() {
                        @Override
                        public void onRenewTokenFailed() {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.close_session)
                                    .setMessage(R.string.close_session_token_message)
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        }
                    }, MyApplication.getAppContext(), fileId, Integer.parseInt(idChat), (int) messageId);
            getGroupMessageFileRequest.addOnOnResponse(new GetGroupMessageFileRequest.OnResponse() {
                @Override
                public void onGetGroupMessageFileRequestResponse(int messageId, String filePath, String contentID, String mimeType) {
                    GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
                    groupMessageDb.setMessageFile(Integer.parseInt(contentID), filePath, messageId, mimeType);
                    mediaCallbacks.onSuccess(filePath);
                }

                @Override
                public void onGetGroupMessageFileRequestFailure(Object error) {
                    mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);

                }
            });
            getGroupMessageFileRequest.doRequest(accessToken);
        }
        else{
            UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
            ChatMessageRest messageRest = userMessageDb.findMessage(messageId);
            String fileId = String.valueOf(messageRest.getIdAdjuntContents().get(position));

            String accessToken = new UserPreferences().getAccessToken();
            GetGalleryContentRequest getGalleryContentRequest = new GetGalleryContentRequest(
                    new BaseRequest.RenewTokenFailed() {
                        @Override
                        public void onRenewTokenFailed() {
                            new AlertDialog.Builder(context)
                                    .setTitle(R.string.close_session)
                                    .setMessage(R.string.close_session_token_message)
                                    .setNegativeButton(android.R.string.ok, null)
                                    .show();
                        }
                    }, MyApplication.getAppContext(), fileId, "");
            getGalleryContentRequest.addOnOnResponse(new GetGalleryContentRequest.OnResponseMessage() {
                @Override
                public void onResponseGetGalleryContentRequest(String contentID, String filePath,
                                                               long messageID, String mimeType) {
                    UserMessageDb userMessageDb = new UserMessageDb(MyApplication.getAppContext());
                    userMessageDb.setMessageFile(Integer.parseInt(contentID), filePath, messageId, mimeType);

                    if (!filePath.endsWith(".aac") && !filePath.endsWith(".mp3") && new UserPreferences().getIsCopyPhotos() && ContextCompat.checkSelfPermission(
                            MyApplication.getAppContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        ImageUtils.safeCopyFileToExternalStorage(context,filePath, contentID);
                    }

                    mediaCallbacks.onSuccess(filePath);

                }

                @Override
                public void onFailureGetGalleryContentRequest(Object error) {
                    mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);

                }
            });
            getGalleryContentRequest.setMessageID(messageId);
            getGalleryContentRequest.doRequest(accessToken);
        }

        /*GroupMessageDb groupMessageDb = new GroupMessageDb(MyApplication.getAppContext());
        GroupMessageRest messageRest = groupMessageDb.findMessage(messageId);
        //If message is NULL
        if (messageRest == null){
            mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);
            return;
        }


        //Get FILE
        final String fileId = String.valueOf(messageRest.getIdContent());

        String accessToken = new UserPreferences().getAccessToken();
        GetGroupMessageFileRequest getGroupMessageFileRequest = new GetGroupMessageFileRequest(
                new BaseRequest.RenewTokenFailed() {
                    @Override
                    public void onRenewTokenFailed() {

                    }
                }, MyApplication.getAppContext(), fileId, Integer.parseInt(idChat), (int) messageId);
        getGroupMessageFileRequest.addOnOnResponse(new GetGroupMessageFileRequest.OnResponse() {
            @Override
            public void onGetGroupMessageFileRequestResponse(int messageId, String filePath, String contentID, String mimeType) {
                Log.d(TAG, "GET CHAT FILE SUCCESS");
                Log.d(TAG, filePath);
                mediaCallbacks.onSuccess(filePath);
            }

            @Override
            public void onGetGroupMessageFileRequestFailure(Object error) {
                Log.d(TAG, "GET CHAT FILE ERROR");
                Log.d(TAG, error.toString());
                mediaCallbacks.onFailure(UserPreferences.AUTO_DOWNLOAD);

            }
        });
        getGroupMessageFileRequest.doRequest(accessToken);*/

    }
}

class DownloadGalleryTask extends AsyncTask<Object,Object,Object> { //change Object to required type
    public AsyncResponse delegate = null;//Call back interface

    public String filePath;
    public String contentID;
    public Context context;
    public MediaManager manager;

    public DownloadGalleryTask(AsyncResponse asyncResponse, String filePath, String contentID, Context context, MediaManager manager) {
        this.delegate = asyncResponse;
        this.filePath = filePath;
        this.contentID = contentID;
        this.context = context;
        this.manager = manager;
    }

    // required methods

    @Override
    protected Object doInBackground(Object... objects) {

        final GalleryDb galleryDb = new GalleryDb(context);
        galleryDb.setPathFromIdContent(Integer.valueOf(contentID), filePath);
        if (manager.userPreferences.getIsCopyPhotos() && manager.checkWriteExternalStoragePermission()) {
            ImageUtils.safeCopyFileToExternalStorage(context,filePath, contentID);
        }
        return null;
    }

    protected void onPostExecute(Object o) {
        // your stuff
        delegate.processFinish();
    }
}

interface AsyncResponse {
    void processFinish();
}
