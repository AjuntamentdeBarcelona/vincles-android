/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import cat.bcn.vincles.lib.vo.FeedItem;
import cat.bcn.vincles.tablet.contracts.OnNotificationFragmentInteractionListener;
import cat.bcn.vincles.tablet.model.FeedModel;

public class NotificationFragmentTemplate extends Fragment {
    private static final String FEEDITEM_ID = "param_feeditem_id";
    protected FeedItem feedItem;
    protected OnNotificationFragmentInteractionListener mListener;

    public NotificationFragmentTemplate() {
        // Required empty public constructor
    }

    public void setFeedItem(FeedItem item) {
        Bundle args = new Bundle();
        args.putLong(FEEDITEM_ID, item.getId());
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long feedItemId = -1;
        if (getArguments() != null)
            feedItemId = getArguments().getLong(FEEDITEM_ID);

        // NOT DEFAULT FRAGMENT
        if (feedItemId > 0) {
            feedItem = FeedModel.getInstance().getItemWithId(feedItemId);
            if (feedItem == null) mListener.discardNotificationFragment();
        }
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
