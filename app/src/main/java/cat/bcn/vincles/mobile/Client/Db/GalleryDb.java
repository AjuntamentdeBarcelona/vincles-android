package cat.bcn.vincles.mobile.Client.Db;


import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class GalleryDb extends BaseDb{

    Context context;

    public GalleryDb (Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public void dropTable() {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm bgRealm) {
                    bgRealm.delete(GalleryContentRealm.class);
                }
            });
        }
    }

    public void insertContent(final GalleryContentRealm galleryContentRealm) {
        if (!existsContentById(galleryContentRealm.getId())) {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        int userId = galleryContentRealm.getUserId();
                        if (galleryContentRealm.getUserCreator()!=null){
                            userId = galleryContentRealm.getUserCreator().getId();
                        }
                        GetUser getUser = realm.where(GetUser.class).equalTo("id", userId).findFirst();
                        UserPreferences userPreferences = new UserPreferences();

                        if (getUser==null && userId == userPreferences.getUserID()){
                            getUser = userPreferences.getUser();
                            realm.copyToRealmOrUpdate(getUser);
                        }
                        galleryContentRealm.setUserCreator(getUser);
                        realm.copyToRealmOrUpdate(galleryContentRealm);
                    }
                });
            }
        }
    }

    public boolean existsContentById(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<GalleryContentRealm> galleryContentRealmsList = realm.where(GalleryContentRealm.class)
                    .equalTo("id", id)
                    .findAll();
            return galleryContentRealmsList.size() > 0;
        }
    }

    public GalleryContentRealm getContentById(int id) {
        try (Realm realm = Realm.getDefaultInstance()) {
            GalleryContentRealm galleryContentRealm = realm.where(GalleryContentRealm.class)
                    .equalTo("id", id)
                    .findFirst();
            if (galleryContentRealm != null) {
                return realm.copyFromRealm(galleryContentRealm);
            }
            return null;
        }
    }

    public String getPathFromIdContent(int idContent) {
        try (Realm realm = Realm.getDefaultInstance()) {
            GalleryContentRealm contentRealm = realm.where(GalleryContentRealm.class)
                    .equalTo("idContent", idContent)
                    .findFirst();

             if (contentRealm!= null && contentRealm.getPath() != null && contentRealm.getPath().length() > 0) {
                return contentRealm.getPath();
             }
            return null;
        }
    }

    public void setPathFromIdContent(final int idContent, final String path) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<GalleryContentRealm> results = realm.where(GalleryContentRealm.class)
                            .equalTo("idContent", idContent)
                            .findAll();
                    for (GalleryContentRealm contentRealm : results) {
                        contentRealm.setPath(path);
                        if (contentRealm.getThumbnailPath() != null) {
                            if (contentRealm.getMimeType().startsWith("video") && contentRealm.getThumbnailPath().equals("")) {
                                contentRealm.setThumbnailPath(ImageUtils.generateVideoThumbnail(context, path));
                            }
                        } else if (contentRealm.getThumbnailPath() == null) {
                            if (contentRealm.getMimeType().startsWith("video")) {
                                contentRealm.setThumbnailPath(ImageUtils.generateVideoThumbnail(context, path));
                            }
                        }
                    }
                }
            });
        }
    }

    public RealmResults<GalleryContentRealm> findContentNotDownloaded(Realm realm) {
        return realm.where(GalleryContentRealm.class)
                .equalTo("path", "")
                .findAll();
    }

    //Realm Instance closed on Fragment
    public RealmResults<GalleryContentRealm> findAll(Realm realm) {
         return realm.where(GalleryContentRealm.class)
                    .sort("inclusionTime", Sort.DESCENDING)
                    .findAll();
    }

    public void setPathToFile(final int id, final String path) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    GalleryContentRealm galleryContentRealm = realm.where(GalleryContentRealm.class)
                            .equalTo("id", id)
                            .findFirst();
                    if (galleryContentRealm != null) {
                        galleryContentRealm.setPath(path);
                    }
                }
            });

        }
    }

    public void deleteContentGalleryByID(final int contentID) {

        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    RealmResults<GalleryContentRealm> galleryContentRealm = realm.where(GalleryContentRealm.class)
                            .equalTo("id", contentID)
                            .findAll();
                    galleryContentRealm.deleteAllFromRealm();
                }
            });
        }
    }
    //Realm Instance closed on Fragment
    public RealmResults<GalleryContentRealm> getContentsPathByUserID (Realm realm) {

        UserPreferences userPreferences = new UserPreferences(context);
        GetUser user = userPreferences.getUser();

        return realm.where(GalleryContentRealm.class)
                .sort("inclusionTime", Sort.DESCENDING)
                .equalTo("userCreator.id",user.getId())
                .findAll();
    }

    //Realm Instance closed on Fragment
    public RealmResults<GalleryContentRealm> getRecivedContentsPath (Realm realm) {
        UserPreferences userPreferences = new UserPreferences(context);
        int logedUserid = userPreferences.getUserID();

        return realm.where(GalleryContentRealm.class)
                .sort("inclusionTime", Sort.DESCENDING)
                .notEqualTo("userCreator.id",logedUserid)
                .findAll();
    }

    public long getLastContentTime() {

        try(Realm realm = Realm.getDefaultInstance()){
            RealmResults<GalleryContentRealm> galleryContentRealm =  realm.where(GalleryContentRealm.class)
                    .sort("inclusionTime", Sort.DESCENDING)
                    .findAll();
            if (galleryContentRealm==null ||
                    galleryContentRealm.size() == 0 ||
                    galleryContentRealm.get(galleryContentRealm.size() - 1) == null)return 0;

            GalleryContentRealm galleryContentRealm1 = realm.copyFromRealm(Objects.requireNonNull(galleryContentRealm.get(galleryContentRealm.size() - 1)));

            return galleryContentRealm1.getInclusionTime();
        }
    }

    public String getPath(int contentID) {
        try (Realm realm = Realm.getDefaultInstance()) {
            GalleryContentRealm galleryContentRealm = realm.where(GalleryContentRealm.class)
                    .equalTo("id", contentID)
                    .findFirst();
            if (galleryContentRealm != null) {
                return galleryContentRealm.getPath();
            }
            return "";
        }
    }

    public ArrayList<Integer> getIdContentFromIds(Integer[] idArray) {
        ArrayList<Integer> returnArr = new ArrayList<>();
        try (Realm realm = Realm.getDefaultInstance()) {
            RealmResults<GalleryContentRealm> galleryContentRealmResults = realm.where(GalleryContentRealm.class)
                    .in("id", idArray)
                    .findAll();
            if (galleryContentRealmResults != null) {
                for (GalleryContentRealm galleryContentRealm : galleryContentRealmResults){
                    returnArr.add(galleryContentRealm.getIdContent());
                }
            }
            return returnArr;
        }
    }

    public ArrayList<String> getMetadataTipusFromArray(Integer[] ids) {

        ArrayList<String> returnArr = new ArrayList<>();

        try (Realm realm = Realm.getDefaultInstance()) {

            RealmResults<GalleryContentRealm> galleryContentRealmResults = realm.where(GalleryContentRealm.class)
                    .in("id", ids)
                    .findAll();
            if (galleryContentRealmResults!= null){
                for (GalleryContentRealm galleryContentRealm : galleryContentRealmResults){
                    String mimeType = galleryContentRealm.getMimeType();
                    String metadataTipus = "";
                    if(mimeType.contains("video")){
                        metadataTipus = "VIDEO_MESSAGE";
                    }
                    else{
                        metadataTipus = "IMAGES_MESSAGE";
                    }
                    returnArr.add(metadataTipus);
                }
            }
            return returnArr;
        }

    }

    public void insertMultipleContent(final List<GalleryContentRealm> galleryContents) {
        try (Realm realm = Realm.getDefaultInstance()) {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.insertOrUpdate(galleryContents);
                }
            });
        }
    }
}
