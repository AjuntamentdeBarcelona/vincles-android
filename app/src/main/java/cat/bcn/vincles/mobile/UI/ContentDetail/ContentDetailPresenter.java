package cat.bcn.vincles.mobile.UI.ContentDetail;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import cat.bcn.vincles.mobile.Client.Business.Media;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.Client.Requests.DeleteGalleryContentRequest;
import cat.bcn.vincles.mobile.Client.Requests.GetUserPhotoRequest;
import cat.bcn.vincles.mobile.UI.Gallery.GallerypPresenter;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import io.realm.Realm;
import io.realm.RealmResults;

public class ContentDetailPresenter implements ContentDetailPresenterContract, GetUserPhotoRequest.OnResponse, DeleteGalleryContentRequest.OnResponse {

    ContentDetailView contentDetailView;
    GalleryDb galleryDb;
    UsersDb usersDb;
    RealmResults<GalleryContentRealm> galleryContentsRealm;
    ArrayList<GalleryContentRealm> galleryContentsRealmUnmanaged = new ArrayList<>();
    UserPreferences userPreferences;
    GalleryContentRealm contentToBeDeleted;
    int userId = -1;
    String filterKind;
    BaseRequest.RenewTokenFailed listener;
    boolean isAvatarWanted = false;
    String avatarPath;
    Realm realm;

    public ContentDetailPresenter(BaseRequest.RenewTokenFailed listener, ContentDetailView contentDetailView, GalleryDb galleryDb,
                                  UsersDb usersDb, UserPreferences userPreferences, String filterKind, Realm realm) {
        this.listener = listener;
        this.contentDetailView = contentDetailView;
        this.galleryDb = galleryDb;
        this.usersDb = usersDb;
        this.filterKind = filterKind;
        this.userPreferences = userPreferences;
        this.realm = realm;
        galleryContentsRealm = getFilteredMedia();

    }

    @Override
    public void loadOwnerName (int position) {
        GalleryContentRealm galleryContentRealm = getGalleryContentRealmForPosition(position);
        if (galleryContentRealm != null && galleryContentRealm.getUserCreator() != null) {
            userId = galleryContentRealm.getUserCreator().getId();
        }
        setOwnerName();
    }

    private GalleryContentRealm getGalleryContentRealmForPosition(int position) {
        if (galleryContentsRealm.size() > position) {
                GalleryContentRealm galleryContentRealm = galleryContentsRealm.get(position);
                if (galleryContentRealm != null)
                return realm.copyFromRealm(galleryContentRealm);
        }
        return null;
    }

    private void setOwnerName() {
        if (contentDetailView == null) return;

        String name = null;
        String lastName = null;

        GetUser user = usersDb.findUserUnmanaged(userId);
        if(user != null) {
            name = user.getName();
            lastName = user.getLastname();
        }

        if(name==null && lastName==null) {
            contentDetailView.setOwnerName(userPreferences.getName() + " " + userPreferences.getLastName());
        } else {
            contentDetailView.setOwnerName(name + " " + lastName);
        }
    }

    public void onUserUpdated(int userId) {
        if (this.userId == userId) {
            setOwnerName();
            if (isAvatarWanted) {
                GetUser user = usersDb.findUserUnmanaged(userId);
                if (!avatarPath.equals(user.getPhoto())) {
                    avatarPath = user.getPhoto();
                    setAvatarPath();
                }
            }
        }
    }

    @Override
    public void updateUserID (int position) {
        GalleryContentRealm galleryContentRealm = getGalleryContentRealmForPosition(position);
        if (galleryContentRealm != null
                &&  galleryContentRealm.getUserCreator() != null) {
            GetUser getUser = usersDb.findUserUnmanaged(galleryContentRealm.getUserCreator().getId());
            if (getUser!=null)
            userId = getUser.getId();
        }
    }

    @Override
    public void loadDate (Context context, int position) {
        GalleryContentRealm galleryContentRealm = getGalleryContentRealmForPosition(position);
        if (galleryContentRealm==null)return;
        long timpeStamp = galleryContentRealm.getInclusionTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timpeStamp);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        String formatedTime = DateUtils.getFormattedHourMinutesFromMillis(context, timpeStamp);

        contentDetailView.setDate(dayOfMonth,month,year,formatedTime);
    }

    @Override
    public void loadAvatar(int position) {
        isAvatarWanted = true;

        if (galleryContentsRealm == null)return;

       // GalleryContentRealm galleryContentRealm =  galleryContentsRealm.get(position);
        GalleryContentRealm galleryContentRealm = getGalleryContentRealmForPosition(position);

        if (galleryContentRealm == null) return;

        if (galleryContentRealm.getUserCreator() == null) return;

        GetUser userForId = usersDb.findUserUnmanaged(galleryContentRealm.getUserCreator().getId());
        if (userForId == null) return;

        userId = userForId.getId();

        GetUser user = usersDb.findUserUnmanaged(userId);
        if (user == null)return;

        avatarPath = user.getPhoto();
        setAvatarPath();
    }

    private void setAvatarPath() {
        if (avatarPath != null && !avatarPath.equals("")) {
            contentDetailView.showAvatar(avatarPath);
        } else {
            contentDetailView.showAvatar("placeholder");
            String accessToken = userPreferences.getAccessToken();
            GetUserPhotoRequest getUserPhotoRequest = new GetUserPhotoRequest(listener, String.valueOf(userId));
            getUserPhotoRequest.addOnOnResponse(this);
            getUserPhotoRequest.doRequest(accessToken);
        }
    }

    /*@Override
    public void saveAvatarPath(int position, String filePath) {
        int userID = usersDb.findUserUnmanaged(galleryContentsRealm.get(position).getUserId()).getId();
        usersDb.setPathAvatarToUser(userID,filePath);
        galleryContentsRealm = getFilteredMedia();
    }*/

    public RealmResults<GalleryContentRealm> getFilteredMedia() {

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
    public void deleteContent (int position) {

        if (position >= 0 && galleryContentsRealm.size() > 0) {

            contentToBeDeleted = getGalleryContentRealmForPosition(position);
            if (contentToBeDeleted==null)return;
            int contentID = contentToBeDeleted.getId();
            String accesToken = userPreferences.getAccessToken();
            DeleteGalleryContentRequest deleteGalleryContentRequest = new DeleteGalleryContentRequest(listener, contentID);
            deleteGalleryContentRequest.addOnOnResponse(this);
            deleteGalleryContentRequest.doRequest(accesToken);
        } else {
            contentDetailView.showErrorRemovingContent();
        }
    }

    @Override
    public void onResponseGetUserPhotoRequest(Uri photo, String userID, int viewID, int contactType) {
        usersDb.setPathAvatarToUser(Integer.valueOf(userID), photo.getPath());
        if (this.userId == Integer.valueOf(userID) && contentDetailView.getAvatarPath().equals("placeholder")) {
            contentDetailView.showAvatar(photo.getPath());
        }
    }

    @Override
    public void onFailureGetUserPhotoRequest(Object error, String userID, int viewID, int contactType) {
        contentDetailView.showError(error);
    }

    @Override
    public void onResponseDeleteGalleryContentRequest(int contentID) {
        String path = contentToBeDeleted.getPath();
        galleryDb.deleteContentGalleryByID(contentID);

        boolean isRemoveSucced = Media.deleteFile(path);

        if (isRemoveSucced) {
            contentDetailView.removedContent();
        } else {
            contentDetailView.showErrorRemovingContent();
        }
    }

    @Override
    public void onFailureDeleteGalleryContentRequest(Object error, int contentID) {

        try{
            contentDetailView.showError(error);
        }catch (Exception e){
            Log.e("Exception", "Error");
        }

    }
}
