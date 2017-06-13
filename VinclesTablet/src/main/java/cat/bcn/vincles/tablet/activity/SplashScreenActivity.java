/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import cat.bcn.vincles.tablet.monitors.SignalStrengthMonitor;
import java.util.Timer;
import java.util.TimerTask;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.VinclesActivity;
import cat.bcn.vincles.tablet.activity.configuration.ConfigMainActivity;
import cat.bcn.vincles.tablet.activity.configuration.ConfigRegisterActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskMainActivity;
import cat.bcn.vincles.tablet.monitors.InternetConnectionMonitor;

public class SplashScreenActivity extends VinclesActivity {
    private static final String TAG = "SplashScreenActivity";
    private static final long SPLASH_SCREEN_DELAY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mainModel.checkPlayServices(this))
            checkMandatoryPermissions();

        InternetConnectionMonitor.checkInternetConnection(this);
        initSignalStrengthMonitor();
    }

    private void checkMandatoryPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.phone_state_permission_title))
                        .setMessage(getString(R.string.phone_state_permission_desc))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(SplashScreenActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        VinclesConstants.PHONE_STATE_PERMISSION_REQUEST_CODE);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        VinclesConstants.PHONE_STATE_PERMISSION_REQUEST_CODE);
            }
        }
        else activateTimer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case VinclesConstants.PHONE_STATE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay!
                    activateTimer();

                } else {
                    // permission denied, boo!
                }
                return;
            }
        }
    }

    private void activateTimer() {
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mainModel.getPassword(mainModel.currentUser);
                if (mainModel.currentUser == null
                        || mainModel.currentUser.username == null
                        || mainModel.currentUser.username.equals("")
                        || mainModel.currentUser.cipher == null
                        || mainModel.currentUser.cipher.length == 0) {
                    launchActivityWithoutTransition(ConfigRegisterActivity.class);

                } else if (mainModel.tour < 8) {
                    launchActivityWithoutTransition(ConfigMainActivity.class);

                } else {
                    // Obtain accessToken
                    if (mainModel.isConnected()) {
                        mainModel.login(new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                // Initialize GCM
                                mainModel.startFCM(SplashScreenActivity.this);
                                // Now, go to to next screen with accessToken!!!
                                launchActivityWithoutTransition(TaskMainActivity.class);
                            }

                            @Override
                            public void onFailure(Object error) {
                                Log.i(TAG, "login() - error: " + error);
                                String errorMessage = mainModel.getErrorByCode(error);
                                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                                toast.show();

                                launchActivityWithoutTransition(TaskMainActivity.class);
                            }
                        }, mainModel.currentUser.username
                        , mainModel.getPassword(mainModel.currentUser));

                    } else {
                        // Now, go to to next screen without accessToken!!!
                        launchActivityWithoutTransition(TaskMainActivity.class);
                    }
                }
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }

    protected void launchActivityWithoutTransition(Class<?> cls) {
        Intent intent = new Intent(SplashScreenActivity.this, cls);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void initSignalStrengthMonitor() {
        mainModel.phoneListener = new SignalStrengthMonitor();
        mainModel.telephonyManager.listen(mainModel.phoneListener, SignalStrengthMonitor.LISTEN_SIGNAL_STRENGTHS);
    }
}
