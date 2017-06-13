/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.tablet.BuildConfig;
import cat.bcn.vincles.tablet.activity.configuration.ConfigMainActivity;
import cat.bcn.vincles.tablet.R;

public class TaskLoginActivity extends TaskActivity {
    private static final String TAG = "TaskLoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_login);
    }

    public void confirm(View view) {
        Log.i(TAG, "confirm()");
        TextView texPassword = (TextView) findViewById(R.id.texPassword);
        String password = texPassword.getText().toString();

        if (password.equals(mainModel.getPIN())) {
            startActivity(new Intent(TaskLoginActivity.this, ConfigMainActivity.class));
            finish();
        } else {
            String pinText = getString(R.string.pin_msg);
            if (BuildConfig.DEBUG) pinText += "\nDebug pin hint: " + mainModel.getPIN();
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.pin_title))
                    .setMessage(pinText)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    /*.setIcon(android.R.drawable.ic_dialog_alert)*/
                    .show();
        }
    }
}
