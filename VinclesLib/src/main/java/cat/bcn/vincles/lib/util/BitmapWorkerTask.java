/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class BitmapWorkerTask extends AsyncTask<byte[], Void, Bitmap> {
    private final WeakReference<ImageView> imageViewReference;
    public byte[] data;

    public BitmapWorkerTask(ImageView imageView) {
        // Use a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }


    // Decode image in background.
    @Override
    protected Bitmap doInBackground(byte[]... params) {
        data = params[0];
        return decodeSampledBitmapFromByte(data, VinclesConstants.IMAGE_MIN_WIDTH, VinclesConstants.IMAGE_MIN_HEIGHT);
    }

    // Once complete, see if ImageView is still around and set bitmap.
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromByte(byte[] data,
                                                     int reqWidth, int reqHeight) {

        Bitmap result = null;
        if (data != null) {
            // Use Vincles method instead 'BitmapFactory.decodeByteArray(data, 0, data.length, options)'
            result = VinclesConstants.getBitmapFromByte(data);
        }

        return result;
    }
}