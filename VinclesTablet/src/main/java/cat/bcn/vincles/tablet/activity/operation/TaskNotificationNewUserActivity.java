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
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.push.AppFCMDefaultListenerImpl;

public class TaskNotificationNewUserActivity extends TaskActivity {
    private static final String TAG = "TNotifNewUserActivity";
    private TextView textUser;
    private ImageView imgPhoto;
    Long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_notification_new_user);

        Bundle extras = getIntent().getExtras();
        userId = extras.getLong("userId");
        User callUser = mainModel.getUser(userId);

        String alias = callUser.alias;
        textUser = (TextView) findViewById(R.id.textUser);
        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);

        textUser.setText(alias);

        if (callUser != null) {
            if (callUser.idContentPhoto != null) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUser(callUser))
                        .signature(new StringSignature(callUser.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgPhoto);
            } else {
                Log.w(TAG, callUser.alias + " has idContentPhoto null!");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // RECEIVE PUSH NOTIFICATIONS
        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(final PushMessage pushMessage) {
                super.onPushMessageReceived(pushMessage);
                User callUser = mainModel.getUser(userId);

                if (callUser != null) {
                    if (callUser.idContentPhoto != null) {
                        if (!TaskNotificationNewUserActivity.this.isFinishing())
                            Glide.with(TaskNotificationNewUserActivity.this)
                                .load(mainModel.getUserPhotoUrlFromUser(callUser))
                                .signature(new StringSignature(callUser.idContentPhoto.toString()))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(imgPhoto);
                    } else {
                        Log.w(TAG, callUser.alias + " has idContentPhoto null!");
                    }
                }
            }

            @Override
            public void onPushMessageError(long idPush, Throwable t) {
                super.onPushMessageError(idPush, t);
                Log.d(null, "GCM: ERROR TRYING TO ACCESS PUSHID: " + idPush);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        CommonVinclesGcmHelper.setPushListener(mainModel.getPushListener());
    }

    public void goToNetwork(View view) {
        Log.i(TAG, "goToNetwork");
        startActivity(new Intent(this, TaskNetworkListActivity.class));
        finish();
    }
}
