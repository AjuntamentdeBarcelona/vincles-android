/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import cat.bcn.vincles.lib.util.AsyncResponse;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.groups.GroupsChatActivity;
import cat.bcn.vincles.tablet.contracts.OnNotificationFragmentInteractionListener;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;

public class GroupNotificationFragment extends NotificationFragmentTemplate {
    private GroupModel groupModel = GroupModel.getInstance();
    private MainModel mainModel = MainModel.getInstance();
    private VinclesGroup vinclesGroup;

    private View rootView;
    private TextView texName;
    private ImageView imgPhoto;

    public static GroupNotificationFragment newInstance() {
        GroupNotificationFragment fragment = new GroupNotificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (feedItem != null) vinclesGroup = groupModel.getGroup(feedItem.getIdData());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notification_group, container, false);
        texName = (TextView) rootView.findViewById(R.id.texName);
        imgPhoto = (ImageView) rootView.findViewById(R.id.imgPhoto);
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
                groupModel.currentGroup = vinclesGroup;
                // Go to detail
                startActivity(new Intent(getActivity(), GroupsChatActivity.class));
            }
        });

        if (vinclesGroup == null) {
            groupModel.getGroupServer(new AsyncResponse() {
                @Override
                public void onSuccess(Object result) {
                    // Get user list of group added
                    groupModel.getGroupUserServerList(new AsyncResponse() {
                        @Override
                        public void onSuccess(Object result) {
                            vinclesGroup = (VinclesGroup) result;
                            updateView();
                        }

                        @Override
                        public void onFailure(Object error) {
                            String errorMessage = mainModel.getErrorByCode(error);
                            Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }, feedItem.getIdData());
                }

                @Override
                public void onFailure(Object error) {
                    String errorMessage = mainModel.getErrorByCode(error);
                    Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
                    toast.show();
                }
            }, feedItem.getIdData());
        } else {
            updateView();
        }

        return rootView;
    }

    private void updateView() {
        texName.setText(vinclesGroup.name);

        VinclesGroup group = groupModel.getGroup(vinclesGroup.getId());
        if (getActivity() != null && !getActivity().isFinishing())
            Glide.with(this)
                .load(groupModel.getGroupPhotoUrlFromGroupId(group.getId()))
                .into(imgPhoto);
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
