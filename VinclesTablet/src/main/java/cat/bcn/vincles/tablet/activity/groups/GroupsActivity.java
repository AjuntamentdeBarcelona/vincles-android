/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.groups;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.VinclesActivity;
import cat.bcn.vincles.tablet.activity.configuration.ConfigMainActivity;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageSendActivity;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.TaskModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class GroupsActivity extends VinclesActivity {
    private final String TAG = this.getClass().getSimpleName();
    protected TaskModel taskModel;
    private GroupModel groupModel;
    private ProgressDialog progressBar;

    protected String currentFilename;
    protected String currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskModel = TaskModel.getInstance();    // INITIALIZATION INSIDE
        groupModel = GroupModel.getInstance();

        // CAUTION: initialize group action & private chat controls
        taskModel.isGroupAction = true;
        taskModel.isPrivateChat = false;
    }

    public void goUser(View view) {
        // Go to User
    }

    public void showProgressBar(boolean cancelable, String text) {
        progressBar = new ProgressDialog(this/*,R.style.DialogCustomTheme*/);
        progressBar.setCancelable(cancelable);
        progressBar.setMessage(text);
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressBar.setInverseBackgroundForced(true);
        progressBar.show();
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            try {
                progressBar.dismiss();
            } catch (Exception e) {
            } finally {
                progressBar = null;
            }
        }
    }

    public void createChat(View view) {
        if (groupModel.currentGroup == null) return;
        Log.i(TAG, "createChat()");
        taskModel.currentGroup = groupModel.currentGroup;
        taskModel.setCurrentChat(new Chat());
        taskModel.getCurrentChat().idChat = taskModel.currentGroup.idChat;
        startActivity(new Intent(this, TaskMessageSendActivity.class));
    }

    public void goGroups(View view) {
        startActivity(new Intent(this, GroupsListActivity.class));
    }

    public void goDinamizerChat(View view) {
        Intent intent = new Intent(this, GroupsDinamizerChatActivity.class);
        startActivity(intent);
    }

    public void goGroupChat(View view) {
        Intent intent = new Intent(this, GroupsChatActivity.class);
        startActivity(intent);
    }

    public void goGroupDetails(View view) {
        Intent intent = new Intent(this, GroupsDetailActivity.class);
        startActivity(intent);
    }

    public void takePhoto(View view) {
        // Indicate file uri to save
        currentFilename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
        File currentImageFile = new File(VinclesConstants.getImagePath(), currentFilename);
        currentImagePath = currentImageFile.getAbsolutePath();
        Uri currentImageUri = Uri.fromFile(currentImageFile);

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
        startActivityForResult(intent, VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // CAUTION: restore current language (camera override it with device language default)
        mainModel.updateLocale(mainModel.language, mainModel.country);

        if (requestCode == VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Send high resolution image
                sendImage();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise userVincles
            }
        }
    }

    private void sendImage() {
        // Create chat with Current Resource to send
        taskModel.setCurrentChat(new Chat());
        taskModel.currentGroup = groupModel.currentGroup;
        taskModel.getCurrentChat().idUserFrom = mainModel.currentUser.getId();
        taskModel.getCurrentChat().idUserTo = taskModel.currentUser.getId();
        taskModel.getCurrentChat().resourceTempList = new ArrayList<Resource>();
        Resource resource = new Resource();
        resource.chat = taskModel.getCurrentChat();
        taskModel.getCurrentChat().metadataTipus = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
        File imageFile = new File(currentImagePath);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        resource.filename = currentFilename;

        resource.data = MultipartBody.Part.createFormData("file", resource.filename, file);

        taskModel.getCurrentChat().resourceTempList.add(resource);
        taskModel.getCurrentChat().sendTime = new Date();

        List<Long> idChatList = new ArrayList<Long>();

        if (groupModel.currentGroup != null) {
            if (!taskModel.isPrivateChat) {
                taskModel.getCurrentChat().idChat = groupModel.currentGroup.idChat;
                idChatList.add(taskModel.currentGroup.idChat);
            } else {
                taskModel.getCurrentChat().idChat = groupModel.currentGroup.idDynamizerChat;
                idChatList.add(taskModel.currentGroup.idDynamizerChat);
            }
        }

        final ProgressDialog loading = new ProgressDialog(this);
        loading.setCancelable(false);
        loading.setMessage(getString(R.string.general_download));
        loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        loading.setInverseBackgroundForced(true);
        loading.show();

        groupModel.sendChatToAll(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                loading.dismiss();
                getLocalData();
            }

            @Override
            public void onFailure(Object error) {
                Log.i(TAG, "sendImage().sendChatToAll() - error: " + error);
                loading.dismiss();
                String errorMessage = mainModel.getErrorByCode(error);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, taskModel.getCurrentChat(), idChatList);
    }

    public void getLocalData() {
        // Override
    }
}
