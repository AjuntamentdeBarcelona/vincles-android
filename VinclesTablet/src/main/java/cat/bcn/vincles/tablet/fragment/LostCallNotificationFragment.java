/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.Calendar;
import java.util.Locale;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskVideoConferenceCallActivity;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class LostCallNotificationFragment extends NotificationFragmentTemplate {
    protected final String TAG = "LostCallFragmentNoti";
    private User userFrom;

    public static LostCallNotificationFragment newInstance() {
        LostCallNotificationFragment fragment = new LostCallNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userFrom = MainModel.getInstance().getUser(feedItem.getIdData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_notification_lost_call, container, false);

        // DATE STRINGS:
        String dateString, timeString;
        Calendar cal = Calendar.getInstance();
        cal.setTime(feedItem.getCreated());
        if (DateUtils.isToday(feedItem.getCreated().getTime())) dateString = getString(R.string.today);
        else {
            dateString = VinclesConstants.getDateString(
                    cal.getTime(),
                    getActivity().getResources().getString(R.string.dateSmallformat),
                    new Locale(getActivity().getResources().getString(R.string.locale_language),
                            getActivity().getResources().getString(R.string.locale_country)));
        }
        timeString = VinclesConstants.getDateString(
                cal.getTime(),
                getActivity().getResources().getString(R.string.timeformat),
                new Locale(getActivity().getResources().getString(R.string.locale_language),
                        getActivity().getResources().getString(R.string.locale_country)));

        ((TextView)rootView.findViewById(R.id.texDay)).setText(dateString);
        ((TextView)rootView.findViewById(R.id.texTime)).setText(timeString);


        if (userFrom != null) {
            if (userFrom.idContentPhoto != null) {
                // ADD PICTURE
                ImageView imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);
                if (getActivity() != null && !getActivity().isFinishing())
                    Glide.with(this)
                        .load(MainModel.getInstance().getUserPhotoUrlFromUser(userFrom))
                        .signature(new StringSignature(userFrom.idContentPhoto.toString()))
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgPhoto);
            } else {
                Log.w(TAG, userFrom.alias + " has idContentPhoto null!");
            }
            ((TextView) rootView.findViewById(R.id.texName)).setText(userFrom.alias);
        }


        // ADD BUTTTONS LISTENERS:
        rootView.findViewById(R.id.btnNotificationDiscard)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.discardNotificationFragment();
                    }
                });

        LinearLayout linCall = (LinearLayout) rootView.findViewById(R.id.btnNotificationView);
        if (!userFrom.isDynamizer) {
            linCall.setVisibility(View.VISIBLE);
            linCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskModel taskModel = TaskModel.getInstance();
                    taskModel.currentUser = userFrom;
                    if (taskModel.currentUser != null) {
                        // Invoke video conference start service
                        TaskVideoConferenceCallActivity.startActivityForOutgoingCall(getActivity(), MainModel.getInstance(), taskModel.currentUser, new AsyncResponse() {
                            @Override
                            public void onSuccess(Object result) {
                                mListener.discardNotificationFragment();
                            }

                            @Override
                            public void onFailure(Object error) {

                            }
                        });
                    }
                }
            });
        } else {
            linCall.setVisibility(View.GONE);
        }
        return rootView;
    }
}
