/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

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
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageListActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageSendActivity;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class GroupsChatVinclesGroupListActivity extends GroupsActivity {
    private static final String TAG = "TaskMessageListActivity";
    public static final String OFFSET_STRING = "USERLIST_OFFSET";
    private List<VinclesGroup> groupList;
    private int groupListInScreen = 8;
    private ViewGroup circleGroupList[] = new ViewGroup[groupListInScreen];
    private int offset = 0;
    private View ll_prev, ll_next, groupListLayout, txNoGroups;
    private GroupModel groupModel = GroupModel.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_chat_list);

        offset = getIntent().getIntExtra(GroupsChatVinclesGroupListActivity.OFFSET_STRING, 0);
        Log.d(null, "USER OFFSET : " + offset);

        ll_next = findViewById(R.id.showMoreGroups);
        ll_prev = findViewById(R.id.showLessGroups);
        groupListLayout = findViewById(R.id.groupListLayout);
        txNoGroups = findViewById(R.id.txNoGroups);

        Resources res = getResources();
        for (int i = 0; i < circleGroupList.length; i++) {
            circleGroupList[i] = (ViewGroup) findViewById(
                    res.getIdentifier("groupCircle" +(i+1) , "id", getPackageName()));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        groupList = groupModel.getGroupList();

        fillCurrentGroupList();
        if (offset == 0) {
            groupModel.getGroupServerList(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.i(TAG, "getGroupServerList() - result");
                    // Now, load from local user list updated!!!
                    groupList = groupModel.getGroupList();

                    fillCurrentGroupList();
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(TAG, "getGroupServerList() - error: " + error);
                }
            });
        }
    }

    public void fillCurrentGroupList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fillCurrentGropuListPrivate();
            }
        });
    }

    public void fillCurrentGropuListPrivate() {
        int i = 0;
        if (groupList.size() == 0) {
            groupListLayout.setVisibility(View.GONE);
            txNoGroups.setVisibility(View.VISIBLE);
            ll_next.setEnabled(false);
            ll_prev.setEnabled(false);
            return;
        } else {
            groupListLayout.setVisibility(View.VISIBLE);
            txNoGroups.setVisibility(View.GONE);
        }

        for (final ViewGroup userLayout : circleGroupList) {
            ImageView image = (ImageView) circleGroupList[i].getChildAt(0);
            TextView text = (TextView) circleGroupList[i].getChildAt(1);

            // FIRST ELEMENT IS "SEND TO ALL"
            if (offset == 0 && i == 0) {
                image.setImageResource(R.drawable.icon_todos_with_background);
                text.setText(R.string.task_contactlist_all);
            } else if (groupList.size() + 1 > offset + i) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(groupModel.getGroupPhotoUrlFromGroupId(groupList.get(offset + i -1).getId()))
                        .signature(new StringSignature(groupList.get(offset + i -1).getId().toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(image);

                text.setText(groupList.get(offset + i -1).name);
            }
            else {
                circleGroupList[i].setVisibility(View.GONE);
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
                    if (userPosition == -1) {
                        taskModel.currentGroup = null; // so, to All groups
                    } else {
                        taskModel.currentGroup = groupList.get(userPosition);
                    }

                    startActivity(new Intent(GroupsChatVinclesGroupListActivity.this, TaskMessageSendActivity.class));
                }
            });

            i++;
        }

        checkButtons();
    }

    private void checkButtons() {
        ll_next.setEnabled(true);
        ll_prev.setEnabled(true);
        if (offset + groupListInScreen > groupList.size()) ll_next.setEnabled(false);
        if (offset == 0) ll_prev.setEnabled(false);
    }

    public void showMore(View v) {
        final Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        if(((String)v.getTag()).equalsIgnoreCase("less")) {
            offset -= groupListInScreen;
            if (offset < 0 ) offset = 0;
            intent.putExtra(GroupsChatVinclesGroupListActivity.OFFSET_STRING, offset);
        } else {
            if (offset + groupListInScreen <= groupList.size()) offset += groupListInScreen;
            intent.putExtra(GroupsChatVinclesGroupListActivity.OFFSET_STRING, offset);
        }

        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void updateView() {
        ViewGroup canvasLayout = (ViewGroup) findViewById(R.id.groupLayout);
        canvasLayout.removeAllViews();
        View resultLayout = getLayoutInflater().inflate(R.layout.activity_task_message_result, canvasLayout);

        /////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\
        //     HACK ALERT: SAME USER MEANS SEND TO ALL  \\
        /////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\
        ImageView image = (ImageView)resultLayout.findViewById(R.id.imgPhoto);
        TextView text = (TextView)resultLayout.findViewById(R.id.texUserName);


        if (taskModel.currentGroup == null) {
            image.setImageResource(R.drawable.icon_todos_with_background);
            text.setText(R.string.task_contactlist_all);
        } else {
            if (!isFinishing())
                Glide.with(this)
                    .load(groupModel.getGroupPhotoUrlFromGroupId(taskModel.currentGroup.getId()))
                    .signature(new StringSignature(taskModel.currentGroup.getId().toString()))
                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                    .into(image);
            text.setText(taskModel.currentGroup.name);
        }
        hideProgressBar();
    }

    public void goBack(View view) {
        Log.i(TAG, "go back to GroupsChatActivity");
        startActivity(new Intent(this, GroupsChatActivity.class));
        finish();
    }
}