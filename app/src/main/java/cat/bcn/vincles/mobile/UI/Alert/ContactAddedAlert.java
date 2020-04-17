package cat.bcn.vincles.mobile.UI.Alert;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import cat.bcn.vincles.mobile.R;

public class ContactAddedAlert implements View.OnClickListener {

    private AlertDialog alert;
    private Activity activity;
    private Button acceptButton;
    private TextView dialogText;
    private ImageView avatarImg;
    private AddContactDialogCallback listener;

    public ContactAddedAlert(Activity activity, AddContactDialogCallback listener) {
        this.activity = activity;
        this.listener = listener;
    }

    public void showMessage(String contactName, String selectedImagePath) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = inflater.inflate(R.layout.alert_contact_added, null);
        avatarImg = alertLayout.findViewById(R.id.userAvatar);
        acceptButton = alertLayout.findViewById(R.id.accept);
        dialogText = alertLayout.findViewById(R.id.dialogText);
        acceptButton.setOnClickListener(this);
        setName(contactName);
        setImage(selectedImagePath);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity, R.style.DialogsTheme);
        alertDialogBuilder
                .setCancelable(false);
        alert = alertDialogBuilder.create();
        alert.setView(alertLayout);
        alert.show();
    }

    private void setImage (String selectedImagePath) {
        if (!"".equals(selectedImagePath)) {
            avatarImg.setImageURI(Uri.fromFile(new File(selectedImagePath)));
        } else {
            avatarImg.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.user));
        }
    }

    private void setName(String name) {
        dialogText.setText(name);
    }

    public void close () {
        alert.cancel();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.accept) {
            if (listener != null) {
                listener.onAcceptButtonClicked();
            }
        }
    }

    public interface AddContactDialogCallback {
        void onAcceptButtonClicked();
    }

}
