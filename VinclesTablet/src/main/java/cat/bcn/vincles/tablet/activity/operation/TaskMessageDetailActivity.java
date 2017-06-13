/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Communication;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.GroupModel;

public class TaskMessageDetailActivity extends TaskActivity  {
    private final String TAG = this.getClass().getSimpleName();
    private VideoView video;
    private ImageView imgPlay;
    private ImageView imgTypeImage;
    private Bitmap bitmap = null;
    private MediaPlayer mediaPlayer;

    private ImageView imgUser;
    private TextView texUserName;
    private TextView texMessageDay;
    private TextView texMessageTime;

    private TextView texTypeText;
    private LinearLayout layout_text;
    private ImageView img1, img2;

    private LinearLayout layout_texto_up_down, linReplay, layout_images;
    private ProgressBar prog_bar, prog_bar_img1, prog_bar_img2;
    private FrameLayout frame_img1, frame_img2;

    private int pag_images = 0;
    private final int MAX_ITEM = 2;

    protected int layout = R.layout.activity_task_message_detail;
    protected boolean isReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (taskModel.getComunnication() != null) {
            setContentView(layout);

            mediaPlayer = new MediaPlayer();
            isReady = false;

            setViews();
            updateViews();
            addListeners();

            // Mark message as 'watched' (Only NOT chat Message)
            if (taskModel.getCurrentMessage() != null) {
                if (taskModel.getCurrentMessage().idChat == null) {
                    if(taskModel.getCurrentMessage().watched != true) {
                        taskModel.markMessageAsWatched(taskModel.getCurrentMessage());
                    }
                }
            } else {
                if (taskModel.getCurrentChat().idChat == null) {
                    if(taskModel.getCurrentChat().watched != true) {
                        taskModel.markMessageAsWatched(taskModel.getCurrentChat());
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    private void setViews() {
        imgUser             = (ImageView) findViewById(R.id.imgUser);
        texUserName         = (TextView) findViewById(R.id.texUserName);
        texMessageDay       = (TextView) findViewById(R.id.texMessageDay);
        texMessageTime      = (TextView) findViewById(R.id.texMessageTime);
        imgPlay             = (ImageView) findViewById(R.id.imgPlay);
        video               = (VideoView) findViewById(R.id.video);
        texTypeText         = (TextView) findViewById(R.id.texTypeText);
        imgTypeImage        = (ImageView) findViewById(R.id.imgTypeImage);

        layout_text         = (LinearLayout) findViewById(R.id.layout_text);
        img1                = (ImageView) findViewById(R.id.img1);
        img2                = (ImageView) findViewById(R.id.img2);

        layout_texto_up_down = (LinearLayout) findViewById(R.id.layout_texto_up_down);
        layout_images       = (LinearLayout) findViewById(R.id.layout_images);
        prog_bar            = (ProgressBar) findViewById(R.id.prog_bar);
        prog_bar_img1       = (ProgressBar) findViewById(R.id.prog_bar_img1);
        prog_bar_img2       = (ProgressBar) findViewById(R.id.prog_bar_img2);
        frame_img1          = (FrameLayout) findViewById(R.id.frame_img1);
        frame_img2          = (FrameLayout) findViewById(R.id.frame_img2);
        linReplay           = (LinearLayout) findViewById(R.id.linReplay);
    }

    private void addListeners () {
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (video.canSeekForward()) {
                    // Force video to show first still
                    video.seekTo(0);
                    video.setBackground(null);
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            video.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                @Override
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    return false;
                }
            });
        }

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

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isReady = true;
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgPlay.setVisibility(View.VISIBLE);
            }
        });

    }

    private void updateViews() {
        if (taskModel.getComunnication().userFrom == null) {
            // Try to get user from local database
            taskModel.getComunnication().userFrom = mainModel.getUser(taskModel.getComunnication().idUserFrom);
        }

        if (taskModel.getComunnication().userFrom != null) {
            if (taskModel.isGroupAction)
                texUserName.setText(taskModel.getComunnication().userFrom.alias);
            else
                texUserName.setText(taskModel.getComunnication().userFrom.name + " " + taskModel.getComunnication().userFrom.lastname);
            if (taskModel.getComunnication().userFrom.idContentPhoto != null) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(mainModel.getUserPhotoUrlFromUserId(taskModel.getComunnication().userFrom.getId()))
                        .signature(new StringSignature(taskModel.getComunnication().userFrom.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgUser);
            } else {
                Log.w(TAG, taskModel.getComunnication().userFrom.alias + " has idContentPhoto null!");
            }
        }

        texMessageTime.setText(VinclesConstants.getDateString(taskModel.getComunnication().sendTime, getResources().getString(R.string.timeformat), new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country))));

        Date now = new Date(System.currentTimeMillis());
        if (now.getDate() == taskModel.getComunnication().sendTime.getDate()) {
            texMessageDay.setText(getString(R.string.task_message_today));
        } else {
            texMessageDay.setText(VinclesConstants.getDateString(taskModel.getComunnication().sendTime, getResources().getString(R.string.dateLargeformat), new Locale(getResources().getString(R.string.locale_language), getResources().getString(R.string.locale_country))));
        }

        layout_text.setVisibility(View.GONE);
        if (layout_texto_up_down != null) layout_texto_up_down.setVisibility(View.GONE);
        video.setVisibility(View.GONE);
        prog_bar.setVisibility(View.GONE);
        prog_bar_img1.setVisibility(View.GONE);
        prog_bar_img2.setVisibility(View.GONE);

        boolean isResourceDeleted = false;
        if (taskModel.getComunnication().metadataTipus != null) {
            switch (taskModel.getComunnication().metadataTipus) {
                case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                    layout_text.setVisibility(View.VISIBLE);
                    texTypeText.setText(taskModel.getComunnication().text);
                    List<Resource> lst_res = taskModel.getComunnication().getResources();
                    if (lst_res != null && lst_res.size() > 0) {
                        texTypeText.setGravity(Gravity.LEFT | Gravity.TOP);
                        if (lst_res.size() > MAX_ITEM)
                            if (layout_texto_up_down != null)
                                layout_texto_up_down.setVisibility(View.VISIBLE);
                    } else {
                        //isResourceDeleted = true;
                        texTypeText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    }
                    show_images(lst_res, taskModel.getComunnication());
                    break;
                case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                    imgTypeImage.setVisibility(View.VISIBLE);
                    if (taskModel.getComunnication().getResources().size() > 0) {
                        Resource it = taskModel.getComunnication().getResources().get(0);
                        loadImage(imgTypeImage, it, taskModel.getComunnication(), prog_bar);
                    } else {
                        isResourceDeleted = true;
                    }
                    break;
                case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                    imgPlay.setVisibility(View.GONE);
                    imgPlay.setColorFilter(Color.BLACK);
                    if (taskModel.getComunnication().getResources().size() > 0) {
                        Resource res = taskModel.getComunnication().getResources().get(0);
                        loadAudio(res, taskModel.getComunnication());
                    } else {
                        isResourceDeleted = true;
                    }
                    break;
                case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                    imgPlay.setVisibility(View.GONE);
                    imgPlay.setColorFilter(null);
                    video.setVisibility(View.GONE);
                    video.setBackground(null);
                    if (taskModel.getComunnication().getResources().size() > 0) {
                        Resource res = taskModel.getComunnication().getResources().get(0);
                        loadVideo(res, taskModel.getComunnication());
                    } else {
                        isResourceDeleted = true;
                    }
                    break;
            }
        } else {
            layout_text.setVisibility(View.VISIBLE);
            texTypeText.setText(taskModel.getComunnication().text);
            List<Resource> lst_res = taskModel.getComunnication().getResources();
            if (lst_res != null && lst_res.size() > 0) {
                texTypeText.setGravity(Gravity.LEFT | Gravity.TOP);
                if (lst_res.size() > MAX_ITEM)
                    if (layout_texto_up_down != null)
                        layout_texto_up_down.setVisibility(View.VISIBLE);
            } else {
                texTypeText.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            }
            show_images(lst_res, taskModel.getComunnication());
        }

        // Check if Message contained a deleted Resource
        if (isResourceDeleted) {
            dialog = new Dialog(this, R.style.DialogCustomTheme);
            dialog.setContentView(R.layout.alert_dialog_template);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            TextView alertText = (TextView) dialog.findViewById(R.id.item_message_title);
            if(taskModel.isGroupAction) {
                alertText.setText(getResources().getString(R.string.task_group_resource_deleted));
            } else {
                alertText.setText(getResources().getString(R.string.task_message_resource_deleted));
            }

            View close_btn2 = dialog.findViewById(R.id.btnClose);
            close_btn2.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    dialog.dismiss();
                    dialog = null;
                }
            });
        }

        // Check if user is inactive for enable/disable reply button
        if (linReplay != null) {
            if (taskModel.getComunnication().userFrom != null
                    && taskModel.getComunnication().userFrom.active == true) {
                linReplay.setEnabled(true);
                linReplay.setAlpha(1.0f);
            } else {
                linReplay.setEnabled(false);
                linReplay.setAlpha(0.4f);
            }
        }
    }

    private Dialog dialog;

    private void loadImage(final ImageView image, final Resource item, final Communication communication, final ProgressBar prog) {
        image.setImageResource(android.R.color.transparent);
        boolean existe = false;
        if (item.filename != null) {
            File file = new File(VinclesConstants.getImagePath() + "/" +item.filename);
            existe = file.exists();
        }
        if (item.filename != ""  && item.filename != null && existe) {
            if (!isFinishing())
                Glide.with(this)
                    .load(VinclesConstants.getImageDirectory() + "/" + item.filename)
                    .into(image);
        } else {
            prog.setVisibility(View.VISIBLE);
            if (taskModel.isGroupAction) {
                taskModel.getServerChatResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        onGetImageResourceDataSuccess(item, (byte[]) result, (Chat)communication, image, prog);
                    }

                    @Override
                    public void onFailure(Object error) {
                        onGetImageResourceDataFail(error, prog);
                    }
                }, GroupModel.getInstance().currentGroup.idChat, communication.getId());
            } else
                taskModel.getServerResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        onGetImageResourceDataSuccess(item, (byte[])result, (Message)communication, image, prog);
                    }

                    @Override
                    public void onFailure(Object error) {
                        onGetImageResourceDataFail(error, prog);
                    }
                }, item.getId());
        }
    }

    private void onGetImageResourceDataSuccess(Resource item, byte[] result, Communication communication, ImageView image, ProgressBar prog) {
        byte[] data = (byte[]) result;

        // Update resource, si ya existe solo guardamos el archivo es disco
        if (item.filename == null || !item.filename.startsWith(VinclesConstants.IMAGE_PREFIX)) {
            item.filename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
            item.type = VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE;
            if (communication instanceof Chat) {
                item.chat = (Chat)communication;
                taskModel.saveChat((Chat)communication);
            } else {
                item.message = (Message)communication;
                taskModel.saveMessage((Message)communication);
            }
            taskModel.saveResource(item);
        }

        // Save locally image
        VinclesConstants.saveImage(data, item.filename);
        prog.setVisibility(View.GONE);

        // Load image once has been saved
        if (!this.isFinishing())
            Glide.with(this)
                    .load(VinclesConstants.getImageDirectory() + "/" + item.filename)
                    .into(image);
    }

    private void onGetImageResourceDataFail(Object error, ProgressBar prog) {
        String errorMessage = mainModel.getErrorByCode(error);
        prog.setVisibility(View.GONE);
        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
        toast.show();
    }

    // Load video from loca/server
    private void loadVideo(final Resource item, final Communication communication) {
        if (item == null)
            return;
        boolean existe = false;
        if (item.filename != null) {
            File file = new File(VinclesConstants.getVideoPath() + "/" +item.filename);
            existe = file.exists();
        }
        if (item.filename == "" || item.filename == null || !existe) {
            prog_bar.setVisibility(View.VISIBLE);
            if (taskModel.isGroupAction) {
                Long idChat = GroupModel.getInstance().currentGroup.idChat;
                if (taskModel.isPrivateChat) {
                    idChat = GroupModel.getInstance().currentGroup.idDynamizerChat;
                }
                taskModel.getServerChatResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        onGetVideoResourceDataSuccess(item, (byte[]) result, communication);
                    }

                    @Override
                    public void onFailure(Object error) {
                        onGetVideoResourceDataFail(error);
                    }
                }, idChat, communication.getId());
            } else {
                taskModel.getServerResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        onGetVideoResourceDataSuccess(item, (byte[]) result, communication);
                    }

                    @Override
                    public void onFailure(Object error) {
                        onGetVideoResourceDataFail(error);
                    }
                }, taskModel.getCurrentMessage().getResources().get(0).getId());
            }
        } else {
            prepareVideo(item);
        }
    }

    private void onGetVideoResourceDataSuccess(Resource item, byte[] result, Communication communication) {
        // Update resource
        if (item.filename == null || !item.filename.startsWith(VinclesConstants.VIDEO_PREFIX)) {
            item.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
            item.type = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;
            if (communication instanceof Chat) {
                item.chat = (Chat)communication;
                taskModel.saveChat((Chat)communication);
            } else {
                item.message = (Message)communication;
                taskModel.saveMessage((Message)communication);
            }
            taskModel.saveResource(item);
        }
        VinclesConstants.saveVideo(result, item.filename);
        prog_bar.setVisibility(View.GONE);
        prepareVideo(item);
    }

    private void onGetVideoResourceDataFail(Object error) {
        prog_bar.setVisibility(View.GONE);
        String errorMessage = mainModel.getErrorByCode(error);
        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void goBack(View view) {
//        finish();
        startActivity(new Intent(this, TaskMessageListActivity.class));
    }

    public void replayMessage(View view) {
        taskModel.currentUser = taskModel.getComunnication().userFrom;
        // Go to detail
        startActivity(new Intent(TaskMessageDetailActivity.this, TaskMessageSendActivity.class));
    }


    private void playVideo() {
        String path = VinclesConstants.getVideoPath() + taskModel.getComunnication().getResources().get(0).filename;
        if (video.isPlaying()) {
            video.pause();
            imgPlay.setVisibility(View.VISIBLE);
        } else {
            video.setVideoPath(path);
            video.requestFocus();
            video.start();
            imgPlay.setVisibility(View.GONE);
        }

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

    // Load audio from loca/server
    private void loadAudio(final Resource item, final Communication communication) {
        if (item == null)
            return;
        boolean existe = false;
        if (item.filename != null) {
            File file = new File(VinclesConstants.getAudioPath() + "/" +item.filename);
            existe = file.exists();
        }
        if (item.filename == "" || item.filename == null || !existe) {
            prog_bar.setVisibility(View.VISIBLE);
            if (taskModel.isGroupAction) {
                Long idChat = GroupModel.getInstance().currentGroup.idChat;
                if (taskModel.isPrivateChat) {
                    idChat = GroupModel.getInstance().currentGroup.idDynamizerChat;
                }
                taskModel.getServerChatResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        onGetAudioResourceDataSuccess(item, (byte[]) result, communication);
                    }

                    @Override
                    public void onFailure(Object error) {
                        onGetAudioResourceDataFail(error);
                    }
                }, idChat, communication.getId());
            } else {
                taskModel.getServerResourceData(new AsyncResponse() {
                    @Override
                    public void onSuccess(Object result) {
                        onGetAudioResourceDataSuccess(item, (byte[]) result, communication);
                    }

                    @Override
                    public void onFailure(Object error) {
                        onGetAudioResourceDataFail(error);
                    }
                }, taskModel.getCurrentMessage().getResources().get(0).getId());
            }
        } else {
            prepareAudio(item);
        }
    }

    private void onGetAudioResourceDataSuccess(Resource item, byte[] result, Communication communication) {
        // Update resource
        if (item.filename == null || !item.filename.startsWith(VinclesConstants.AUDIO_PREFIX)) {
            item.filename = VinclesConstants.AUDIO_PREFIX + new Date().getTime() + VinclesConstants.AUDIO_EXTENSION;
            item.type = VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE;
            if (communication instanceof Chat) {
                item.chat = (Chat)communication;
                taskModel.saveChat((Chat)communication);
            } else {
                item.message = (Message)communication;
                taskModel.saveMessage((Message)communication);
            }
            taskModel.saveResource(item);
        }
        VinclesConstants.saveAudio(result, item.filename);
        prog_bar.setVisibility(View.GONE);
        prepareAudio(item);
    }

    private void onGetAudioResourceDataFail(Object error) {
        prog_bar.setVisibility(View.GONE);
        String errorMessage = mainModel.getErrorByCode(error);
        Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void prepareAudio(Resource item) {
        String path = VinclesConstants.getAudioPath() + item.filename;
        File file = new File(path);
        if (file.exists()) {
            imgPlay.setVisibility(View.VISIBLE);
            imgPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playAudio();
                }
            });

            try {
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
            } catch (IOException e) {
                Log.e(TAG, "mediaPlayer.start() - error: " + e);
            }
        }
    }

    private void playAudio() {
        if (isReady) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                //imgPlay.setVisibility(View.VISIBLE);
            } else {
                //imgPlay.setVisibility(View.GONE);
                mediaPlayer.start();
            }
        }
    }

    public void prevResource(View view) {
        List<Resource> lst_res = taskModel.getComunnication().getResources();
        if (pag_images <= 0)
            return;
        pag_images--;
        show_images(lst_res,taskModel.getComunnication());
    }

    public void nextResource(View view) {
        List<Resource> lst_res = taskModel.getComunnication().getResources();
        if (pag_images >= lst_res.size() / MAX_ITEM || (lst_res.size() <= (pag_images+1)*MAX_ITEM))
            return;
        pag_images++;
        show_images(lst_res,taskModel.getComunnication());
    }

    private void show_images(List<Resource> lst_res, Communication communication) {
        if (lst_res == null || lst_res.size() == 0) {
            layout_images.setVisibility(View.GONE);
            return;
        }
        if (lst_res.size() > pag_images*MAX_ITEM + 0) {
            loadImage(img1, lst_res.get(pag_images*MAX_ITEM + 0), communication, prog_bar_img1);
        }
        if (lst_res.size() > pag_images*MAX_ITEM + 1) {
            loadImage(img2, lst_res.get(pag_images*MAX_ITEM + 1), communication, prog_bar_img2);
            frame_img2.setVisibility(View.VISIBLE);
        } else {
            frame_img2.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onDestroy() {
        stopAndReleaseMedia();
        super.onDestroy();
    }

    public void stopAllMedia() {
        if (video != null) {
            video.stopPlayback();
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void stopAndReleaseMedia() {
        if (video != null) {
            video.stopPlayback();
            video = null;
        }
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}