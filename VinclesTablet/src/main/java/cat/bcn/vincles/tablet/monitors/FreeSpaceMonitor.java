/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.monitors;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;
import java.io.File;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.lib.vo.PushMessage;
import cat.bcn.vincles.tablet.R;
import cat.bcn.vincles.tablet.model.MainModel;

public class FreeSpaceMonitor  {
    private static final String TAG = FreeSpaceMonitor.class.getSimpleName();
    public static boolean alreadyAdviced = false;
    private static long limit = 500777793;
    private static Dialog dialog;

    public static void checkFreeSpace(Context ctx) {
        long freeBytes = new File(VinclesConstants.getImagePath()).getUsableSpace();

        if (freeBytes < limit) {
            if (dialog != null && dialog.isShowing()) {
                try {
                    dialog.dismiss();
                    dialog = null;
                } catch (Exception e) {}
            }
            dialog = new Dialog(ctx, R.style.DialogCustomTheme);
            dialog.setContentView(R.layout.alert_dialog_space);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            TextView alertText = (TextView) dialog.findViewById(R.id.item_message_title);
            alertText.setText(ctx.getString(R.string.free_space_low_message, MainModel.getInstance().currentUser.alias));
            View close_btn = dialog.findViewById(R.id.btnClose);
            close_btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    alreadyAdviced = true;
                    dialog.dismiss();
                    dialog = null;
                }
            });
        } else {
            if (dialog != null && dialog.isShowing()) {
                try {
                    dialog.dismiss();
                    dialog = null;
                } catch (Exception e) {}
            }
        }
    }
}
