/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.operation;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.i2cat.seg.and2cat.webrtcat4.WebRTCat;
import com.i2cat.seg.and2cat.webrtcat4.WebRTCatErrorCode;
import com.i2cat.seg.and2cat.webrtcat4.WebRTCatPeerConnectionClient;
import com.i2cat.seg.and2cat.webrtcat4.WebRTStats;
import org.appspot.apprtc.PercentFrameLayout;
import org.webrtc.SurfaceViewRenderer;
import java.io.IOException;
import cat.bcn.vincles.lib.VinclesApp;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.fragment.IncomingVideoConferenceCallFragment;
import cat.bcn.vincles.tablet.fragment.OutgoingVideoConferenceCallFragment;
import cat.bcn.vincles.tablet.model.FeedModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.VideoModel;

public class TaskVideoConferenceCallActivity extends TaskActivity
        implements WebRTCat.WebRTCatCallbacks,
                   IncomingVideoConferenceCallFragment.IncomingCallCallbacks,
                   OutgoingVideoConferenceCallFragment.OutgoingCallCallbacks {
    private static final String TAG = "TaskVCCallActivity";

    // New for Android 23 (Marshmallow) -- runtime permissions!
    // See http://developer.android.com/intl/es/training/permissions/requesting.html.
    private static final int PERMISSIONS_REQUEST_CODE = 1;
    private static final String[] MY_PERMISSIONS = new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.ACCESS_NETWORK_STATE
    };

    private static final String WEBRTCAT4_URL_PRE_EURECAT = "https://your-vc-server:port";
    private static final String WEBRTCAT4_URL_PRE_AZURE   = "https://your-vc-server:port";
    private static final String WEBRTCAT4_URL_PRO_AZURE   = "https://your-vc-server:port";

    public static final int ERROR_CODE_CANT_NOTIFY_PEER = 600;
    public static final String ROOM_NAME = "roomName";
    public static final String CALLEE_NAME = "calleeName";
    public static final String CALLEE_ID = "calleeId";
    public static final String CALLER_ID = "callerId";
    public static final String IS_INCOMING_CALL = "isIncomingCall";

    public static boolean isInVC = false;

    private WebRTCat webrtcat;
    private boolean isIncomingCall;
    private String idRoom;
    private IncomingVideoConferenceCallFragment incomingCallFragment;
    private OutgoingVideoConferenceCallFragment outgoingCallFragment;
    private boolean isCallConnected;
    private long callerId;

    // Object that will be invoked when the callee notification was either sent successfully or not.
    // Holding a reference to the handler using a static member is not a good pattern per se,
    // but in certain situations is considered acceptable... (see http://stackoverflow.com/a/7984845)
    private static AsyncResponse notifSentHandler;

    public static void startActivityForIncomingCall(Context context, Long callerId, String roomName) {
        Intent intent = new Intent(context, TaskVideoConferenceCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(IS_INCOMING_CALL, true);
        intent.putExtra(ROOM_NAME, roomName);
        intent.putExtra(CALLER_ID, callerId);

        context.startActivity(intent);
    }

    public static void startActivityForOutgoingCall(final Context context, final MainModel mainModel, final User callee, final AsyncResponse handler) {
        String callerName = mainModel.currentUser.username;
        String calleeName = callee.username;
        String idRoom = callerName + "-" + calleeName + "-" + System.currentTimeMillis();
        Long calleeId = callee.getId();

        notifSentHandler = handler;

        Intent intent = new Intent(context, TaskVideoConferenceCallActivity.class);
        intent.putExtra(ROOM_NAME, idRoom);
        intent.putExtra(CALLEE_NAME, calleeName);
        intent.putExtra(CALLEE_ID, calleeId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isInVC = true;

        setContentView(R.layout.activity_task_videoconference_call);

        isIncomingCall = getIntent().getBooleanExtra(IS_INCOMING_CALL, false);
        idRoom = getIntent().getStringExtra(ROOM_NAME);
        if (idRoom == null) {
            idRoom = "DEFAULT";
            Log.w(TAG, "Room name not found in this activity's intent! Using " + idRoom);
        }

        if (isIncomingCall) {
            callerId = getIntent().getLongExtra(CALLER_ID, -1);
        } else {
            findViewById(R.id.hangupLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.hangupLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    giveUpCall(v);
                }
            });
            outgoingCallFragment = new OutgoingVideoConferenceCallFragment();
            outgoingCallFragment.setCallbacks(this);
            outgoingCallFragment.setCurrentUser(taskModel.currentUser);
            outgoingCallFragment.setCurrentUserPhotoUrl(mainModel.getUserPhotoUrlFromUser(taskModel.currentUser));
            addFragment(R.id.call_fragment_container, outgoingCallFragment);
        }

        SurfaceViewRenderer localView = (SurfaceViewRenderer)findViewById(R.id.local_video_view);
        // Important: set to true in order for the local video view to always appear on top of the remote video view.
        localView.setZOrderMediaOverlay(true);

        if (!needPermissions()) {
            initWebRTCat();
        }
    }

    private void startConference() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.hangupLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.hangupLayout).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        endCall(v);
                    }
                });
                findViewById(R.id.webrtcatView).setVisibility(View.VISIBLE);
            }
        });
    }

    private void initWebRTCat() {
        webrtcat = new WebRTCat(this, this);
        // Use the username of the current user (Vincles grandpa)
        webrtcat.setUsername(mainModel.currentUser.username);

        WebRTCatPeerConnectionClient.PeerConnectionParameters peerConnectionParameters = new WebRTCatPeerConnectionClient.PeerConnectionParameters(
                true,       /* video call */
                false,      /* loopback */
                false,      /* tracing */
                false,      /* use camera 2 */
                0,          /* video width */
                0,          /* video height */
                0,          /* video fps */
                1000,       /* video bitrate */
                "VP8",      /* video codec */
                true,       /* hwcodec enabled */
                false,      /* capture to texture enabled */
                32,         /* audio bitrate */
                "OPUS",     /* audio codec */
                false,      /* no audio processing enabled */
                false,      /* aec dump enabled */
                false,      /* open sles enabled */
                true,       /* disable built-in AEC (auto echo cancellation) */
                true,       /* disable built-in AGC (auto gain control) */
                true,       /* disable built-in NS (noise suppressor) */
                false);     /* enable level control */

        Uri roomUri = Uri.parse(getUrl());
        webrtcat.connect(roomUri, idRoom, peerConnectionParameters);
    }

    private boolean needPermissions() {
        boolean needPermissions = false;
        for (String permission : MY_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                needPermissions = true;
                break;
            }
        }

        if (needPermissions) {
            Log.d(TAG, "Requesting permissions");
            // User has not granted us all the permissions we need -- request them now.
            ActivityCompat.requestPermissions(this, MY_PERMISSIONS, PERMISSIONS_REQUEST_CODE);
        }

        return needPermissions;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSIONS_REQUEST_CODE) {
            Log.e(TAG, "Unexpected request code: received " + requestCode + ", expecting " + PERMISSIONS_REQUEST_CODE);
            return;
        }

        // We need all permissions to be granted.
        boolean allPermsOk = (grantResults.length == MY_PERMISSIONS.length);
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                allPermsOk = false;
                break;
            }
        }

        if (allPermsOk) {
            initWebRTCat();
        } else {
            // Can't do anything else, so exit the app.
            Log.i(TAG, "Permissions not granted, exiting");
            this.finish();
        }
    }

    public boolean onRoomConnected(String roomId, boolean isInitiator) {
        Log.i(TAG, "*** CONNECTED TO ROOM " + roomId);
        if (isIncomingCall) {
            if (isInitiator) {
                // Invalid state: we expect to receive a call offer (because this activity was started
                // by a notification message) but instead signaling considers as the caller (initiator).
                Log.w(TAG, "Joined room " + roomId + " as callee but signaling thinks we're the caller! Disconnecting...");
                // Just finish (don't show "Lost Call" Activity for now) but add a "Lost Call" feed item.
                finishAndAddLostCallFeedItem();
                return false;
            }
        } else {
            notifyCallee();
            setWebRTCatViews();

            String calleeName = getIntent().getStringExtra("calleeName");
            webrtcat.call(calleeName);
        }
        return true;
    }

    @Override
    public void onIncomingCall() {
        User caller = mainModel.getUser(callerId);
        if (caller != null) {
            String callerPhotoUrl = mainModel.getUserPhotoUrlFromUser(caller);
            incomingCallFragment = new IncomingVideoConferenceCallFragment();
            incomingCallFragment.setCaller(caller);
            incomingCallFragment.setCallerPhotoUrl(callerPhotoUrl);
            incomingCallFragment.setCallbacks(this);

            addFragment(R.id.call_fragment_container, incomingCallFragment);
        } else {
            Log.e(TAG, "Caller with id=" + callerId + " not found");
        }
    }

    @Override
    public void onIncomingCallCancelled() {
        // Caller hung up before we had a chance to accept or reject the call.
        // At this point the webrtcat object is already disconnected.
        Log.i(TAG, "Caller hang up before user could accept or reject");
        // Just finish (don't show "Lost Call" Activity for now) but add a "Lost Call" feed item.
        finishAndAddLostCallFeedItem();
    }

    @Override
    public void onCallConnected() {
        isCallConnected = true;
        if (outgoingCallFragment != null) {
            removeFragment(outgoingCallFragment);
        }
        startConference();
        Log.i(TAG, "Call connected!");
    }

    @Override
    public void onCallOfferFailed() {
        // At this point the webrtcat object is already in disconnected state.
        Log.i(TAG, "Call offer failed, ending activity");
        // show "Lost Call" if the callee rejects the call.
        finishAndShowLostCall();
    }

    @Override
    public void onHangup() {
        Log.i(TAG, "Hangup reported, ending activity");
        finish();
    }

    @Override
    public void onStats(WebRTStats webRTStats) {
        // Ignored for now.
    }

    @Override
    public void onError(WebRTCatErrorCode webRTCatErrorCode) {
        int resId = isCallConnected ? R.string.error_while_video_calling : R.string.error_start_video_calling;
        showErrorMessage(resId, webRTCatErrorCode.getIntCode());
        finishAndShowLostCall();
    }

    @Override
    public void onAcceptCall() {
        removeFragment(incomingCallFragment);
        Toast.makeText(this, getString(R.string.task_call_calling), Toast.LENGTH_SHORT).show();
        setWebRTCatViews();
        webrtcat.acceptCall();
    }

    @Override
    public void onRejectCall() {
        removeFragment(incomingCallFragment);
        webrtcat.rejectCall();
        // rejectCall() doesn't call onHangup() since we never picked up the call to begin with,
        // so just exit now.
        finish();
    }

    @Override
    public void onIncomingCallTimeout() {
        if (!isCallConnected) {
            finishAndShowLostCall();
        }
    }

    @Override
    public void onOutgoingCallTimeout() {
        if (!isCallConnected) {
            finishAndShowLostCall();
        }
    }

    // This method is called by the "Penjar" button to indicate that the user doesn't want to continue making the call.
    public void giveUpCall(View v) {
        webrtcat.hangup();
        // Activity will automatically finish() when onHangup() is called.
    }

    // Called by the "Penjar" button to end an ongoing call.
    public void endCall(View view) {
        webrtcat.hangup();
        // Activity will automatically finish() when onHangup() is called.
    }

    private void setWebRTCatViews() {
        SurfaceViewRenderer localRender = (SurfaceViewRenderer) findViewById(R.id.local_video_view);
        SurfaceViewRenderer remoteRender = (SurfaceViewRenderer) findViewById(R.id.remote_video_view);
        PercentFrameLayout localRenderLayout = (PercentFrameLayout) findViewById(R.id.local_video_layout);
        PercentFrameLayout remoteRenderLayout = (PercentFrameLayout) findViewById(R.id.remote_video_layout);
        webrtcat.setViews(localRender, remoteRender, localRenderLayout, remoteRenderLayout);
    }

    private void addFragment(final int containerId, final Fragment fragment) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try  {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(containerId, fragment);
                    ft.commitAllowingStateLoss();
                } catch (Exception e) {
                    Log.e(TAG, "Unable to add fragment", e);
                }
            }
        });
    }

    private void removeFragment(final Fragment fragment) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(fragment);
                    ft.commitAllowingStateLoss();
                } catch (Exception e) {
                    Log.e(TAG, "Unable to remove fragment", e);
                }
            }
        });
    }

    private void notifyCallee() {
        Long calleeId = getIntent().getLongExtra(CALLEE_ID, -1);
        VideoModel videoModel = VideoModel.getInstance();
        try {
            videoModel.startVideoConference(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Go to detail
                    if (notifSentHandler != null) {
                        notifSentHandler.onSuccess(result);
                    }
                }

                @Override
                public void onFailure(Object error) {
                    String errorMessage = mainModel.getErrorByCode(error);
                    Log.e(TAG, "Unable to send notification: " + errorMessage);
                    showErrorMessage(R.string.error_start_video_calling, ERROR_CODE_CANT_NOTIFY_PEER);
                    if (notifSentHandler != null) {
                        notifSentHandler.onFailure(error);
                    }
                    // We can't proceed with the call if we failed to notify the peer.
                    finishAndShowLostCall();
                }
            }, calleeId, idRoom);
        } catch (IOException e) {
            Log.e(TAG, "Unable to send notification: ", e);
            showErrorMessage(R.string.error_start_video_calling, ERROR_CODE_CANT_NOTIFY_PEER);
            if (notifSentHandler != null) {
                notifSentHandler.onFailure(e);
            }
            // We can't proceed with the call if we failed to notify the peer.
            finishAndShowLostCall();
        }
    }

    private void showErrorMessage(int resourceId, int errorCode) {
        final String errorMessage = getString(resourceId, errorCode);
        Log.e(TAG, errorMessage);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(TaskVideoConferenceCallActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishAndShowLostCall() {
        if (webrtcat != null) {
            webrtcat.disconnect(WebRTCat.DisconnectReason.CLIENT_SHUTDOWN);
        }
        finish();

        // Start the "Missed Call" (aka LostCall) Activity.
        Intent intent = new Intent(this, TaskCallLostCallActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        // Indicate to the Lost Call Activity whether we were in a call or not at the time that we decided to exit.
        intent.putExtra("wasCallConnected", isCallConnected);
        startActivity(intent);
    }

    private void finishAndAddLostCallFeedItem() {
        if (webrtcat != null) {
            webrtcat.disconnect(WebRTCat.DisconnectReason.CLIENT_SHUTDOWN);
        }
        finish();
        FeedModel.getInstance().addLostCall(callerId);
    }

    @Override
    protected void onDestroy() {
        isInVC = false;
        Log.d(TAG, "*** ON DESTROY");
        webrtcat.disconnect(WebRTCat.DisconnectReason.CLIENT_SHUTDOWN);
        super.onDestroy();
    }

    private Window wind;
    @Override
    protected void onResume() {
        TaskVideoConferenceCallActivity.isInVC = true;
        super.onResume();

        /****** block is needed to raise the application if the lock is *********/
        wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        /* ^^^^^^^block is needed to raise the application if the lock is*/
    }

    @Override
    protected void onPause() {
        TaskVideoConferenceCallActivity.isInVC = false;
        super.onPause();
    }

    private String getUrl () {
        switch(VinclesApp.getVinclesApp().getAppFlavour()) {
            case VinclesApp.FLAVOUR_PRE_EURECAT:
                return WEBRTCAT4_URL_PRE_EURECAT;
            case VinclesApp.FLAVOUR_PRE_AZURE:
                return WEBRTCAT4_URL_PRE_AZURE;
            case VinclesApp.FLAVOUR_PRO_AZURE:
                return WEBRTCAT4_URL_PRO_AZURE;
            case VinclesApp.FLAVOUR_PRODUCTION:
            default:
                return WEBRTCAT4_URL_PRE_AZURE;
        }
    }
}