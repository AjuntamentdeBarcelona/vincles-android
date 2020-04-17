package cat.bcn.vincles.mobile.UI.ContentDetail;

import android.content.Context;

public interface ContentDetailPresenterContract {
    void loadOwnerName(int position);
    void updateUserID(int position);
    void loadDate(Context context, int position);
    void loadAvatar(int position);
   // void saveAvatarPath(int position, String filePath);
    void deleteContent(int position);
}
