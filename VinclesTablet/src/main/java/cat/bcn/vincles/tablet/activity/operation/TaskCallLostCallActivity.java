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
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.tablet.R;

public class TaskCallLostCallActivity extends TaskActivity {
    private final String TAG = this.getClass().getSimpleName();
    private ImageView imagePhoto;
    private TextView textName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_call_lostcall);

        textName = (TextView)findViewById(R.id.texName);
        imagePhoto = (ImageView) findViewById(R.id.imgPhoto);

        checkIfCallWasConnected(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        checkIfCallWasConnected(intent);
    }

    private void checkIfCallWasConnected(Intent intent) {
        boolean wasCallConnected = intent.getBooleanExtra("wasCallConnected", false);
        Log.d(TAG, "wasCallConnected? " + wasCallConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Long imageId = taskModel.currentUser.idContentPhoto;
        if (imageId == null) {
            imageId = 0L;
            Log.w(TAG, "Current user's idContentPhoto is null; defaulting to 0 instead");
        }

        if (!isFinishing())
            Glide.with(this)
                .load(mainModel.getUserPhotoUrlFromUser(taskModel.currentUser))
                .signature(new StringSignature(imageId.toString()))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(imagePhoto);
        textName.setText(getString(R.string.task_call_closed, taskModel.currentUser.alias));
    }

    public void goSendMessage(View v) {
        startActivity(new Intent(this, TaskMessageSendActivity.class));
        finish();
    }

    public void goCallAgain(View v) {
        startActivity(new Intent(this, TaskCallActivity.class));
        finish();
    }
}