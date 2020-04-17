package cat.bcn.vincles.mobile.UI.Gallery;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import cat.bcn.vincles.mobile.Client.Db.Model.GalleryContentRealm;
import io.realm.RealmResults;

interface GalleryView {

    static final int DELETE_OK = 0;
    static final int DELETE_PARTIALLY_OK = 1;
    static final int DELETE_NOT_OK = 2;

    void showErrorMessage (Object error);
    void showErrorSavingFile(Object error);
    void savingFileOk();
    void savingFilePictureIsUri(Uri uri);
    void closeAlertSavingImage();
    void updateContents(RealmResults<GalleryContentRealm> contentPaths);
    void onUpdateIsInSelectionMode();
    void onDeleteResults(int results);
    void onFileAdded();
    void updateTitleSelectedLabel();
    boolean checkWriteExternalStoragePermission();
    void updateEnabledButtons(boolean enable);
    void showGalleryContent(List<GalleryContentRealm> galleryContentRealmList);
    void onShareContentSelectionMode(ArrayList<Integer> itemIDs, ArrayList<String> paths,
                                     ArrayList<String> metadata);
    void resetSelectMode();
    void setAdapterItemsSelected(ArrayList<Integer> itemsSelected);

}
