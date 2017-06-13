/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Network;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.model.MainModel;

public class SignalReceiver extends BroadcastReceiver {
    private final String TAG = this.getClass().getSimpleName();
    private MainModel mainModel = MainModel.getInstance();
    WifiManager wifiManager;

    public SignalReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mainModel.getConnectionType() == ConnectivityManager.TYPE_WIFI) {
            // Control Wifi Signal Strength
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            int numberOfLevels = VinclesConstants.WIFI_SIGNAL_NUMBER;
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);

            // THIS CAN CRASH WHEN APPLICATION IS NOT ACTIVE (RUNNING FROM BACKGROUND)
            // actualActivity == null cause Monitors are launching fake FCMs
            try {
                if (mainModel.context != null) {

                    if (level < VinclesConstants.WIFI_SIGNAL_BOTTOM) {
                        // Show Low Signal Message
                        mainModel.isLowConnection = true;
                        mainModel.getPushListener().onPushMessageReceived(
                                new PushMessage().setType(PushMessage.TYPE_STRENGTH_CONNECTION_LOW));
                    } else if (level >= VinclesConstants.WIFI_SIGNAL_TOP) {
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
