/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.security.acl.Group;
import java.util.Calendar;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMainActivity;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.ResourceModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class ConfigMainActivity extends ConfigActivity {
    private static final String TAG = "ConfigMainActivity";
    private ProgressDialog progressBar;
    private boolean oldAvoidServerCalls;
    private View [] buttons;
    private View btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_main);

        buttons = new View[8];
        buttons[0] = findViewById(R.id.config_btn_nom);         // NAME
        buttons[1] = findViewById(R.id.config_btn_photo);       // PHOTO
        buttons[2] = findViewById(R.id.config_btn_languaje);    // LANGUAJE
        buttons[3] = findViewById(R.id.config_btn_font);        // FONT
        buttons[4] = findViewById(R.id.config_btn_volume);      // VOLUME
        buttons[5] = findViewById(R.id.config_btn_brightness);  // BRIGHTNESS
        buttons[6] = findViewById(R.id.config_btn_margins);     // MARGINS
        buttons[7] = findViewById(R.id.config_btn_line);        // GRAPHIC LINE
        btnConfirm = findViewById(R.id.btnConfirm);             // CONFIRM

        oldAvoidServerCalls = MainModel.avoidServerCalls;
    }

    @Override
    protected void onResume() {
        super.onResume();

        int step = mainModel.tour;
        if (step >= buttons.length) step =  buttons.length-1;
        for (; step >= 0; step--) {
            buttons[step].setEnabled(true);
        }

        if (mainModel.tour < buttons.length) {
            btnConfirm.setVisibility(View.INVISIBLE);
        }
    }

    public void goToView(View view) {
        String value = (String) ((AppCompatButton) view).getTag();

        Log.i(TAG, "Got to task num: " + value);
        switch (value) {
            case ("1"):
                startActivity(new Intent(this, ConfigNomActivity.class));
                break;
            case ("2"):
                startActivity(new Intent(this, ConfigImageActivity.class));
                break;
            case ("3"):
                startActivity(new Intent(this, ConfigLanguageActivity.class));
                break;
            case ("4"):
                startActivity(new Intent(this, ConfigFontActivity.class));
                break;
            case ("5"):
                startActivity(new Intent(this, ConfigVolumeActivity.class));
                break;
            case ("6"):
                startActivity(new Intent(this, ConfigBrightnessActivity.class));
                break;
            case ("7"):
                startActivity(new Intent(this, ConfigScreenActivity.class));
                break;
            case ("8"):
                startActivity(new Intent(this, ConfigThemeActivity.class));
                break;
        }
    }

    public void goToTaskMain(View view) {
        finishConfiguration();
    }

    private void finishConfiguration() {
        // FIST LAUNCH
        if (mainModel.tour == buttons.length) {
            progressBar = new ProgressDialog(this/*,R.style.DialogCustomTheme*/);
            progressBar.setMessage(getString(R.string.first_launch_configuration));
            progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressBar.setInverseBackgroundForced(true);
            progressBar.setCancelable(false);
            progressBar.show();

            // SLEEP TIME ENOUGH TO SEE MESSAGE AND DO NOT DISTURB
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startAutoConfigurationStep();
                }
            }, 1000);
        }
        else startMainActivity();
    }

    private void startAutoConfigurationStep() {
        mainModel.tour++;
        MainModel.avoidServerCalls = false;
        Log.i(TAG, "Auto Configuration Step " + (mainModel.tour));
        final AsyncResponse response = new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                Log.i(TAG, "Auto Configuration Step " + (mainModel.tour) + " OK!");
                startAutoConfigurationStep();
            }

            @Override
            public void onFailure(Object error) {
                Log.e(TAG, "getUserServerList() - error: " + error);
                progressBar.dismiss();
            }
        };

        Calendar calFrom = VinclesConstants.getCalendarWithoutTime(Calendar.getInstance());
        String dateFrom = String.valueOf(calFrom.getTime().getTime());
        final GroupModel groupModel = GroupModel.getInstance();

        switch (mainModel.tour) {
            case 9:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_1));
                TaskModel.getInstance().getUserServerList(response);
                break;
            case 10:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_2));
                TaskModel.getInstance().getTaskServerList(response, mainModel.currentUser.idCalendar, dateFrom, null);
                break;
            case 11:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_3));
                TaskModel.getInstance().getMessageServerList(response, null, null);
                break;
            case 12:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_4));
                groupModel.getGroupServerList(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.i(TAG, "getGroupServerList() - result");
                        for (VinclesGroup group : groupModel.getGroupList()) {
                            groupModel.getChatServerList(new AsyncResponse() {
                                @Override public void onSuccess(Object result) {}
                                @Override public void onFailure(Object error) {}
                            }, group.idChat, "0", String.valueOf(System.currentTimeMillis()));

                            groupModel.getGroupUserServerList(new AsyncResponse() {
                                @Override public void onSuccess(Object result) {}
                                @Override public void onFailure(Object error) {}
                            }, group.getId());

                            groupModel.getChatServerList(new AsyncResponse() {
                                @Override public void onSuccess(Object result) {}
                                @Override public void onFailure(Object error) {}
                            }, group.idDynamizerChat, "0", String.valueOf(System.currentTimeMillis()));
                        }
                        response.onSuccess(result);
                    }

                    @Override
                    public void onFailure(Object error) {
                        response.onFailure(error);
                        Log.e(TAG, "getGroupServerList() - error: " + error);
                        Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            case 13:
                progressBar.setMessage(getString(R.string.first_launch_configuration_step_5));
                ResourceModel.getInstance().getServerResourceList(response, "", "");
                break;
            default:
                MainModel.avoidServerCalls = oldAvoidServerCalls;
                mainModel.updateTourStep(mainModel.tour);
                if (progressBar != null) {
                    progressBar.dismiss();
                }
                startMainActivity();
                return;
        }
    }

    @Override
    public void onBackPressed() {
        if (mainModel.tour > buttons.length)
            startMainActivity();
        else super.onBackPressed();
    }

    private void startMainActivity() {
        Intent i = new Intent(this, TaskMainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }
}
