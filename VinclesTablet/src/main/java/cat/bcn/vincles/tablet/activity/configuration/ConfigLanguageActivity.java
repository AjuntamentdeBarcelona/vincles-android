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

public class ConfigLanguageActivity extends ConfigActivity {
    private static final String TAG = "ConfigLanguageActivity";
    private int step = 3;
    private Switch swiLocale;
    String language, country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_language);

        swiLocale = (Switch) findViewById(R.id.swiLocale);
        if (mainModel.language.equals("ca")) {
            swiLocale.setChecked(true);
        } else {
            swiLocale.setChecked(false);
        }

        language = mainModel.language;
        country = mainModel.country;
        swiLocale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateLocale(swiLocale);
            }
        });
    }

    public void updateLocale(View view) {
        boolean result = ((Switch) view).isChecked();
        if (result) {
            mainModel.updateLocale("ca", "ES");
        } else {
            mainModel.updateLocale("es", "ES");
        }

        Log.i(TAG, "updateLocale() - result: " + result);
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");

        // Go to next screen
        language = mainModel.language;
        country = mainModel.country;
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigFontActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainModel.updateLocale(language, country);
        mainModel.savePreferences(VinclesTabletConstants.APP_LANGUAGE, language, VinclesConstants.PREFERENCES_TYPE_STRING);
        mainModel.savePreferences(VinclesTabletConstants.APP_COUNTRY, country, VinclesConstants.PREFERENCES_TYPE_STRING);
    }
}
