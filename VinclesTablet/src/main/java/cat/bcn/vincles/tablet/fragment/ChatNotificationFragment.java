/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.Chat;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.groups.GroupsChatActivity;
import cat.bcn.vincles.tablet.activity.groups.GroupsDinamizerChatActivity;
import cat.bcn.vincles.tablet.contracts.OnNotificationFragmentInteractionListener;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class ChatNotificationFragment extends NotificationFragmentTemplate {
    private static final String TAG = "ChatNotFragment";
    private String jsonChat;
    private Chat chat;
    private GroupModel groupModel = GroupModel.getInstance();
    private TaskModel taskModel = TaskModel.getInstance();
    private VinclesGroup group;
    private boolean isChatFromDynamizer;

    private OnNotificationFragmentInteractionListener mListener;

    public ChatNotificationFragment() {
        // Required empty public constructor
    }

    public static ChatNotificationFragment newInstance() {
        ChatNotificationFragment fragment = new ChatNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (feedItem != null && feedItem.getIdData() != null) {
            chat = groupModel.getChat(feedItem.getIdData());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView;
        if (chat != null) {
            rootView = inflater.inflate(R.layout.fragment_notification_chat, container, false);

            TextView texName = (TextView) rootView.findViewById(R.id.texName);
            texName.setText(chat.text);

            isChatFromDynamizer = false;
            if (chat.idChat != null) {
                group = groupModel.getGroupByChat(chat.idChat);
                if (group == null) {
                    group = groupModel.getGroupByDynamizerChat(chat.idChat);
                    if (group != null) {
                        isChatFromDynamizer = true;
                    }
                }
            }
            if (group != null) {
                TextView texMessageType = (TextView) rootView.findViewById(R.id.texMessageType);
                if (!isChatFromDynamizer) {
                    texMessageType.setText(getString(R.string.main_notification_chat_title, group.name));
                } else {
                    texMessageType.setText(getString(R.string.main_notification_chat_title_dynamizer, group.name));
                }

                ImageView imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);
                if (getActivity() != null && !getActivity().isFinishing())
                    Glide.with(this)
                        .load(groupModel.getGroupPhotoUrlFromGroupId(group.getId()))
//                    .signature(new StringSignature(group.idContentPhoto.toString())) // TODO: GROUPS CAN'T UPDATE PICTURES
                        .into(imgPhoto);
            }
            // buttons
            View discard = (View)rootView.findViewById(R.id.btnNotificationDiscard);
            discard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.discardNotificationFragment();
                }
            });

            View accept = (View) rootView.findViewById(R.id.btnNotificationView);
            accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.discardNotificationFragment();
                    groupModel.currentGroup = group;
                    // Go to detail
                    if (!isChatFromDynamizer) {
                        startActivity(new Intent(getActivity(), GroupsChatActivity.class));
                    } else {
                        startActivity(new Intent(getActivity(), GroupsDinamizerChatActivity.class));
                    }
                }
            });
        } else {
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
