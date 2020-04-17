package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.util.Log;
import cat.bcn.vincles.mobile.Client.Db.GalleryDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import cat.bcn.vincles.mobile.Client.Db.UsersDb;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Client.Requests.BaseRequest;
import cat.bcn.vincles.mobile.UI.Gallery.GallerypPresenter;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class ContentDetailAugmentedPresenter implements ContentDetailAugmentedPresenterContract {

    GalleryDb galleryDb;
    RealmResults<GalleryContentRealm> galleryContentsRealm;
    String filterKind;
    Realm realm;

    ContentDetailAugmentedView contentDetailAugmentedView;

    public ContentDetailAugmentedPresenter(GalleryDb galleryDb, String filterKind, Realm realm, ContentDetailAugmentedView cDaV) {
        this.galleryDb = galleryDb;
        this.filterKind = filterKind;
        this.realm = realm;
        galleryContentsRealm = getFilteredMedia();
        this.contentDetailAugmentedView = cDaV;
        this.galleryContentsRealm.addChangeListener(new RealmChangeListener<RealmResults<GalleryContentRealm>>() {
            @Override
            public void onChange(RealmResults<GalleryContentRealm> galleryContentRealms) {
                contentDetailAugmentedView.contentAdded();
            }
        });
    }

    public RealmResults<GalleryContentRealm> getFilteredMedia() {
        Log.d("flt","cont det filter:"+filterKind);
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


}
