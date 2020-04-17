package cat.bcn.vincles.mobile.UI.Gallery;

import android.net.Uri;

import java.io.File;

public interface GalleryPresenterContract {

    static final String FILTER_ALL_FILES = "filter_all_files";
    static final String FILTER_ALL_MY_FILES = "filter_my_files";
    static final String FILTER_RECIVED_FILES = "filter_recived_files";
    static final String ACTION_DELETE = "action_delete";
    static final String ACTION_SHARE = "action_share";

    void getContent(long to);
    void pushImageToAPI(Object picture, boolean isUri);
    void pushVideoToAPI(Uri fileUri);
    void saveImage(int idContent);
    void filterMedia(String filterKind);
    void itemSelected(int contentID, int index);
    void deleteSelectedContent();
   // void getGalleryPathByContentId(int id, int contentId);
    void setInSelectionMode(boolean inSelectionMode);
    void setInShareMode(boolean inShareMode);
    void setInDeleteMode(boolean inDeleteMode);
    Boolean getInShareMode();
    Boolean getInDeleteMode();
    void onCreateView();
}
