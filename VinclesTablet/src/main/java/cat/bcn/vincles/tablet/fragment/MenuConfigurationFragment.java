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
import cat.bcn.vincles.tablet.activity.configuration.ConfigMainActivity;
import cat.bcn.vincles.tablet.R;

public class MenuConfigurationFragment extends Fragment {
    private static final String TAG = "MenuOperationFragment";

    View configurationMenu;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.menu_configuration_fragment, container, false);
        configurationMenu = view.findViewById(R.id.btnConfigurationMenu);
        configurationMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "go to Main Home!");
                getActivity().finishAffinity();
                getActivity().startActivity(new Intent(getActivity(), ConfigMainActivity.class));
                getActivity().finish();
            }
        });
        return view;
    }
}
