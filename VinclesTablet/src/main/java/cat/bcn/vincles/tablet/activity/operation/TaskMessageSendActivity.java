/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.groups.GroupsChatActivity;
import cat.bcn.vincles.tablet.activity.groups.GroupsDinamizerChatActivity;
import cat.bcn.vincles.tablet.model.GroupModel;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class TaskMessageSendActivity extends TaskActivity {
    private static final String TAG = "TaskMessageSendActivity";
    private View togMessageRecord, togMessageLayout, togMessageVideo, togMessageAudio, linMessageCentral, btnPlay, imgMessageOff;
    private View dots[];
    private VideoView video;
    private int activeDot = 0;
    private boolean isVideo = true;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private Timer timer;
    private Uri videoUri;
    private String audioPath;
    private String audioName;
    ViewGroup canvasLayout, actionButtonsLayout,audioButtonsLayout, micDotsLayout;
    private GroupModel groupModel = GroupModel.getInstance();
    private TextView texUserName;
    private ImageView imgUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_message_send);

        togMessageVideo = findViewById(R.id.togMessageVideo);
        togMessageAudio = findViewById(R.id.togMessageAudio);
        togMessageLayout = findViewById(R.id.togMessageLayout);
        togMessageRecord = findViewById(R.id.togMessageRecord);
        linMessageCentral = findViewById(R.id.linMessageCentral);
        imgMessageOff = findViewById(R.id.imgMessageOff);
        video = (VideoView) findViewById(R.id.video);
        btnPlay = findViewById(R.id.btnPlay);
        texUserName     = (TextView) findViewById(R.id.texUserName);
        imgUser         = (ImageView) findViewById(R.id.imgUser);

        canvasLayout = (ViewGroup) findViewById(R.id.messageCanvas);
        actionButtonsLayout = (ViewGroup) findViewById(R.id.actionButtonsLayout);
        audioButtonsLayout = (ViewGroup) findViewById(R.id.audioButtonsLayout);
        micDotsLayout = (ViewGroup) findViewById(R.id.mic_dots);

        int dotsLength = micDotsLayout.getChildCount();
        dots = new View[dotsLength];
        activeDot = dotsLength-1;
        dots = new View[dotsLength];
        for (int i = 0; i < micDotsLayout.getChildCount(); i++) {
            dots[i] = micDotsLayout.getChildAt(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (taskModel.isGroupAction) {
            if (taskModel.isPrivateChat) {
                if (taskModel.currentGroup.dynamizer != null) {
                    texUserName.setText(taskModel.currentGroup.dynamizer.alias);
                    if (taskModel.currentGroup.dynamizer.idContentPhoto != null) {
                        if (!isFinishing())
                            Glide.with(this)
                                .load(mainModel.getUserPhotoUrlFromUserId(taskModel.currentGroup.dynamizer.getId()))
                                .signature(new StringSignature(taskModel.currentGroup.dynamizer.idContentPhoto.toString()))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(imgUser);
                    } else {
                        Log.w(TAG, taskModel.currentGroup.dynamizer.alias + " has idContentPhoto null!");
                    }
                }
            } else {
                texUserName.setText(taskModel.currentGroup.name);
                if (!isFinishing())
                    Glide.with(this)
                        .load(groupModel.getGroupPhotoUrlFromGroupId(taskModel.currentGroup.getId()))
                        .signature(new StringSignature(taskModel.currentGroup.getId().toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgUser);
            }
        } else {
            User userFrom = taskModel.currentUser;
            if (userFrom != null) {
                texUserName.setText(userFrom.name + " " + userFrom.lastname);
                if (userFrom.idContentPhoto != null) {
                    if (!isFinishing())
                        Glide.with(this)
                            .load(mainModel.getUserPhotoUrlFromUser(userFrom))
                            .signature(new StringSignature(userFrom.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(imgUser);
                } else {
                    Log.w(TAG, userFrom.alias + " has idContentPhoto null!");
                }
            }
        }


        togMessageAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togMessageVideo.setBackgroundResource(R.drawable.my_btn_gris);
                togMessageVideo.setClickable(true);
                togMessageAudio.setBackgroundResource(R.drawable.my_btn_rojo_flat);
                togMessageAudio.setClickable(false);
                ((Button)togMessageRecord).setText(getString(R.string.task_message_button_record_audio));
                isVideo = false;
            }
        });

        togMessageVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togMessageAudio.setBackgroundResource(R.drawable.my_btn_gris);
                togMessageAudio.setClickable(true);
                togMessageVideo.setBackgroundResource(R.drawable.my_btn_rojo_flat);
                togMessageVideo.setClickable(false);
                ((Button)togMessageRecord).setText(getString(R.string.task_message_button_record_video));
                isVideo = true;
            }
        });
        togMessageRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordMessage();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopDotsAnimation();
    }

    public void sendMessage(View view) {
        Log.i(TAG, "sendMessage()");
        showProgressBar(false,getString(R.string.general_upload));
        if (!taskModel.isGroupAction) {
            taskModel.getCurrentMessage().idUserFrom = mainModel.currentUser.getId();
            taskModel.getCurrentMessage().idUserTo = taskModel.currentUser.getId();
            taskModel.getCurrentMessage().resourceTempList = new ArrayList<Resource>();
            Resource resource = new Resource();
            resource.message = taskModel.getCurrentMessage();

            if (isVideo) {
                taskModel.getCurrentMessage().metadataTipus = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;

                // Load video from local system
                String videoPath = mainModel.getRealVideoPathFromURI(videoUri);
                File videoFile = new File(videoPath);
                RequestBody file = RequestBody.create(MediaType.parse("video/mp4"), videoFile);
                resource.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
                resource.data = MultipartBody.Part.createFormData("file", resource.filename, file);
            } else {
                taskModel.getCurrentMessage().metadataTipus = VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE;

                // Load audio from local system
                File audioFile = new File(audioPath);
                RequestBody file = RequestBody.create(MediaType.parse("audio/aac"), audioFile);
                resource.filename = audioName;
                resource.data = MultipartBody.Part.createFormData("file", resource.filename, file);
            }

            taskModel.getCurrentMessage().resourceTempList.add(resource);
            taskModel.getCurrentMessage().sendTime = new Date();

            List<User> userList = new ArrayList<User>();
            if ((taskModel.currentUser.getId().longValue() == mainModel.currentUser.getId().longValue())) {
                userList = taskModel.getUserList();
            } else {
                userList.add(taskModel.currentUser);
            }
            taskModel.sendMessageToAll(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    updateView();
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "sendMessageToAll() - error: " + error);
                    hideProgressBar();
                    String errorMessage = mainModel.getErrorByCode(error);
                    Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, taskModel.getCurrentMessage(), userList);
        } else {
            taskModel.getCurrentChat().idUserFrom = mainModel.currentUser.getId();
            taskModel.getCurrentChat().idUserTo = taskModel.currentUser.getId();
            taskModel.getCurrentChat().resourceTempList = new ArrayList<Resource>();
            Resource resource = new Resource();
            resource.chat = taskModel.getCurrentChat();

            if (isVideo) {
                taskModel.getCurrentChat().metadataTipus = VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE;

                // Load video from local system
                String videoPath = mainModel.getRealVideoPathFromURI(videoUri);
                File videoFile = new File(videoPath);
                RequestBody file = RequestBody.create(MediaType.parse("video/mp4"), videoFile);
                resource.filename = VinclesConstants.VIDEO_PREFIX + new Date().getTime() + VinclesConstants.VIDEO_EXTENSION;
                resource.data = MultipartBody.Part.createFormData("file", resource.filename, file);
            } else {
                taskModel.getCurrentChat().metadataTipus = VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE;

                // Load audio from local system
                File audioFile = new File(audioPath);
                RequestBody file = RequestBody.create(MediaType.parse("audio/aac"), audioFile);
                resource.filename = audioName;
                resource.data = MultipartBody.Part.createFormData("file", resource.filename, file);
            }

            taskModel.getCurrentChat().resourceTempList.add(resource);
            taskModel.getCurrentChat().sendTime = new Date();

            List<Long> idChatList = new ArrayList<Long>();

            if (!taskModel.isPrivateChat) {
                if (taskModel.currentGroup == null) {
                    for (VinclesGroup group : groupModel.getGroupList()) {
                        idChatList.add(group.idChat);
                    }
                } else {
                    idChatList.add(taskModel.currentGroup.idChat);
                }
            } else {
                idChatList.add(taskModel.currentGroup.idDynamizerChat);
            }

            groupModel.sendChatToAll(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    updateView();
                }

                @Override
                public void onFailure(Object error) {
                    Log.i(TAG, "sendChatToAll() - error: " + error);
                    hideProgressBar();
                    String errorMessage = mainModel.getErrorByCode(error);
                    Toast toast = Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, taskModel.getCurrentChat(), idChatList);
        }
    }

    private void updateView() {
        canvasLayout.removeAllViews();
        View resultLayout = getLayoutInflater().inflate(R.layout.activity_task_message_result, canvasLayout);

        /////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\
        //     HACK ALERT: SAME USER MEANS SEND TO ALL  \\
        /////////////////////////\\\\\\\\\\\\\\\\\\\\\\\\\
        ImageView image = (ImageView)resultLayout.findViewById(R.id.imgPhoto);
        TextView text = (TextView)resultLayout.findViewById(R.id.texUserName);
        TextView back = (TextView) resultLayout.findViewById(R.id.btnSendText);
        releaseMedia();

        if (!taskModel.isGroupAction) {
            if (taskModel.currentUser != null) {
                if ((taskModel.currentUser.getId().longValue() == mainModel.currentUser.getId().longValue())) {
                    image.setImageResource(R.drawable.icon_todos_with_background);
                    text.setText(R.string.task_contactlist_all);
                } else {
                    text.setText(taskModel.currentUser.name);
                    if (taskModel.currentUser.idContentPhoto != null) {
                        if (!isFinishing())
                            Glide.with(this)
                                .load(mainModel.getUserPhotoUrlFromUser(taskModel.currentUser))
                                .signature(new StringSignature(taskModel.currentUser.idContentPhoto.toString()))
                                .error(R.drawable.user).placeholder(R.color.superlightgray)
                                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                .into(image);
                    } else {
                        Log.w(TAG, taskModel.currentUser.alias + " has idContentPhoto null!");
                    }
                }
            }
            back.setText(R.string.message_back);
        } else {
            if (taskModel.currentGroup == null) {
                image.setImageResource(R.drawable.icon_todos_with_background);
                text.setText(R.string.task_contactlist_all);
            } else {
                if (taskModel.isPrivateChat) {
                    if (taskModel.currentGroup.dynamizer != null) {
                        text.setText(taskModel.currentGroup.dynamizer.alias);
                        if (taskModel.currentGroup.dynamizer.idContentPhoto != null) {
                            if (!isFinishing())
                                Glide.with(this)
                                    .load(mainModel.getUserPhotoUrlFromUserId(taskModel.currentGroup.dynamizer.getId()))
                                    .signature(new StringSignature(taskModel.currentGroup.dynamizer.idContentPhoto.toString()))
                                    .error(R.drawable.user).placeholder(R.color.superlightgray)
                                    .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                                    .into(image);
                        } else {
                            Log.w(TAG, taskModel.currentGroup.dynamizer.alias + " has idContentPhoto null!");
                        }
                    }
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
            }

            if (!taskModel.isPrivateChat) {
                back.setText(getString(R.string.task_group_back));
            } else {
                back.setText(getString(R.string.task_group_dinamizer_back, taskModel.currentGroup.name));
            }
        }
        hideProgressBar();
    }

    public void deleteMessage(View view) {
        back();
    }

    public void goBack(View view) {
        back();
    }

    private void back() {
        if (!taskModel.isGroupAction) {
            Log.i(TAG, "go back to TaskMessageListActivity");
            startActivity(new Intent(this, TaskMessageListActivity.class));
        } else {
            if (!taskModel.isPrivateChat) {
                Log.i(TAG, "go back to GroupsChatActivity");
                startActivity(new Intent(this, GroupsChatActivity.class));
            } else {
                Log.i(TAG, "go back to GroupsDinamierChatActivity");
                startActivity(new Intent(this, GroupsDinamizerChatActivity.class));
            }
        }
        finish();
    }

    private void recordMessage() {
        togMessageLayout.setVisibility(View.GONE);
        togMessageRecord.setVisibility(View.GONE);

        if (isVideo) {
            recordVideo();
        } else {
            video.setVisibility(View.GONE);
            togMessageRecord.setVisibility(View.GONE);
            imgMessageOff.setVisibility(View.VISIBLE);
            audioButtonsLayout.setVisibility(View.VISIBLE);
            recordAudio();
        }
    }

    public void stopMessage(View v) {
        Log.i(TAG, "stopMessage()");
        stopDotsAnimation();

        actionButtonsLayout.setVisibility(View.VISIBLE);
        togMessageRecord.setVisibility(View.GONE);
        btnPlay.setVisibility(View.VISIBLE);
        audioButtonsLayout.setVisibility(View.GONE);
        imgMessageOff.setVisibility(View.GONE);

        if (isVideo) {
            ((Button)btnPlay).setText(R.string.task_message_button_play);
            ((Button)togMessageRecord).setText(R.string.task_message_button_play);
            // Video stop from Camera itself!
            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    video.setVideoURI(videoUri);
                    video.start();
                    btnPlay.setVisibility(View.GONE);
                }
            });
        } else {
            ((Button)btnPlay).setText(R.string.task_message_button_play_audio);
            stopRecordAudio();
            ((Button)togMessageRecord).setText(R.string.task_message_button_play);
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(audioPath);
            } catch (IOException e) {
                Log.e(TAG, "mediaPlayer.start() - error: " + e);
            }

            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        try {
                            mediaPlayer = new MediaPlayer();
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setDataSource(audioPath);
                            mediaPlayer.prepare();
                            mediaPlayer.start();
                        } catch (IOException e) {
                            Log.e(TAG, "mediaPlayer.start() - error: " + e);
                        }
                    }
                }
            });
        }
    }

    private void recordVideo() {
        Log.i(TAG, "recordVideo()");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, VinclesConstants.VIDEO_QUALITY);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, VinclesConstants.VIDEO_SIZE_LIMIT);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, VinclesConstants.VIDEO_DURATION_LIMIT);
        intent.putExtra("android.intent.extras.CAMERA_FACING", 1);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, VinclesConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VinclesConstants.CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            imgMessageOff.setVisibility(View.GONE);
            togMessageLayout.setVisibility(View.GONE);
            video.setVisibility(View.VISIBLE);

            videoUri = data.getData();
            video.setVideoURI(videoUri);
            video.requestFocus();
            video.seekTo(10);

            video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    btnPlay.setVisibility(View.VISIBLE);
                }
            });

            stopMessage(null);
        }
        else {
            togMessageLayout.setVisibility(View.VISIBLE);
            togMessageRecord.setVisibility(View.VISIBLE);
        }
    }

    private void recordAudio() {
        audioName = VinclesConstants.AUDIO_PREFIX + new Date().getTime() + VinclesConstants.AUDIO_EXTENSION;
        audioPath = VinclesConstants.getAudioPath() + audioName;
        Log.i(TAG, "recordAudio() - file: " + audioPath);

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setOutputFile(audioPath);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(VinclesConstants.AUDIO_ENCODING_BIT_RATE);
        mediaRecorder.setAudioSamplingRate(VinclesConstants.AUDIO_SAMPLING_RATE);
        mediaRecorder.setAudioChannels(VinclesConstants.AUDIO_CHANNELS);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();

            startDotsAnimation();
        } catch (IOException e) {
            Log.e(TAG, "startRecording() - error: " + e);
        }
    }

    private void stopDotsAnimation() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }

    private void startDotsAnimation() {
        stopDotsAnimation();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dots[activeDot].setBackgroundResource(R.drawable.button_circle_grey);
                        if (activeDot >= dots.length-1) activeDot = 0;
                        else activeDot++;
                        dots[activeDot].setBackgroundResource(R.drawable.button_circle_red);
                    }
                });
            }
        }, 0, 1000);
    }

    private void stopRecordAudio() {
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        } catch (RuntimeException e) {
            // STOP FAILED
        }
    }

    @Override
    protected void onDestroy() {
        releaseMedia();
        super.onDestroy();
    }

    private void releaseMedia() {
        if (isVideo && video != null) {
            video.stopPlayback();
            video = null;
        }
        else if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}