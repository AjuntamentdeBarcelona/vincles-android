/***************************************************************************************************************

 Copyright (c) 2016-2017 i2CAT Foundation. All rights reserved.
 Use of this source code is governed by the LICENSE file in the root of the source tree.

****************************************************************************************************************/
package cat.bcn.vincles.lib.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

public class ImageUtils {
    public static void loadBitmap(byte[] data, ImageView imageView) {
        if (data != null) {
            if (cancelPotentialWork(data, imageView)) {
                final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                final AsyncByte asyncByte = new AsyncByte(null, data, null, task);
                imageView.setImageDrawable(asyncByte);
                task.execute(data);
            }
        }
    }

    public static boolean cancelPotentialWork(byte[] data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final byte[] bitmapData = bitmapWorkerTask.data;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || bitmapData != data) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncByte) {
                final AsyncByte asyncByte = (AsyncByte) drawable;
                return asyncByte.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap decodeSampledBitmapFromByte(byte[] data, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data,0,data.length,options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(data,0,data.length,options);
    }

    public static int getSquareCropDimensionForBitmap(Bitmap bitmap)
    {
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }

    public static Bitmap resizeScale(Bitmap bitmap, float scale) {
        int newWidth = (int)(bitmap.getWidth()*scale);
        int newHeight = (int)(bitmap.getHeight()*scale);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    public static Bitmap resize(Bitmap bitmap, int newWidth, int newHeight) {
        if (newWidth < VinclesConstants.IMAGE_MIN_WIDTH) {
            newWidth = VinclesConstants.IMAGE_MIN_WIDTH;
        }
        if(newHeight < VinclesConstants.IMAGE_MIN_HEIGHT) {
            newHeight = VinclesConstants.IMAGE_MIN_HEIGHT;
        }

        int sampleSize = calculateInSampleSize(bitmap, newWidth, newHeight);

        return resizeScale(bitmap, sampleSize);
    }

    public static int calculateInSampleSize(Bitmap bitmap, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = bitmap.getHeight();
        final int width = bitmap.getWidth();
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }
}
