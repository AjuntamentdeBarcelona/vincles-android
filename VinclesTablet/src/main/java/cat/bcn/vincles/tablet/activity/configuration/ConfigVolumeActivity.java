/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class ConfigVolumeActivity extends ConfigActivity {
    private static final String TAG = "ConfigVolumeActivity";
    private int step = 5;
    int volume_percent;
    private SeekBar seekBar;
    private ImageView volumeImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_volume);

        // Update view
        ViewGroup v = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        mainModel.setMargins(v);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        volumeImage = (ImageView) findViewById(R.id.volumeImage);
    }

    @Override
    public void onResume() {
        super.onResume();
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress(1);

        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int volume_level = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        volume_percent = (volume_level*100) / am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        mainModel.updateVolume(volume_percent);

        if (mainModel.volume == 100)
            volumeImage.setImageResource(R.drawable.icon_volume_alto);

        seekBar.setProgress(volume_percent);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 100) volumeImage.setImageResource(R.drawable.icon_volume_alto);
                else volumeImage.setImageResource(R.drawable.icon_volume);
                mainModel.updateVolume(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");

        volume_percent = mainModel.volume;
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigBrightnessActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainModel.updateVolume(volume_percent);
        mainModel.savePreferences(VinclesTabletConstants.VOLUME, volume_percent, VinclesConstants.PREFERENCES_TYPE_INT);
    }
}
