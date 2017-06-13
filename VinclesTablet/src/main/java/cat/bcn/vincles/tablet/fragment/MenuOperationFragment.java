/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.activity.operation.TaskMainActivity;

public class MenuOperationFragment extends Fragment {
    private static final String TAG = "MenuOperationFragment";

    View imgOperationMenu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.menu_operation_fragment, container, false);
        imgOperationMenu = view.findViewById(R.id.imgOperationMenu);
        imgOperationMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "go to Operation Home!");
                Intent i = new Intent(getActivity(), TaskMainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);

            }
        });
        return view;
    }
}
