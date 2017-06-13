/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.monitors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.model.MainModel;

public class BatteryLevelMonitor extends BroadcastReceiver {
    private static final String TAG = BatteryLevelMonitor.class.getSimpleName();
    public static boolean lowBattery = false;
    public static float batteryPct = 90f;
    private MainModel mainModel = MainModel.getInstance();

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        batteryPct = level / (float)scale;

        if(Intent.ACTION_BATTERY_LOW.equalsIgnoreCase(intentAction)){
            lowBattery = true;

            // THIS CAN CRASH WHEN APPLICATION IS NOT ACTIVE (RUNNING FROM BACKGROUND)
            // actualActivity == null cause Monitors are launching fake FCMs
            try {
                if (mainModel.context != null) {
                    mainModel.getPushListener().onPushMessageReceived(
                            new PushMessage().setType(PushMessage.TYPE_BATTERY_LOW));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }

        if(Intent.ACTION_BATTERY_OKAY.equalsIgnoreCase(intentAction) || isCharging){
            lowBattery = false;

            // THIS CAN CRASH WHEN APPLICATION IS NOT ACTIVE (RUNNING FROM BACKGROUND)
            // actualActivity == null cause Monitors are launching fake FCMs
            try {
                if (mainModel.context != null) {
                    mainModel.getPushListener().onPushMessageReceived(
                            new PushMessage().setType(PushMessage.TYPE_BATTERY_OKAY));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
