package cat.bcn.vincles.mobile.Client.Business;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;

public class Media {

    public static final String APP_MEDIA_DIRECTORY = "VINCLES";

    public static String saveFileImage(Context context, InputStream inputStream, String fileName) {
        String fullPath = "";
        try {
            ContextWrapper wrapper = new ContextWrapper(context);
            File file = wrapper.getDir(APP_MEDIA_DIRECTORY,MODE_PRIVATE);
            file = new File(file, fileName);
            fullPath = file.getAbsolutePath();
            Log.d(fullPath,fullPath);
            FileUtils.copyInputStreamToFile(inputStream, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fullPath;
    }

    public static boolean deleteFile(String pathfile) {
        File file = new File(pathfile);
        boolean succeed = file.exists();
        file.delete();
        return  succeed;
    }
}
