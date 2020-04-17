package cat.bcn.vincles.mobile.Client.Preferences;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import cat.bcn.vincles.mobile.Client.Model.GetUser;
import cat.bcn.vincles.mobile.Client.Model.UserRegister;
import cat.bcn.vincles.mobile.Utils.Constants;
import cat.bcn.vincles.mobile.Utils.MyApplication;

import static cat.bcn.vincles.mobile.Utils.Constants.READ_PREFS;

public class UserPreferences {

    public static final int FONT_SIZE_SMALL = 0;
    public static final int FONT_SIZE_MEDIUM = 1;
    public static final int FONT_SIZE_BIG = 2;
    public static final String AUTO_DOWNLOAD = "AUTO_DOWNLOAD";

    private static final String PREFS_VINCLES = "PREFSVINCLES";
    private Context context;

    public UserPreferences () {
        this.context = MyApplication.getAppContext();
    }

    public UserPreferences (Context context) {
        this.context = context;

        if (context == null){
            this.context = MyApplication.getAppContext();
        }
    }

    public void clear() {
        context.getSharedPreferences(PREFS_VINCLES, 0).edit().clear().apply();
    }

    public void setUserID(int userID) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("userID", userID);
        editor.commit();
    }

    public int getUserID() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("userID", 0);
    }

    public int notificationUpperIdGetAndSubtract() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        int currentValue = sharedpreferences.getInt("notificationUpperId", Integer.MAX_VALUE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("notificationUpperId", currentValue-1);
        editor.commit();
        return currentValue;
    }

    public int notificationSystemUpperIdGetAndSubtract() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        int currentValue = sharedpreferences.getInt("notificationSystemUpperId", Integer.MAX_VALUE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("notificationSystemUpperId", currentValue-1);
        editor.commit();
        return currentValue;
    }

    public void setInstallationSet(boolean installationSet) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("installationSet", installationSet);
        editor.commit();
    }

    public boolean isInstallationSet() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("installationSet", false);
    }

    public void setOperatingSystem(String operatingSystem) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("operatingSystem", operatingSystem);
        editor.commit();
    }

    public String getOperatingSystem() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("operatingSystem", "ANDROID");
    }

    public void setPushToken(String pushToken) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("pushToken", pushToken);
        editor.commit();
    }

    public String getPushToken() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("pushToken", "ANDROID");
    }

    public void setImei(String imei) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("imei", imei);
        editor.commit();
    }

    public String getImei() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("imei", "ANDROID");
    }

    public void setDeviceId(String deviceId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("deviceId", deviceId);
        editor.commit();
    }

    public String getDeviceId() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("deviceId", "ANDROID");
    }

    public void setPin(String pin) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("pin", pin);
        editor.commit();
    }

    public String getPin() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("pin", "ANDROID");
    }

    public void setIdSession(int idSession) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("idSession", idSession);
        editor.commit();
    }

    public int getIdSession() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("idSession", 0);
    }

    public void setUserLanguage(String language) {
        Log.d("lng","setLanguage: "+language);
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("language", language);
        editor.commit();
    }

    public String getUserLanguage() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("language", UserRegister.LANGUAGE_NOT_SET);
    }

    public int getFontSize() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("fontSize", FONT_SIZE_MEDIUM);
    }

    public void setFontSize(int fontSize) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("fontSize", fontSize);
        editor.commit();
    }

    public void setScope(String scope) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("scope", scope);
        editor.commit();
    }

    public String getScope() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("scope", "");
    }

    public void setTokenType(String tokenType) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("tokenType", tokenType);
        editor.commit();
    }

    public String getTokenType() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("tokenType", "");
    }

    public void setExpiresIn(int expiresIn) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("expiresIn", expiresIn);
        editor.commit();
    }

    public int getExpiresIn() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("expiresIn", 0);
    }

    public void setRefreshToken(String refreshToken) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("refreshToken", refreshToken);
        editor.commit();
    }

    public String getRefreshToken() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("refreshToken", "");
    }

    public void setAccessToken(String accessToken) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("accessToken", accessToken);
        editor.commit();
    }

    public String getAccessToken() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("accessToken", "");
    }

    public void setIdInstallation(long idInstallation) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("idInstallation", idInstallation);
        editor.commit();
    }

    public Long getIdInstallation() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("idInstallation", -1);
    }

    public void setIdCircle(int idCircle) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("idCircle", idCircle);
        editor.commit();
    }

    public String getIdCircle() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("idCircle", "0");
    }


    public void setIsUserSenior(boolean isUserSenior) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isUserSenior", isUserSenior);
        editor.commit();
    }

    public boolean getIsUserSenior() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("isUserSenior", true);
    }

    public void setIdLibrary(int idLibrary) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("idLibrary", idLibrary);
        editor.commit();
    }

    public int getIdLibrary() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("idLibrary", 0);
    }

    public void setIdCalendar(int idCalendar) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("idCalendar", idCalendar);
        editor.commit();
    }

    public int getIdCalendar() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getInt("idCalendar", 0);
    }

    public void setAlias(String alias) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("alias", alias);
        editor.commit();
    }

    public String getAlias() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("alias", "");
    }

    public void setUsername(String username) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("username", username);
        editor.commit();
    }

    public String getUsername() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("username", "");
    }

    public void setUserAvatar(String filePath) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("userAvatar", filePath);
        editor.commit();
    }

    public String getUserAvatar() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("userAvatar", "");
    }

    public void setFirstTimeUserAccessApp(boolean isFirstTimeUserAccessApp) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isFirstTimeUserAccessApp", isFirstTimeUserAccessApp);
        editor.commit();
    }

    public boolean isFirstTimeUserAccessApp() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("isFirstTimeUserAccessApp", true);
    }

    public void setName(String name) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("name", name);
        editor.commit();
    }

    public String getName() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("name", "");
    }

    public void setLastName(String lastName) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("lastName", lastName);
        editor.commit();
    }

    public String getLastName() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("lastName", "");
    }

    public void setEmail(String email) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("email", email);
        editor.commit();
    }

    public String getEmail() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("email", "");
    }

    public void setBirthdate(long birthdate) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("birthdate", birthdate);
        editor.commit();
    }

    public long getBirthdate() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("birthdate", 0);
    }

    public void setPhone(String phone) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("phone", phone);
        editor.commit();
    }

    public String getPhone() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("phone", "");
    }

    public void setGender(String gender) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("gender", gender);
        editor.commit();
    }

    public String getGender() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("gender", "MALE");
    }

    public void setLivesInBarcelona(Boolean livesInBarcelona) {
        if (livesInBarcelona == null) {
            livesInBarcelona = false;
        }
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("livesInBarcelona", livesInBarcelona);
        editor.commit();
    }

    public Boolean livesInBarcelona() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("livesInBarcelona", false);
    }

    public void setRegisterPicture(String registerPicture) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString("registerPicture", registerPicture);
        editor.commit();
    }

    public String getRegisterPicture() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getString("registerPicture", "");
    }

    public void setLastDownloadedNotification(long time) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("lastDownloadedNotification", time);
        editor.apply();
    }

    public long getLastDownloadedNotification() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("lastDownloadedNotification", 0);
    }

    public void setLoginDataDownloaded(boolean loginDataDownloaded) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("loginDataDownloaded", loginDataDownloaded);
        editor.apply();
    }

    public boolean getLoginDataDownloaded() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("loginDataDownloaded", false);
    }

    public void setIsAutodownload(boolean isAutodownload) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isAutodownload", isAutodownload);
        editor.commit();
    }

    public boolean getIsAutodownload() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("isAutodownload", true);
    }

    public void setIsCopyPhotos(boolean isCopyPhotos) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("isCopyPhotos", isCopyPhotos);
        editor.commit();
    }

    public boolean getIsCopyPhotos() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("isCopyPhotos", true);
    }

    public void setIsSyncCalendars(boolean isAutodownload) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("SyncCalendars", isAutodownload);
        editor.commit();
    }

    public boolean getIsSyncCalendars() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("SyncCalendars", false);
    }

    public void setCalendarId(long calendarId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("CalendarId", calendarId);
        editor.commit();
    }

    public long getCalendarId() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("CalendarId", -1);
    }

    public Set<String> getInvitedUsers() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        Set<String> setId = sharedpreferences.getStringSet("invitedUsers", null);
        if(setId == null){
            setId = new HashSet<String>();
        }
        return setId;
    }

    public void addInvitedUser(Integer userId) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        Set<String> sharedString = sharedpreferences.getStringSet("invitedUsers", new HashSet<String>());
        sharedString.add(String.valueOf(userId));
        editor.putStringSet("invitedUsers", sharedString);
        editor.commit();

    }

    public void saveServerTime(long currentTime) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putLong("serverTime", currentTime);
        editor.commit();
    }

    public Long getServerTime() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getLong("serverTime", -1);
    }


    public boolean isLoggedIn() {
        int userID = getUserID();
        boolean isLogged = getLoginDataDownloaded();

        if (userID != 0 && isLogged){
            return true;
        }
        else if (userID != 0){
            return false;
        }
        return false;
    }

    public void setRenewTokenFailed(boolean renewTokenFailed) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean("renewTokenFailed", renewTokenFailed);
        editor.apply();
    }

    public boolean getRenewTokenFailed() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(PREFS_VINCLES, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean("renewTokenFailed", false);
    }

    public void continueToApp(boolean b) {
//        Log.d("continuecheck", String.format("setContinueToApp(%b)", b));
        SharedPreferences sp = context.getSharedPreferences(READ_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.CONTINUE_TO_APP, b);
        editor.apply();
    }

    public boolean getcontinueToApp() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(READ_PREFS, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean(Constants.CONTINUE_TO_APP, true);
    }

    public void setContinueToAppCheck(boolean b) {
        SharedPreferences sp = context.getSharedPreferences(READ_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.CONTINUE_TO_APP_CHECK, b);
        editor.apply();
    }

    public void setGetNotifications(boolean b) {
        SharedPreferences sp = context.getSharedPreferences(READ_PREFS, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(Constants.GET_NOTIFICATIONS, b);
        editor.apply();
    }

    public boolean getNotifications() {
        SharedPreferences sharedpreferences = context.getSharedPreferences(READ_PREFS, Context.MODE_PRIVATE);
        return sharedpreferences.getBoolean(Constants.GET_NOTIFICATIONS, false);
    }

    public GetUser getUser() {

        GetUser getUser = new GetUser();
        getUser.setId(getUserID());
        getUser.setName(getName());
        getUser.setLastname(getLastName());
        getUser.setAlias(getAlias());
        getUser.setEmail(getEmail());
        getUser.setBirthdate(getBirthdate());
        getUser.setGender(getGender());

        return getUser;
    }
}
