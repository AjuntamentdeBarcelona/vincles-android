package cat.bcn.vincles.mobile.Client.Business.Firebase;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class Installation {

    public static final String OS_ANDROID= "ANDROID";
    public static final int INSTALLATION_PLATFORM_VERSION = 2;

    private Long idUser;

    private long id;

    @SerializedName("so")
    private String operatingSystem;

    private String deviceId;

    private String pushToken;

    private String pin;

    private String appVersion;

    private String imei;

    private int idSession;



    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public int getIdSession() {
        return idSession;
    }

    public void setIdSession(int idSession) {
        this.idSession = idSession;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public JsonObject toJSON() {
        //{
        //    "idUser" : 0,
        //    "so" : <"ANDROID" or "IOS">,
        //    "pushToken" : "pushtokeninfo"
        //}

        JsonObject json = new JsonObject();
        json.addProperty("idUser", this.idUser);
        json.addProperty("so", this.operatingSystem);
        json.addProperty("pushToken", this.pushToken);
        json.addProperty("deviceId", this.deviceId);
        json.addProperty("installationId", this.deviceId + this.idUser); //using deviceId + userId as InstallationId
        json.addProperty("appVersion", this.appVersion);
        json.addProperty("platformVersion", INSTALLATION_PLATFORM_VERSION);
        json.addProperty("imei", this.imei);

        return json;
    }

    public static Installation updateFromJSON(Installation installation, JsonObject json) {

        if (json != null) {
            installation.setIdUser(json.get("idUser").getAsLong());
            installation.setId(json.get("id").getAsInt());
            installation.setOperatingSystem(json.get("so").getAsString());
            installation.setPushToken(json.get("pushToken").getAsString());
            installation.setDeviceId(json.get("deviceId").getAsString());
            installation.setDeviceId(json.get("IMEI").getAsString());
            installation.setPin(json.get("pin").getAsString());
            installation.setIdSession(json.get("idSession").getAsInt());
        }

        UserPreferences userPreferences = new UserPreferences(MyApplication.getAppContext());
        userPreferences.setOperatingSystem(installation.getOperatingSystem());
        userPreferences.setPushToken(installation.getPushToken());
        userPreferences.setDeviceId(installation.getDeviceId());
        userPreferences.setImei(installation.getImei());
        userPreferences.setPin(installation.getPin());
        userPreferences.setIdSession(installation.getIdSession());
        userPreferences.setIdInstallation(installation.getId());

        return installation;
    }

    public static Installation getInstallation() {
        UserPreferences userPreferences = new UserPreferences(MyApplication.getAppContext());
        if (!userPreferences.isInstallationSet()) return null;
        Installation installation = new Installation();
        installation.setIdUser((long) userPreferences.getUserID());
        installation.setOperatingSystem(userPreferences.getOperatingSystem());
        installation.setPushToken(userPreferences.getPushToken());
        installation.setDeviceId(userPreferences.getDeviceId());
        installation.setImei(userPreferences.getImei());
        installation.setPin(userPreferences.getPin());
        installation.setAppVersion(BuildConfig.VERSION_NAME);
        return installation;
    }

    public static boolean isInstalled() {
        return new UserPreferences(MyApplication.getAppContext()).isInstallationSet();
    }
}
