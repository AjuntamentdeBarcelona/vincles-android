/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.ArrayList;
import java.util.List;
import cat.bcn.vincles.lib.dao.ChatDAOImpl;
import cat.bcn.vincles.lib.push.CommonVinclesGcmHelper;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.adapter.ChatAdapter;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.ResourceModel;
import cat.bcn.vincles.tablet.push.AppFCMDefaultListenerImpl;

public class GroupsChatActivity extends GroupsActivity {
    private final String TAG = this.getClass().getSimpleName();
    protected GroupModel groupModel = GroupModel.getInstance();
    private ListView lisMessage;
    protected ChatAdapter adapter;
    protected List<Chat> chatList;
    protected TextView groupTitle;
    protected ImageView groupImage;
    protected int listHeight;

    public static final int MAX_ITEM = 4;

    protected int layout = R.layout.activity_groups_chat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        taskModel.resultMessage = false;
        lisMessage = (ListView) findViewById(R.id.lisMessage);
        adapter = new ChatAdapter(getApplicationContext(), 0, new ArrayList<Chat>(), 0, MAX_ITEM);
        groupImage = (ImageView) findViewById(R.id.group_photo);
        groupTitle = (TextView) findViewById(R.id.group_title);

        // CAUTION: initialize group action & private chat controls
        taskModel.isGroupAction = true;
        taskModel.isPrivateChat = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.messagesLayout).post(new Runnable() {
            @Override
            public void run() {
                listHeight = findViewById(R.id.messagesLayout).getMeasuredHeight()/MAX_ITEM;
                updateViews();
                addListeners();
            }
        });

        customizeActivity();

        // RECEIVE PUSH NOTIFICATIONS
        CommonVinclesGcmHelper.setPushListener(new AppFCMDefaultListenerImpl(this) {
            @Override
            public void onPushMessageReceived(PushMessage pushMessage) {
                // Add to chat
                if (pushMessage.getType().equals(PushMessage.TYPE_NEW_CHAT)) {
                    ResourceModel.getInstance().loadChatGroupResource(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            getLocalData();
                        }

                        @Override
                        public void onFailure(Object error) {
                            MainModel mainModel = MainModel.getInstance();
                            String errorMessage = mainModel.getErrorByCode(error);
                            Toast toast = Toast.makeText(mainModel.context, errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }, new ChatDAOImpl().get(pushMessage.getIdData()));
                }
                else super.onPushMessageReceived(pushMessage);
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

    // OVERRIDED IN GroupsDinamizerChatActivity TO MANAGE DINAMIZER CHAT ALSO ( HINT: USE CHAT_ID )
    protected void customizeActivity() {
        if (groupModel.currentGroup != null) {
            groupTitle.setText(groupModel.currentGroup.name);
            if (!isFinishing())
                Glide.with(this)
                    .load(groupModel.getGroupPhotoUrlFromGroupId(groupModel.currentGroup.getId()))
                    .signature(new StringSignature(groupModel.currentGroup.getId().toString()))
                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                    .into(groupImage);
        }
    }

    @Override
    public void getLocalData() {
        if (groupModel.currentGroup == null) return;
        adapter.clear();
        chatList = groupModel.getChatList(groupModel.currentGroup.idChat, false);
        if (chatList.size() > 0) {
            adapter.addAll(chatList);
        } else {
            adapter = new ChatAdapter(GroupsChatActivity.this, 0, chatList, listHeight, MAX_ITEM);
            addListeners();

        }
        refreshButtons();
    }

    // HINT: USE CHAT_ID (vinclesGroup.idChat or vinclesGroup.idDynamizerChat)
    protected void updateViews() {
        if (groupModel.currentGroup == null) return;
        chatList = groupModel.getChatList(groupModel.currentGroup.idChat, true);
        adapter = new ChatAdapter(GroupsChatActivity.this, 0, chatList, listHeight, MAX_ITEM);

        if (!MainModel.avoidServerCalls) {
            showProgressBar(false, getString(R.string.general_download));
            groupModel.getGroupUserServerList(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    Log.i(TAG, "getUserServerList() - result");
                    groupModel.getChatServerList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            Log.i(TAG, "getMessageServerList() - result");
                            hideProgressBar();

                            // Local data is refreshed
                            getLocalData();
                        }

                        @Override
                        public void onFailure(Object error) {
                            Log.e(TAG, "getMessageServerList() - error: " + error);
                            hideProgressBar();
                            Toast toast = Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_messsage_load_list), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }, groupModel.currentGroup.idChat, "", "");
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(TAG, "getGroupUserServerList() - error: " + error);
                    hideProgressBar();
                    String errorMessage = mainModel.getErrorByCode(error);
                    Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, groupModel.currentGroup.getId());
        }

        refreshButtons();
    }

    protected void addListeners() {
        // ListView Item Click Listener
        lisMessage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // ListView Clicked item value
                taskModel.setCurrentChat((Chat) lisMessage.getItemAtPosition(adapter.getRealMessagePosition(position)));

                // Go to detail
                Intent intent = new Intent(GroupsChatActivity.this, GroupsChatDetailActivity.class);
                startActivity(intent);
            }
        });

        // Assign adapter to ListView
        lisMessage.setAdapter(adapter);
    }

    public void prevMessage(View view) {
        adapter.showLess();
        refreshButtons();
    }

    public void nextMessage(View view) {
        adapter.showMore();
        refreshButtons();
    }

    public void refreshButtons() {
        if (adapter.isMore())
            findViewById(R.id.ll_next).setEnabled(true);
        else
            findViewById(R.id.ll_next).setEnabled(false);

        if (adapter.isLess())
            findViewById(R.id.ll_prev).setEnabled(true);
        else
            findViewById(R.id.ll_prev).setEnabled(false);
    }
}