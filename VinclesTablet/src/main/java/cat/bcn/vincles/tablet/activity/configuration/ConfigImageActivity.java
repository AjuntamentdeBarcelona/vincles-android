/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.tablet.activity.configuration;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.util.Date;
import cat.bcn.vincles.lib.util.VinclesConstants;
import cat.bcn.vincles.tablet.R;

public class ConfigImageActivity extends ConfigActivity {
    private static final String TAG = "ConfigImageActivity";
    private int step = 2;

    private ImageView imgPhoto;
    private View btnConfirm, btnRepeat, btnPhoto;
    private String currentFilename;
    private String currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_image);

        imgPhoto = (ImageView) this.findViewById(R.id.imgPhoto);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnRepeat = findViewById(R.id.btnRepeat);
        btnPhoto = findViewById(R.id.btnPhoto);

        if (!isFinishing())
            Glide.with(this)
                .load(VinclesConstants.getImageDirectory() + "/" + mainModel.currentUser.imageName)
                .error(R.drawable.user).placeholder(R.color.superlightgray)
                .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                .into(imgPhoto);

        if (mainModel.tour > step)
            havePhoto();
    }

    public void takePhoto(View view) {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Indicate file uri to save
            currentFilename = VinclesConstants.IMAGE_PREFIX + new Date().getTime() + VinclesConstants.IMAGE_EXTENSION;
            File currentImageFile = new File(VinclesConstants.getImagePath(), currentFilename);
            currentImagePath = currentImageFile.getAbsolutePath();
            Uri currentImageUri = Uri.fromFile(currentImageFile);

            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
            startActivityForResult(intent, VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_RESULT);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VinclesConstants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (!isFinishing())
                    Glide.with(this)
                        .load(VinclesConstants.getImageDirectory() + "/" + currentFilename)
                        .error(R.drawable.user).placeholder(R.color.superlightgray)
                        .dontAnimate()      // GLIDE BUG WITH PLACEHOLDERS
                        .into(imgPhoto);// Load low resolution image

                havePhoto();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise userVincles
            }
        }
    }

    private void havePhoto() {
        btnPhoto.setVisibility(View.GONE);
        btnRepeat.setVisibility(View.VISIBLE);
        btnConfirm.setVisibility(View.VISIBLE);
    }

    public void confirm(View view) {
        if (currentFilename != null && !currentFilename.equals("")) {
            Log.i(TAG, "confirm() - photo: " + currentFilename);
            mainModel.currentUser.imageName = currentFilename;
            mainModel.saveUser(mainModel.currentUser);

            // Update photo at server
            mainModel.updateUserPhoto(currentFilename);
        }

        // Go to next screen
        if (mainModel.tour < step) mainModel.updateTourStep(step);
        startActivity(new Intent(this, ConfigLanguageActivity.class));
    }
}
