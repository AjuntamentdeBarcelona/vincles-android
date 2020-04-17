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

public class AlertConfirmOrCancel implements View.OnClickListener{


    public static final String BUTTONS_HORIZNTAL = "BUTTONS_HORIZNTAL";
    public static final String BUTTONS_VERTICAL = "BUTTONS_VERTICAL";

    private AlertConfirmOrCancelInterface alertConfirmOrCancelInterface;
    public AlertDialog alert;
    private Button acceptBtn, canclelBtn;
    private ImageView close_dialog;
    private Activity context;
    private String cancelText, acceptText;



    public AlertConfirmOrCancel(Activity context, AlertConfirmOrCancelInterface alertConfirmOrCancelInterface) {
        this.context = context;
        acceptText = "";
        cancelText = "";
        this.alertConfirmOrCancelInterface = alertConfirmOrCancelInterface;
    }

    public void showMessage(String message, String title, String type) {
        showMessage(message, title, type, false);
    }

    public void showMessage(String message, String title, String type, boolean cancelable) {

        if (OtherUtils.activityCannotShowDialog(context)) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = null;
        if (type.equals(BUTTONS_HORIZNTAL)) {
            alertLayout = inflater.inflate(R.layout.alert_confirm_or_cancel_horizontal, null);
        } else {
            alertLayout = inflater.inflate(R.layout.alert_confirm_or_cancel_vertical, null);
        }

        ((TextView)alertLayout.findViewById(R.id.dialogText)).setText(message);
        ((TextView)alertLayout.findViewById(R.id.dialogTitle)).setText(title);

        acceptBtn = alertLayout.findViewById(R.id.accept);
        canclelBtn = alertLayout.findViewById(R.id.cancel);
        close_dialog = alertLayout.findViewById(R.id.close_dialog);
        if (!"".equals(acceptText) || !"".equals(cancelText)) {
            acceptBtn.setText(acceptText);
            canclelBtn.setText(cancelText);
        }
        acceptBtn.setOnClickListener(this);
        canclelBtn.setOnClickListener(this);
        close_dialog.setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.DialogsTheme);
        alertDialogBuilder.setCancelable(cancelable);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        alert.show();
    }

    public void setButtonsText(String acceptText, String cancelText) {
        this.acceptText = acceptText;
        this.cancelText = cancelText;
    }

    public void resetAlert() {
        acceptText = "";
        cancelText = "";
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.accept) {
            alertConfirmOrCancelInterface.onAccept(this);
        } else if (view.getId() == R.id.cancel) {
            resetAlert();
            alertConfirmOrCancelInterface.onCancel(this);
        } else if (view.getId() == R.id.close_dialog) {
            resetAlert();
            alertConfirmOrCancelInterface.onCancel(this);
            //alert.dismiss();
        }
    }

    public void dismissSafely() {
        if (alert != null && alert.isShowing()) alert.dismiss();
    }

    public interface AlertConfirmOrCancelInterface {
        void onAccept(AlertConfirmOrCancel alertConfirmOrCancel);
        void onCancel(AlertConfirmOrCancel alertConfirmOrCancel);
    }
}
