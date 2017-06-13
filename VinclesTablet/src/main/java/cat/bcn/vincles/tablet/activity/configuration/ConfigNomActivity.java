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
import android.widget.EditText;
import android.widget.TextView;
import cat.bcn.vincles.tablet.R;

public class ConfigNomActivity extends ConfigActivity {
    private static final String TAG = "ConfigNomActivity";
    private int step = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_name);

        ((TextView) findViewById(R.id.edtName)).setText(mainModel.currentUser.alias);
    }

    public void confirm(View view) {
        String alias = ((EditText) findViewById(R.id.edtName)).getText().toString();

        if (!mainModel.currentUser.alias.equals(alias)) {
            Log.i(TAG, "confirm() - alias: " + alias);
            mainModel.currentUser.alias = alias;
            mainModel.saveUser(mainModel.currentUser);

            // Update at server
            mainModel.updateUserServer(mainModel.currentUser);
        }

        // Go to next screen
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigImageActivity.class));
    }
}