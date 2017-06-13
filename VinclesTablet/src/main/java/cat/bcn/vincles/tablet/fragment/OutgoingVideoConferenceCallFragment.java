/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.Timer;
import java.util.TimerTask;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class OutgoingVideoConferenceCallFragment extends Fragment {
    private ViewGroup micDotsLayout;
    private View dots[];
    private int activeDot = 0;
    private int callTime = 0;
    private Timer timer;

    private ImageView imgPhoto;
    private User currentUser;
    private String currentUserPhotoUrl;

    private OutgoingCallCallbacks callbacks;

    public interface OutgoingCallCallbacks {
        void onOutgoingCallTimeout();
    }

    public void setCallbacks(OutgoingCallCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void setCurrentUserPhotoUrl(String currentUserPhotoUrl) {
        this.currentUserPhotoUrl = currentUserPhotoUrl;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_outgoing_videoconference_call, container, false);

        micDotsLayout = (ViewGroup)view.findViewById(R.id.mic_dots);
        int dotsLength = micDotsLayout.getChildCount()-1;
        dots = new View[dotsLength];
        activeDot = dotsLength-1;
        for (int i = 0; i < dotsLength; i++) {
            dots[i] = micDotsLayout.getChildAt(i);
        }

        imgPhoto = (ImageView)view.findViewById(R.id.imgPhoto);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Window wind;
    @Override
    public void onResume() {
        super.onResume();
        startDotsAnimation();

        if (getActivity() != null && !getActivity().isFinishing())
            Glide.with(this)
                .load(currentUserPhotoUrl)
                .signature(new StringSignature(currentUser.idContentPhoto.toString()))
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(imgPhoto);

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
        stopDotsAnimation();
    }

    private void stopDotsAnimation() {
        if (timer != null) {
            timer.purge();
            timer.cancel();
            timer = null;
        }
    }

    private void startDotsAnimation() {
        callTime = 0;
        stopDotsAnimation();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dots[activeDot].setBackgroundResource(R.drawable.button_circle_grey);
                        if (activeDot >= dots.length-1) activeDot = 0;
                        else activeDot++;
                        dots[activeDot].setBackgroundResource(R.drawable.button_circle_red);

                        callTime++;
                        if (callTime >= VinclesTabletConstants.VC_OUTGOING_CALL_TIMEOUT_SECS) {
                            callbacks.onOutgoingCallTimeout();
                        }
                    }
                });
            }
        }, 0, 1000);
    }
}
