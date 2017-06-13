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
import android.widget.CompoundButton;
import android.widget.Switch;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class ConfigThemeActivity extends ConfigActivity {
    private static final String TAG = "ConfigThemeActivity";
    private int step = 8;
    private Switch swiTheme;
    private String theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_theme);

        swiTheme = (Switch) findViewById(R.id.swiTheme);
        if (getIntent().hasExtra("theme"))
            theme = getIntent().getStringExtra("theme");
        else theme = mainModel.theme;
        Log.d(TAG, "THEME: " + theme);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mainModel.theme.equals(VinclesTabletConstants.CLARA_THEME)) {
            swiTheme.setChecked(false);
        } else {
            swiTheme.setChecked(true);
        }

        swiTheme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                changeTheme(buttonView);
            }
        });

        findViewById(R.id.selectImgClara).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainModel.changeTheme(VinclesTabletConstants.CLARA_THEME);
                refreshScreen();
            }
        });

        findViewById(R.id.selectImgFosca).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainModel.changeTheme(VinclesTabletConstants.FOSCA_THEME);
                refreshScreen();
            }
        });
    }

    public void changeTheme(View view) {
        boolean result = ((Switch) view).isChecked();
        if (result) {
            mainModel.changeTheme(VinclesTabletConstants.FOSCA_THEME);
        } else {
            mainModel.changeTheme(VinclesTabletConstants.CLARA_THEME);
        }

        refreshScreen();

        Log.i(TAG, "updateLocale() - result: " + result);
    }

    private void refreshScreen() {
        final Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        overridePendingTransition(0, 0);
        intent.putExtra("theme", theme);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");

        // Go to next screen
        theme = swiTheme.isChecked() ? VinclesTabletConstants.FOSCA_THEME : VinclesTabletConstants.CLARA_THEME;
        mainModel.changeTheme(theme);
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigMainActivity.class));
    }

    @Override
    protected void onDestroy() {
        mainModel.changeTheme(theme);
        mainModel.savePreferences(VinclesTabletConstants.APP_THEME, mainModel.theme, VinclesConstants.PREFERENCES_TYPE_STRING);
        super.onDestroy();
    }
}
