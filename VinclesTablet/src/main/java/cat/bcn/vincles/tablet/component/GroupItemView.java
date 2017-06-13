/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.component;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import cat.bcn.vincles.tablet.activity.groups.GroupsListActivity;

public class GroupItemView extends LinearLayout {

    public GroupItemView(Context context, int layoutRes) {
        super(context);
        inflate(getContext(), layoutRes, this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getResources().getDisplayMetrics().heightPixels / GroupsListActivity.GROUPS_SCREEN_DIVIDER;
        super.onMeasure(widthMeasureSpec, View.MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}