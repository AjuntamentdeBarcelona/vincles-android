/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.TaskModel;

public class TaskNetworkDetailActivity extends TaskActivity {
    private static final String TAG = "TaskNetDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (taskModel.view.equals(TaskModel.TASK_DELETE_USER)) {
            setContentView(R.layout.activity_task_user_request);

            // Update view
            TextView texName = (TextView) findViewById(R.id.texName);
            texName.setText(taskModel.currentUser.name + " " + taskModel.currentUser.lastname);

            ImageView imgPhoto = (ImageView) findViewById(R.id.imgPhoto);
            if (taskModel.currentUser != null) {
                if(taskModel.currentUser.idContentPhoto != null) {
                    if (!isFinishing())
                        Glide.with(this)
                            .load(mainModel.getUserPhotoUrlFromUser(taskModel.currentUser))
                            .signature(new StringSignature(taskModel.currentUser.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(imgPhoto);
                } else {
                    Log.w(TAG, taskModel.currentUser.alias + " has idContentPhoto null!");
                }
            }

            TextView texRequestMsg = (TextView) findViewById(R.id.texRequestMsg);
            texRequestMsg.setText(getResources().getString(R.string.task_network_detail_request));

            View btnTaskOK = findViewById(R.id.btnTaskOK);
            btnTaskOK.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeNetworkUser();
                }
            });
            View btnTaskCancel = findViewById(R.id.btnTaskCancel);
            btnTaskCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        } else if (taskModel.view.equals(TaskModel.TASK_NETWORK_CODE)) {
            setContentView(R.layout.activity_task_network_new);

            // Update view
            TextView texNetworkCode = (TextView) findViewById(R.id.texNetworkCode);
            texNetworkCode.setText(taskModel.code);
        } else if (taskModel.view.equals(TaskModel.TASK_USER_RESULT)) {
            setContentView(R.layout.activity_task_user_result);

            // Update view
            TextView texUserResultMsg = (TextView) findViewById(R.id.texUserResultMsg);
            texUserResultMsg.setText(getResources().getString(R.string.task_network_detail_result, taskModel.currentUser.name));
            TextView texUserName = (TextView) findViewById(R.id.texUserName);
            texUserName.setVisibility(View.GONE);

            findViewById(R.id.usrImage).setVisibility(View.GONE);
            View btnUserResultAction = findViewById(R.id.btnUserResultAction);
            btnUserResultAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(TaskNetworkDetailActivity.this, TaskNetworkListActivity.class));
                }
            });
        }

    }

    public void goBack(View view) {
        Log.i(TAG, "go back to TaskNetworkListActivity");
        startActivity(new Intent(this, TaskNetworkListActivity.class));
    }

    private void removeNetworkUser() {
        showProgressBar(false,getString(R.string.delete_user_spinner));
        taskModel.removeNetworkUser(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                taskModel.view = TaskModel.TASK_USER_RESULT;
                hideProgressBar();
                startActivity(new Intent(TaskNetworkDetailActivity.this, TaskNetworkDetailActivity.class));
            }

            @Override
            public void onFailure(Object error) {
                hideProgressBar();
                Log.e(TAG, "removeNetworkUser() - error: " + error);
            }
        });
    }
}