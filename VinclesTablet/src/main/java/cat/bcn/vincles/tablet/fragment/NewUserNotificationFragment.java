/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.Calendar;
import java.util.Locale;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskNetworkListActivity;
import cat.bcn.vincles.tablet.model.MainModel;

public class NewUserNotificationFragment extends NotificationFragmentTemplate {
    protected final String TAG = "LostCallFragmentNoti";
    private User userFrom;
    private View rootView;

    public static NewUserNotificationFragment newInstance() {
        NewUserNotificationFragment fragment = new NewUserNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (feedItem != null) userFrom = MainModel.getInstance().getUser(feedItem.getIdData());
    }

    public void refreshUser() {
        if (rootView != null) {
            userFrom = MainModel.getInstance().getUser(feedItem.getIdData());
            // ADD PICTURE
            ImageView imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);

            if (userFrom != null) {
                ((TextView) rootView.findViewById(R.id.texName)).setText(userFrom.alias);
                if (userFrom.idContentPhoto != null) {
                    Glide.with(this)
                            .load(MainModel.getInstance().getUserPhotoUrlFromUser(userFrom))
                            .signature(new StringSignature(userFrom.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(imgPhoto);
                } else {
                    Log.w(TAG, userFrom.alias + " has idContentPhoto null!");
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notification_new_user, container, false);

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

        // ADD PICTURE
        ImageView imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);
        if (userFrom != null) {
            ((TextView)rootView.findViewById(R.id.texName)).setText(userFrom.alias);
            if (userFrom.idContentPhoto != null) {
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
        }

        // ADD BUTTTONS LISTENERS:
        rootView.findViewById(R.id.btnNotificationDiscard)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.discardNotificationFragment();
                    }
                });

        rootView.findViewById(R.id.btnNotificationView)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), TaskNetworkListActivity.class);
                        startActivity(i);
                        mListener.discardNotificationFragment();
                    }
                });

        return rootView;
    }
}
