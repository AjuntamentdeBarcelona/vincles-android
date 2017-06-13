/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.monitors.BatteryLevelMonitor;
import cat.bcn.vincles.tablet.monitors.InternetConnectionMonitor;
import cat.bcn.vincles.tablet.monitors.SignalStrengthMonitor;
import cat.bcn.vincles.tablet.push.AppFCMDefaultListenerImpl;

public class VinclesActivity extends AppCompatActivity {
    protected MainModel mainModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // WITHOUT TRY CATCH:
        // ERROR INTO MODEL INITIALIZATION IS A CRITICAL ERROR SO LET IT BLOW UP
        mainModel = MainModel.getInstance();
        mainModel.initialize(this);
        setTheme(mainModel.getResourceTheme());
        super.onCreate(savedInstanceState);

        // Update view
        ViewGroup v = (ViewGroup)findViewById(android.R.id.content);
        if (v != null) mainModel.setMargins(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mainModel.getPushListener() != null && mainModel.getPushListener() instanceof AppFCMDefaultListenerImpl)
            ((AppFCMDefaultListenerImpl) mainModel.getPushListener()).setActualActivity(this);

        try {
            checkBatteryStatus();
            checkInternetStatus();
//            checkSignalStatus();
        } catch (Exception e) {
            // THIS PART SHOULD NEVER CRASH THE APP
            e.printStackTrace();
        }
    }

    public void checkInternetStatus() {
        if (InternetConnectionMonitor.isNoConnection) {
            if (findViewById(R.id.alert_layout) == null) {
                LayoutInflater inflater = getLayoutInflater();
                getWindow().addContentView(inflater.inflate(R.layout.alert_layout, null),
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            findViewById(R.id.alert_layout).setVisibility(View.VISIBLE);
            View v = findViewById(R.id.signal_layout);
            v.setVisibility(View.VISIBLE);
        }
        else {
            View v = findViewById(R.id.signal_layout);
            if (v != null) {
                v.setVisibility(View.GONE);
                if (!BatteryLevelMonitor.lowBattery)
                    findViewById(R.id.alert_layout).setVisibility(View.GONE);
            }
        }
    }

    public void checkBatteryStatus() {
        if (BatteryLevelMonitor.lowBattery) {
            if (findViewById(R.id.alert_layout) == null) {
                LayoutInflater inflater = getLayoutInflater();
                getWindow().addContentView(inflater.inflate(R.layout.alert_layout, null),
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            findViewById(R.id.alert_layout).setVisibility(View.VISIBLE);
            View v = findViewById(R.id.battery_layout);
            v.setVisibility(View.VISIBLE);
        }
        else {
            View v = findViewById(R.id.battery_layout);
            if (v != null) {
                v.setVisibility(View.GONE);
                if (!InternetConnectionMonitor.isNoConnection)
                    findViewById(R.id.alert_layout).setVisibility(View.GONE);
            }
        }
    }

    public void checkStrengthSignalStatus() {
        if (mainModel.isLowConnection) {
            if (findViewById(R.id.alert_layout) == null) {
                LayoutInflater inflater = getLayoutInflater();
                getWindow().addContentView(inflater.inflate(R.layout.alert_layout, null),
                        new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            findViewById(R.id.alert_layout).setVisibility(View.VISIBLE);
            View v = findViewById(R.id.strength_signal_layout);
            v.setVisibility(View.VISIBLE);
        } else {
            removeStrengthSignalStatus();
        }
    }

    public void removeStrengthSignalStatus() {
        View v = findViewById(R.id.strength_signal_layout);
        if (v != null) {
            v.setVisibility(View.GONE);
            if (!BatteryLevelMonitor.lowBattery)
                findViewById(R.id.alert_layout).setVisibility(View.GONE);
        }
    }
}
