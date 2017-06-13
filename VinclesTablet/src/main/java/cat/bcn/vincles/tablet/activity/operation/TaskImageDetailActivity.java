/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.app.Dialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.groups.GroupsChatVinclesGroupListActivity;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.ResourceModel;
import cat.bcn.vincles.tablet.model.TaskModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TaskImageDetailActivity extends TaskActivity {
    private final String TAG = this.getClass().getSimpleName();
    public final static String PARAM_SHARED_IMAGE = "PARAM_SHARED_IMAGE";
    private ImageView imgPhoto,imgUser,imgPlay;;
    private TextView textList,texDate;
    private TextView texUserName,button_remove;
    private VideoView video;
    private int currImage = 0;
    private LinearLayout ll_next, ll_prev, linShareAndDelete, remove, share;
    private ResourceModel resourceModel = ResourceModel.getInstance();
    private String currentFilename;
    private String currentImagePath;
    private ProgressBar prog_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_image);

        Bundle extras = getIntent().getExtras();
        if (extras != null) currImage = extras.getInt(TaskImageDetailActivity.PARAM_SHARED_IMAGE, 0);

        setViews();
        updateViews();
        addListeners();
    }

    private void updateViews() {
        getLocalResources();

        if (!MainModel.avoidServerCalls) {
            showProgressBar(false, getString(R.string.general_download));
            resourceModel.getResourceList(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    resourceModel.getServerResourceList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            hideProgressBar();
                            // Get resources again!!!
                            getLocalResources();
                        }

                        @Override
                        public void onFailure(Object error) {
                            hideProgressBar();
                            // Get resources again!!!
                            getLocalResources();
                        }
                    }, "", "");
                }

                @Override
                public void onFailure(Object error) {
                    Log.e(TAG, "getServerResourceList() - error: " + error);
                    hideProgressBar();

                    String errorMessage = mainModel.getErrorByCode(error);
                    Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
        }
    }

    // Synchronize with local data
    private void getLocalResources() {
        resourceModel.resourceList = resourceModel.getLocalResourceList();
        showImage(currImage);
    }

    private void setViews() {
        imgPhoto        = (ImageView) findViewById(R.id.imgPhoto);
        textList        = (TextView) findViewById(R.id.textList);
        ll_prev         = (LinearLayout) findViewById(R.id.ll_prev);
        ll_next         = (LinearLayout) findViewById(R.id.ll_next);
        texUserName     = (TextView) findViewById(R.id.texUserName);
        imgUser         = (ImageView) findViewById(R.id.imgUser);
        video           = (VideoView) findViewById(R.id.video);
        imgPlay         = (ImageView) findViewById(R.id.imgPlay);
        button_remove   = (TextView) findViewById(R.id.button_remove);
        texDate         = (TextView) findViewById(R.id.texDate);
        prog_bar        = (ProgressBar) findViewById(R.id.prog_bar);
        linShareAndDelete = (LinearLayout) findViewById(R.id.linShareAndDelete);
        remove = (LinearLayout) findViewById(R.id.remove);
        share = (LinearLayout) findViewById(R.id.share);
    }

    private void addListeners() {
        imgPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (video.canSeekForward()) {
                    // Force video to show first still
                    video.seekTo(10);
                    video.setBackground(null);

                }
            }
        });

        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgPlay.setVisibility(View.VISIBLE);
            }
        });

        video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (video.isPlaying()) {
                    video.pause();
                    imgPlay.setVisibility(View.VISIBLE);
                } else {
                    video.start();
                    imgPlay.setVisibility(View.GONE);
                }
                return false;
            }
        });

    }

    private void playVideo() {
        if (video.isPlaying()) {
            video.pause();
            imgPlay.setVisibility(View.VISIBLE);
        } else {
            video.requestFocus();
            video.start();
            imgPlay.setVisibility(View.GONE);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void shareImage(View view) {
        taskModel.view = TaskModel.TASK_SHARE_IMAGE;

        if (!taskModel.isGroupAction) {
            // Create message with Current Resource to send
            taskModel.setCurrentMessage(new Message());
            taskModel.getCurrentMessage().userFrom = mainModel.currentUser;

            // Add only one resource per share action
            Resource resource = resourceModel.resourceList.get(currImage);
            taskModel.getCurrentMessage().metadataTipus = resource.type;
            taskModel.getCurrentMessage().resourceTempList = new ArrayList<Resource>();
            taskModel.getCurrentMessage().resourceTempList.add(resource);

            Intent i = new Intent(this, TaskImageListActivity.class);
            i.putExtra(TaskImageDetailActivity.PARAM_SHARED_IMAGE, currImage);
            startActivity(i);
        } else {

            // Create message with Current Resource to send
            taskModel.setCurrentChat(new Chat());
            taskModel.getCurrentChat().userFrom = mainModel.currentUser;

            // Add only one resource per share action
            Resource resource = resourceModel.resourceList.get(currImage);
            taskModel.getCurrentChat().metadataTipus = resource.type;
            taskModel.getCurrentChat().resourceTempList = new ArrayList<Resource>();
            taskModel.getCurrentChat().resourceTempList.add(resource);

            startActivity(new Intent(this, GroupsChatVinclesGroupListActivity.class));
        }
    }

    public void deleteImage(View view) {
        if (resourceModel.resourceList.size() <= 0)
            return;
        final Dialog dialog = new Dialog(this,R.style.DialogCustomTheme);
        dialog.setContentView(R.layout.dialog_custom);

        TextView txt_title = (TextView)dialog.findViewById(R.id.txt_title);
        String filename = resourceModel.resourceList.get(currImage).filename;
        if (filename.startsWith(VinclesConstants.VIDEO_PREFIX))
            txt_title.setText(getString(R.string.task_gallery_button_remove_video));
        else
            txt_title.setText(getString(R.string.task_gallery_button_remove_image));
        LinearLayout btn_yes = (LinearLayout)dialog.findViewById(R.id.btn_yes);
        LinearLayout btn_no = (LinearLayout)dialog.findViewById(R.id.btn_no);

        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resourceModel.deleteResourceFromLibrary(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // Only delete from disk images not associated with messages
                        if (taskModel.currentResource.type.equals(VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE)
                                && (taskModel.currentResource.message == null && taskModel.currentResource.chat == null)) {
                            VinclesConstants.deleteImage(taskModel.currentResource.filename);
                        }
                        resourceModel.deleteResource(taskModel.currentResource);
                        resourceModel.resourceList = resourceModel.getLocalResourceList();
                        currImage = 0;
                        showImage(currImage);
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, taskModel.currentResource.getId());
            }
        });

        dialog.show();
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

    private void sendImage() {
        // Compound current resource and send to server & update resource reference!!!
        final Resource currentResource = new Resource();
        currentResource.type = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
        currentResource.filename = currentFilename;

        // Update view
        resourceModel.resourceList = resourceModel.getLocalResourceList();
        resourceModel.resourceList.add(0, currentResource); // Add in memory
        currImage = 0;
        showImage(currImage);

        File imageFile = new File(currentImagePath);
        RequestBody file = RequestBody.create(MediaType.parse("image/jpeg"), imageFile);
        currentResource.data = MultipartBody.Part.createFormData("file", currentResource.filename, file);

        // Inhabilitate buttons
        linShareAndDelete.setEnabled(false);
        remove.setEnabled(false);
        share.setEnabled(false);
        linShareAndDelete.setAlpha(0.4f);

        taskModel.sendResource(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                final Long resourceId = Long.parseLong((String)result);

                resourceModel.addResourceToLibrary(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        // Update resource id and save
                        currentResource.setId(resourceId);
                        taskModel.saveResource(currentResource);

                        // Restore buttons
                        linShareAndDelete.setEnabled(true);
                        remove.setEnabled(true);
                        share.setEnabled(true);
                        linShareAndDelete.setAlpha(1.0f);
                    }

                    @Override
                    public void onFailure(Object error) {
                        String errorMessage = mainModel.getErrorByCode(error);
                        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }, resourceId);
            }

            @Override
            public void onFailure(Object error) {
                String errorMessage = mainModel.getErrorByCode(error);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, currentResource.data);
    }

    public void nextImage(View view) {
        if (currImage < resourceModel.resourceList.size()-1) {
            currImage++;
            showImage(currImage);
        }
    }

    public void previousImage(View view) {
        if (currImage > 0) {
            currImage--;
            showImage(currImage);
        }
    }

    private void showImage(int index) {
        video.setVisibility(View.GONE);
        imgPhoto.setVisibility(View.GONE);
        imgPlay.setVisibility(View.GONE);

        if (resourceModel.resourceList.size() > 1) {
            if (index == 0) {
                ll_prev.setEnabled(false);
                ll_next.setEnabled(true);
            } else if (index == resourceModel.resourceList.size() - 1) {
                ll_prev.setEnabled(true);
                ll_next.setEnabled(false);
            } else {
                ll_prev.setEnabled(true);
                ll_next.setEnabled(true);
            }
        } else {
            ll_prev.setEnabled(false);
            ll_next.setEnabled(false);
        }

        if (resourceModel.resourceList == null || resourceModel.resourceList.size() <= 0) {
            imgPhoto.setImageBitmap(null);
            textList.setText("");
            texUserName.setText("");
            texDate.setText("");
            video.setVisibility(View.GONE);
            imgPhoto.setVisibility(View.GONE);
            imgPlay.setVisibility(View.GONE);
            imgUser.setVisibility(View.GONE);
            findViewById(R.id.remove).setVisibility(View.GONE);
            findViewById(R.id.share).setVisibility(View.GONE);
            findViewById(R.id.texError).setVisibility(View.VISIBLE);
            return;
        }
        findViewById(R.id.remove).setVisibility(View.VISIBLE);
        findViewById(R.id.share).setVisibility(View.VISIBLE);
        findViewById(R.id.texError).setVisibility(View.GONE);
        Resource currentResource = resourceModel.resourceList.get(index);
        String filename = currentResource.filename;
        if (filename != null) {
            if (filename.startsWith(VinclesConstants.IMAGE_PREFIX)) {
                imgPhoto.setVisibility(View.VISIBLE);
                if (!isFinishing())
                    Glide.with(this)
                        .load(VinclesConstants.getImageDirectory() + "/" + filename)
                        .into(imgPhoto);// Load low resolution image
                button_remove.setText(getString(R.string.task_gallery_button_remove_image));
            } else if (filename.startsWith(VinclesConstants.VIDEO_PREFIX)) {
                String path = VinclesConstants.getVideoPath() + filename;
                button_remove.setText(getString(R.string.task_gallery_button_remove_video));
                File file = new File(path);
                if (file.exists()) {
                    video.setVisibility(View.VISIBLE);
                    imgPlay.setVisibility(View.VISIBLE);
                    video.setBackground(null);
                    video.setVideoPath(path);
                    video.requestFocus();
                    video.start();
                    video.pause();
                } else {
                    Log.e(TAG, "NO EXISTE:" + filename);
                }
            }
        } else {
            if (currentResource.message != null) {
                if (currentResource.message.metadataTipus != null) {
                    switch (currentResource.message.metadataTipus) {
                        case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                            imgPhoto.setVisibility(View.VISIBLE);
                            loadImage(currentResource, imgPhoto);
                            break;
                        case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                            imgPhoto.setVisibility(View.VISIBLE);
                            loadImage(currentResource, imgPhoto);
                            break;
                        case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                            video.setVisibility(View.VISIBLE);
                            imgPlay.setVisibility(View.VISIBLE);
                            video.setBackground(null);
                            loadVideo(currentResource);
                            break;
                    }
                } else {
                    imgPhoto.setVisibility(View.VISIBLE);
                    loadImage(currentResource, imgPhoto);
                }
            } else if (currentResource.chat != null) {
                if (currentResource.chat.metadataTipus != null) {
                    switch (currentResource.chat.metadataTipus) {
                        case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                            imgPhoto.setVisibility(View.VISIBLE);
                            loadImage(currentResource, imgPhoto);
                            break;
                        case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                            imgPhoto.setVisibility(View.VISIBLE);
                            loadImage(currentResource, imgPhoto);
                            break;
                        case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                            video.setVisibility(View.VISIBLE);
                            imgPlay.setVisibility(View.VISIBLE);
                            video.setBackground(null);
                            loadVideo(currentResource);
                            break;
                    }
                } else {
                    imgPhoto.setVisibility(View.VISIBLE);
                    loadImage(currentResource, imgPhoto);
                }
            }
        }
        textList.setText("" + (currImage + 1) + " " + getString(R.string.task_gallery_of) + " " + resourceModel.resourceList.size());
        try {
            texDate.setText(VinclesConstants.getDateString(currentResource.inclusionTime, getResources().getString(R.string.dateLargeformat), new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country))));
        } catch (Exception e) {
            e.printStackTrace();
            texDate.setText("");
        }
        User userFrom = mainModel.currentUser;
        Message message = currentResource.message;
        if (message != null) {
            userFrom = mainModel.getUser(message.idUserFrom);
        } else {
            Chat chat = currentResource.chat;
            if (chat != null) {
                userFrom = mainModel.getUser(chat.idUserFrom);
            }
        }
        if (userFrom != null) {
            imgUser.setVisibility(View.VISIBLE);
            texUserName.setText(userFrom.alias);
            if (userFrom.idContentPhoto != null) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUser(userFrom))
                        .signature(new StringSignature(userFrom.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgUser);
            } else {
                Log.w(TAG, userFrom.alias + "has  idContentPhoto is null!");
                imgUser.setImageResource(R.drawable.user);
            }
        }

        taskModel.currentResource = currentResource;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

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

    private void loadImage(final Resource item, final ImageView image) {
        prog_bar.setVisibility(View.VISIBLE);
        taskModel.getServerResourceData(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                byte[] data = (byte[]) result;

                // Update resource, si ya existe solo guardamos el archivo es disco
                item.filename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
                item.type = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
                taskModel.saveResource(item);

                // Save locally image
                VinclesConstants.saveImage(data, item.filename);
                prog_bar.setVisibility(View.GONE);

                // Load image once has been saved
                if (!isFinishing())
                    Glide.with(getApplicationContext())
                        .load(VinclesConstants.getImageDirectory() + "/" + item.filename)
                        .into(image);
            }

            @Override
            public void onFailure(Object error) {
                String errorMessage = mainModel.getErrorByCode(error);
                prog_bar.setVisibility(View.GONE);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, item.getId());
    }

    private void loadVideo(final Resource item) {
        prog_bar.setVisibility(View.VISIBLE);
        taskModel.getServerResourceData(new AsyncResponse() {
            @Override
            public void onSuccess(Object result) {
                // Update resource
                item.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
                item.type = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;
                taskModel.saveResource(item);

                VinclesConstants.saveVideo((byte[])result, item.filename);
                prog_bar.setVisibility(View.GONE);

                prepareVideo(item);
            }

            @Override
            public void onFailure(Object error) {
                String errorMessage = mainModel.getErrorByCode(error);
                prog_bar.setVisibility(View.GONE);
                Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                toast.show();
            }
        }, item.getId());
    }

    private void prepareVideo(Resource item) {
        String path = VinclesConstants.getVideoPath() + item.filename;
        File file = new File(path);
        if (file.exists()) {
            imgPlay.setVisibility(View.VISIBLE);
            imgPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playVideo();
                }
            });
            video.setVisibility(View.VISIBLE);
            video.setBackground(null);
            video.setVideoPath(path);
            video.requestFocus();
            video.start();
            video.pause();
        }
    }
}