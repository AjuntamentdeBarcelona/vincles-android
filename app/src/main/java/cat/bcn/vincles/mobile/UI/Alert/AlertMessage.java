package cat.bcn.vincles.mobile.UI.Alert;


import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class AlertMessage implements View.OnClickListener {

    public static final String TITTLE_ERROR = "ERROR";
    public static final String TITTLE_INFO = "INFO";
    public static final String TITTLE_REMINDER = "REMINDER";
    public static final String TITTLE_SYSTEM = "SYSTEM";
    public static final String TITTLE_INVITATION = "INVITATION";

    AlertMessageInterface alertMessageInterface;
    CancelMessageInterface cancelMessageInterface;
    DismissMessageInterface dismissMessageInterface;
    public AlertDialog alert;
    String title;
    String type;

    public AlertMessage(AlertMessageInterface alertMessageInterface, String title) {
        this.alertMessageInterface = alertMessageInterface;
        this.title = title;
    }

    public void setCancelMessageInterface(CancelMessageInterface cancelMessageInterface) {
        this.cancelMessageInterface = cancelMessageInterface;
    }

    public void setDismissMessageInterface(DismissMessageInterface dismissMessageInterface) {
        this.dismissMessageInterface = dismissMessageInterface;
    }

    public void dismissSafely() {
        if (alert != null && alert.isShowing()) alert.dismiss();
    }

    public void showMessage(Activity activity, String message, String type) {
        this.type = type;
        if (OtherUtils.activityCannotShowDialog(activity)) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View alertLayout;

        if(type.equals("logout")) {
            alertLayout = inflater.inflate(R.layout.alert_logout_dialog, null);
        } else {
            alertLayout = inflater.inflate(R.layout.alert_error_dialog, null);
        }

        TextView dialogTitle = alertLayout.findViewById(R.id.dialogTitle);
        TextView dialogText = alertLayout.findViewById(R.id.dialogText);
        Button ok = alertLayout.findViewById(R.id.ok);
        Button cancel = alertLayout.findViewById(R.id.cancel);
        ImageView close_dialog = alertLayout.findViewById(R.id.close_dialog);

        if(type.equals("logout")) {
            ok.setText(R.string.close_session);
            cancel.setVisibility(View.VISIBLE);
        } else if (type.equals("renewTokenError")) {
            close_dialog.setVisibility(View.INVISIBLE);
        }

        switch (title) {
            case TITTLE_ERROR:
                title = activity.getResources().getString(R.string.error);
                break;
            case TITTLE_INFO:
                title = activity.getResources().getString(R.string.information);
                break;
            case TITTLE_REMINDER:
                title = activity.getResources().getString(R.string.reminder_alert_title);
                break;
            case TITTLE_SYSTEM:
                title = activity.getResources().getString(R.string.app_name);
                break;
            case TITTLE_INVITATION:
                title = activity.getResources().getString(R.string.group_detail_send_invite_ok_title);
                break;
        }
        dialogTitle.setText(title);

        if (ok != null) ok.setOnClickListener(this);
        if (cancel != null) cancel.setOnClickListener(this);
        if (close_dialog != null) close_dialog.setOnClickListener(this);
        if (dialogText != null) dialogText.setText(message);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogsTheme);
        alertDialogBuilder
                .setCancelable(false);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        if(activity != null && !activity.isFinishing()){
            alert.show();
        }

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ok) {
            alertMessageInterface.onOkAlertMessage(this, type);
        } else if (view.getId() == R.id.cancel) {
            alert.cancel();
            if (cancelMessageInterface != null) cancelMessageInterface.onCancelAlertMessage();
            else alertMessageInterface.onOkAlertMessage(this, type);
        } else if (view.getId() == R.id.close_dialog) {
            if (!type.equals("renewTokenError"))
                alert.cancel();
            if (dismissMessageInterface != null) dismissMessageInterface.onDismissAlertMessage();
        }

    }

    public interface AlertMessageInterface {
        void onOkAlertMessage(AlertMessage alertMessage, String type);
    }

    public interface CancelMessageInterface {
        void onCancelAlertMessage();
    }

    public interface DismissMessageInterface {
        void onDismissAlertMessage();
    }
}
