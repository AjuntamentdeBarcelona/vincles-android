/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.push;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import java.util.Random;
import cat.bcn.vincles.lib.push.CommonRegistrationFCMService;
import cat.bcn.vincles.lib.vo.Installation;
import cat.bcn.vincles.tablet.model.MainModel;

public class VinclesInstanceIDListenerService extends FirebaseInstanceIdService {

    private static final String TAG = "VicnlesIDLS";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // DO NOT SEND REGISTRATION BEFORE USER REGISTER (FIRST LAUNCH)
        if (Installation.findById(Installation.class, 1) != null)
            forceRefreshToken(this);
    }
    // [END refresh_token]

    public static void forceRefreshToken(Context context) {
        // Get updated InstanceID token.
        Log.d(TAG, "Firebase TOKEN REFRESH: " + FirebaseInstanceId.getInstance().getToken());
        try {
            String imei = "";
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(TELEPHONY_SERVICE);
                imei = telephonyManager.getDeviceId();
                if (imei == null) {
                    imei = "i2cat" + new Random().nextLong();
                }
                CommonRegistrationFCMService.sendFCMRegistrationToServer(imei);
            } else {
                // THE APP DOES NOT HAVE THE PERMISSION, THIS IS NOT POSSIBLE
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}