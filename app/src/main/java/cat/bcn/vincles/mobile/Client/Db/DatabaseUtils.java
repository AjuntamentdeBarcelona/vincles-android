package cat.bcn.vincles.mobile.Client.Db;


import android.app.ActivityManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.util.Objects;

import cat.bcn.vincles.mobile.Client.Business.CalendarSyncManager;
import cat.bcn.vincles.mobile.Client.NetworkUsage.DataUsageUtils;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import io.realm.Realm;

import static android.content.Context.ACTIVITY_SERVICE;

public class DatabaseUtils {

    public static void dropAllTables() {

        Context context = MyApplication.getAppContext();

        //clear user preferences
        new UserPreferences(context).clear();

        //remove events from native calendar
        CalendarSyncManager calendarSyncManager = new CalendarSyncManager();
        calendarSyncManager.deleteCalendar();

        //deleteData realm databases
        GalleryDb galleryDb = new GalleryDb(context);
        galleryDb.dropTable();
        UserGroupsDb userGroupsDb = new UserGroupsDb(context);
        userGroupsDb.dropTable();
        UsersDb usersDb = new UsersDb(context);
        usersDb.dropTable();
        UserMessageDb userMessageDb = new UserMessageDb(context);
        userMessageDb.dropTable();
        GroupMessageDb groupMessageDb = new GroupMessageDb(context);
        groupMessageDb.dropTable();
        NotificationsDb notificationsDb = new NotificationsDb(context);
        notificationsDb.dropTable();
        MeetingsDb meetingsDb = new MeetingsDb(context);
        meetingsDb.dropTable();
        Realm realm = Realm.getDefaultInstance();


        try{
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(@NonNull Realm realm) {
                    realm.deleteAll();

                }
            });
        }
        finally {
            realm.close();
        }





        //deleteData photos
        clearApplicationData(context);
    }


    public static void clearApplicationData(Context context) {
        File cacheDirectory = context.getCacheDir();
        File applicationDirectory = new File(cacheDirectory.getParent());
        if (applicationDirectory.exists()) {
            String[] fileNames = applicationDirectory.list();
            for (String fileName : fileNames) {
                if (!fileName.equals("lib")) {
                    deleteFile(new File(applicationDirectory, fileName));
                }
            }
        }
    }

    public static boolean deleteFile(File file) {
        Log.d("dlfiles","Delete file: "+file.getPath());
        String path = file.getPath();
        boolean deletedAll = true;
        if (file != null) {
            if (file.isDirectory()) {
                String[] children = file.list();
                for (int i = 0; i < children.length; i++) {
                    deletedAll = deleteFile(new File(file, children[i])) && deletedAll;
                }
            } else if ((path.endsWith(".jpg") || path.endsWith(".jpeg") || path.endsWith(".mp4")
                    || path.endsWith(".aac") || path.endsWith(".mp3"))) {
                deletedAll = file.delete();
            } else {
                return true;
            }
        }

        return deletedAll;
    }
}
