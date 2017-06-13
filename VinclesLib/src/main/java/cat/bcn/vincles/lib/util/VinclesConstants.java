/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VinclesConstants {
    public static final String PREFERENCES_TYPE_STRING = "string";
    public static final String PREFERENCES_TYPE_FLOAT = "float";
    public static final String PREFERENCES_TYPE_BOOLEAN = "boolean";
    public static final String PREFERENCES_TYPE_INT = "int";
    public static final String PREFERENCES_TYPE_LIST = "list";
    public static final String PREFERENCES_TYPE_LONG = "long";

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 98;
    public static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 99;
    public static final int PHONE_STATE_PERMISSION_REQUEST_CODE = 100;
    public static final int IMAGE_MIN_WIDTH = 640;
    public static final int IMAGE_MIN_HEIGHT = 480;
    public static final float IMAGE_REDUCTION = 0.3f;

    public static final int AUDIO_ENCODING_BIT_RATE = 96000; // bits per second - bps (9600 | 44100)
    public static final int AUDIO_SAMPLING_RATE = 8000; // samples per second - Hz (8000 | 44100)
    public static final int AUDIO_CHANNELS = 1; // mono(1), stereo(2)

    public static final int VIDEO_QUALITY = 0; // low(0), high(1)
    public static final long VIDEO_SIZE_LIMIT = 10485760L; //10 MB * 1024 * 1024
    public static final int VIDEO_DURATION_LIMIT = 120; // in seconds

    public static final String GENDER_MALE = "MALE";
    public static final String GENDER_FEMALE = "FEMALE";

    public class TYPEFACE {
        public static final String REGULAR  = "fonts/Akkurat.ttf";
        public static final String BOLD     = "fonts/Akkurat-Bold.ttf";
        public static final String LIGHT    = "fonts/Akkurat-Light";
    }

    public enum TASK_STATE {
        PENDING, ACCEPTED, REJECTED
    }

    public static final String LOGIN_SUFFIX = "@suffix.org";

    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String IMAGE_PREFIX = "imagen";
    public static final String IMAGE_USER_PREFIX = "user";
    public static final String IMAGE_TEMP = "temp";
    public static final String IMAGE_RESIZE_TEMP = "rsz";
    public static final String VIDEO_EXTENSION = ".mp4";
    public static final String VIDEO_PREFIX = "video";
    public static final String AUDIO_EXTENSION = ".aac";
    public static final String AUDIO_PREFIX = "audio";
    public static final String APP_MEDIA_DIRECTORY = "VINCLES";
    public static final String[] USER_TYPES = {"PARTNER", "CHILD", "GRANDCHILD", "OTHER", "FRIEND", "VOLUNTEER", "CAREGIVER", "SIBLING", "NEPHEW"};

    public final class RESOURCE_TYPE {
        public static final String VIDEO_MESSAGE = "VIDEO_MESSAGE";
        public static final String AUDIO_MESSAGE = "AUDIO_MESSAGE";
        public static final String TEXT_MESSAGE = "TEXT_MESSAGE";
        public static final String IMAGES_MESSAGE = "IMAGES_MESSAGE";
    }

    public static final int WIFI_SIGNAL_NUMBER = 5;
    public static final int WIFI_SIGNAL_TOP = 4;
    public static final int WIFI_SIGNAL_BOTTOM = 3;
    public static final int TELEPHONY_SIGNAL_TOP = 3;
    public static final int TELEPHONY_SIGNAL_BOTTOM = 2;

    public static Boolean hasKnownType(String value) {
        Boolean result = false;
        if (value != null) {
            if (value.equals(VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE.toString())
                    || value.equals(VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE.toString())
                    || value.equals(VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE.toString())
                    || value.equals(VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE.toString())) {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    public static long getDayInterval(Date d1, Date d2) {
        final long DAY_MILLIS = 1000 * 60 * 60 * 24;
        long day1 = d1.getTime() / DAY_MILLIS;
        long day2 = d2.getTime() / DAY_MILLIS;
        return (day1 - day2);
    }

    public static String getDateString(Date date, String pattern, Locale locale) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
            String result = sdf.format(date);

            return result;
        } catch (Exception e) {
            return "";
        }
    }

    public static Date getStringDate(String value, String pattern, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);

        Date result = null;
        try {
            result = sdf.parse(value);
        } catch (ParseException e) {
            result = new Date();
        }

        return result;
    }

    public static byte[] getByteFromBitmap(Bitmap bm) {
        byte[] result = null;

        if (bm != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            result = baos.toByteArray();
        }
        return result;
    }

    public static byte[] resizeAndRotate(byte[] data) {
        byte[] result = null;
        if (data != null) {
            ExifInterface exif = getExifInterface(data, IMAGE_RESIZE_TEMP + IMAGE_EXTENSION);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap bitmap = resizeByte(data);
            Bitmap rotatedBitmap = rotate(bitmap, orientation);
            result = getByteFromBitmap(rotatedBitmap);
        }
        return result;
    }

    private static Bitmap resizeByte(byte[] data) {
        Bitmap bitmap = null;
        if (data != null) {
            bitmap = ImageUtils.decodeSampledBitmapFromByte(data, VinclesConstants.IMAGE_MIN_WIDTH, VinclesConstants.IMAGE_MIN_HEIGHT);
        }
        return bitmap;
    }

    private static ExifInterface getExifInterface(byte[] data, String filename) {
        ExifInterface originalExif = null;
        if (data != null) {
            try {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), VinclesConstants.APP_MEDIA_DIRECTORY);
                File file = new File(dir.getAbsolutePath(), filename);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(data);
                bos.flush();
                bos.close();
                originalExif = new ExifInterface(file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return originalExif;
    }


    public static Bitmap getBitmapFromByte(byte[] bytes) {
        System.gc();

        // EXIF ROTATION CHECK BEFORE USING BITMAP
        Bitmap bitmap = null;
        if (bytes != null && bytes.length > 0) {
            try {
                File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), VinclesConstants.APP_MEDIA_DIRECTORY);
                File file = new File(dir.getAbsolutePath(), IMAGE_TEMP + IMAGE_EXTENSION);
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
                bos.write(bytes);
                bos.flush();
                bos.close();

                Bitmap original = BitmapFactory.decodeFile(file.getAbsolutePath());
                ExifInterface originalExif = new ExifInterface(file.getAbsolutePath());

                int orientation = originalExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
                bitmap = rotate(original, orientation);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bitmap == null) {
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        }
        return bitmap;
    }

    private static Bitmap rotate(Bitmap original, int orientation) {
        Bitmap result = null;
        if (original != null) {
            Matrix matrix = new Matrix();
            int rotate = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                rotate = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                rotate = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                rotate = 270;
            }

            matrix.postRotate(rotate);

            result = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        }
        return result;
    }

    public static String getB64FromBitmap(Bitmap bm) {
        String result = "cGhvdG8="; // "cGhvdG8=" >> "photo"

        byte[] b = getByteFromBitmap(bm);
        if (b != null) {
            result = Base64.encodeToString(b, Base64.DEFAULT);
        }

        return result;
    }

    public static byte[] getByteFromBase64(String b64) {
        byte[] result = null;
        if (b64 != null) {
            result = Base64.decode(b64.getBytes(), Base64.DEFAULT);
        }
        return result;
    }

    public static int getMinutesInterval(Date date) {
        try {
            Date now = new Date();
            int diffInMinutes = (int) ((now.getTime() - date.getTime()) / (1000 * 60));
            return diffInMinutes;
        } catch (Exception e) {
            return 0;
        }
    }

    public static Calendar getCalendarWithoutTime(Calendar date) {
        date.set(Calendar.HOUR_OF_DAY,0);
        date.set(Calendar.MINUTE,0);
        date.set(Calendar.SECOND,0);
        date.set(Calendar.MILLISECOND,0);
        return date;
    }

    public static File getImageDirectory() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), VinclesConstants.APP_MEDIA_DIRECTORY);
        // Create the storage directory if it does not exist
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    public static File getVideoDirectory() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), VinclesConstants.APP_MEDIA_DIRECTORY);
        // Create the storage directory if it does not exist
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    public static File getAudioDirectory() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), VinclesConstants.APP_MEDIA_DIRECTORY);
        // Create the storage directory if it does not exist
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                return null;
            }
        }
        return dir;
    }

    public static String getImagePath() {
        return getImageDirectory().getAbsolutePath() + "/";
    }

    public static String getVideoPath() {
        return getVideoDirectory().getAbsolutePath() + "/";
    }

    public static String getAudioPath() {
        return getAudioDirectory().getAbsolutePath() + "/";
    }

    public static void saveImage(byte[] data, String filename) {
        FileOutputStream outputStream;
        try {
            File file = new File(VinclesConstants.getImageDirectory(), filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAudio(byte[] data, String filename) {
        FileOutputStream outputStream;

        try {
            File file = new File(VinclesConstants.getAudioDirectory(), filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveVideo(byte[] data, String filename) {
        FileOutputStream outputStream;

        try {
            File file = new File(VinclesConstants.getVideoDirectory(), filename);
            outputStream = new FileOutputStream(file);
            outputStream.write(data);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteImage(String filename) {
        File file = new File(VinclesConstants.getImagePath() + "/" + filename);
        return file.delete();
    }

    public static boolean deleteVideo(String filename) {
        File file = new File(VinclesConstants.getVideoPath() + "/" + filename);
        return file.delete();
    }

    public static boolean deleteAudio(String filename) {
        File file = new File(VinclesConstants.getAudioPath() + "/" + filename);
        return file.delete();
    }

    public static boolean isEmailValid(CharSequence target) {
        if (target == null)
            return false;

        return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
