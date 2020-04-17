package cat.bcn.vincles.mobile.UI.Chats;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertListenAudioChat;
import cat.bcn.vincles.mobile.Utils.Constants;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static cat.bcn.vincles.mobile.UI.Chats.ChatPresenter.MEDIA_AUDIO;

public class ChatAudioRecorderFragment extends Fragment implements AlertListenAudioChat.AlertListenAudioChatInterface, AlertMessage.AlertMessageInterface {

    MediaRecorder audioRecorder;
    CountDownTimer audioTimer;

    ChatPresenterContract presenter;
    ChatFragmentView view;
    String audioPath;

    boolean saveMedia = false;
    boolean clickSave = false;
    boolean isShowingAudioPreview = false;

    public static boolean RECORDING_AUDIO = false;

    void setPresenterAudio(ChatPresenterContract presenter, ChatFragmentView view) {
        this.presenter = presenter;
        this.view = view;

        if (clickSave && presenter != null) presenter.onClickSendAudio();
        else if (saveMedia && presenter != null) presenter.onSaveMediaFile(audioPath, MEDIA_AUDIO);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopRecording(false, false);
        presenter = null;
        view = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecording(false, false);

        if (audioRecorder != null) {
            audioRecorder.release();
            audioRecorder = null;
        }
        if (audioTimer != null) {
            audioTimer.cancel();
            audioTimer = null;
        }
        ChatAudioRecorderFragment.RECORDING_AUDIO = false;
        this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    void startRecording() {
        stopRecording(false, false);
        try {
            audioPath = ImageUtils.createAudioFile(MyApplication.getAppContext()).getPath();

            audioRecorder = new MediaRecorder();
            audioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            audioRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            audioRecorder.setOutputFile(audioPath);
            audioRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

            audioRecorder.prepare();

            try {
                audioRecorder.start();
            } catch (Exception e) {
                Log.d("ERROR", "Error:"+e);
            }

            audioTimer = new CountDownTimer(Constants.AUDIO_RECORD_MAX_TIME,16) {
                @Override
                public void onTick(long millisUntilFinished) {
                    Log.d("qwer","audio timer tick");
                    Log.d("progresss", "progresss: " + String.valueOf(Constants.AUDIO_RECORD_MAX_TIME-(int)millisUntilFinished));

                    if (view != null) view.setAudioProgress(Constants.AUDIO_RECORD_MAX_TIME-(int)millisUntilFinished,
                            DateUtils.getFormatedTimeFromMillisAudioRecorder((int)millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    try {
                        if (view != null) view.setAudioProgress(Constants.AUDIO_RECORD_MAX_TIME,
                                DateUtils.getFormatedTimeFromMillisAudioRecorder(Constants.AUDIO_RECORD_MAX_TIME));
                        if (presenter != null) {
                            presenter.onClickSendAudio();
                        } else {
                            clickSave = true;
                        }
                    } catch (Exception e) {
                        System.out.println("Error " + e.getMessage());
                    }
                }
            }.start();
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
            ChatAudioRecorderFragment.RECORDING_AUDIO = true;
        } catch (IOException e) {
            Log.e("audio", "could not write to file");
        }
    }

    void stopRecording(boolean save, boolean repeat) {
        if (repeat){
            presenter.onClickAudio();
        }
        else{
            if (audioTimer != null) {
                audioTimer.cancel();
                audioTimer = null;
            }
            if (audioRecorder != null) {
                try {
                    audioRecorder.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                audioRecorder.release();
                audioRecorder = null;
                if (save) {
                    showAudioPreviewAlert();
              /*  if (presenter != null) {
                    presenter.onSaveMediaFile(audioPath, MEDIA_AUDIO);
                } else {
                    saveMedia = true;
                }*/
                }
            }
            this.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            ChatAudioRecorderFragment.RECORDING_AUDIO = false;
        }

    }

    private void showAudioPreviewAlert() {
        new AlertListenAudioChat(getActivity(),this).showMessage(audioPath, getResources().getString(R.string.chat_send_audio), AlertConfirmOrCancel.BUTTONS_HORIZNTAL);
        isShowingAudioPreview = true;
    }


    @Override
    public void onSend(AlertListenAudioChat alertListenAudioChatInterface) {
        alertListenAudioChatInterface.dismissSafely();

        if (presenter != null) {
                    presenter.onSaveMediaFile(audioPath, MEDIA_AUDIO);
              } else {
                    saveMedia = true;
              }
        isShowingAudioPreview = false;
    }

    @Override
    public void onRepeat(AlertListenAudioChat alertListenAudioChatInterface) {
        stopRecording(false, true);
        alertListenAudioChatInterface.dismissSafely();
        isShowingAudioPreview = false;
    }

    @Override
    public void onCancel(AlertListenAudioChat alertListenAudioChatInterface) {
        alertListenAudioChatInterface.dismissSafely();
        stopRecording(false, false);
        isShowingAudioPreview = false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isShowingAudioPreview", isShowingAudioPreview);
        outState.putString("audioPath", audioPath);
        //Save the fragment's state here
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            //Restore the fragment's state here
            audioPath = savedInstanceState.getString("audioPath");
            isShowingAudioPreview = savedInstanceState.getBoolean("isShowingAudioPreview");

            if (isShowingAudioPreview){
                showAudioPreviewAlert();
            }
        }
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
       alertMessage.dismissSafely();
    }
}
