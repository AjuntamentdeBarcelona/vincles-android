package cat.bcn.vincles.mobile.UI.ContentDetail;


import android.graphics.Bitmap;
import android.net.Uri;

public interface ContentDetailView {
    void setOwnerName(String ownerName);
    int getCurrentPage();
    void setDate(int day, int month, int year, String formatedTime);
    void showAvatar(String path);
    void showError(Object error);
    void showConfirmationRemoveContent();
    void removedContent();
    void showErrorRemovingContent();
    void showErrorOpeningImage();
    String getAvatarPath();

    void contentAdded();

    void isLoadingMore(boolean isLoading);
}
