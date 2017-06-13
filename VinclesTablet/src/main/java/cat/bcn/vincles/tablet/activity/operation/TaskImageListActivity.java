/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.TaskModel;

public class TaskImageListActivity extends TaskActivity {
    private static final String TAG = "TaskImageListActivity";
    private Message message;
    public static final String OFFSET_STRING = "USERLIST_OFFSET";
    private List<User> userlist;
    private int usersInScreen = 8;
    ViewGroup circleUsers[] = new ViewGroup[usersInScreen];
    int offset = 0, currImage = 0;
    private View ll_prev, ll_next, userListLayout, txNoUsers;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) currImage = extras.getInt(TaskImageDetailActivity.PARAM_SHARED_IMAGE, 0);

        if (taskModel.view.equals(TaskModel.TASK_SHARE_IMAGE)) {
            setContentView(R.layout.activity_task_user_list);

            offset = getIntent().getIntExtra(TaskMessageUserListActivity.OFFSET_STRING, 0);
            Log.d(null, "USER OFFSET : " + offset);

            ll_next = findViewById(R.id.showMoreUsers);
            ll_prev = findViewById(R.id.showLessUsers);
            userListLayout = findViewById(R.id.userListLayout);
            txNoUsers = findViewById(R.id.txNoUsers);

            Resources res = getResources();
            for (int i = 0; i < circleUsers.length; i++) {
                circleUsers[i] = (ViewGroup) findViewById(
                        res.getIdentifier("userCircle" +(i+1) , "id", getPackageName()));
            }
        } else if (taskModel.view.equals(TaskModel.TASK_USER_RESULT)) {
            setContentView(R.layout.activity_task_user_result);

            TextView texUserResultMsg = (TextView) findViewById(R.id.texUserResultMsg);
            TextView texUserName = (TextView) findViewById(R.id.texUserName);
            ImageView userPhoto = (ImageView) findViewById(R.id.usrImage);

            if (taskModel.getCurrentMessage().metadataTipus.equalsIgnoreCase(VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE))
                texUserResultMsg.setText(getString(R.string.task_gallery_share_result,
                        getString(R.string.task_gallery_share_result_image)));
            else
                texUserResultMsg.setText(getString(R.string.task_gallery_share_result,
                        getString(R.string.task_gallery_share_result_video)));
            if ((taskModel.currentUser.getId().longValue() == mainModel.currentUser.getId().longValue())) {
                texUserName.setText(R.string.task_contactlist_all);
                userPhoto.setImageResource(R.drawable.icon_todos_with_background);
            } else {
                if (taskModel.currentUser != null) {
                    texUserName.setText(taskModel.currentUser.name);
                    if (taskModel.currentUser.idContentPhoto != null) {
                        if (!isFinishing())
                            Glide.with(this)
                                .load(mainModel.getUserPhotoUrlFromUser(taskModel.currentUser))
                                .signature(new StringSignature(taskModel.currentUser.idContentPhoto.toString()))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(userPhoto);
                    } else {
                        Log.w(TAG, taskModel.currentUser.alias + "has  idContentPhoto is null!");
                    }
                }
            }

            View btnUserResultAction = findViewById(R.id.btnUserResultAction);
            btnUserResultAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(TaskImageListActivity.this, TaskImageDetailActivity.class);
                    if (currImage != 0) {
                        i.putExtra(TaskImageDetailActivity.PARAM_SHARED_IMAGE, currImage);
                    }
                    startActivity(i);
                }
            });

            TextView btnUserResultActionText = (TextView) findViewById(R.id.btnUserResultActionText);
            btnUserResultActionText.setText(getResources().getString(R.string.image_back));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (taskModel.view.equals(TaskModel.TASK_SHARE_IMAGE)) {
            userlist = taskModel.getUserList();

            fillCurrentUsers();
            if (offset == 0) {
                taskModel.getUserServerList(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        Log.i(TAG, "getUserServerList() - result");
                        // Now, load from local user list updated!!!
                        userlist = taskModel.getUserList();

                        fillCurrentUsers();
                    }

                    @Override
                    public void onFailure(Object error) {
                        Log.e(TAG, "getUserServerList() - error: " + error);
                    }
                });
            }
        }
    }

    public void fillCurrentUsers() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fillCurrentUsersPrivate();
            }
        });
    }

    private void fillCurrentUsersPrivate() {
        int i = 0;
        if (userlist.size() == 0) {
            userListLayout.setVisibility(View.GONE);
            txNoUsers.setVisibility(View.VISIBLE);
            ll_next.setEnabled(false);
            ll_prev.setEnabled(false);
            return;
        } else {
            userListLayout.setVisibility(View.VISIBLE);
            txNoUsers.setVisibility(View.GONE);
        }

        for (final ViewGroup userLayout : circleUsers) {
            ImageView image = (ImageView) circleUsers[i].getChildAt(0);
            TextView text = (TextView) circleUsers[i].getChildAt(1);

            // FIRST ELEMENT IS "SEND TO ALL"
            if (offset == 0 && i == 0) {
                image.setImageResource(R.drawable.icon_todos_with_background);
                text.setText(R.string.task_contactlist_all);
            } else if (userlist.size() + 1 > offset + i) {
                if (userlist.get(offset + i -1) != null) {
                    text.setText(userlist.get(offset + i -1).name);
                    if (userlist.get(offset + i -1).idContentPhoto != null) {
                        if (!isFinishing())
                            Glide.with(this)
                                .load(mainModel.getUserPhotoUrlFromUser(userlist.get(offset + i -1)))
                                .signature(new StringSignature(userlist.get(offset + i -1).idContentPhoto.toString()))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(image);
                    } else {
                        Log.w(TAG, userlist.get(offset + i -1).alias + "has  idContentPhoto is null!");
                    }
                }
            }
            else {
                circleUsers[i].setVisibility(View.GONE);
            }

            // ONCLICKS FOR ALL
            if (offset == 0 && i == 0) userLayout.setTag(-1);
            else userLayout.setTag(Math.max(0, offset + i -1));
            userLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int userPosition = (int)v.getTag();

                    /////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\
                    //     HACK ALERT: SAME USER MEANS SEND TO ALL  \\
                    /////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\
                    if (userPosition == -1) taskModel.currentUser = mainModel.currentUser;
                    else taskModel.currentUser = userlist.get(userPosition);

                    List<User> userList = new ArrayList<User>();
                    if ((taskModel.currentUser.getId().longValue() == mainModel.currentUser.getId().longValue())) {
                        userList = taskModel.getUserList();
                    } else {
                        userList.add(taskModel.currentUser);
                    }

                    // Update & Send Message to Server
                    taskModel.getCurrentMessage().idUserFrom = mainModel.currentUser.getId();
                    taskModel.getCurrentMessage().idUserTo = taskModel.currentUser.getId();
                    showProgressBar(false,getString(R.string.general_upload));
                    taskModel.sendMessageToUserList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            // Refresh to result view
                            hideProgressBar();
                            taskModel.view = TaskModel.TASK_USER_RESULT;
                            startActivity(getIntent());
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.i(TAG, "sendMessage() - error: " + error);
                            hideProgressBar();
                            String errorMessage = mainModel.getErrorByCode(error);
                            Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }, taskModel.getCurrentMessage(), userList);
                }
            });

            i++;
        }

        checkButtons();
    }

    private void checkButtons() {
        ll_next.setEnabled(true);
        ll_prev.setEnabled(true);
        if (offset + usersInScreen > userlist.size()) ll_next.setEnabled(false);
        if (offset == 0) ll_prev.setEnabled(false);
    }

    public void showMore(View v) {
        final Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if(((String)v.getTag()).equalsIgnoreCase("less")) {
            offset -= usersInScreen;
            if (offset < 0 ) offset = 0;
            intent.putExtra(TaskMessageUserListActivity.OFFSET_STRING, offset);
        } else {
            if (offset + usersInScreen <= userlist.size()) offset += usersInScreen;
            intent.putExtra(TaskMessageUserListActivity.OFFSET_STRING, offset);
        }

        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}