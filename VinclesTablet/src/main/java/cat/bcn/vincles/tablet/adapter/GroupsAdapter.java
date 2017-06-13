/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.List;
import cat.bcn.vincles.lib.util.FontCache;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.VinclesGroup;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.component.GroupItemView;
import cat.bcn.vincles.tablet.model.GroupModel;
import cat.bcn.vincles.tablet.model.MainModel;

public class GroupsAdapter extends ArrayAdapter<VinclesGroup> {
    private Context mContext;
    private LayoutInflater inflater;
    private Typeface tf;
    public GroupsAdapter(Context context, int resource, List<VinclesGroup> objects) {
        super(context, resource, objects);
        mContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        tf = FontCache.get(VinclesConstants.TYPEFACE.REGULAR, context);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VinclesGroup item = getItem(position);
        MainModel mainModel = MainModel.getInstance();
        View rowView = convertView;

        if (rowView == null) {
            rowView = new GroupItemView(getContext(), R.layout.item_list_group);
        }

        ViewHolder viewHolder = new ViewHolder();
        // Lookup view for data population
        viewHolder.imgPhoto = (ImageView) rowView.findViewById(R.id.item_group_photo);
        viewHolder.texTitle = (TextView) rowView.findViewById(R.id.item_group_title);
        rowView.setTag(viewHolder);

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        try {
            Glide.with(getContext())
                    .load(GroupModel.getInstance().getGroupPhotoUrlFromGroupId(item.getId()))
//                    .signature(new StringSignature(item.idContentPhoto.toString()))   // TODO: GROUPS CANNOT CHANGE PICTURE
                    .into(viewHolder.imgPhoto);
        } catch (Exception e) { e.printStackTrace(); }
        holder.texTitle.setText(item.name);

        return rowView;
    }

    static class ViewHolder {
        public ImageView imgPhoto;
        public TextView texTitle;
    }
}
