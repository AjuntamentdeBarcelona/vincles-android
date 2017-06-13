/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import cat.bcn.vincles.lib.VinclesApp;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.model.MainModel;

public class InternetConnectionMonitor extends BroadcastReceiver {
    private static final String TAG = InternetConnectionMonitor.class.getSimpleName();
    public static boolean isNoConnection = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        checkInternetConnection(context);
        // THIS CAN CRASH WHEN APPLICATION IS NOT ACTIVE (RUNNING FROM BACKGROUND)
        // actualActivity == null cause Monitors are launching fake FCMs
        try {
            if (isNoConnection)
                MainModel.getInstance().getPushListener().onPushMessageReceived(
                        new PushMessage().setType(PushMessage.TYPE_NO_CONNECTION));
            else
                MainModel.getInstance().getPushListener().onPushMessageReceived(
                        new PushMessage().setType(PushMessage.TYPE_CONNECTION_OKAY));
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isNoConnection = activeNetwork == null || !activeNetwork.isConnectedOrConnecting();
        return isNoConnection;
    }
}
