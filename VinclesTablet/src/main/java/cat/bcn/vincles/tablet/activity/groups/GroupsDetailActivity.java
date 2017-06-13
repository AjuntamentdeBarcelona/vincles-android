/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.GroupModel;

public class GroupsDetailActivity extends GroupsActivity {
    private final String TAG = this.getClass().getSimpleName();
    private GroupModel groupModel = GroupModel.getInstance();
    private TextView groupTitle, groupDescription;
    private ImageView groupImage;
    private View[] usersView;
    private int pag = 0;
    private List<User> allUsers;
    private int MAX_ITEM = 5;
    private View ll_prev, ll_next;
    private User dynamizer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_detail);

        groupImage = (ImageView) findViewById(R.id.group_photo);
        groupTitle = (TextView) findViewById(R.id.group_title);
        groupDescription = (TextView)findViewById(R.id.info_group_desc);
        ll_next = findViewById(R.id.ll_next);
        ll_prev = findViewById(R.id.ll_prev);

        usersView = new View[MAX_ITEM];
        for (int i = 0; i < MAX_ITEM; i++) {
            int resId = getResources().getIdentifier("groupCircle"+(i+1), "id", getPackageName());
            usersView[i] = findViewById(resId);
        }

        // CAUTION: initialize group action & private chat controls
        taskModel.isGroupAction = true;
        taskModel.isPrivateChat = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (groupModel.currentGroup == null) return;
        groupTitle.setText(groupModel.currentGroup.name);
        if (!isFinishing())
            Glide.with(this)
                .load(groupModel.getGroupPhotoUrlFromGroupId(groupModel.currentGroup.getId()))
                .signature(new StringSignature(groupModel.currentGroup.getId().toString()))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(groupImage);

        dynamizer = groupModel.currentGroup.dynamizer;
        ((TextView)findViewById(R.id.item_message_fullname)).setText(dynamizer.name + " " + dynamizer.lastname);
        if (dynamizer != null) {
            if (dynamizer.idContentPhoto != null) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUserId(dynamizer.getId()))
                        .signature(new StringSignature(dynamizer.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into((ImageView)findViewById(R.id.item_message_photo));
            } else {
                Log.w(TAG, dynamizer.alias + "has  idContentPhoto is null!");
            }

            groupTitle.setText(groupModel.currentGroup.name);
            groupDescription.setText(groupModel.currentGroup.description);
        }




        updateViews();
    }

    private void updateViews() {
        if (groupModel.currentGroup == null) return;
        allUsers = groupModel.getUserListByGroup(groupModel.currentGroup);

        List<User> lst_pag = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_ITEM,allUsers.size()); i++) {
            lst_pag.add(allUsers.get(pag*MAX_ITEM + i));
        }
        refreshUsers(lst_pag);
    }

    private void refreshUsers (List<User> lst_pag) {
        ((TextView)findViewById(R.id.info_group_users)).setText(getString(R.string.task_groups_users, ""+allUsers.size()));
        for (int i = 0; i < MAX_ITEM; i++) {
            if (i < lst_pag.size()) {
                ImageView img = (ImageView)usersView[i].findViewById(R.id.imgPhoto);
                usersView[i].setVisibility(View.VISIBLE);
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUser(lst_pag.get(i)))
                        .signature(new StringSignature(lst_pag.get(i).getId().toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(img);

                ((TextView)usersView[i].findViewById(R.id.texUserName))
                        .setText(lst_pag.get(i).alias);
            } else {
                usersView[i].setVisibility(View.GONE);
            }
        }

        // ENABLE / DISABLE BUTTONS
        if (pag <= 0) ll_prev.setEnabled(false);
        else ll_prev.setEnabled(true);

        // ENABLE / DISABLE BUTTONS
        if ((pag+1)*MAX_ITEM >= allUsers.size()) ll_next.setEnabled(false);
        else ll_next.setEnabled(true);
    }

    public void prevMessage(View view) {
        if (pag <= 0)
            return;
        pag--;

        List<User> lst_pag = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_ITEM,allUsers.size()); i++) {
            lst_pag.add(allUsers.get(pag*MAX_ITEM + i));
        }

        refreshUsers(lst_pag);
    }

    public void nextMessage(View view) {
        if (pag >= allUsers.size() / MAX_ITEM || (allUsers.size() <= (pag+1)*MAX_ITEM))
            return;
        pag++;

        List<User> lst_pag = new ArrayList<>();
        for (int i = 0; i < Math.min(MAX_ITEM,allUsers.size()) && (pag*MAX_ITEM + i < allUsers.size()); i++) {
            lst_pag.add(allUsers.get(pag*MAX_ITEM + i));
        }
        refreshUsers(lst_pag);
    }

    public void goClose (View v) {
        finish();
    }
}
