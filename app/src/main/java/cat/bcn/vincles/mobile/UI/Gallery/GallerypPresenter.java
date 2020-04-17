package cat.bcn.vincles.mobile.UI.Gallery;


import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.google.gson.JsonObject;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.AddContentInTheGallery;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.DeleteGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GalleryAddContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetGalleryContentsRequest;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class GallerypPresenter implements GalleryPresenterContract,
        GetGalleryContentsRequest.OnResponse,
        GalleryAddContentRequest.OnResponse, AddContentInTheGallery.OnResponse,
        DeleteGalleryContentRequest.OnResponse {

    UserPreferences userPreferences;
    GalleryDb galleryDb;
    UsersDb usersDb;
    GalleryView galleryView;
    int numberContentToDownload = 0 ;
    int numberContentToDownloaded = 0 ;
    Context context;
    ImageUtils imageUtils;
    private int idPhoto;
    private File imageFile;
    private ArrayList<Integer> itemsSelected;
    int numberItemsSelectedResponseCorrect = 0;
    private List<Integer> itemsSelectedResponse;
    private boolean isInSelectionMode = false;
    private List<GalleryContentRealm> galleryContentRealmList;
    private List<Integer> contentRequested;
    private boolean isInSelectionShare = false;
    private boolean isInSelectionDelete = false;

    private String filterKind = FILTER_ALL_FILES;
    private String actionKind = null;

    boolean addedAnyNewPicutre = false;
    BaseRequest.RenewTokenFailed listener;
    Integer selectedContents = 0;
    boolean isLoadingMore = false;
    boolean noMoreContent = false;
    File videoFile = null;
    Realm realm;

    public GallerypPresenter(BaseRequest.RenewTokenFailed listener, Context context, GalleryView galleryView,
                             UserPreferences userPreferences, GalleryDb galleryDb, UsersDb usersDb,
                             @Nullable ArrayList<Integer> itemsSelected, String filterKind, String actionKind,
                             boolean isInSelectionMode, Realm realm) {
        Log.d("filtfa","onCreate presenter");
        this.context = context;
        this.listener = listener;
        if (filterKind != null) this.filterKind = filterKind;
        if (actionKind != null) this.actionKind = actionKind;

        this.galleryView = galleryView;
        this.userPreferences = userPreferences;
        this.galleryDb = galleryDb;
        this.usersDb = usersDb;
        imageUtils = new ImageUtils();
        if (itemsSelected == null) {
            this.itemsSelected = new ArrayList<>();
        } else {
            this.itemsSelected = itemsSelected;
        }
        this.isInSelectionMode = isInSelectionMode;
        itemsSelectedResponse = new ArrayList<>();
        contentRequested = new ArrayList<>();
        this.realm = realm;
        galleryContentRealmList = galleryDb.findAll(this.realm);

        if (actionKind != null){
            if(actionKind.equals(ACTION_DELETE)){
                setInDeleteMode(true);
            }
            if(actionKind.equals(ACTION_SHARE)){
                setInShareMode(true);
            }
        }

    }

    @Override
    public void onCreateView() {
        if (isInSelectionMode && this.itemsSelected.size() == 1) {
            Log.d("glerysel","create presenter, enabled true");
            galleryView.updateEnabledButtons(true);
        } else if (isInSelectionMode && this.itemsSelected.size() == 0) {
            Log.d("glerysel","create presenter, enabled false");
            galleryView.updateEnabledButtons(false);
        }
    }

    @Override
    public void getContent(long to) {
        if (isLoadingMore)return;
        if (noMoreContent)return;
        isLoadingMore = true;
        String accessToken = userPreferences.getAccessToken();
        GetGalleryContentsRequest getContentRequest = new GetGalleryContentsRequest(listener, to);
        getContentRequest.addOnOnResponse(this);
        getContentRequest.doRequest(accessToken);

    }

    @Override
    public void pushImageToAPI(Object picture, boolean isUri) {
        File file;
        if (isUri) {
            file = new File(((Uri)picture).getPath());
        } else {
            /*Bitmap photo = (Bitmap) picture;
            Uri photoUri = imageUtils.getImageUri(context, photo);
            galleryView.savingFilePictureIsUri(photoUri);
            file = new File(photoUri.getPath());*/
            file = new File((String)picture);
            Log.d("tag", String.valueOf(file.length()) );
        }

        File newFile = ImageUtils.getResizedFile(file);
        //    Log.d("tag", String.valueOf(newFile.length()) );

        addContentToGallery(newFile, "image/jpeg");
    }

    @Override
    public void pushVideoToAPI(Uri fileUri) {
        File file = new File(fileUri.getPath());
        Log.d("compressor", "size0: " + file.length());



        File f = new File( Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + "/Vincles/videos");
        if (f.mkdirs() || f.isDirectory()){
            Log.d("BV", "current: " + String.valueOf(Build.VERSION.SDK_INT));
            Log.d("BV", "lollipop: " + String.valueOf(Build.VERSION_CODES.LOLLIPOP));

            videoFile = file;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                addContentToGallery(file, "video/mp4");
            }
            else{
                try{

                    new VideoCompressAsyncTask(context).execute(fileUri.getPath(), f.getPath());
                }
                catch (Exception e){
                    addContentToGallery(file, "video/mp4");
                    Log.e("VideoCompressAsyncTask", "An error occurred");
                }
            }
        }

    }


    @Override
    public void saveImage(int id) {
        int userID = userPreferences.getUserID();
        String mimeType = ImageUtils.getMimeType(imageFile.getPath());

        long inclusionTime;
        if (mimeType.contains("video")) {
            inclusionTime = System.currentTimeMillis();
        } else {
            String imageName = imageFile.getName();
            String name = imageName.split("_")[0];
            inclusionTime = Long.valueOf(name.replace(".jpg",""));
        }

        if (new UserPreferences().getIsCopyPhotos() && ImageUtils.checkWriteExternalStoragePermission(context)){
            ImageUtils.saveMediaExternalMemory(this.context,imageFile.getName(),imageFile.getPath());
        }

        GalleryContentRealm galleryContentRealm = new GalleryContentRealm(id, idPhoto,
                mimeType, userID,inclusionTime);
        String absolutePath = imageFile.getAbsolutePath();
        galleryContentRealm.setPath(absolutePath);
        galleryDb.insertContent(galleryContentRealm);
        galleryView.onFileAdded();
        galleryView.closeAlertSavingImage();
    }

    private void addContentToGallery (File file, String mediaType) {
        this.imageFile = file;
        String imageFileName = imageFile.getName();


        RequestBody fileB = RequestBody.create(MediaType.parse(mediaType), imageFile);
        MultipartBody.Part body =
                MultipartBody.Part.createFormData("file", imageFileName, fileB);


        Log.d("vidrc","addContentToGallery request:"+body);
        String accessToken = userPreferences.getAccessToken();
        GalleryAddContentRequest galleryAddContentRequest = new GalleryAddContentRequest(listener, body);
        galleryAddContentRequest.addOnOnResponse(this);
        galleryAddContentRequest.doRequest(accessToken);
    }

    public void setActionKind(String actionKind){
        this.actionKind = actionKind;
    }

    public void filterMedia(String filterKind) {
        Log.d("filtfa","filterMedia presenter");
        this.filterKind = filterKind;
        galleryView.showGalleryContent(getFilteredMedia(filterKind));
    }

    public RealmResults<GalleryContentRealm> getFilteredMedia(String filterKind) {
        Log.d("filtfa","getFilteredMedia presenter");
        switch (filterKind) {
            default:
            case GallerypPresenter.FILTER_ALL_FILES:
                return galleryDb.findAll(realm);
            case  GallerypPresenter.FILTER_ALL_MY_FILES:
                return galleryDb.getContentsPathByUserID(realm);
            case GallerypPresenter.FILTER_RECIVED_FILES:
                return galleryDb.getRecivedContentsPath(realm);
        }
    }

    @Override
    public void itemSelected(int contentID, int index) {
        Log.d("glery"," item selected list before:"+itemsSelected.toString());
        Log.d("glery"," item selected contentID:"+contentID);

        boolean added = true;
        if (itemsSelected.contains(contentID)) {
            itemsSelected.removeAll(Arrays.asList(contentID));
            added = false;
        } else {
            itemsSelected.add(contentID);
        }
        Log.d("glerysel","item selected, added:"+added+" size:"+this.itemsSelected.size());
        if (isInSelectionMode && added && this.itemsSelected.size() == 1) {
            galleryView.updateEnabledButtons(true);
        } else if (isInSelectionMode && !added && this.itemsSelected.size() == 0) {
            galleryView.updateEnabledButtons(false);
        }
        galleryView.updateTitleSelectedLabel();
        Log.d("glery"," item selected list after:"+itemsSelected.toString());

        galleryView.setAdapterItemsSelected(itemsSelected);
    }

    public boolean isInSelectionMode() {
        return isInSelectionMode;
    }

    @Override
    public void setInSelectionMode(boolean inSelectionMode) {
        isInSelectionMode = inSelectionMode;
    }

    @Override
    public void setInDeleteMode(boolean inDeleteMode) {

        isInSelectionDelete = inDeleteMode;
    }

    @Override
    public void setInShareMode(boolean inShareMode) {
        isInSelectionShare = inShareMode;
    }

    @Override
    public Boolean getInDeleteMode() {
        return isInSelectionDelete;
    }

    @Override
    public Boolean getInShareMode() {
        return isInSelectionShare;
    }


    @Override
    public void onResponseGetGalleryContentsRequest(ArrayList<GalleryContentRealm> galleryContentList) {
        isLoadingMore = false;

        if (galleryContentList == null)return;
        if (galleryContentList.size() == 0)return;
        if (galleryContentList.size() != 10) noMoreContent = true;

        saveNewGalleryContent(galleryContentList);

    }

    private void saveNewGalleryContent(List<GalleryContentRealm> galleryContentList) {
        galleryDb.insertMultipleContent(galleryContentList);
        /*AsyncTaskSaveGalleryContent asyncTask=new AsyncTaskSaveGalleryContent();
        asyncTask.execute(galleryContentList);*/
    }


    @Override
    public void onFailureGetGalleryContentsRequest(Object error) {
        isLoadingMore = false;
        galleryView.showErrorMessage(error);
    }

    @Override
    public void onResponseGalleryAddContentRequest(JsonObject galleryAddedContent) {
        idPhoto = galleryAddedContent.getAsJsonObject().get("id").getAsInt();

        Log.d("vidrc","onResponseGalleryAddContentRequest request:"+galleryAddedContent.toString());

        AddContentInTheGallery addContentInTheGallery = new AddContentInTheGallery(listener, idPhoto);
        addContentInTheGallery.addOnOnResponse(this);
        addContentInTheGallery.doRequest(userPreferences.getAccessToken());
    }

    @Override
    public void deleteSelectedContent() {
        Log.d("glry","deleteSelectedContent");
        itemsSelectedResponse.clear();
        String accessToken = userPreferences.getAccessToken();
        for (int i = 0; i < itemsSelected.size(); i++) {
            Log.d("glry","deleteSelectedContent item "+i+" id:"+itemsSelected.get(i));
            DeleteGalleryContentRequest deleteGalleryContentRequest = new DeleteGalleryContentRequest(listener, itemsSelected.get(i));
            deleteGalleryContentRequest.addOnOnResponse(this);
            deleteGalleryContentRequest.doRequest(accessToken);
        }

        if(itemsSelected.size() == 0){
            onNewDeleteResponse();
        }
    }

    @Override
    public void onResponseDeleteGalleryContentRequest(final int contentID) {
        numberItemsSelectedResponseCorrect++;
        Log.d("glry","remove responseOK id:"+contentID);
        itemsSelected.removeAll(Arrays.asList(contentID));
        //Delete from device
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = galleryDb.getPath(contentID);
                galleryDb.deleteContentGalleryByID(contentID);
                Media.deleteFile(path);
            }
        }).start();

        onNewDeleteResponse();
    }

    @Override
    public void onFailureDeleteGalleryContentRequest(Object error, int contentID) {
        Log.d("glry","remove responseFAIL id:"+contentID);
        itemsSelectedResponse.add(contentID);
        onNewDeleteResponse();
    }

    synchronized private void onNewDeleteResponse() {
        Log.d("glry","onNewDeleteResponse Number:"+numberItemsSelectedResponseCorrect+" size:"+itemsSelectedResponse.size()  +" size2:"+itemsSelected.size());
        if (itemsSelectedResponse.size() == itemsSelected.size()) {

            /*if (itemsSelected.size() > 0 && itemsSelectedResponse.size() == 0){
                galleryView.onDeleteResults(GalleryView.DELETE_NOT_OK);
            }else*/
            if (itemsSelected.size() == 0) {
                isInSelectionMode = false;
                galleryView.resetSelectMode();
                galleryView.onUpdateIsInSelectionMode();

                itemsSelected.clear();
                itemsSelectedResponse.clear();
                numberItemsSelectedResponseCorrect = 0;

                galleryView.onDeleteResults(GalleryView.DELETE_OK);
            } else if (numberItemsSelectedResponseCorrect == 0) {
                galleryView.onDeleteResults(GalleryView.DELETE_NOT_OK);
            } else {

                galleryView.onDeleteResults(GalleryView.DELETE_PARTIALLY_OK);
            }
            //itemsSelected.clear();
            //itemsSelectedResponse.clear();
            //numberItemsSelectedResponseCorrect = 0;
        }
    }

    @Override
    public void onFailureGalleryAddContentRequest(Object error) {
        Log.d("vidrc","onFailureGalleryAddContentRequest request result:"+error);
        galleryView.closeAlertSavingImage();
        galleryView.showErrorSavingFile(error);
    }

    @Override
    public void onResponseAddContentInTheGallery(int id) {
        saveImage(id);
        galleryView.savingFileOk();
    }

    @Override
    public void onFailureAddContentInTheGallery(Object error) {
        Log.d("vidrc","onFailureAddContentInTheGallery request result:"+error);
        galleryView.closeAlertSavingImage();
        galleryView.showErrorSavingFile(error);
    }


    public ArrayList<Integer> getItemsSelected() {
        return itemsSelected;
    }
    public void resetItemsSelected() {

        this.itemsSelected = new ArrayList<>();

    }

    public String getActionKind() {
        return actionKind;
    }
    public String getFilterKind() {
        return filterKind;
    }

    public void onShareSelectionModeClicked() {

        ArrayList<Integer> items = getItemsSelected();
        ArrayList<String> paths = new ArrayList<>();
        ArrayList<String> metadatas = new ArrayList<>();

        for (Integer id : items) {
            for (GalleryContentRealm galleryContentRealm : galleryContentRealmList) {
                if (galleryContentRealm.getId() == id) {
                    paths.add(galleryContentRealm.getPath());
                    metadatas.add(galleryContentRealm.getMimeType());
                    break;
                }
            }
        }

        galleryView.onShareContentSelectionMode(items, paths, metadatas);
    }

    class VideoCompressAsyncTask extends AsyncTask<String, String, String> {

        Context mContext;

        public VideoCompressAsyncTask(Context context) {
            mContext = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... paths) {
            String filePath = "";
            try {
                MediaExtractor mex = new MediaExtractor();
                try {
                    mex.setDataSource(paths[0]);// the adresss location of the sound on sdcard.
                } catch (IOException e) {

                    e.printStackTrace();
                }
                if (mex.getTrackCount() != 0){
                    MediaFormat mf = mex.getTrackFormat(0);
                    int width = mf.getInteger(MediaFormat.KEY_WIDTH);
                    int height = mf.getInteger(MediaFormat.KEY_HEIGHT);
                    int newWidth = (int) (width * 0.75);
                    int newHeight = (int) (height * 0.75);

                    filePath = SiliCompressor.with(mContext).compressVideo(paths[0], paths[1], newWidth, newHeight, 1500000 );
                }

            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return filePath;

        }


        @Override
        protected void onPostExecute(String compressedFilePath) {
            super.onPostExecute(compressedFilePath);
            File imageFile = new File(compressedFilePath);
            float length = imageFile.length() / 1024f; // Size in KB
            String value;
            if (length >= 1024)
                value = length / 1024f + " MB";
            else
                value = length + " KB";

            Log.d("VideoCompressAsyncTask", "Path compressedFilePath: " + compressedFilePath);
            Log.d("VideoCompressAsyncTask", "Path imageFile: " + imageFile.getPath());

            Log.d("VideoCompressAsyncTask", "size: " + imageFile.length());

            //if compression is not successfull add the video as is
            if (!compressedFilePath.equals("")){
                addContentToGallery(imageFile, "video/mp4");
            }
            else{
                addContentToGallery(videoFile, "video/mp4");
            }


        }
    }

    /*private class AsyncTaskSaveGalleryContent extends AsyncTask<List<GalleryContent>, String, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        @SafeVarargs
        @Override
        protected final Boolean doInBackground(List<GalleryContent>... galleryContentList) {
             for (GalleryContent galleryContent :galleryContentList[0]) {
                if (!galleryDb.existsContentById(galleryContent.getId())) {

                    Realm realm = Realm.getDefaultInstance();

                    galleryDb.insertContent(new GalleryContentRealm(galleryContent.getId(),
                            galleryContent.getIdContent(), galleryContent.getMimeType(),
                            galleryContent.getUser().getId(), galleryContent.getInclusionTime()));

                    GetUser getUserRealm = realm.where(GetUser.class).equalTo("id", galleryContent.getUser().getId()).findFirst();

                    if(getUserRealm == null){
                        Log.d("room id", String.valueOf(galleryContent.getUser().getId()));
                        usersDb.saveGetUser(galleryContent.getUser(), true);
                    }
                }

            }

            return true;
        }
        @Override
        protected void onPostExecute(Boolean bitmap) {
            super.onPostExecute(bitmap);
            galleryView.showGalleryContent(galleryContentRealmList);


        }


    }*/
}
