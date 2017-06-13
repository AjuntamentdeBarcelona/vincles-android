/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.monitors;

import android.net.ConnectivityManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.util.Log;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.model.MainModel;

public class SignalStrengthMonitor extends PhoneStateListener {
    private final String TAG = this.getClass().getSimpleName();
    private MainModel mainModel = MainModel.getInstance();

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        if (mainModel.getConnectionType() == ConnectivityManager.TYPE_MOBILE) {
            int level = 0;
            if (signalStrength.isGsm()) {
                int asu = signalStrength.getGsmSignalStrength();
                Log.i(TAG, "signalStrength.isGsm(): " + asu);
                if (asu < 2) {
                    level = 1;
                } else if (asu >= 2 && asu < 5) {
                    level = 2;
                } else if (asu >= 5 && asu < 8) {
                    level = 3;
                } else if (asu >= 8 && asu < 12) {
                    level = 4;
                } else if (asu >= 12) {
                    level = 5;
                }
            } else {
                int cdmaDbm = signalStrength.getCdmaDbm();
                Log.i(TAG, "signalStrength.getCdmaDbm(): " + cdmaDbm);
                if (cdmaDbm >= -89) {
                    level = 5;
                } else if (cdmaDbm >= -97) {
                    level = 4;
                } else if (cdmaDbm >= -103) {
                    level = 3;
                } else if (cdmaDbm >= -107) {
                    level = 2;
                } else if (cdmaDbm < -109) {
                    level = 1;
                }
            }
            Log.i(TAG, "level: " + level);


            // THIS CAN CRASH WHEN APPLICATION IS NOT ACTIVE (RUNNING FROM BACKGROUND)
            // actualActivity == null cause Monitors are launching fake FCMs
            try {
                if (mainModel.context != null) {
                    if (level < VinclesConstants.TELEPHONY_SIGNAL_BOTTOM) {
                        // Show Low Signal Message
                        mainModel.isLowConnection = true;
                        mainModel.getPushListener().onPushMessageReceived(
                                new PushMessage().setType(PushMessage.TYPE_STRENGTH_CONNECTION_LOW));
                    } else if (level >= VinclesConstants.TELEPHONY_SIGNAL_TOP) {
                        // Hide Low Signal Message
                        mainModel.isLowConnection = false;
                        mainModel.getPushListener().onPushMessageReceived(
                                new PushMessage().setType(PushMessage.TYPE_STRENGTH_CONNECTION_OK));
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}