/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class ConfigFontActivity extends ConfigActivity {
    private static final String TAG = "ConfigNomActivity";
    private int step = 4;
    private SeekBar seekBar;
    private TextView txFontMsg;
    float fontSize;
    boolean restore = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_font);

        seekBar = (SeekBar) findViewById(R.id.seekBar);

        // Msg
        txFontMsg = (TextView) findViewById(R.id.txtFontMsg);
        txFontMsg.setText(getResources().getString(R.string.task_config_font_msg, mainModel.currentUser.alias));

        if (getIntent().hasExtra("fontSize"))
            fontSize = getIntent().getFloatExtra("fontSize", mainModel.fontSize);
        else fontSize = mainModel.fontSize;
        Log.d(TAG, "FONT SIZE: " + fontSize + " / " + mainModel.fontSize);

        findViewById(R.id.btnConfigurationMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainModel.updateFontSize(fontSize);
                finishAffinity();
                startActivity(new Intent(ConfigFontActivity.this, ConfigMainActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress(1);
        if (mainModel.fontSize == VinclesTabletConstants.FONT_SMALL) seekBar.setProgress(0);
        if (mainModel.fontSize == VinclesTabletConstants.FONT_LARGE) seekBar.setProgress(2);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        mainModel.updateFontSize(VinclesTabletConstants.FONT_SMALL);
                        refreshScreen();
                        break;
                    case 1:
                        mainModel.updateFontSize(VinclesTabletConstants.FONT_NORMAL);
                        refreshScreen();
                        break;
                    case 2:
                        mainModel.updateFontSize(VinclesTabletConstants.FONT_LARGE);
                        refreshScreen();
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void refreshScreen() {
        final Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        overridePendingTransition(0, 0);
        intent.putExtra("fontSize", fontSize);
        startActivity(intent);
        overridePendingTransition(0, 0);
        restore = false;
        finish();
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");

        Log.d(TAG, "FINISH FONT SIZ: " + fontSize + " / " + mainModel.fontSize);
        fontSize = mainModel.fontSize;
        mainModel.savePreferences(VinclesTabletConstants.FONT_SIZE, fontSize, VinclesConstants.PREFERENCES_TYPE_FLOAT);
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigVolumeActivity.class));
        restore = false;
    }
}
