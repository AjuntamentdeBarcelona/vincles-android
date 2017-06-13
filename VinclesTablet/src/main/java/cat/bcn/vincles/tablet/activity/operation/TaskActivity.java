/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.VinclesActivity;
import cat.bcn.vincles.tablet.activity.configuration.ConfigMainActivity;
import cat.bcn.vincles.tablet.model.TaskModel;

public class TaskActivity extends VinclesActivity {
    protected TaskModel taskModel;
    protected ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskModel = TaskModel.getInstance();    // INITIALIZATION INSIDE
    }

    // Go to Home
    public void goToHome() {
        startActivity(new Intent(this, ConfigMainActivity.class));
        finish();
    }

    public void showProgressBar(boolean cancelable, String text) {
        progressBar = new ProgressDialog(this, R.style.ProgressCustomTheme);
        progressBar.setCancelable(cancelable);
        progressBar.setMessage(text);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.setInverseBackgroundForced(true);
        progressBar.show();
    }

    public void hideProgressBar() {
        if (progressBar != null)
            progressBar.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);
    }
}
