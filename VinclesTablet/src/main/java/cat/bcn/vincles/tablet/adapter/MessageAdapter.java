/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import cat.bcn.vincles.lib.util.FontCache;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.Message;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.util.VinclesTabletConstants;

public class MessageAdapter extends ArrayAdapter<Message> {
    private final String TAG = this.getClass().getSimpleName();
    protected int itemHeight = 0;
    private Context mContext;
    private LayoutInflater inflater;
    private Typeface tf;
    public MessageAdapter(Context context, int resource, List<Message> objects) {
        super(context, resource, objects);
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tf = FontCache.get(VinclesConstants.TYPEFACE.BOLD, context);

    }

    public void setFixedItemHeight(int pFixedItemHeight) {
        itemHeight = pFixedItemHeight;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message item = getItem(position);
        MainModel mainModel = MainModel.getInstance();
        View rowView = convertView;

        if (rowView == null) {

            if (mainModel != null && mainModel.theme.equals(VinclesTabletConstants.FOSCA_THEME))
                rowView = inflater.inflate(R.layout.item_list_message_fosca, parent, false);
            else
                rowView = inflater.inflate(R.layout.item_list_message_clara, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            // Lookup view for data population
            viewHolder.imgPhoto = (ImageView) rowView.findViewById(R.id.item_message_photo);
            viewHolder.texType = (TextView) rowView.findViewById(R.id.item_message_type);
            viewHolder.texTitle = (TextView) rowView.findViewById(R.id.item_message_title);
            viewHolder.texFullName = (TextView) rowView.findViewById(R.id.item_message_fullname);
            viewHolder.texDay = (TextView) rowView.findViewById(R.id.item_message_day);
            viewHolder.texTime = (TextView) rowView.findViewById(R.id.item_message_time);
            viewHolder.layout = (LinearLayout) rowView.findViewById(R.id.layout);
            // Custom Font
            viewHolder.texType.setTypeface(tf);
            viewHolder.texTitle.setTypeface(tf);
            viewHolder.texFullName.setTypeface(tf);
            viewHolder.texDay.setTypeface(tf);
            viewHolder.texTime.setTypeface(tf);
            rowView.setTag(viewHolder);
        }

        // Set fixed height if it is
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (itemHeight != 0) rowView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight));
        }
        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();

        // Populate the data into the template view using the data object
        if (item.userFrom != null) {
            if (item.userFrom.idContentPhoto != null) {
                try {
                    Glide.with(getContext())
                            .load(mainModel.getUserPhotoUrlFromUser(item.userFrom))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(holder.imgPhoto);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                Log.w(TAG, item.userFrom.alias + " has idContentPhoto null!");
            }
            holder.texFullName.setText(item.userFrom.name);
        } else holder.imgPhoto.setImageResource(R.drawable.user);

        switch (item.metadataTipus) {
            case VinclesConstants.RESOURCE_TYPE.VIDEO_MESSAGE:
                holder.texType.setText(R.string.task_message_video);
                break;
            case VinclesConstants.RESOURCE_TYPE.AUDIO_MESSAGE:
                holder.texType.setText(R.string.task_message_audio);
                break;
            case VinclesConstants.RESOURCE_TYPE.TEXT_MESSAGE:
                holder.texType.setText(R.string.task_message_text);
                break;
            case VinclesConstants.RESOURCE_TYPE.IMAGES_MESSAGE:
                holder.texType.setText(R.string.task_message_image);
                break;
            default:
                holder.texType.setText(item.metadataTipus);
                break;
        }

        if (item.watched) {
            holder.texTitle.setVisibility(View.GONE);
            if (mainModel != null && mainModel.theme.equals(VinclesTabletConstants.FOSCA_THEME)) {
                holder.layout.setBackgroundResource(R.drawable.item_background_fosca_def);
                holder.texFullName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.texDay.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                holder.texTime.setTextColor(ContextCompat.getColor(mContext, R.color.white));

            }
            else {
                holder.layout.setBackgroundResource(R.drawable.item_background_clara_def);
            }

        }
        else {
            holder.texTitle.setVisibility(View.VISIBLE);
            if (mainModel != null && mainModel.theme.equals(VinclesTabletConstants.FOSCA_THEME)) {
                holder.layout.setBackgroundResource(R.drawable.item_background_fosca_notread);
                holder.texFullName.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                holder.texDay.setTextColor(ContextCompat.getColor(mContext, R.color.black));
                holder.texTime.setTextColor(ContextCompat.getColor(mContext, R.color.black));
            }
            else {
                holder.layout.setBackgroundResource(R.drawable.item_background_clara_notread);
            }
        }

        Date now = new Date(System.currentTimeMillis());
        if (now.getDate() == item.sendTime.getDate()) {
            holder.texDay.setText(mContext.getString(R.string.task_message_today));
        } else {
            holder.texDay.setText(VinclesConstants.getDateString(item.sendTime, getContext().getResources().getString(R.string.dateLargeformat), new Locale(getContext().getResources().getString(R.string.locale_language), getContext().getResources().getString(R.string.locale_country))));
        }
        holder.texTime.setText(VinclesConstants.getDateString(item.sendTime, getContext().getResources().getString(R.string.timeformat), new Locale(getContext().getResources().getString(R.string.locale_language), getContext().getResources().getString(R.string.locale_country))));

        return rowView;
    }

    static class ViewHolder {
        public LinearLayout layout;
        public ImageView imgPhoto;
        public TextView texType;
        public TextView texTitle;
        public TextView texFullName;
        public TextView texDay;
        public TextView texTime;
    }
}
