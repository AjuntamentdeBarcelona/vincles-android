/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.VinclesActivity;
import cat.bcn.vincles.tablet.model.MainModel;

public class ConfigActivity extends VinclesActivity {
    public final int REQUEST_CAMERA_RESULT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case REQUEST_CAMERA_RESULT:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, getString(R.string.permission_camera), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);
    }
}
