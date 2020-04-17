package cat.bcn.vincles.mobile.UI.Calls;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.appspot.apprtc.PercentFrameLayout;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.io.File;

import cat.bcn.vincles.mobile.BuildConfig;
import cat.bcn.vincles.mobile.Client.Business.NotificationProcessor;
import cat.bcn.vincles.mobile.Client.Preferences.UserPreferences;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.UI.Alert.AlertConfirmOrCancel;
import cat.bcn.vincles.mobile.UI.Alert.AlertMessage;
import cat.bcn.vincles.mobile.UI.Common.BaseActivity;
import cat.bcn.vincles.mobile.UI.FragmentManager.MainFragmentManagerActivity;
import cat.bcn.vincles.mobile.Utils.MyApplication;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

import static cat.bcn.vincles.mobile.Utils.MyApplication.getAppContext;

public class CallsActivity extends BaseActivity implements CallsActivityView, View.OnClickListener, CallsWebRTCatManager.CallsCallbacks, AlertConfirmOrCancel.AlertConfirmOrCancelInterface, AlertMessage.AlertMessageInterface {

    public static final String EXTRAS_CALL_MODE = "extras_call_mode";
    public static final String EXTRAS_ID_ROOM = "extras_id_room";
    public static final String EXTRAS_USER_ID = "extras_user_id";
    public static final String EXTRAS_USER_NAME = "extras_user_name";
    public static final String EXTRAS_USER_LASTNAME = "extras_user_lastname";
    public static final String EXTRAS_USER_IS_VINCLES = "extras_user_is_vincles";
    public static final String EXTRAS_USER_AVATAR_PATH = "extras_user_avatar_path";
    private static final String ALERT_TYPE_PERMISSIONS = "SETTINGS_PERMISSIONS";

    private static final String PRESENTER_FRAGMENT_TAG = "calls_presenter_fragment_tag";

    private static final int CALL_PERMISSIONS_REQUEST = 0;


    CallsPresenterContract presenter;

    TextView title;
    ImageView avatarLeft, avatarRight;
    View bullets[] = new View[5];
    FrameLayout bottomBar;
    View back;

    ViewGroup ongoingCallLayout;
    ImageView hangButton;
    SurfaceViewRenderer localView, remoteView;
    PercentFrameLayout localRenderLayout, remoteRenderLayout;

    int callMode, userId;
    boolean userIsVincles;
    String userName, userLastname, userAvatarPath, idRoom;
    int bulletsPosition = 0;
    public CallsWebRTCatManager callsManager;

    Handler handler;

    AlertConfirmOrCancel alertConfirmOrCancel;
    boolean showingConfirmCancelAlert = false;

    boolean closed = false;

    private MediaPlayer mediaPlayer = null;

    Runnable bulletsAnimation = new Runnable() {
        @Override
        public void run() {

            setBulletsPosition(bulletsPosition);
            bulletsPosition = (bulletsPosition+1)%(getResources()
                    .getInteger(R.integer.calls_number_of_bullets)+1);
            handler.postDelayed(this, getResources().getInteger(R.integer.millis_animation_calling));
        }
    };

    @Override
    public void onStop() {
        Log.d("incalock","call activity onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("incalock","call activity onDestroy");
        ((MyApplication)getAppContext()).callsActivity = null;

        //if these lines go on onStop receiving a call with the screen locked does not work
        if (presenter != null) presenter.onStopActivity();
        if (callsManager != null) callsManager.hangUpPressed();
        stopRingSound();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            showingConfirmCancelAlert = true;
            alertConfirmOrCancel.showMessage(getString(R.string.calls_sure_exit_alert), "Eliminar", AlertConfirmOrCancel.BUTTONS_VERTICAL);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MyApplication)getAppContext()).callsActivity = this;

        OtherUtils.sendAnalyticsView(this,
                getResources().getString(R.string.tracking_call));

        LocalBroadcastManager.getInstance(this).registerReceiver(callErrorBroadcastReceiver,
                new IntentFilter(NotificationProcessor.CALL_ERROR_BROADCAST));

        Log.d("incalock","call activity onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(callErrorBroadcastReceiver);
        Log.d("incalock","call activity onPause");
    }

    private BroadcastReceiver callErrorBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onCallError(intent.getExtras().getString("idRoom"));
        }
    };

    private void onCallError(String idRoom) {
        Log.d("cllerr", "onReceivedError, roomId:"+idRoom+" this.idRoom:"+this.idRoom);
        if (this.idRoom.equals(idRoom)) {
            presenter.onOutgoingCallError();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("startVidConf", "onCreate");

        Log.d("callvid","call activity oncreate");

        alertConfirmOrCancel = new AlertConfirmOrCancel(this,this);
        alertConfirmOrCancel.setButtonsText(getString(R.string.calls_sure_exit_alert_positive),
                getString(R.string.calls_sure_exit_alert_negative));

        if (savedInstanceState != null) {
            showingConfirmCancelAlert = savedInstanceState.getBoolean("showingConfirmCancelAlert");
            if (showingConfirmCancelAlert) {
                alertConfirmOrCancel.showMessage(getString(R.string.calls_sure_exit_alert),
                        "Eliminar", AlertConfirmOrCancel.BUTTONS_VERTICAL);
            }
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.calls_activity_layout);
        Log.d("callvid","call activity oncreate2");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }

        Bundle data = getIntent().getExtras();
        callMode = data.getInt(EXTRAS_CALL_MODE);
        Log.d("callvid", "calls activity mode:"+callMode);
        userId = data.getInt(EXTRAS_USER_ID);
        userName = data.getString(EXTRAS_USER_NAME);
        userLastname = data.getString(EXTRAS_USER_LASTNAME);
        userIsVincles = data.getBoolean(EXTRAS_USER_IS_VINCLES);
        userAvatarPath = data.getString(EXTRAS_USER_AVATAR_PATH);
        if (callMode == RECEIVING_CALL) {
            idRoom = data.getString(EXTRAS_ID_ROOM);
        }

        handler = new Handler();
        Log.d("callvid","call activity oncreate3");

        CallsPresenter presenterFragment = (CallsPresenter) getSupportFragmentManager().findFragmentByTag(PRESENTER_FRAGMENT_TAG);
        if (presenterFragment != null && savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.remove(presenterFragment).commit();
            presenterFragment = null;
        }
        if (presenterFragment == null) {
            Log.d("callvid", "create presenter with callmode:"+callMode);
            presenterFragment = CallsPresenter.newInstance(this, savedInstanceState, callMode,
                    userId, userAvatarPath, userName, userIsVincles);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(presenterFragment, PRESENTER_FRAGMENT_TAG).commit();
        } else {
            presenterFragment.setExternalVars(this, savedInstanceState);
        }
        presenter = presenterFragment;


        back = findViewById(R.id.back);
        title = findViewById(R.id.calls_title);
        avatarLeft = findViewById(R.id.avatar_left);
        avatarRight = findViewById(R.id.avatar_right);
        bottomBar = findViewById(R.id.bottom_bar);
        bullets[0] = findViewById(R.id.bullet_0);
        bullets[1] = findViewById(R.id.bullet_1);
        bullets[2] = findViewById(R.id.bullet_2);
        bullets[3] = findViewById(R.id.bullet_3);
        bullets[4] = findViewById(R.id.bullet_4);


        ongoingCallLayout = findViewById(R.id.ongoing_call_layout);
        hangButton = findViewById(R.id.cancel_call_button);
        remoteView = findViewById(R.id.remote_video_view);
        localView = findViewById(R.id.local_video_view);
        remoteRenderLayout = findViewById(R.id.remote_video_layout);
        localRenderLayout = findViewById(R.id.local_video_layout);

        findViewById(R.id.ongoing_call_hung_up_button).setOnClickListener(this);
        back.setOnClickListener(this);

        presenter.onCreateActivity();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("showingConfirmCancelAlert", showingConfirmCancelAlert);
    }

    @Override
    public void onClick(View v) {
        Log.d("incalock","callActivity onClick");
        switch (v.getId()) {
            case R.id.back:
                finishCallActivity();
                break;
            case R.id.reject_call_button:
                Log.d("callvid", "reject_call_button callsManager.onRejectCall()");
                presenter.onIncomingCallRejected();
                callsManager.onRejectCall();
                stopRingSound();
                finishCallActivity();
                break;
            case R.id.answer_button:
                Log.d("cllerr", "callsActivity answer_button");
                if (checkForCallPermissions()) {
                    Log.d("cllerr", "callsActivity answer_button has permissions");
                    acceptCall();
                } else {
                    if (shouldShowRequestPermissionRationaleCalls() && android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M){
                        showSettingsAlert(getResources().getString(R.string.should_accept_permissions_phone_call));
                    }
                    else{
                        Log.d("cllerr", "callsActivity answer_button request permissions");
                        requestCallPermissions();
                    }

                }
                v.setEnabled(false);
                break;
            case R.id.cancel_call_button:
                Log.d("callvid", "cancel_call_button callsManager.hangUpPressed()");
                callsManager.hangUpPressed();
                finishCallActivity();
                break;
            case R.id.send_message_button:
                finishCallActivity();
                break;
            case R.id.retry_button:
                presenter.onButtonClicked(CallsPresenterContract.RETRY_BUTTON);
                break;
            case R.id.ongoing_call_hung_up_button:
                Log.d("callvid", "ongoing_call_hung_up_button callsManager.hangUpPressed()");
                callsManager.hangUpPressed();
                break;

        }
    }

    private void showSettingsAlert(String message) {
        AlertMessage alertMessage = new AlertMessage(this, AlertMessage.TITTLE_INFO);
        alertMessage.showMessage(this,message, ALERT_TYPE_PERMISSIONS);
    }

    @Override
    public void onOkAlertMessage(AlertMessage alertMessage, String type) {
        switch (type){
            case ALERT_TYPE_PERMISSIONS:
                alertMessage.alert.cancel();
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                startActivity(i);
                break;
            default:
                alertMessage.alert.cancel();
        }
    }

    private void acceptCall() {
        Log.d("cllerr", "callsActivity acceptCall");
        presenter.onButtonClicked(CallsPresenterContract.ANSWER_CALL_BUTTON);
        stopRingSound();

        callsManager.onAcceptCall();
    }

    private boolean checkForCallPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED &&
                (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M
                        || ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CHANGE_NETWORK_STATE)
                        == PackageManager.PERMISSION_GRANTED);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean shouldShowRequestPermissionRationaleCalls(){

        return !shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO) &&

                !shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE) &&
                !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) &&
                (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.M
                        || !shouldShowRequestPermissionRationale(Manifest.permission.CHANGE_NETWORK_STATE));
    }

    private void requestCallPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.CHANGE_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE},
                CALL_PERMISSIONS_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        if (checkForCallPermissions()) {
            acceptCall();
        }
    }

    @Override
    public void setMode(final int mode) {
        Log.d("callvid", "setMode:"+mode);
        this.callMode = mode;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mode == ONGOING_CALL) {
                    ongoingCallLayout.setVisibility(View.VISIBLE);

                    handler.removeCallbacks(bulletsAnimation);
                } else {
                    ongoingCallLayout.setVisibility(View.GONE);
                    bottomBar.removeAllViews();

                    if (mode == INCOMING_CALL_ERROR || mode == OUTGOING_CALL_ERROR) {
                        stopRingSound();
                    }

                    int bottomBarResId;
                    switch (mode) {
                        case RECEIVING_CALL: default:
                            bottomBarResId = R.layout.fragment_calls_receiving_bottom_bar;
                            back.setVisibility(View.GONE);
                            break;
                        case MAKING_CALL:
                            bottomBarResId = R.layout.fragment_calls_calling_bottom_bar;
                            back.setVisibility(View.GONE);
                            break;
                        case CALL_NO_ANSWER:
                            bottomBarResId = R.layout.fragment_calls_retry_bottom_bar;
                            back.setVisibility(View.VISIBLE);
                            break;
                        case INCOMING_CALL_ERROR:
                        case OUTGOING_CALL_ERROR:
                            bottomBarResId = -1;
                            back.setVisibility(View.VISIBLE);
                            break;
                    }
                    if (bottomBarResId == -1) {
                        bottomBar.removeAllViews();
                    } else {
                        LayoutInflater.from(CallsActivity.this).inflate(bottomBarResId, bottomBar);
                    }
                    switch (mode) {
                        case RECEIVING_CALL: default:
                            setAvatarLeftSize(true);
                            View reject = bottomBar.findViewById(R.id.reject_call_button);
                            if (reject != null) reject.setOnClickListener(CallsActivity.this);
                            View answer = bottomBar.findViewById(R.id.answer_button);
                            if (answer != null) answer.setOnClickListener(CallsActivity.this);
                            title.setText(getResources().getString(R.string.calls_being_called_title, userName));
                            avatarRight.setVisibility(View.GONE);
                            for (View v : bullets) {
                                v.setVisibility(View.GONE);
                            }
                            break;
                        case MAKING_CALL:
                            setAvatarLeftSize(false);
                            bottomBar.findViewById(R.id.cancel_call_button).setOnClickListener(CallsActivity.this);
                            title.setText(getResources().getString(R.string.calls_calling_title, userName));
                            avatarRight.setVisibility(View.VISIBLE);
                            for (int i = 0; i < (getResources().getInteger(R.integer.calls_number_of_bullets)); i++) {
                                bullets[i].setVisibility(View.VISIBLE);
                            }
                            break;
                        case CALL_NO_ANSWER:
                            setAvatarLeftSize(false);
                            bottomBar.findViewById(R.id.send_message_button).setOnClickListener(CallsActivity.this);
                            bottomBar.findViewById(R.id.retry_button).setOnClickListener(CallsActivity.this);
                            title.setText(getResources().getString(R.string.calls_no_answer_title, userName));
                            avatarRight.setVisibility(View.GONE);
                            for (int i = 0; i < (getResources().getInteger(R.integer.calls_number_of_bullets)); i++) {
                                bullets[i].setVisibility(View.GONE);
                            }
                            break;
                        case OUTGOING_CALL_ERROR:
                        case INCOMING_CALL_ERROR:
                            avatarLeft.setVisibility(View.GONE);
                            avatarRight.setVisibility(View.GONE);
                            for (View v : bullets) {
                                v.setVisibility(View.GONE);
                            }
                            if (mode == OUTGOING_CALL_ERROR) {
                                title.setText(getResources().getString(R.string.calls_error_before_call));
                            }
                            break;
                    }
                    setAvatarPath(mode != MAKING_CALL, userAvatarPath);
                    setAvatarPath(mode == MAKING_CALL, new UserPreferences(getApplicationContext())
                            .getUserAvatar());

                    handler.removeCallbacks(bulletsAnimation);
                    handler.post(bulletsAnimation);
                }
            }
        });

    }

    private void setAvatarLeftSize(boolean big) {
        int size = (int) (big ? getResources().getDimension(R.dimen.calls_avatar_size_big)
                        : getResources().getDimension(R.dimen.calls_avatar_size));
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) avatarLeft.getLayoutParams();
        params.width = size;
        params.height = size;
    }

    @Override
    public void setupCallManager(CallsWebRTCatManager callsWebRTCatManager) {
        Log.d("callvid", "setupCallManager");
        localView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        remoteView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        callsManager = callsWebRTCatManager;
        callsManager.setListener(this);
        callsManager.setSurfaceRenderers(localView, remoteView, localRenderLayout, remoteRenderLayout);
        callsManager.connectWebRTCat(this, idRoom);
    }

    @Override
    public void setPathAvatar(String path) {
        userAvatarPath = path;
        setAvatarPath(callMode != MAKING_CALL, path);
    }

    @Override
    public void finishCallActivity() {
        //If task is root we have to start MainActivity before finish CallsActivity
        if (isTaskRoot()) {
            Intent intent = new Intent().setClass(
                    CallsActivity.this, MainFragmentManagerActivity.class);
            intent.putExtra(EXIT_EXTRA, true);
            startActivity(intent);
        }

        finish();
    }

    @Override
    public void onCallTimeout() {
        callsManager.hangUpPressed();
    }

    private void setAvatarPath(boolean isLeft, String path) {
        ImageView imageView = isLeft ? avatarLeft : avatarRight;
        if (path == null || path.length() == 0) {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.user));
            return;
        }
        path = path.replace("file://","");
        Glide.with(this)
                .load(path.equals("placeholder") ?
                        getResources().getDrawable(R.drawable.user)
                        : new File(path))
                .apply(RequestOptions.overrideOf(200, 200))
                .into(imageView);
    }

    private void setBulletsPosition(int position) {
        for (int i = 0; i < bullets.length; i++) {
            bullets[i].setSelected(i < position);
        }
    }


    @Override
    public void sendCallNotification(int idUser, String idRoom) {
        Log.d("cllerr", "sendCallNotification");
        this.idRoom = idRoom;
        presenter.sendCallNotification(idUser, idRoom);
    }

    @Override
    public void onIncomingCallConnected() {
        presenter.onIncomingCallConnected();
    }

    @Override
    public void onOutgoingCallConnected() {
        presenter.onOutgoingCallConnected();
    }

    @Override
    public void onIncomingCallCancelled() {
        Log.d("incalock","incoming call cancelled");
        presenter.onIncomingCallCancelled();
        finishCallActivity();
    }

    @Override
    public void onCallOfferFailed() {
        Log.d("incalock","call offer failed");
        presenter.timeOutExecuted();

     //   finishCallActivity();
        /*
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                callMode = CallsActivityView.CALL_NO_ANSWER;
                setMode(callMode);
            }
        });
        stopRingSound();
        presenter.onError();
        */
    }

    @Override
    public void onHangup() {
        if (callMode != CALL_NO_ANSWER) {
            finishCallActivity();
            Log.d("incalock","onHangup callsActivity");
        }
    }

    @Override
    public void onError(final String error) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                title.setText(error);
            }
        });
        stopRingSound();
        presenter.onError();
    }

    @Override
    public void onNotifyError(int idUser, String idRoom) {
        presenter.onNotifyError(idUser, idRoom);
    }

    @Override
    public void onAccept(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.dismissSafely();
        showingConfirmCancelAlert = false;
        callsManager.hangUpPressed();
        finishCallActivity();
        Log.d("incalock","on accept Alert");
    }

    @Override
    public void onCancel(AlertConfirmOrCancel alertConfirmOrCancel) {
        alertConfirmOrCancel.dismissSafely();
        showingConfirmCancelAlert = false;
    }

    public void startRingSound() {
        stopRingSound();
        mediaPlayer = MediaPlayer.create(this, R.raw.ring);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void stopRingSound() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            mediaPlayer = null;
        }
    }

}
