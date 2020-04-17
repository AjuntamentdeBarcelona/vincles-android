package cat.bcn.vincles.mobile.Client.Business.Firebase;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Random;

import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class FirebaseInstanceIDListener extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. This call is initiated by the
     * InstanceID provider.
     */
    @Override
    public void onTokenRefresh() {

        if (new UserPreferences(MyApplication.getAppContext()).isInstallationSet()) {
            forceRefreshToken(this);
        }
    }

    public static void forceRefreshToken(Context context) {
        // Get updated InstanceID token.
        try {
            String imei = null;
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && telephonyManager != null) {
                    imei = telephonyManager.getImei();
                } else if (telephonyManager != null) {
                    imei = telephonyManager.getDeviceId();
                }
            }
            CommonRegistrationFCMService.sendFCMRegistrationToServer(imei);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
