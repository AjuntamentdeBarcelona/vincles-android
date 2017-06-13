/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class IncomingVideoConferenceCallFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private TextView texName;
    private ImageView imgPhoto;
    private User caller;
    private String callerPhotoUrl;
    private Handler checkIfEstablish;
    private View colorCircleView, backgroundCircleView;
    private MediaPlayer mp = null;

    public interface IncomingCallCallbacks {
        void onAcceptCall();
        void onRejectCall();
        void onIncomingCallTimeout();
    }

    private IncomingCallCallbacks callbacks;

    public void setCallbacks(IncomingCallCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setCaller(User caller) {
        this.caller = caller;
    }

    public void setCallerPhotoUrl(String callerPhotoUrl) {
        this.callerPhotoUrl = callerPhotoUrl;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incoming_videoconference_call, container, false);

        texName = (TextView)view.findViewById(R.id.texName);
        imgPhoto = (ImageView)view.findViewById(R.id.imgPhoto);

        texName.setText(getString(R.string.task_notification_videoconference_title, caller.alias));
        colorCircleView = view.findViewById(R.id.colorCircleAnimation);
        backgroundCircleView = view.findViewById(R.id.backgroundCircleAnimation);

        view.findViewById(R.id.btnPickUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onAcceptCall();
            }
        });

        view.findViewById(R.id.btnHangUp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onRejectCall();
            }
        });

        if (caller != null) {
            if (caller.idContentPhoto != null) {
                if (getActivity() != null && !getActivity().isFinishing())
                    Glide.with(this)
                        .load(callerPhotoUrl)
                        .signature(new StringSignature(caller.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgPhoto);
            } else {
                Log.w(TAG, caller.alias + " has idContentPhoto null!");
            }
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        stopMediaPlayer();
        mp = MediaPlayer.create(this.getActivity(), R.raw.ring);
        mp.setLooping(true);
        mp.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopMediaPlayer();
    }

    private void stopMediaPlayer() {
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
                mp.release();
            }
            mp = null;
        }
    }

    private Window wind;
    @Override
    public void onResume() {
        super.onResume();
        if (checkIfEstablish != null) checkIfEstablish.removeCallbacksAndMessages(null);
        checkIfEstablish = new Handler();
        checkIfEstablish.postDelayed(new Runnable() {
            @Override
            public void run() {
            callbacks.onIncomingCallTimeout();
            }
        }, (VinclesTabletConstants.VC_INCOMING_CALL_TIMEOUT_SECS * 1000));

        startLoopAnimaton();

        /****** block is needed to raise the application if the lock is *********/
        wind = this.getActivity().getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        /* ^^^^^^^block is needed to raise the application if the lock is*/
    }

    @Override
    public void onPause() {
        super.onPause();
        if (checkIfEstablish != null) {
            checkIfEstablish.removeCallbacksAndMessages(null);
            checkIfEstablish = null;
        }
    }

    private void startLoopAnimaton() {
        final AnimatorSet growCircle1 = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.call_pulse_circle1);
        growCircle1.setTarget(colorCircleView);

        final AnimatorSet userAnimation = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.call_user_rotate);
        userAnimation.setTarget(imgPhoto);

        final AnimatorSet growCircle2 = (AnimatorSet) AnimatorInflater.loadAnimator(getActivity(), R.animator.call_pulse_circle1);
        growCircle2.setTarget(backgroundCircleView);
        growCircle2.setStartDelay(500);

        growCircle1.start();
        growCircle2.start();

        growCircle1.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                growCircle1.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        growCircle2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                growCircle2.setStartDelay(0);
                growCircle2.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        userAnimation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                backgroundCircleView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }
}
