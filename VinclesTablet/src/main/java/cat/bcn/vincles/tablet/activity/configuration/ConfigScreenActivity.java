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
import android.widget.Button;
import android.widget.Switch;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMainActivity;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class ConfigScreenActivity extends ConfigActivity {
    private static final String TAG = "ConfigScreenActivity";
    private int step = 7;
    int margin_left, margin_right;
    private Button lessMarginLeft, lessMarginRight, moreMarginLeft, moreMarginRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_screen);

        lessMarginLeft = (Button) findViewById(R.id.lessMarginLeft);
        lessMarginRight = (Button) findViewById(R.id.lessMarginRight);
        moreMarginLeft = (Button) findViewById(R.id.moreMarginLeft);
        moreMarginRight = (Button) findViewById(R.id.moreMarginRight);

        margin_left = mainModel.marginLeft;
        margin_right = mainModel.marginRight;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateButtonsStatus();
    }

    public void updateMarginLeft(View view) {
        boolean result = ((Button) view).getText().toString().equalsIgnoreCase("+");
        mainModel.updateMarginLeft(result);

        ViewGroup v = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        mainModel.setMargins(v);

        Log.i(TAG, "updateMargin() - result: " + result);
        updateButtonsStatus();
    }

    public void updateMarginRight(View view) {
        boolean result = ((Button) view).getText().toString().equalsIgnoreCase("+");
        mainModel.updateMarginRight(result);

        ViewGroup v = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        mainModel.setMargins(v);

        Log.i(TAG, "updateMargin() - result: " + result);
        updateButtonsStatus();
    }

    private void updateButtonsStatus() {
        if (mainModel.marginLeft != VinclesTabletConstants.MARGIN_LEFT_NORMAL) {
            lessMarginLeft.setEnabled(true);
            moreMarginLeft.setEnabled(false);
        } else {
            lessMarginLeft.setEnabled(false);
            moreMarginLeft.setEnabled(true);
        }

        if (mainModel.marginRight != VinclesTabletConstants.MARGIN_RIGHT_NORMAL) {
            lessMarginRight.setEnabled(true);
            moreMarginRight.setEnabled(false);
        } else {
            lessMarginRight.setEnabled(false);
            moreMarginRight.setEnabled(true);
        }
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");

        // Go to next screen
        margin_left = mainModel.marginLeft;
        margin_right = mainModel.marginRight;
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigThemeActivity.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainModel.updateMarginLeft(margin_left != VinclesTabletConstants.MARGIN_LEFT_NORMAL);
        mainModel.updateMarginRight(margin_right != VinclesTabletConstants.MARGIN_RIGHT_NORMAL);
        mainModel.savePreferences(VinclesTabletConstants.MARGIN_LEFT, margin_left, VinclesConstants.PREFERENCES_TYPE_INT);
        mainModel.savePreferences(VinclesTabletConstants.MARGIN_RIGHT, margin_right, VinclesConstants.PREFERENCES_TYPE_INT);
    }
}
