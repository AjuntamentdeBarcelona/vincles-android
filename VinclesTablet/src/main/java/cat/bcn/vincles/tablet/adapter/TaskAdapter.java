/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Task;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.component.TaskItemView;
import cat.bcn.vincles.tablet.model.MainModel;

public class TaskAdapter extends FixedItemsAdapterTemplate<Task> {
    private final String TAG = this.getClass().getSimpleName();

    public TaskAdapter(Context context, int resource, List<Task> objects, int rowHeight, int mMAX_ITEMS) {
        super(context, resource, objects, rowHeight, mMAX_ITEMS);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // DO NOT CRASH IN VOID LIST MESSAGES
        if (itemsList.size() <= 0) {
            rowView = new TaskItemView(getContext(), 0);
            rowView.setVisibility(View.GONE);
            return rowView;
        }

        // DO NOT CRASH IN LAST ITEMS
        int realListPosition = getRealMessagePosition(position);
        if (realListPosition >= itemsList.size()) realListPosition = itemsList.size() - 1;

        Task item = itemsList.get(realListPosition);
        if (item == null) return null;

        // GET ROW HEIGHT
        int viewRows = getFullRowSpace(position);

        // IS SHOWED NOT INTO THIS BUNDLE?
        if (!isShowed(position)) viewRows = 0;

        if (rowView == null|| rowView.getTag() == null) {
            rowView = new TaskItemView(mContext, itemHeight);

            ViewHolder viewHolder = new ViewHolder();
            // Lookup view for data population
            viewHolder.imgPhoto = (ImageView) rowView.findViewById(R.id.imgPhoto);
            viewHolder.texName = (TextView) rowView.findViewById(R.id.texName);
            viewHolder.texMessage = (TextView) rowView.findViewById(R.id.texMessage);
            viewHolder.texTimeFrom = (TextView) rowView.findViewById(R.id.texTimeFrom);
            viewHolder.texTimeTo = (TextView) rowView.findViewById(R.id.textimeTo);
            viewHolder.texStatus = (TextView) rowView.findViewById(R.id.texStatus);
            rowView.setTag(viewHolder);
        }

        // SET VIEW HEIGHT
        ((TaskItemView)rowView).setHeight(viewRows*itemHeight);

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Populate the data into the template view using the data object
        holder.texMessage.setText(item.description);
        holder.texTimeFrom.setText(VinclesConstants.getDateString(item.getDate(), getContext().getString(R.string.timeformat),
                new Locale(getContext().getString(R.string.locale_language), getContext().getString(R.string.locale_country)))
        );
        holder.texTimeTo.setText(VinclesConstants.getDateString(new Date(item.getDate().getTime() + 60000*item.duration), getContext().getString(R.string.timeformat),
                new Locale(getContext().getString(R.string.locale_language), getContext().getString(R.string.locale_country)))
        );
        switch (item.state) {
            case Task.STATE_ACCEPTED: holder.texStatus.setText(getContext().getString(R.string.task_calendar_status_accept)); break;
            case Task.STATE_PENDING: holder.texStatus.setText(getContext().getString(R.string.task_calendar_status_pending)); break;
            case Task.STATE_REJECTED: holder.texStatus.setText(getContext().getString(R.string.task_calendar_status_rejected)); break;
        }

        // Populate the data into the template view using the data object
        if (item.owner != null) {
            holder.texName.setText(item.owner.alias);
            if (item.owner.idContentPhoto != null) {
                try {
                    Glide.with(getContext())
                            .load(MainModel.getInstance().getUserPhotoUrlFromUser(item.owner))
                            .signature(new StringSignature(item.owner.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(holder.imgPhoto);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                Log.w(TAG, item.owner.alias + " has idContentPhoto null!");
            }
        }

        // Return the completed view to render on screen
        return rowView;
    }

    static class ViewHolder {
        public LinearLayout layout;
        public ImageView imgPhoto;
        public TextView texMessage;
        public TextView texTimeFrom;
        public TextView texTimeTo;
        public TextView texStatus;
        public TextView texName;
    }
}