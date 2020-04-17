package cat.bcn.vincles.mobile.UI.Calls;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.Uri;
import android.util.Log;
import android.view.Display;

import net.i2cat.seg.webrtcat4.WebRTCat;
import net.i2cat.seg.webrtcat4.WebRTCatErrorCode;
import net.i2cat.seg.webrtcat4.WebRTCatParams;
import net.i2cat.seg.webrtcat4.stats.WebRTStats;

import org.appspot.apprtc.AppRTCAudioManager;
import org.appspot.apprtc.PercentFrameLayout;
import org.webrtc.SurfaceViewRenderer;

import cat.bcn.vincles.mobile.BuildConfig;

import cat.bcn.vincles.mobile.Client.Enviroment.Environment;
import cat.bcn.vincles.mobile.Client.NetworkUsage.DataUsageUtils;
import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.MyApplication;

public class CallsWebRTCatManager implements WebRTCat.WebRTCatCallbacks {

    private static final String videoCodec = "VP8";
    private static final String audioCodec = "OPUS";
    private static final boolean hwCodecEnabled = true;
    private static final boolean captureToTextureEnabled = false;
    private static final boolean noAudioProcessing = false;
    private static final boolean aecDump = false;
    private static final boolean openSLES = false;
    private static final boolean displayHud = false;
    private static final boolean tracing = false;
    private static final boolean disableAEC = true;
    private static final boolean disableAGC = true;
    private static final boolean disableNS = true;
    private static final boolean enableLevelControl = false;
    private static final int startVideoBitrate = 70;
    private static final int startAudioBitrate = 32;
    private static final int videoFPS = 20;
    private static final int videoWidth = 300;
    private static final int videoHeight = 300;
    private Uri roomUri;

    private String roomId, callerName, calleeName;
    private boolean isIncomingCall;
    private int callerId, calleeId;
    private boolean callerIsVincles, calleeIsVincles;

    private WebRTCat webRTCat;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;

    CallsCallbacks listener;

    boolean callConnectedDone = false;

    private Activity activity;

    private DataUsageUtils dataUsageUtils;
    long inAudio = 0;
    long inVideo = 0;
    long outAudio = 0;
    long outVideo = 0;

    public Boolean isCalling = false;

    public CallsWebRTCatManager(int callerId, int calleeId, String callerName, String calleeName,
                                boolean callerIsVincles, boolean calleeIsVincles,
                                boolean isIncomingCall) {
        this.callerId = callerId;
        this.calleeId = calleeId;
        this.callerName = callerName;
        this.calleeName = calleeName;
        this.callerIsVincles = callerIsVincles;
        this.calleeIsVincles = calleeIsVincles;
        this.isIncomingCall = isIncomingCall;
        roomUri = Uri.parse(Environment.getVcBaseUrl());
        this.dataUsageUtils = new DataUsageUtils();
    }

    public void setListener(CallsCallbacks listener) {
        this.listener = listener;
    }

    public void connectWebRTCat(Activity activity, String roomId) {
        this.activity = activity;
        webRTCat = new WebRTCat(activity, this);
        isCalling = true;

        String myUsername;
        if (isIncomingCall) {
            myUsername = calleeName;
        } else {
            myUsername = callerName;
        }
        webRTCat.setUsername(myUsername);


        if (isIncomingCall) {
            this.roomId = roomId;
        } else {
            this.roomId = generateRoomId();
            Log.d("roomId", this.roomId);
        }


        Display display = this.activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;


        WebRTCatParams webrtcatParams = new WebRTCatParams(roomUri.toString(), this.roomId);
        webrtcatParams.setAudioCodec(audioCodec);
        webrtcatParams.setAudioStartingBitrate(startAudioBitrate);
        webrtcatParams.setVideoCodec(videoCodec);
        webrtcatParams.setVideoWidth(100);
        webrtcatParams.setVideoHeight(100 * (height / width));
        webrtcatParams.setVideoFps(videoFPS);
        webrtcatParams.setVideoStartingBitrate(startVideoBitrate);
        webRTCat.connect(webrtcatParams);
    }

    public void setSurfaceRenderers(SurfaceViewRenderer local, SurfaceViewRenderer remote,
                                    PercentFrameLayout localPFL, PercentFrameLayout remotePFL) {
        localRender = local;
        remoteRender = remote;
        localRenderLayout = localPFL;
        remoteRenderLayout = remotePFL;
        local.setZOrderMediaOverlay(true);
    }

    private String generateRoomId() {
        return "android-" + (callerIsVincles ? "vin" : "xp") + "-" + callerId + "-" +
                (calleeIsVincles ? "vin" : "xp") + "-" + calleeId + "-"
                + System.currentTimeMillis();
    }


    void hangUpPressed() {
        webRTCat.hangup();
    }
    void cameraSwitchPressed() {

    }
    boolean toggleAudioMutePressed() {
        return false;
    }

    public void onAcceptCall() {

        webRTCat.setViews(localRender, remoteRender, localRenderLayout, remoteRenderLayout);
        webRTCat.acceptCall();
    }

    public void onRejectCall() {
        webRTCat.rejectCall();
        // rejectCall() doesn't call onHangup() since we never picked up the call to begin with
    }


    @Override
    public boolean onRoomConnected(String roomId, boolean isInitiator) {
        Log.d("callvid", "room connected! "+roomId);
        if (isIncomingCall) {
            if (isInitiator) {
                // Invalid state: we expect to receive a call offer (because this activity was started
                // by a notification message) but instead signaling considers as the caller (initiator).
                Log.w(CallsWebRTCatManager.class.getName(), "Joined room " + roomId + " as callee but signaling thinks we're the caller! Disconnecting...");
                // Nothing else to do but finish this activity (we'll disconnect on onDestroy()).
                //todo finish();
                if (this.activity != null) {
                     this.activity.finish();
                }
                return false;
            }
        } else {
            webRTCat.setViews(localRender, remoteRender, localRenderLayout, remoteRenderLayout);
            webRTCat.call(calleeName);
            // Notify callee only when we are already in the room.
            if (listener != null) {
                Log.d("callvid", "onRoomConnected sendNotif roomId:"+roomId);
                listener.sendCallNotification(calleeId, roomId);
            }
        }
        return true;
    }

    @Override
    public void onIncomingCall() {
        Log.d("callvid", "onIncomingCall");
    }

    @Override
    public void onIncomingCallCancelled() {
        Log.d("callvid", "onIncomingCallCancelled");
        // At this point the webrtcat object is already in disconnected state.
        String msg = "Missed call from Unknown caller";
        if (callerName != null) {
            msg = "Missed call from " + callerName;
        }
        if (listener != null) listener.onIncomingCallCancelled();
        isCalling = false;

    }

    @Override
    public void onCallConnected() {
        callConnectedDone = true;
        Log.d("callvid", "onCallConnected");
        if (isIncomingCall) {
            if (listener != null) {
                listener.onIncomingCallConnected();
            }
        } else {
            if (listener != null) {
                listener.onOutgoingCallConnected();
            }
        }
        isCalling = true;

        //todo cancel timeout
    }

    @Override
    public void onCallOfferFailed() {
        isCalling = false;

        Log.d("callvid", "onCallOfferFailed");
        if (listener != null) listener.onCallOfferFailed();
    }

    @Override
    public void onHangup() {
        Log.d("callvid", "onHangup");
        isCalling = false;

        dataUsageUtils.addCallDataUsage(roomId,inAudio,inVideo,outAudio,outVideo);
        if (listener != null) listener.onHangup();
    }

    @Override
    public void onStats(WebRTStats stats) {
        Log.d("callvid", "onStats");

        Log.d("onStats", "onStats");
        Log.d("onStats InAudio", String.valueOf(stats.getInAudioStatsBytes()));
        Log.d("onStats InVideo", String.valueOf(stats.getInVideoStatsBytes()));
        Log.d("onStats OutAudio", String.valueOf(stats.getOutAudioStatsBytes()));
        Log.d("onStats OutVideo", String.valueOf(stats.getOutVideoStatsBytes()));

        inAudio += inAudio + stats.getInAudioStatsBytes();
        inVideo += inVideo + stats.getInVideoStatsBytes();
        outAudio += outAudio + stats.getOutAudioStatsBytes();
        outVideo += outVideo + stats.getOutVideoStatsBytes();

    }

    @Override
    public void onError(WebRTCatErrorCode errCode) {
        isCalling = false;

        if (callConnectedDone) {
            if (listener != null) listener.onError(MyApplication.getAppContext().getResources()
                    .getString(R.string.calls_error_during_call));
        } else {
            if (listener != null) listener.onError(MyApplication.getAppContext().getResources()
                    .getString(R.string.calls_error_before_call));
        }
        if ((errCode.getIntCode() == 101 || errCode.getIntCode() == 102) && listener != null)
            listener.onNotifyError(calleeId, roomId);
        /*switch (errCode.getIntCode()) {
            case 101:
            case 102:
            case 103:
                if (listener != null) listener.onError(MyApplication.getAppContext().getResources()
                        .getString(R.string.calls_error1));
                break;
            case 201:
            case 202:
                if (listener != null) listener.onError(MyApplication.getAppContext().getResources()
                        .getString(R.string.calls_error2));
                break;

            case 104:
                if (!callConnectedDone) {
                    if (listener != null) listener.onError(MyApplication.getAppContext().getResources()
                            .getString(R.string.calls_error1));
                } else {
                    if (listener != null) listener.onError(MyApplication.getAppContext().getResources()
                            .getString(R.string.calls_error2));
                }
                break;
            case 105:
            case 301:
            case 302:
            case 500:
            default:
                if (listener != null) listener.onError(null);

        }*/

        Log.d("callvid", "CALL onError:"+errCode);
        dataUsageUtils.addCallDataUsage(roomId,inAudio,inVideo,outAudio,outVideo);
    }

    interface CallsCallbacks {
        void sendCallNotification(int idUser, String idRoom);
        void onIncomingCallConnected();
        void onOutgoingCallConnected();
        void onIncomingCallCancelled();
        void onCallOfferFailed();
        void onHangup();
        void onError(String error);
        void onNotifyError(int idUser, String idRoom);
    }
}
