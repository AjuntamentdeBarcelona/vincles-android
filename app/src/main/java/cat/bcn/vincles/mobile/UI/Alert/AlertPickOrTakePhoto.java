package cat.bcn.vincles.mobile.UI.Alert;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import cat.bcn.vincles.mobile.R;
import cat.bcn.vincles.mobile.Utils.ImageUtils;
import cat.bcn.vincles.mobile.Utils.OtherUtils;

public class AlertPickOrTakePhoto implements View.OnClickListener {

    AlertPickOrTakePhotoInterface alertPickOrTakePhotoInterface;
    AlertPickOrTakePhotoClosed alertPickOrTakePhotoClosed;
    AlertDialog alert;
    Button takePictureBtn, galleryBtn;
    ImageView close_dialog;
    Activity activity;
    ImageView avatarImg;
    boolean isCameraMode = true;

    public AlertPickOrTakePhoto(Activity activity, AlertPickOrTakePhotoInterface alertPickOrTakePhotoInterface) {
        this.activity = activity;
        this.alertPickOrTakePhotoInterface = alertPickOrTakePhotoInterface;
    }

    public void showMessage() {
        if (OtherUtils.activityCannotShowDialog(activity)) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = inflater.inflate(R.layout.alert_pick_or_take_photo, null);
        avatarImg = alertLayout.findViewById(R.id.userAvatar);
        takePictureBtn = alertLayout.findViewById(R.id.takePicture);
        galleryBtn = alertLayout.findViewById(R.id.gallery);
        close_dialog = alertLayout.findViewById(R.id.close_dialog);

        takePictureBtn.setOnClickListener(this);
        galleryBtn.setOnClickListener(this);
        close_dialog.setOnClickListener(this);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogsTheme);
        alertDialogBuilder
                .setCancelable(false);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        alert.show();
    }

    public void showAcceptOrCancelBtns() {
        isCameraMode = false;
        if (takePictureBtn!=null)takePictureBtn.setText(R.string.accept);
        if(galleryBtn!=null)galleryBtn.setText(R.string.cancel);

    }

    public void hideAccpetOrCancelBtns() {
        isCameraMode = true;
        takePictureBtn.setText(R.string.camera);
        galleryBtn.setText(R.string.gallery);
    }

    public void resetImage () {
        avatarImg.setImageDrawable(activity.getResources().getDrawable(R.drawable.user));
    }

    public void setImage (Uri selectedImageUri) {
        avatarImg.setImageURI(selectedImageUri);
    }

    public void setImagePath (String path) {
        if (avatarImg == null)return;
        ImageUtils.setImageToImageView(new File(path), avatarImg, activity, false);

    }

    public void close () {
        alert.cancel();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.takePicture) {
            if (isCameraMode) {
                alertPickOrTakePhotoInterface.onTakePhoto(this);
            } else {
                alertPickOrTakePhotoInterface.onAcceptPhoto(this);
            }
        } else if (view.getId() == R.id.gallery) {
            if (isCameraMode) {
                alertPickOrTakePhotoInterface.onPickPhoto(this);
            } else {
                alertPickOrTakePhotoInterface.onCancelPhoto(this);
            }
        } else if (view.getId() == R.id.close_dialog) {
            if (alertPickOrTakePhotoClosed != null) alertPickOrTakePhotoClosed.onClosed();
            alert.cancel();
        }
    }

    public void setAlertPickOrTakePhotoClosed(AlertPickOrTakePhotoClosed alertPickOrTakePhotoClosed) {
        this.alertPickOrTakePhotoClosed = alertPickOrTakePhotoClosed;
    }

    public interface AlertPickOrTakePhotoInterface {
        void onTakePhoto(AlertPickOrTakePhoto alertPickOrTakePhoto);

        void onPickPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto);

        void onAcceptPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto);

        void onCancelPhoto(AlertPickOrTakePhoto alertPickOrTakePhoto);
    }

    public interface AlertPickOrTakePhotoClosed {
        void onClosed();
    }

}
