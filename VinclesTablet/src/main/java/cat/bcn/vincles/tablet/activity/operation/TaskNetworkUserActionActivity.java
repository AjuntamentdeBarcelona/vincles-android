/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.TaskModel;

public class TaskNetworkUserActionActivity extends TaskActivity {
    private static final String TAG = "TaskNetDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_user_request_action);

        // Update view
        TextView texName = (TextView) findViewById(R.id.texName);
        texName.setText(taskModel.currentUser.name + " " + taskModel.currentUser.lastname);

        ImageView imgPhoto = (ImageView) findViewById(R.id.imgPhoto);

        if (taskModel.currentUser != null) {
            if (taskModel.currentUser.idContentPhoto != null) {
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
        } else {
            imgPhoto.setImageResource(R.drawable.user);
        }
    }

    public void goNetwork(View view) {
        finish();
    }
    public void goRemove(View view) {
        startActivity(new Intent(this, TaskNetworkDetailActivity.class));
    }

    public void goBack(View view) {
        Log.i(TAG, "go back to TaskNetworkListActivity");
        startActivity(new Intent(this, TaskNetworkListActivity.class));
    }
}