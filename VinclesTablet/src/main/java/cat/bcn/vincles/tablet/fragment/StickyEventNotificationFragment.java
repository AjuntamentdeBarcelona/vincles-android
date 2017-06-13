/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import java.util.Date;
import java.util.Locale;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskCalendarActivity;
import cat.bcn.vincles.tablet.contracts.OnNotificationFragmentInteractionListener;
import cat.bcn.vincles.tablet.model.FeedModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class StickyEventNotificationFragment extends NotificationFragmentTemplate {
    private final String TAG = this.getClass().getSimpleName();
    private TaskModel taskModel = TaskModel.getInstance();
    private MainModel mainModel = MainModel.getInstance();
    private User userOwner;

    public static StickyEventNotificationFragment newInstance() {
        StickyEventNotificationFragment fragment = new StickyEventNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userOwner = mainModel.getUser(feedItem.getExtraId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        if (taskModel.getTAskById(feedItem.getIdData()) != null) {  // IF IT IS NOT DELETED
            rootView = inflater.inflate(R.layout.fragment_notification_sticky_event, container, false);
            // FILL DATA:
            ((TextView)rootView.findViewById(R.id.texDescription)).setText(feedItem.getSubtext());

            // DATE STRINGS:
            String dateString, timeString;
            Date itemDate = new Date(feedItem.getItemDate());
            Calendar cal = Calendar.getInstance();
            cal.setTime(itemDate);
            if (DateUtils.isToday(itemDate.getTime())) dateString = getString(R.string.today);
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

            ((TextView)rootView.findViewById(R.id.texMessageDay)).setText(dateString);
            ((TextView)rootView.findViewById(R.id.texMessageTime)).setText(timeString);

            // Add picture
            ImageView imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);
            if (userOwner != null) {
                if (userOwner.idContentPhoto != null) {
                    if (getActivity() != null && !getActivity().isFinishing())
                        Glide.with(this)
                            .load(mainModel.getUserPhotoUrlFromUser(userOwner))
                            .signature(new StringSignature(userOwner.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(imgPhoto);
                } else {
                    Log.w(TAG, userOwner.alias + " has idContentPhoto null!");
                }
            } else {
                imgPhoto.setImageResource(R.drawable.user);
            }

            LinearLayout btnEventDiscard = (LinearLayout) rootView.findViewById(R.id.btnEventDiscard);
            LinearLayout btnEventConfirm = (LinearLayout) rootView.findViewById(R.id.btnEventConfirm);

            // Button Listener
            btnEventDiscard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Discard notification
                    if (FeedModel.checkEventTime(taskModel.getTAskById(feedItem.getIdData()))) {
                        final Dialog dialog = new Dialog(getActivity(), R.style.DialogCustomTheme);
                        dialog.setContentView(R.layout.alert_dialog_template);
                        dialog.setCanceledOnTouchOutside(true);
                        dialog.show();

                        TextView alertText = (TextView)dialog.findViewById(R.id.item_message_title);
                        alertText.setText(R.string.cannot_delete_event);
                        View close_btn = dialog.findViewById(R.id.btnClose);
                        close_btn.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                    mListener.discardNotificationFragment();
                }
            });

            btnEventConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go to diary
                    Intent i = new Intent(getActivity(), TaskCalendarActivity.class);
                    i.putExtra("date", new Date(feedItem.getItemDate()).getTime());
                    startActivity(i);
                    mListener.discardNotificationFragment();
                }
            });
        }
        else {
            rootView = inflater.inflate(R.layout.fragment_notification_default, container, false);
            // Delete pushMessage
            mListener.discardNotificationFragment();
        }
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNotificationFragmentInteractionListener) {
            mListener = (OnNotificationFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnNotificationFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
