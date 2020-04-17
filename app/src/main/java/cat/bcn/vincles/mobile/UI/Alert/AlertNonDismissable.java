package cat.bcn.vincles.mobile.UI.Alert;


import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class AlertNonDismissable {

    public AlertDialog alert;
    String message;
    TextView dialogText;
    boolean showProgressbar = true;

    public AlertNonDismissable(String message, boolean showProgressbar) {
        this.message = message;
        this.showProgressbar = showProgressbar;
    }

    public void dismissSafely() {
        if (alert != null && alert.isShowing()) alert.dismiss();
    }

    public void showMessage(Activity activity) {
        if (OtherUtils.activityCannotShowDialog(activity)) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = inflater.inflate(R.layout.alert_non_dismissable, null);
        dialogText = alertLayout.findViewById(R.id.dialogText);

        if (!showProgressbar) {
            alertLayout.findViewById(R.id.progressbar).setVisibility(View.GONE);
        }

        dialogText.setText(message);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogsTheme);
        alertDialogBuilder
                .setCancelable(false);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        alert.show();
    }

    public void showMessage(Activity activity, String string) {
        showMessage(activity);
        changeMessage(string);
    }

    public void changeMessage(String message) {
        dialogText.setText(message);
    }

}
