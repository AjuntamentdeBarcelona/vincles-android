/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.model;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import javax.net.ssl.SSLException;
import cat.bcn.vincles.lib.business.ServiceGenerator;
import cat.bcn.vincles.lib.business.UserService;
import cat.bcn.vincles.lib.dao.UserDAO;
import cat.bcn.vincles.lib.dao.UserDAOImpl;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.push.VinclesPushListener;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ErrorHandler;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.Model;
import cat.bcn.vincles.lib.util.TokenAuthenticator;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.util.VinclesError;
import cat.bcn.vincles.lib.vo.Installation;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.monitors.SignalStrengthMonitor;
import cat.bcn.vincles.tablet.push.AppFCMDefaultListenerImpl;
import cat.bcn.vincles.tablet.push.VinclesInstanceIDListenerService;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainModel implements Model {
    private static final String TAG = "MainModel";
    public static boolean avoidServerCalls = true;
    private boolean initialized;
    private UserDAO userDAO;
    private static MainModel instance;
    public ProgressDialog busy;
    public Network currentNetwork;
    public String theme;
    public String accessToken;
    public VinclesPushListener vinclesPushListener;
    private long lastNotificationCheck;
    private boolean checkingNotificationsWorking = false;
    private ConnectivityManager connectivityManager;
    private AudioManager audioManager;
    public NetworkInfo networkInfo;
    public TelephonyManager telephonyManager;
    public SignalStrengthMonitor phoneListener;
    public boolean isLowConnection;
    private NetworkModel networkModel;

    public static MainModel getInstance() {
        if (instance == null) {
            instance = new MainModel();
        }
        return instance;
    }

    public String language;
    public String country;
    public User currentUser;
    public int tour;
    public float fontSize;
    public int volume;
    public boolean brightnessAutomatic;
    public int brightness;
    public int marginLeft;
    public int marginRight;
    public SharedPreferences preferences;
    public Context context;
    public Locale locale;

    private MainModel() {
    }

    public void initialize(Context context) {
        initialize(context, false);
    }

    public void initialize(Context context, boolean forceUpdate) {
        if (!initialized || forceUpdate) {
            initialized = true;
            this.context = context;

            networkModel = NetworkModel.getInstance();
            userDAO = new UserDAOImpl();

            initData();
        }
    }

    private void initData() {
        audioManager = (AudioManager) context.getSystemService(context.AUDIO_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // Read preferences
        preferences = context.getSharedPreferences(VinclesTabletConstants.APP_PREFERENCES, Context.MODE_PRIVATE);
        language = preferences.getString(VinclesTabletConstants.APP_LANGUAGE, "ca");
        country = preferences.getString(VinclesTabletConstants.APP_COUNTRY, "ES");
        fontSize = preferences.getFloat(VinclesTabletConstants.FONT_SIZE, VinclesTabletConstants.FONT_NORMAL);
        volume = preferences.getInt(VinclesTabletConstants.VOLUME, VinclesTabletConstants.VOLUME_NORMAL);//volume_level);
        brightness = preferences.getInt(VinclesTabletConstants.BRIGHTNESS, VinclesTabletConstants.BRIGHTNESS_NORMAL);
        brightnessAutomatic = preferences.getBoolean(VinclesTabletConstants.BRIGHTNESS_AUTOMATIC, false);
        marginLeft = preferences.getInt(VinclesTabletConstants.MARGIN_LEFT, VinclesTabletConstants.MARGIN_LEFT_NORMAL);
        marginRight = preferences.getInt(VinclesTabletConstants.MARGIN_RIGHT, VinclesTabletConstants.MARGIN_RIGHT_NORMAL);
        theme = preferences.getString(VinclesTabletConstants.APP_THEME, VinclesTabletConstants.CLARA_THEME);
        lastNotificationCheck = preferences.getLong(VinclesTabletConstants.APP_LASTNOTIFICATIONCHECK, System.currentTimeMillis());

        tour = preferences.getInt(VinclesTabletConstants.TOUR, 0);

        // Recover status
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_SETTINGS) == PackageManager.PERMISSION_GRANTED) {
            updateLocale(language, country);
            updateFontSize(fontSize);
            updateVolume(volume);

            if (brightnessAutomatic) {
                updateAutomaticBrightness(brightnessAutomatic);
            } else {
                updateBrightness(brightness);
            }
        }

        long userId = preferences.getLong(VinclesTabletConstants.USER_ID, 0l);
        currentUser = userDAO.get(userId);
        if (currentUser == null) {
            currentUser = new User();
        } else {
            // Update TokenAuthenticator values for re-login
            TokenAuthenticator.username = currentUser.username;
            TokenAuthenticator.password = currentUser.cipher;
        }
        TokenAuthenticator.model = this;

        // Select current network
        currentNetwork = networkModel.getNetwork(preferences.getLong(VinclesTabletConstants.NETWORK_CODE, 0));
        if (currentNetwork == null) {
            currentNetwork = new Network();
            currentNetwork.setId(userId);
            currentNetwork.userVincles = currentUser;
            networkModel.saveNetwork(currentNetwork);

            // Update local reference
            savePreferences(VinclesTabletConstants.NETWORK_CODE, currentNetwork.getId(), VinclesConstants.PREFERENCES_TYPE_LONG);
        }
    }

    public void updateTokenAuthenticator(String username, byte[] pass) {
        // Update TokenAuthenticator values for re-login
        TokenAuthenticator.username = username;
        TokenAuthenticator.password = pass;
    }

    public boolean isConnected() {
        Log.i(TAG, "isConnected()");
        boolean result = false;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                result = true;
            }
        }
        return result;
    }

    public int getConnectionType() {
//        Log.i(TAG, "getConnectionType()");
        int result = 0;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                result = networkInfo.getType();
            }
        }
        return result;
    }

    private void synchronize() {
        Log.i(TAG, "synchronize()");
        // TODO: synchronize new data & commit marked data to update!!!
        busy.dismiss();
    }

    public void updateTourStep(int step) {
        Resources res = context.getResources();
        tour = step;
        savePreferences(VinclesTabletConstants.TOUR, step, VinclesConstants.PREFERENCES_TYPE_INT);
    }

    public void updateLocale(String language, String country) {
        this.language = language;
        this.country = country;

        locale = new Locale(language, country);
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = locale;
        res.updateConfiguration(conf, dm);
    }

    public void updateFontSize(float value) {
        Resources res = context.getResources();
        Configuration conf = res.getConfiguration();
        DisplayMetrics dm = res.getDisplayMetrics();
        conf.fontScale = value;
        fontSize = conf.fontScale;
        res.updateConfiguration(conf, dm);
    }

    private int getVolumeFromPercentage(int maxVolumeStream, int percentage) {
        return (percentage * maxVolumeStream) / 100;
    }

    public void updateVolume(int percentage) {
        Resources res = context.getResources();
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        Configuration conf = res.getConfiguration();
        DisplayMetrics dm = res.getDisplayMetrics();

        Log.d(TAG, "UPDATE VOLUME: " + getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), percentage)
                + " / " + audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), percentage), AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM), percentage), AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_RING,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), percentage), AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_DTMF,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_DTMF), percentage), AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), percentage), AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), percentage), AudioManager.FLAG_PLAY_SOUND);
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                getVolumeFromPercentage(audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), percentage), AudioManager.FLAG_PLAY_SOUND);

        volume = percentage;
        res.updateConfiguration(conf, dm);
    }

    public void updateBrightness(int value) {
        Log.i(TAG, "updateBrightness() - result: " + value);
        brightness = value;

        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
    }

    public void updateAutomaticBrightness(boolean value) {
        Log.i(TAG, "updateAutomaticBrightness() - result: " + value);
        brightnessAutomatic = value;

        if (brightnessAutomatic) {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        } else {
            Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        }
    }

    public void updateMarginLeft(boolean value) {
        Log.i(TAG, "updateMarginLeft() - result: " + value);
        if (value) {
            marginLeft = VinclesTabletConstants.MARGIN_LEFT_LARGE;
        } else {
            marginLeft = VinclesTabletConstants.MARGIN_LEFT_NORMAL;
        }
    }

    public void updateMarginRight(boolean value) {
        Log.i(TAG, "updateMarginRight() - result: " + value);
        if (value) {
            marginRight = VinclesTabletConstants.MARGIN_RIGHT_LARGE;
        } else {
            marginRight = VinclesTabletConstants.MARGIN_RIGHT_NORMAL;
        }
    }

    public void setMargins(ViewGroup view) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(marginLeft, 0, marginRight, 0);
            view.requestLayout();
        }
    }

    public void savePreferences(String key, Object value, String type) {
        SharedPreferences.Editor editor = preferences.edit();
        switch (type) {
            case VinclesConstants.PREFERENCES_TYPE_STRING:
                editor.putString(key, (String) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_FLOAT:
                editor.putFloat(key, (Float) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_BOOLEAN:
                editor.putBoolean(key, (boolean) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_INT:
                editor.putInt(key, (int) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_LIST:
                editor.remove(key);
                editor.putStringSet(key, (Set<String>) value);
                break;
            case VinclesConstants.PREFERENCES_TYPE_LONG:
                editor.putLong(key, (long) value);
                break;
        }
        editor.commit();
    }

    public long saveUser(User item) {
        return userDAO.save(item);
    }

    public User getUser(Long id) {
        User result = userDAO.get(id);
        if (result == null) {
            // Default User
            result = new User();
            result.active = false;
            result.name = context.getString(R.string.user_default_name);
            result.lastname = context.getString(R.string.user_default_lastname);
            result.alias = context.getString(R.string.user_default_name) + " " + context.getString(R.string.user_default_lastname);;
        }
        return result;
    }

    public Bitmap getLowImage(String filename) {
        Bitmap result = ImageUtils.decodeSampledBitmapFromFile(VinclesConstants.getImageDirectory() + "/" + filename, VinclesConstants.IMAGE_MIN_WIDTH, VinclesConstants.IMAGE_MIN_HEIGHT);
        return result;
    }

    public void login(final AsyncResponse response, String username, String password) {
        Log.i(TAG, "login()");
        UserService client = ServiceGenerator.createLoginService(UserService.class);
        Call<JsonObject> call = client.login(username, password);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                Log.i(TAG, "result: " + result.body());

                if (result.isSuccessful()) {
                    // Set authToken globally for further request
                    JsonObject json = result.body();
                    accessToken = json.get("access_token").getAsString();

                    response.onSuccess(true);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void changeTheme(String theme) {
        this.theme = theme;
    }

    public int getResourceTheme() {
        int resource;
        if (theme.equals(VinclesTabletConstants.FOSCA_THEME)) {
            resource = R.style.VinclesFoscaTheme;
        } else {
            resource = R.style.VinclesClaraTheme;
        }

        return resource;
    }

    public String getErrorByCode(Object error) {
        String result = context.getResources().getString(R.string.error_default);
        if (error instanceof Exception) {
            result = ((Exception) error).getMessage();
            if (error instanceof SocketTimeoutException || error instanceof SSLException) {
                result = context.getResources().getString(R.string.error_sockettimeoutexception);
            } else if (error instanceof ConnectException) {
                result = context.getResources().getString(R.string.error_connection_exception);
            } else if (error instanceof IOException) {
                if (((IOException) error).getMessage().equals(VinclesError.ERROR_LOGIN)) {
                    result = context.getResources().getString(R.string.error_login);
                }
            }
        } else if (error instanceof Error) {
            result = ((Error) error).getMessage();
        } else {
            String code = "";
            if (error instanceof VinclesError) {
                code = ((VinclesError)error).getCode();
            } else {
                code = (String) error;
            }
            switch (code) {
                case "1908":
                    result = context.getResources().getString(R.string.error_1908);
                    break;
                case "1606":
                    result = context.getResources().getString(R.string.error_1606);
                    break;
                case "1321":
                    result = context.getResources().getString(R.string.error_1321);
                    break;
                case "1301":
                    result = context.getResources().getString(R.string.error_1300);
                    break;
                case "1": // VinclesError.ERROR_CONNECTION
                    result = context.getResources().getString(R.string.error_sockettimeoutexception);
                    break;
                case "invalid_grant": // VinclesError.ERROR_LOGIN
                    result = context.getResources().getString(R.string.error_login);
                    break;
                case VinclesError.ERROR_CANCEL: // VinclesError.ERROR_CANCEL
                    result = context.getResources().getString(R.string.error_cancel);
                    break;
                case "2008":
                    // User can't access this group
                    result = context.getResources().getString(R.string.error_2008);
                    break;
                case "1501":
                    // Content not found in library
                    result = context.getResources().getString(R.string.error_1501);
                    break;
                default: // VinclesError.ERROR_DEFAULT
                    result = context.getResources().getString(R.string.error_default);
                    break;
            }

            Log.e(TAG, "Code: " + code +" , Error: " + result);
        }
        return result;
    }

    @Override
    public void updateAccessToken(String token) {
        accessToken = token;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Long getCurrentUserId() {
        return currentUser.getId();
    }

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    public void startFCM(Activity activity) {
        if (Installation.findById(Installation.class, 1) == null) {
            VinclesInstanceIDListenerService.forceRefreshToken(activity);
        }
        // SET MESSAGE LISTENER HERE
        CommonVinclesGcmHelper.setPushListener(getPushListener());
    }

    public VinclesPushListener getPushListener() {
        if (vinclesPushListener == null) {
            vinclesPushListener = new AppFCMDefaultListenerImpl(context);
        }
        return vinclesPushListener;
    }

    public boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                activity.finish();
            }
            return false;
        }
        return true;
    }

    public String getRealVideoPathFromURI(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA);
            return cursor.getString(idx);
        } catch (Exception e) {
            return uri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void getAndSetUserID(final AsyncResponse response, final String username, final String password) {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getMyUserInfo();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    User tempUser = User.fromJSON(json);

                    currentUser.setId(tempUser.getId());
                    currentUser.idCalendar = tempUser.idCalendar;
                    currentUser.idContentPhoto = tempUser.idContentPhoto;
                    currentUser.name = tempUser.name;
                    currentUser.lastname = tempUser.lastname;
                    currentNetwork.setId(tempUser.getId());
                    currentUser.username = username;
                    currentUser.birthdate = tempUser.birthdate;
                    currentUser.gender = tempUser.gender;
                    currentUser.phone = tempUser.phone;

                    currentUser.isUserVincles = true;
                    saveUser(currentUser);
                    setPassword(currentUser, password);
                    updateTokenAuthenticator(currentUser.username, currentUser.cipher);
                    savePreferences(VinclesTabletConstants.USER_ID, tempUser.getId(), VinclesConstants.PREFERENCES_TYPE_LONG);
                    savePreferences(VinclesTabletConstants.NETWORK_CODE, currentNetwork.getId(), VinclesConstants.PREFERENCES_TYPE_LONG);
                    response.onSuccess(true);
                } else {
                    Log.e(TAG, "Error getting and setting User ID: " + result.code() + " " + result.message() + "\n" + result.body().toString());
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "getAndSetUserID() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public String getPIN() {
        Installation temp = Installation.findById(Installation.class, 1);
        if (temp != null) {
            return temp.getPin();
        }
        else return "1235";
    }

    public void updateUserServer(User user) {
        Log.i(TAG, "updateUserServer()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.updateUser(user.toJSON());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    // Nothing to do!!!
                    Log.i(TAG, "result: " + result.body());
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    String errorMessage = getErrorByCode(error);
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(TAG, "error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void getUserPhoto(final AsyncResponse response, final User user) {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<ResponseBody> call = client.getUserPhoto(user.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> result) {
                if (result.isSuccessful()) {
                    byte[] data = null;
                    try {
                        data = IOUtils.toByteArray(result.body().byteStream());
                        String imageName = VinclesConstants.IMAGE_USER_PREFIX + "_" +
                                user.idContentPhoto + "_" + new Date().getTime() +
                                VinclesConstants.IMAGE_EXTENSION;
                        VinclesConstants.saveImage(data, imageName);

                        // Update reference to user image file
                        user.imageName = imageName;
                        userDAO.save(user);
                        response.onSuccess(imageName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    response.onFailure(error);
                    Log.e(TAG, "error: " + error.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG, "getUserPhoto() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public void updateUserPhoto(String nameFile) {
        Log.i(TAG, "updateUserPhoto()");
        File imageFile = new File(VinclesConstants.getImagePath(), nameFile);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        MultipartBody.Part data = MultipartBody.Part.createFormData("file", nameFile, file);

        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.updateUserPhoto(data);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call2, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    // Nothing to do!!!
                    Log.i(TAG, "result: " + result.body());
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    String errorMessage = getErrorByCode(error);
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(TAG, "error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call2, Throwable t) {
                Log.i(TAG, "login() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void getUserInfo(AsyncResponse response, long userId) {
        if (userDAO.isOnlyGroupUser(userId))
            getBasicUserInfo(response, userId);
        else
            getFullUserInfo(response, userId);
    }

    private void getFullUserInfo(final AsyncResponse response, final long userId) {
        Log.i(TAG, "getFullUserInfo()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getFullUserInfo(userId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, final Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    final User user = User.fromJSON(json);
                    if (checkPhotoAndSaveUser(user, false))
                        // Get user photo
                        getUserPhoto(new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                response.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Object error) {
                                String errorMessage = getErrorByCode(error);
                                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                                toast.show();
                                Log.e(TAG, "getUserPhoto - error: " + errorMessage);
                                response.onFailure(error);
                            }
                        }, user);
                    else response.onSuccess(result);
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    String errorMessage = getErrorByCode(error);
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(TAG, "error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "getFullUserInfo() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    private void getBasicUserInfo(final AsyncResponse response, final long userId) {
        Log.i(TAG, "getFullUserInfo()");
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.getBasicUserInfo(userId);
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, final Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    final User user = User.fromJSON(json);
                    user.isUserVincles = true;
                    if (checkPhotoAndSaveUser(user, true))
                        // Get user photo
                        getUserPhoto(new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                response.onSuccess(user);
                            }

                            @Override
                            public void onFailure(Object error) {
                                String errorMessage = getErrorByCode(error);
                                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                                toast.show();
                                Log.e(TAG, "getUserPhoto - error: " + errorMessage);
                                response.onFailure(error);
                            }
                        }, user);
                    else response.onSuccess(result);
                } else {
                    VinclesError error = ErrorHandler.parseError(result);
                    String errorMessage = getErrorByCode(error);
                    Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                    Log.e(TAG, "error: " + errorMessage);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "getFullUserInfo() - error: " + t.getMessage());
                String errorMessage = getErrorByCode(t);
                Toast toast = Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    public void checkNewNotifications() {
        if (checkingNotificationsWorking) return;
        checkingNotificationsWorking = true;
        // SHOULD NOT WORK ON MAIN THREAD
        new Thread() {
            @Override
            public void run()
            {
                try {
                    long result =
                            CommonVinclesGcmHelper.checkNewNotifications(lastNotificationCheck+1, accessToken);
                    if (result != -1 && result != lastNotificationCheck) {
                        lastNotificationCheck = result;
                        savePreferences(VinclesTabletConstants.APP_LASTNOTIFICATIONCHECK, lastNotificationCheck, VinclesConstants.PREFERENCES_TYPE_LONG);
                    }
                } catch (Exception e) {} finally {
                    checkingNotificationsWorking = false;
                }
            }
        }.start();
    }

    public boolean checkPhotoAndSaveUser(User item, boolean isBasicUpdate) {
        User tempUser = userDAO.get(item.getId());
        if (tempUser != null) {
            boolean isNewPicture = tempUser.idContentPhoto != item.idContentPhoto;
            if (isBasicUpdate) {
                tempUser.name = item.name;
                tempUser.lastname = item.lastname;
                tempUser.alias = item.alias;
                tempUser.gender = item.gender;
                tempUser.idContentPhoto = item.idContentPhoto;
                userDAO.save(tempUser);
            }
            else userDAO.save(item);
            return isNewPicture;
        } else {
            userDAO.save(item);
            return true;
        }
    }

    public String getUserPhotoUrlFromUser(final User user) {
        return getUserPhotoUrlFromUser (user, new AsyncResponse() {
            @Override public void onSuccess(Object result) { }
            @Override public void onFailure(Object error) { }
        });
    }

    public String getUserPhotoUrlFromUser(final User user, final AsyncResponse resp) {
        if (!new File(VinclesConstants.getImageDirectory() + "/" + user.imageName).exists()) {
            user.usrImgStatus = 0;
            getUserPhoto(new AsyncResponse() {
                @Override public void onSuccess(Object result) {
                    user.usrImgStatus = 1;
                    resp.onSuccess(result);
                }
                @Override public void onFailure(Object error) {
                    user.usrImgStatus = 1;
                    resp.onFailure(error);
                }
            }, user);
        }
        return (VinclesConstants.getImageDirectory() + "/" + user.imageName);
    }

    public GlideUrl getUserPhotoUrlFromUserId(Long userId) {
        GlideUrl glideUrl = new GlideUrl(
                ServiceGenerator.getApiBaseUrl() + "API_URL_USERS" + userId + "/photo", new LazyHeaders.Builder()
                .addHeader("Authorization", "Bearer " + MainModel.getInstance().getAccessToken())
                .build());
        return glideUrl;
    }

    public void resetPassword(final AsyncResponse response) {
        UserService client = ServiceGenerator.createService(UserService.class, accessToken);
        Call<JsonObject> call = client.resetPassword();
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> result) {
                if (result.isSuccessful()) {
                    JsonObject json = result.body();
                    String newPassword = json.get("newPassword").getAsString();
                    response.onSuccess(newPassword);
                } else {
                    String errorCode = ErrorHandler.parseError(result).getCode();
                    response.onFailure(errorCode);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.i(TAG, "resetPassword() - error: " + t.getMessage());
                response.onFailure(t);
            }
        });
    }

    public String getPassword(User user) {
        return userDAO.getPassword(user);
    }

    public void setPassword(User user, String password) {
        userDAO.setPassword(user, password);
    }
}