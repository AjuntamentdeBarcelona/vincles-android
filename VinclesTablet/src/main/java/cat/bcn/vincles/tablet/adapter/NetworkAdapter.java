/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import java.util.List;
import cat.bcn.vincles.lib.util.ImageUtils;
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.MainModel;

public class NetworkAdapter extends ArrayAdapter<User> {
    private final String TAG = this.getClass().getSimpleName();

    public NetworkAdapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_network, parent, false);
        }

        // Lookup view for data population
        TextView texName = (TextView) convertView.findViewById(R.id.item_network_name);
        TextView texLastname = (TextView) convertView.findViewById(R.id.item_network_lastname);
        ImageView photo = (ImageView) convertView.findViewById(R.id.item_network_image);

        // Populate the data into the template view using the data object
        texName.setText(user.name + "(" + user.getId() + ")");

        if (user != null) {
            texLastname.setText(user.lastname);
            if (user.idContentPhoto != null) {
                try {
                    Glide.with(getContext())
                            .load(MainModel.getInstance().getUserPhotoUrlFromUser(user))
                            .signature(new StringSignature(user.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(photo);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                Log.w(TAG, user.alias + " has idContentPhoto null!");
            }
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
