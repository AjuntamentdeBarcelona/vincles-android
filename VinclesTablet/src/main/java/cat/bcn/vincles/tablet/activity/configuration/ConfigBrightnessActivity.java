/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class ConfigBrightnessActivity extends ConfigActivity {
    private static final String TAG = "ConfigVolumeActivity";
    private int step = 6;
    private SeekBar seeBrightness;
    private Switch swiAutomatic;

    boolean brightnessAutomatic;
    int brightness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_brightness);

        seeBrightness = (SeekBar) findViewById(R.id.seekBar);
        seeBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progressChanged = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressChanged = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mainModel.updateBrightness(progressChanged);
            }
        });
        swiAutomatic = (Switch) findViewById(R.id.swiAutomatic);
        swiAutomatic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateAutomaticBrightness(swiAutomatic);
            }
        });
        brightness = mainModel.brightness;
        brightnessAutomatic = mainModel.brightnessAutomatic;
    }

    @Override
    protected void onResume() {
        super.onResume();

        seeBrightness.setProgress(mainModel.brightness);
        if (mainModel.brightnessAutomatic) {
            swiAutomatic.setChecked(true);
        } else {
            swiAutomatic.setChecked(false);
        }
        int isAutomatic = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
        if (isAutomatic == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) swiAutomatic.setChecked(true);
        else swiAutomatic.setChecked(false);
    }

    public void updateAutomaticBrightness(View view) {
        boolean result = ((Switch) view).isChecked();
        mainModel.updateAutomaticBrightness(result);

        Log.i(TAG, "updateAutomaticBrightness() - result: " + result);
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");

        // Go to next screen
        brightness = mainModel.brightness;
        brightnessAutomatic = mainModel.brightnessAutomatic;
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigScreenActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainModel.updateBrightness(brightness);
        mainModel.updateAutomaticBrightness(brightnessAutomatic);
        mainModel.savePreferences(VinclesTabletConstants.BRIGHTNESS, brightness, VinclesConstants.PREFERENCES_TYPE_INT);
        mainModel.savePreferences(VinclesTabletConstants.BRIGHTNESS_AUTOMATIC, brightnessAutomatic, VinclesConstants.PREFERENCES_TYPE_BOOLEAN);
    }
}
