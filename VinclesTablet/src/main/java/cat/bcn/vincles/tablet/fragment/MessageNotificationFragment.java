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
import cat.bcn.vincles.lib.dao.MessageDAOImpl;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.Resource;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMessageDetailActivity;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class MessageNotificationFragment extends NotificationFragmentTemplate {
    private static final String TAG = "MessageNotificationF";
    private Message message;
    private User userFrom;
    private TaskModel taskModel;

    public static MessageNotificationFragment newInstance() {
        MessageNotificationFragment fragment = new MessageNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        taskModel = TaskModel.getInstance();

        if (feedItem != null) message = taskModel.getMessage(feedItem.getIdData());
        if (message != null) {
            userFrom = MainModel.getInstance().getUser(message.idUserFrom);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        if (message != null) {
            rootView = inflater.inflate(R.layout.fragment_notification_message, container, false);

            // FILL DATA:
            TextView messageTitle = (TextView) rootView.findViewById(R.id.texMessageType);
            if (message.metadataTipus != null) {
                switch (message.metadataTipus) {
                    case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                        messageTitle.setText(getString(R.string.main_notification_message_new_text));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                        messageTitle.setText(getString(R.string.main_notification_message_new_image));
                        break;
                    case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                        messageTitle.setText(getString(R.string.main_notification_message_new_video));
                        break;
                }
            } else {
                messageTitle.setText(getString(R.string.main_notification_message_new_text));
            }

            if (userFrom != null)
                ((TextView)rootView.findViewById(R.id.texName)).setText(userFrom.alias);

            // DATE STRINGS:
            String dateString, timeString;
            Calendar cal = Calendar.getInstance();
            cal.setTime(message.sendTime);
            if (DateUtils.isToday(message.sendTime.getTime())) dateString = getString(R.string.today);
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

            // ADD PICTURE
            ImageView imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);
            if (userFrom != null) {
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
            if (message != null) {
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
                                // UPDATE MESSAGES WITH RESOURCES AND LAUNCH
                                if (MainModel.getInstance().avoidServerCalls) {
                                    taskModel.setCurrentMessage(message);
                                    startActivity(new Intent(getActivity(), TaskMessageDetailActivity.class));
                                    mListener.discardNotificationFragment();
                                } else {
                                    TaskModel.getInstance().getMessageServerList(new AsyncResponse() {
                                        @Override
                                        public void onSuccess(Object result) {
                                            taskModel.setCurrentMessage(message);
                                            startActivity(new Intent(getActivity(), TaskMessageDetailActivity.class));
                                            mListener.discardNotificationFragment();
                                        }

                                        @Override
                                        public void onFailure(Object error) {
                                            taskModel.setCurrentMessage(message);
                                            startActivity(new Intent(getActivity(), TaskMessageDetailActivity.class));
                                            mListener.discardNotificationFragment();
                                        }
                                    }, "", "");
                                }
                            }
                        });
            }
        }
        else {
            rootView = inflater.inflate(R.layout.fragment_notification_default, container, false);
            // Delete pushMessage
            mListener.discardNotificationFragment();
        }
        return rootView;
    }
}
