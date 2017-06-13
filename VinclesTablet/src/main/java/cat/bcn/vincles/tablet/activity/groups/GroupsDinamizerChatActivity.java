/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.ArrayList;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageSendActivity;
import cat.bcn.vincles.tablet.adapter.ChatAdapter;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;

public class GroupsDinamizerChatActivity extends GroupsChatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private User dynamizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        layout = R.layout.activity_groups_chat_dinamizer;
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        if (groupModel.currentGroup != null)
            dynamizer = groupModel.currentGroup.dynamizer;
        super.onResume();

        // CAUTION: initialize group action & private chat controls
        taskModel.isGroupAction = true;
        taskModel.isPrivateChat = true;
    }

    @Override
    protected void customizeActivity() {
        if (groupModel.currentGroup == null) return;
        groupTitle.setText(getString(R.string.task_groups_dinamizer_title, dynamizer.name + " " + dynamizer.lastname, groupModel.currentGroup.name));

        if (dynamizer != null) {
            if (dynamizer.idContentPhoto != null) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUserId(dynamizer.getId()))
                        .signature(new StringSignature(dynamizer.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(groupImage);
            } else {
                Log.w(TAG, dynamizer.alias + "has  idContentPhoto is null!");
            }
        }
    }

    @Override
    public void updateViews() {
        if (groupModel.currentGroup == null) return;
        chatList = groupModel.getDynamizerChatList();
        adapter = new ChatAdapter(GroupsDinamizerChatActivity.this, 0, chatList, listHeight, MAX_ITEM);

        if (!MainModel.avoidServerCalls) {
            showProgressBar(false, getString(R.string.general_download));
            groupModel.getChatServerList(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    hideProgressBar();

                    // Local data is refreshed
                    getLocalData();
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(TAG, "getPrivateMessageServerList() - error: " + error);
                    hideProgressBar();
                    Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, groupModel.currentGroup.idDynamizerChat, "", "");
        }

        refreshButtons();
    }

    @Override
    public void getLocalData() {
        adapter.clear();
        chatList = groupModel.getDynamizerChatList();
        if (chatList.size() > 0) {
            adapter.addAll(chatList);
        } else {
            adapter = new ChatAdapter(GroupsDinamizerChatActivity.this, 0, chatList, listHeight, MAX_ITEM);
        }
        refreshButtons();
    }

    @Override
    public void createChat(View view) {
        if (groupModel.currentGroup == null) return;
        Log.i(TAG, "createChat() private");
        taskModel.currentGroup = groupModel.currentGroup;
        taskModel.setCurrentChat(new Chat());
        taskModel.getCurrentChat().idChat = taskModel.currentGroup.idDynamizerChat;
        startActivity(new Intent(this, TaskMessageSendActivity.class));
    }

    @Override
    public void takePhoto(View view) {
        super.takePhoto(view);
    }
}