/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.adapter;

import android.content.Context;
import android.content.Intent;
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
import cat.bcn.vincles.lib.vo.User;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskNetworkUserActionActivity;
import cat.bcn.vincles.tablet.model.MainModel;
import cat.bcn.vincles.tablet.model.TaskModel;

public class List4Adapter extends ArrayAdapter<User> {
    private final String TAG = this.getClass().getSimpleName();

    public List4Adapter(Context context, int resource, List<User> objects) {
        super(context, resource, objects);
    }

    @Override
    public int getCount() {
        return (super.getCount()/4)+1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list_4, parent, false);
        }

        fillUserAtPosition(convertView.findViewById(R.id.groupCircle1), position*4);
        fillUserAtPosition(convertView.findViewById(R.id.groupCircle2), position*4+1);
        fillUserAtPosition(convertView.findViewById(R.id.groupCircle3), position*4+2);
        fillUserAtPosition(convertView.findViewById(R.id.groupCircle4), position*4+3);

        return convertView;
    }

    private View fillUserAtPosition(View layout, int  userlist_position) {
        ImageView imgPhoto = (ImageView) layout.findViewById(R.id.imgPhoto);
        TextView userNameText = (TextView) layout.findViewById(R.id.texUserName);

        if (userlist_position >= super.getCount()) {
            imgPhoto.setImageResource(0);
            userNameText.setText("");
            return layout;
        }

        final User user = getItem(userlist_position);

        // ADD TAG
        layout.setTag(userlist_position);

        // ADD TEXT
        userNameText.setText(user.name);

        // ADD PICTURE
        if (user != null) {
            if (user.idContentPhoto != null) {
                try {
                    Glide.with(getContext())
                            .load(MainModel.getInstance().getUserPhotoUrlFromUser(user))
                            .signature(new StringSignature(user.idContentPhoto.toString()))
                            .error(R.drawable.user).placeholder(R.color.superlightgray)
                            .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                            .into(imgPhoto);
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                Log.w(TAG, user.alias + " has idContentPhoto null!");
            }
        }

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)v.getTag();
                Log.d(null, "POSITION: " + position + " / USER: " + user.name);
                TaskModel.getInstance().currentUser = user;
                TaskModel.getInstance().view = TaskModel.TASK_DELETE_USER;
                Intent i = new Intent(getContext(), TaskNetworkUserActionActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(i);
            }
        });
        return layout;
    }
}
