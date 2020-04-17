package cat.bcn.vincles.mobile.UI.Alert;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.DateUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class AlertListenAudioChat implements View.OnClickListener, SeekBar.OnSeekBarChangeListener{


    public static final String BUTTONS_HORIZNTAL = "BUTTONS_HORIZNTAL";
    public static final String BUTTONS_VERTICAL = "BUTTONS_VERTICAL";

    private AlertListenAudioChatInterface alertListenAudioChatInterface;
    public AlertDialog alert;
    private Button acceptBtn, canclelBtn;
    private ImageView close_dialog;
    private Activity context;
    private String cancelText, acceptText;
    private AppCompatImageView playPauseButton;
    private String audioPath;
    private MediaPlayer mediaPlayer;
    private TextView proggressTime;
    private SeekBar seekbar;
    private int duration;
    private CountDownTimer audioTimer;
    private Boolean isCanceled = false;


    public AlertListenAudioChat(Activity context, AlertListenAudioChatInterface alertListenAudioChatInterface) {
        this.context = context;
        acceptText = "";
        cancelText = "";
        this.alertListenAudioChatInterface = alertListenAudioChatInterface;
    }

    public void showMessage(String audioPath, String title, String type) {
        if (OtherUtils.activityCannotShowDialog(context)) {
            return;
        }
        this.audioPath = audioPath;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = null;
        if (type.equals(BUTTONS_HORIZNTAL)) {
            alertLayout = inflater.inflate(R.layout.alert_listen_audio_chat_horizontal, null);
        } else {
            alertLayout = inflater.inflate(R.layout.alert_listen_audio_chat_vertical, null);
        }

        ((TextView)alertLayout.findViewById(R.id.dialogTitle)).setText(title);

        acceptBtn = alertLayout.findViewById(R.id.accept);
        canclelBtn = alertLayout.findViewById(R.id.cancel);
        close_dialog = alertLayout.findViewById(R.id.close_dialog);
        playPauseButton = alertLayout.findViewById(R.id.play_iv);
        proggressTime = alertLayout.findViewById(R.id.proggress_time);
        seekbar = alertLayout.findViewById(R.id.seekbar);


        if (!"".equals(acceptText) || !"".equals(cancelText)) {
            acceptBtn.setText(acceptText);
            canclelBtn.setText(cancelText);
        }
        acceptBtn.setOnClickListener(this);
        canclelBtn.setOnClickListener(this);
        close_dialog.setOnClickListener(this);
        playPauseButton.setOnClickListener(this);
        seekbar.setOnSeekBarChangeListener(this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.DialogsTheme);
        alertDialogBuilder
                .setCancelable(false);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        alert.show();
    }





    @Override
    public void onClick(View view) {
        if (mediaPlayer!=null){
            resetMediaPlayer();
        }
        if (view.getId() == R.id.accept) {
            alertListenAudioChatInterface.onSend(this);
        } else if (view.getId() == R.id.cancel) {
            alertListenAudioChatInterface.onRepeat(this);
        } else if (view.getId() == R.id.close_dialog) {
            alertListenAudioChatInterface.onCancel(this);
            alert.dismiss();
        }else if(view.getId() == R.id.play_iv){
            playOrPauseAudio();

        }
    }

    private void resetMediaPlayer() {
        if (audioTimer != null) audioTimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();
        playPauseButton.setSelected(false);
    }

    private void playOrPauseAudio() {
        if (mediaPlayer != null){
            stopAudio();
        }
        if (!isCanceled){
            try {
                playAudio();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void stopAudio() {
        isCanceled = true;
        Log.d("stop", "Stop");
        if (audioTimer != null) audioTimer.cancel();
        if (mediaPlayer != null) mediaPlayer.release();

        audioTimer = null;
        mediaPlayer = null;
        playPauseButton.setSelected(false);
        duration = 0;
        setPlayPosition(0);
        seekbar.setProgress(0);

        resetCancel();

    }

    private void resetCancel() {
        CountDownTimer countDownTimer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                isCanceled = false;
            }
        }.start();
    }

    private void playAudio() throws IOException, IllegalStateException {

        isCanceled = false;
        playPauseButton.setSelected(true);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setDataSource(audioPath);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.prepare();
        duration = mediaPlayer.getDuration();
        seekbar.setMax(duration);
        mediaPlayer.start();
        createCountdownTimer(0);

    }

    private void createCountdownTimer(int currentProgress) {
        audioTimer = new CountDownTimer(mediaPlayer.getDuration() - currentProgress,
                16) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isCanceled ){
                    cancel();
                    return;
                }

                setPlayPosition((int) (duration - millisUntilFinished));

            }

            @Override
            public void onFinish() {
                setPlayPosition(0);
                stopAudio();
            }
        }.start();
    }

    public void dismissSafely() {
        if (alert != null && alert.isShowing()) alert.dismiss();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if(b){
            if (mediaPlayer == null)return;
            mediaPlayer.seekTo(progress);

            if(audioTimer!=null) {
                audioTimer.cancel();
            } else {
                Toast.makeText(context, R.string.error_1001, Toast.LENGTH_SHORT).show();
            }

            if (!isCanceled){
                createCountdownTimer(progress);
            }


            proggressTime.setText(DateUtils.getFormatedTimeFromMillis(progress));

        }

    }

    private void setPlayPosition(int position) {
//Make sure you update Seekbar on UI thread
        context.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(mediaPlayer != null){
                    Log.d("position", String.valueOf( mediaPlayer.getCurrentPosition()));
                    int mCurrentPosition = mediaPlayer.getCurrentPosition();
                    seekbar.setProgress(mCurrentPosition);
                }
            }
        });

    //    seekbar.setProgress(position);
        proggressTime.setText(DateUtils.getFormatedTimeFromMillis(position));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public interface AlertListenAudioChatInterface {
        void onSend(AlertListenAudioChat alertListenAudioChatInterface);
        void onRepeat(AlertListenAudioChat alertListenAudioChatInterface);
        void onCancel(AlertListenAudioChat alertListenAudioChatInterface);

    }
}
