package cat.bcn.vincles.mobile.Utils;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cat.bcn.vincles.mobile.Client.Db.GroupMessageDb;
import cat.bcn.vincles.mobile.Client.Db.Model.GroupRealm;
import cat.bcn.vincles.mobile.Client.Db.UserGroupsDb;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import io.realm.RealmList;

public class OtherUtils {

    private static final int DEFAULT_THRESHOLD_MEGABYTES = 100;
    public static final String ALERT_TYPE_PERMISSIONS = "SETTINGS_PERMISSIONS"; // TODO: 5/9/19 Use this in the whole app, remove duplicates from other places
    public static final int MY_PERMISSIONS_REQUEST_CAMERA_PHOTO = 2; // TODO: 5/9/19 Use this in the whole app, remove duplicates from other places


    public static boolean activityCannotShowDialog(Activity activity) {
        return activity == null || activity.isFinishing()
                || ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                        && activity.isDestroyed());
    }

    public static int[] convertIntegers(List<Integer> integers)
    {
        if(integers == null){
            return null;
        }
        int[] ret = new int[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i).intValue();
        }
        return ret;
    }

    public static Integer[] convertIntegersP(List<Integer> integers)
    {
        if(integers == null){
            return null;
        }
        Integer[] ret = new Integer[integers.size()];
        for (int i=0; i < ret.length; i++)
        {
            ret[i] = integers.get(i);
        }
        return ret;
    }

    public static ArrayList<Integer> convertIntegers(int[] integers) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i=0; i < integers.length; i++) {
            list.add(integers[i]);
        }
        return list;
    }

    public static RealmList<Integer> convertIntegersToRealmList(int[] integers) {
        RealmList<Integer> list = new RealmList<>();
        for (int i=0; i < integers.length; i++) {
            list.add(integers[i]);
        }
        return list;
    }

    public static RealmList<Integer> convertIntegersToRealmList(ArrayList<Integer> integers) {
        RealmList<Integer> list = new RealmList<>();
        if (integers != null) {
            for (int i=0; i < integers.size(); i++) {
                list.add(integers.get(i));
            }
        }
        return list;
    }

    public static RealmList<String> convertStringsToRealmList(ArrayList<String> strings) {
        RealmList<String> list = new RealmList<>();
        if (strings != null)
            list.addAll(strings);
        return list;
    }


    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_VIDEO_CAPTURE = 2;

    /**
     * Sends a MediaStore.ACTION_IMAGE_CAPTURE intent and returns the path where the photo file will
     * eventually be saved. The result of the intent's execution is handled by an Activity.
     * @param activity the activity that should handle the result, through its onActivityResult(),
     *                 of the intent's execution.
     * @param isAvatar whether the resulting photo file should be saved as an avatar.
     * @return the path where the photo file will eventually be saved; null if the activity is not
     * an instance of AlertMessage.AlertMessageInterface.
     */
    public static String sendPhotoIntent(@NonNull Activity activity, boolean isAvatar) {
        return OtherUtils.sendPhotoIntent(activity, null, isAvatar);
    }

    /**
     * Sends a MediaStore.ACTION_IMAGE_CAPTURE intent and returns the path where the photo file will
     * eventually be saved. The result of the intent's execution is handled by a Fragment.
     * @param fragment the fragment that should handle the result, through its onActivityResult(),
     *                of the intent's execution.
     * @param isAvatar whether the resulting photo file should be saved as an avatar.
     * @return the path where the photo file will eventually be saved; null if the fragment is not
     * an instance of AlertMessage.AlertMessageInterface.
     * @throws IllegalArgumentException if the fragment is not associated with an Activity or the
     * fragment is not an instance of AlertMessage.AlertMessageInterface
     */
    public static String sendPhotoIntent(@NonNull Fragment fragment, boolean isAvatar) {
        if (fragment.getActivity() == null) {
            throw new IllegalArgumentException("The fragment has a null activity associated or is associated to a Context");
        }

        return OtherUtils.sendPhotoIntent(fragment.getActivity(), fragment, isAvatar);
    }

    /**
     * Sends a MediaStore.ACTION_IMAGE_CAPTURE intent and returns the path where the photo file will
     * eventually be saved.
     * @param activity
     * @param fragment
     * @param isAvatar
     * @return the path where the photo file will eventually be saved; null if the activity is not
     * an instance of AlertMessage.AlertMessageInterface.
     * @throws IllegalArgumentException if the fragment is not an instance of AlertMessage.AlertMessageInterface
     * when the fragment is not null, or if the activity is not an instance of AlertMessage.AlertMessageInterface
     * when the fragment is null.
     */
    private static String sendPhotoIntent(Activity activity, Fragment fragment, boolean isAvatar) {
        // If there is no AlertMessage.AlertMessageInterface involved, do not send the intent
        AlertMessage.AlertMessageInterface ami = null;
        if (fragment instanceof AlertMessage.AlertMessageInterface) {
            ami = (AlertMessage.AlertMessageInterface) fragment;
        } else if (activity instanceof AlertMessage.AlertMessageInterface) {
            ami = (AlertMessage.AlertMessageInterface) activity;
        } else {
            throw new IllegalArgumentException("Cannot send the ACTION_IMAGE_CAPTURE intent: There is no AlertMessage.AlertMessageInterface involved.");
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                if (isAvatar) {
                    photoFile = ImageUtils.createAvatarFile(activity);
                } else {
                    photoFile = ImageUtils.createImageFile(activity);
                }
                Uri photoURI = null;
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                    photoURI = Uri.fromFile(photoFile);
                } else {
                    photoURI = FileProvider.getUriForFile(activity,
                            "com.example.android.fileprovider",
                            photoFile);
                }
                if (ContextCompat.checkSelfPermission(activity,
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    if (fragment != null) {
                        SharedPreferences sp = fragment.getActivity().getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("SHOWING_CAMERA", true);
                        editor.commit();

                        fragment.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    } else {
                        SharedPreferences sp = activity.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("SHOWING_CAMERA", true);
                        editor.commit();
                        activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }

                } else {
                    boolean ask = true;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        SharedPreferences sp = activity.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                        if(!sp.getBoolean("CAMERA", false)){
//                            ask = true;
                        } else {
                            if (!activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                                ask = false;
                            }
                        }
                    }

                    if(ask){
                        SharedPreferences sp = activity.getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("CAMERA", true);
                        editor.apply();
                        ActivityCompat.requestPermissions(activity,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA_PHOTO);
                    }
                    else{
                        showSettingsAlert(activity, ami, activity.getResources().getString(R.string.should_accept_permissions_camera_photo));
                    }

                }
                return photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                //TODO handle error
            }
        }
        return null;
    }

    private static void showSettingsAlert(Activity activity, AlertMessage.AlertMessageInterface ami, String message) {
        AlertMessage alertMessage = new AlertMessage(ami, AlertMessage.TITTLE_INFO);
        alertMessage.showMessage(activity, message, ALERT_TYPE_PERMISSIONS);
    }

    private static long availableMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
    }

    public static boolean isLowMemory() {

        Log.d("lowMemory", "availableMemorySize: " + String.valueOf(availableMemorySize() / (1024 * 1024)));

        return availableMemorySize() / (1024 * 1024) <= DEFAULT_THRESHOLD_MEGABYTES;
    }

    public static void sendVideoIntent(Fragment fragment) {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //  takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT,120); // 2 minutes
        takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY,1); // low quality
        long maxVideoSize = 10*1024*1024; // 10 MB
      //  takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, maxVideoSize);
        if (takeVideoIntent.resolveActivity(fragment.getContext().getPackageManager()) != null) {
            SharedPreferences sp = fragment.getActivity().getSharedPreferences("readPrefs", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("SHOWING_CAMERA", true);
            editor.commit();


            fragment.startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null && activity.getCurrentFocus() != null) {
                inputMethodManager.hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(), 0);
            }
        }
    }

    public static boolean arraysEqual(ArrayList<Integer> a1, RealmList<Integer> a2) {
        if (a1.size() != a2.size()) return false;
        for (int val : a1) {
            if (!a2.contains(val)) return false;
        }
        return true;
    }

    public static String getMeetingInvitationState(String state, Resources resources) {
        switch (state) {
            case "PENDING": default:
                return resources.getString(R.string.calendar_invited_state);
            case "ACCEPTED":
                return resources.getString(R.string.calendar_accepted_state);
            case "REJECTED":
                return resources.getString(R.string.calendar_rejected_state);

        }
    }

    public static float getTextSizeNumberBullet(Resources resources, int sizePx) {
        float density = resources.getDisplayMetrics().density;
        int size = (int) (sizePx / density);
        Log.d("bullt","getTextSizeNumberBullet size: "+size);
        if (size < 18) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_very_small);
        } else if (size < 20) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_small);
        } else if (size < 23) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_normal);
        } else if (size < 25) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_big);
        } else if (size < 28) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_very_big);
        } else if (size < 32) {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_huge);
        } else {
            return resources.getDimension(R.dimen.contacts_icon_number_text_size_huge_x);
        }

    }

    public static String getDuration(int lengthDate, Resources resources) {
        switch (lengthDate) {
            case 30:
                return resources.getString(R.string.calendar_meeting_duration_30);
            case 60:
                return resources.getString(R.string.calendar_meeting_duration_60);
            case 90:
                return resources.getString(R.string.calendar_meeting_duration_90);
            case 120:
                return resources.getString(R.string.calendar_meeting_duration_120);
        }
        return "";
    }

    public static String getArticleBeforeName(Locale locale, String name, String gender) {
        if (name==null)return "";
        if (!DateUtils.isCatalan(locale)) return "";
        if ("aeiou".indexOf(Character.toLowerCase(name.charAt(0))) != -1) {
            return "L'";
        }
        if (gender != null){
            if (gender.equals(UserRegister.MALE)) return "El ";
            return "La ";
        }else{
            return "";
        }
    }

    public static void sendAnalyticsView(Activity activity, String name) {
        if (activity != null) {
            FirebaseAnalytics.getInstance(activity).setCurrentScreen(activity, name, activity.getClass().getSimpleName());
        }
    }

    public static void clearFragmentsBackstack(FragmentManager fm) {
        for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    public static void updateGroupOrDynChatInfo(int idChat) {
        Context context = MyApplication.getAppContext();
        int idMe = new UserPreferences(context).getUserID();
        GroupMessageDb groupMessageDb = new GroupMessageDb(context);
        UserGroupsDb userGroupsDb = new UserGroupsDb(MyApplication.getAppContext());
        GroupRealm groupRealm = userGroupsDb.getGroupFromIdChatUnmanaged(idChat);
        if (groupRealm != null) {
            new UserGroupsDb(context).setMessagesInfo(idChat, groupMessageDb
                            .getNumberUnreadMessagesReceived(idMe, idChat),
                    groupMessageDb.getTotalNumberMessages(idChat));
        } else { //its dynamizer
            new UserGroupsDb(context).setDynamizerMessagesInfo(idChat, groupMessageDb
                            .getNumberUnreadMessagesReceived(idMe, idChat),
                    groupMessageDb.getTotalNumberMessages(idChat));
        }
    }

    public static void saveAccount(String username, String password, AccountManager accountManager) {
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        boolean saveNewAccount = false;
        if (accounts != null && accounts.length > 0) {
            if (accounts[0].name.equals(username)) {
                accountManager.setPassword(accounts[0], password);
            } else {
                saveNewAccount = true;
                removeAccount(accounts[0], accountManager);
            }
        } else {
            saveNewAccount = true;
        }
        if (saveNewAccount) {
            Account account = new Account(username, "cat.bcn.vincles.mobile");
            accountManager.addAccountExplicitly(account, password, null);
        }
    }

    public static void updateAccountPassword(String username, String password, AccountManager accountManager) {
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        if (accounts != null && accounts.length > 0) {
            if (accounts[0].name.equals(username)) {
                accountManager.setPassword(accounts[0], password);
            }
        }
    }

    private static void removeAccount(Account account, AccountManager accountManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccountExplicitly(account);
        } else {
            accountManager.removeAccount(account, null, null);
        }
    }

    public static void deleteAccountIfExisting(AccountManager accountManager) {
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType("cat.bcn.vincles.mobile");
        } catch (Exception ignored) {

        }
        if (accounts != null && accounts.length > 0) {
            removeAccount(accounts[0], accountManager);
        }
    }

    public static boolean isFileTooBigForServer(String filePath) {
        File file = new File(filePath);
        int file_size = Integer.parseInt(String.valueOf(file.length()/1024)); //size in KB
        Log.d("flsze","file too big? size:"+file_size);
        return 10*1024 < file_size;
    }

    public static boolean checkIfMicrophoneIsBusy(){
        AudioRecord audio = null;
        boolean ready = true;
        try{
            int baseSampleRate = 44100;
            int channel = AudioFormat.CHANNEL_IN_MONO;
            int format = AudioFormat.ENCODING_PCM_16BIT;
            int buffSize = AudioRecord.getMinBufferSize(baseSampleRate, channel, format );
            audio = new AudioRecord(MediaRecorder.AudioSource.MIC, baseSampleRate, channel, format, buffSize );
            audio.startRecording();
            short buffer[] = new short[buffSize];
            int audioStatus = audio.read(buffer, 0, buffSize);

            Log.e("checkIfMicrophoneIsBusy", "Error");

            Log.e("checkIfMicrophoneIsBusy", "audioStatus: " + audioStatus);

            if(audioStatus == AudioRecord.ERROR_INVALID_OPERATION || audioStatus == AudioRecord.STATE_UNINITIALIZED /* For Android 6.0 */)
                ready = false;
        }
        catch(Exception e){
            ready = false;
        }
        finally {
            try{
                if (audio != null) {
                    audio.release();
                }
            }
            catch(Exception e){
                ready = false;
                Log.e("checkIfMicrophoneIsBusy", "Error");
            }
        }

        return ready;
    }

    public static void cancelProcessingNotifications(Context appContext) {
        Log.d("cancelProcessing", "cancelNotification");
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) appContext.getSystemService(ns);
        if (nMgr != null) {
            Log.d("cancelProcessing", "es cancela la noti");
            nMgr.cancelAll();
        }

    }


    public static String getBaseHostName() {
        String[] parts = cat.bcn.vincles.mobile.Client.Enviroment.Environment.getApiBaseUrl().split("//");
        if (parts.length>0){
            return parts[1];
        }
        return "";
    }

    // Remove duplicates from a list
    public static <T> void removeDuplicates(List<T> list) {
        if (list != null) {
            // Since HashSet does not allow duplicates, here we get rid of them
            Set<T> set = new LinkedHashSet<>(list);

            list.clear();

            list.addAll(set);
        }
    }
}
