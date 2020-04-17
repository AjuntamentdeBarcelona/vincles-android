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

public class AlertRetry implements View.OnClickListener {

    AlertSaveImageInGalleryInterface alertSaveImageInGalleryInterface;
    public AlertDialog alert;
    Button takePictureBtn, galleryBtn, acceptBtn, canclelBtn;
    ImageView close_dialog;
    Activity activity;
    ImageView avatarImg;

    public AlertRetry(Activity activity, AlertSaveImageInGalleryInterface alertSaveImageInGalleryInterface) {
        this.activity = activity;
        this.alertSaveImageInGalleryInterface = alertSaveImageInGalleryInterface;
    }

    public void showMessage(String message) {
        if (OtherUtils.activityCannotShowDialog(activity)) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = inflater.inflate(R.layout.alert_save_image_gallery, null);

        acceptBtn = alertLayout.findViewById(R.id.accept);
        canclelBtn = alertLayout.findViewById(R.id.cancel);

        acceptBtn.setOnClickListener(this);
        canclelBtn.setOnClickListener(this);

        if (message != null) {
            ((TextView)alertLayout.findViewById(R.id.next_text)).setText(message);
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogsTheme);
        alertDialogBuilder
                .setCancelable(false);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        alert.show();
    }

    public void dismissSafely() {
        if (alert != null && alert.isShowing()) alert.dismiss();
    }

    @Override
    public void onClick(View view) {
         if (view.getId() == R.id.accept) {
             alertSaveImageInGalleryInterface.onRetryAccept(this);
        } else if (view.getId() == R.id.cancel) {
             alertSaveImageInGalleryInterface.onRetryCancel(this);
        }
    }

    public interface AlertSaveImageInGalleryInterface {

        void onRetryAccept(AlertRetry alertRetry);

        void onRetryCancel(AlertRetry alertRetry);
    }

}
