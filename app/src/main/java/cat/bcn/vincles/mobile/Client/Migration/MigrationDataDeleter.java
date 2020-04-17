package cat.bcn.vincles.mobile.Client.Migration;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;

public class MigrationDataDeleter {

    private static final String VINCLES = "VINCLES";

    public static void deleteData(Context context) {

        //deleting old shared preferences
        String folderPath = context.getApplicationInfo().dataDir + "/shared_prefs/";
        File sharedPreferenceFile = new File(folderPath);
        File[] listFiles = sharedPreferenceFile.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                boolean deleted = file.delete();
            }
        }

        //deleting old SQL database
        context.deleteDatabase(Fase1SQLiteHelper.MIGRATION_DB_NAME);

        //delete external media files
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            File videosDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES), VINCLES);
            File audiosDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MUSIC), VINCLES);
            File picturesDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), VINCLES);

            deleteRecursive(videosDir);
            deleteRecursive(audiosDir);
            deleteRecursive(picturesDir);
        }
    }

    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

}
