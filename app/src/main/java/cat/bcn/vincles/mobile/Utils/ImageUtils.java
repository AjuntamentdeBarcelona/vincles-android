package cat.bcn.vincles.mobile.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cat.bcn.vincles.mobile.R;

import static android.content.Context.MODE_PRIVATE;

public class ImageUtils {

    private final static int MAXIMAGEDIMENSION = 1000;
    private final static int COMPRESSIONQUALITY = 70;
    private final static int MAXIMAGEDIMENSIONPROFILE = 200;

    public static Uri getImageUri(Context context, Bitmap image) {

        Calendar c = Calendar.getInstance();

        ContextWrapper wrapper = new ContextWrapper(context);
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, generateUniqueFileName()+".jpg");
        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,COMPRESSIONQUALITY,stream);
            stream.flush();
            stream.close();
        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }
        String absolutaPath = file.getAbsolutePath();
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());
        return savedImageURI;
    }

    public static Bitmap getResizedBitmap(Bitmap image, Boolean isProfile) {
        if(image == null){
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();

        int maxDim = 0;
        if(isProfile){
            maxDim = MAXIMAGEDIMENSIONPROFILE;
        }
        else{
            maxDim = MAXIMAGEDIMENSION;
        }

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            if(width > maxDim){
                width = maxDim;
                height = (int) (width / bitmapRatio);
            }

        } else {
            if(height > maxDim){
                height = maxDim;
                width = (int) (height * bitmapRatio);
            }

        }

        Bitmap resized = Bitmap.createScaledBitmap(image, width, height, true);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, COMPRESSIONQUALITY, out);


        return BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public static File getResizedFile(File file) {

        String filePath = file.getPath();




        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if (bitmap == null){
            return null;
        }
        Bitmap resized = getResizedBitmap(bitmap, false);


        ExifInterface ei = null;
        try {
            ei = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ei == null){
            return null;
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(resized, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(resized, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(resized, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = resized;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSIONQUALITY, bos);
        byte[] bitmapdata = bos.toByteArray();

//write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    public static String generateUniqueFileName() {
        return System.currentTimeMillis() + "_"
                + UUID.randomUUID().toString().substring(0, 7);
    }

    public static Uri saveFile(InputStream data) {
        ContextWrapper wrapper = new ContextWrapper(MyApplication.getAppContext());
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, generateUniqueFileName() +".jpg");
        try {
            FileUtils.copyInputStreamToFile(data, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file);
    }

    public static Uri saveFile(Context context, Bitmap image) {

        Calendar c = Calendar.getInstance();

        ContextWrapper wrapper = new ContextWrapper(context);
        File file = wrapper.getDir("Images",MODE_PRIVATE);
        file = new File(file, generateUniqueFileName()+".jpg");
        try{
            OutputStream stream = null;
            stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.JPEG,100,stream);
            stream.flush();
            stream.close();
        }catch (IOException e) // Catch the exception
        {
            e.printStackTrace();
        }
        String absolutaPath = file.getAbsolutePath();
        Uri savedImageURI = Uri.parse(file.getAbsolutePath());
        return savedImageURI;
    }

    public static String getRealPathFromURI(Uri contentURI, Activity context) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = context.managedQuery(contentURI, projection, null,
                null, null);
        if (cursor == null)
            return null;
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        if (cursor.moveToFirst()) {
            String s = cursor.getString(column_index);
            return s;
        }
        return null;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getMimeType(Uri uri, Context context) {
        String mimeType = "";
        if (context != null) {
            // URI belongs to "content" scheme
            if ("content".equals(uri.getScheme())) {
                ContentResolver contentResolver = context.getContentResolver();
                mimeType = contentResolver.getType(uri);
            // URI belongs to "file" scheme
            } else if ("file".equals(uri.getScheme())) {
                // TODO: 7/9/19 Find a "formal" way to extract the mime type from an URI belonging to the "file" scheme.
                mimeType = ImageUtils.getMimeType(uri.getPath());
            }
        }
        return mimeType;
    }

    public static String getImage64(String imagePath){

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

        Bitmap resized = getResizedBitmap(bitmap, true);


        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, COMPRESSIONQUALITY, bos);
        byte[] bitmapdata = bos.toByteArray();

        return Base64.encodeToString(bitmapdata, Base64.DEFAULT);
    }

    public static Bitmap getCorrectlyOrientedImage(Context context, String path) throws IOException {

        InputStream is = context.getContentResolver().openInputStream(Uri.parse(path));
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, Uri.parse(path));

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(Uri.parse(path));

        srcBitmap = BitmapFactory.decodeStream(is);

        is.close();

        /*
         * if the orientation is not 0 (or -1, which means we don't know), we
         * have to do a rotation.
         */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    public static int getOrientation(Context context, Uri photoUri) {
        /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels, int heigh, int width) {


        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, heigh);
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static void drawCutoutBackground(View view) {
        int width = view.getWidth();
        int height = view.getHeight();
        float radius = height / 2 + view.getResources().getDimension(R.dimen.main_avatar_radius_margin);

        Paint transparentPaint = new Paint();
        transparentPaint.setColor(0xFFFFFF);
        transparentPaint.setAlpha(0);
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(Color.TRANSPARENT);
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(view.getResources().getColor(R.color.darkGray));
        backgroundPaint.setAntiAlias(true);

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap bitmap = Bitmap.createBitmap(width, height, conf); // this creates a MUTABLE bitmap

        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(0,0,width,height, backgroundPaint);
        canvas.drawRect(0,0, radius, height, transparentPaint);
        canvas.drawCircle(radius, height/2, radius, transparentPaint);

        BitmapDrawable drawable = new BitmapDrawable(view.getResources(), bitmap);
        view.setBackground(drawable);
    }

    public static File createAvatarFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        return image;
    }

    public static File createImageFile(Context context) throws IOException {
        // Create an image file name
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, generateUniqueFileName() + ".jpg");

        Log.d("newimg","create image, name:"+image.getAbsolutePath()+" currTime:"+System.currentTimeMillis());

        return image;
    }

    public static File createAudioFile(Context context) throws IOException {
        Long timeStamp = System.currentTimeMillis();
        String audioFileName = timeStamp.toString();
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, audioFileName + ".aac");
    }

    public static String decodeFile(String path) throws IOException, IllegalArgumentException  {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;
        Bitmap adjustedBitmap = null;
        ExifInterface exif = new ExifInterface(path);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
        try {
            // Part 1: Decode image


            Bitmap unscaledBitmap = ScalingUtils.decodeFile(path, MAXIMAGEDIMENSION, MAXIMAGEDIMENSION, ScalingUtils.ScalingLogic.FIT);
            int width = unscaledBitmap.getWidth();

            if (!(unscaledBitmap.getWidth() <= MAXIMAGEDIMENSION && unscaledBitmap.getHeight() <= MAXIMAGEDIMENSION)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtils.createScaledBitmap(unscaledBitmap, MAXIMAGEDIMENSION, MAXIMAGEDIMENSION, ScalingUtils.ScalingLogic.FIT);

            } else {
                unscaledBitmap.recycle();
                return path;
            }

            adjustedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            scaledBitmap.recycle();
            // Store to tmp file
            int width2 = adjustedBitmap.getWidth();

            File f = new File(path);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSIONQUALITY, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            adjustedBitmap.recycle();
        } catch (Throwable e) {
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;

    }

    public static Bitmap decodeFileWithRotation(String path) {
        return ImageUtils.decodeFileWithRotation(path, null);
    }

    public static Bitmap decodeFileWithRotation(String path, BitmapFactory.Options options) {
        Bitmap adjustedBitmap;

        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            return null;
        }
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        if (rotation != 0f) {
            matrix.preRotate(rotationInDegrees);
        }

        // Decode a file path into a bitmap
        Bitmap unscaledBitmap = BitmapFactory.decodeFile(path, options);
        //Log.d("photoTaken", String.format("unscaled image size %d", unscaledBitmap.getByteCount()));
        // Resize the bitmap
        Bitmap resizedBitmap = ImageUtils.getResizedBitmap(unscaledBitmap, true);
        //Log.d("photoTaken", String.format("resized image size %d", resizedBitmap.getByteCount()));

        // Rotate the image if necessary (applying the matrix with the rotation factor)
        adjustedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);
        //Log.d("photoTaken", String.format("adjusted image size %d", adjustedBitmap.getByteCount()));

        unscaledBitmap.recycle();

        return adjustedBitmap;
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }

    /**
     * Retrieve video frame image from given video path
     *
     * @param p_videoPath
     *            the video file path
     *
     * @return Bitmap - the bitmap is video frame image
     *
     * @throws Throwable
     */
    @SuppressLint("NewApi")
    public static Bitmap retriveVideoFrameFromVideo(String p_videoPath)
            throws Throwable
    {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try
        {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception m_e)
        {
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.getMessage());
        }
        finally
        {
            if (m_mediaMetadataRetriever != null)
            {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }

    public static void saveMediaExternalMemory(Context context, String fileName,
                                               String internalFilePath) {
        if (isExternalStorageWritable()) {
            File externalDir = getPublicAlbumStorageDir();
            try {
                final File f =  storeFileExternalMemory(new File(internalFilePath), externalDir, fileName);

                updateGallery(context, f);

            } catch (IOException e) {
                Log.d("SaveFileExternalMemory","storeFileExternalMemory error: "+e);
            }
        }
    }

    private static void updateGallery(Context context, File f) {
        Log.d("SaveFileExternalMemory","updateGallery");

        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(f);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);


        /*MediaScannerConnection.scanFile(context, new String[] {

                        f != null ? f.getAbsolutePath() : null},

                null, new MediaScannerConnection.OnScanCompletedListener() {

                    public void onScanCompleted(String path, Uri uri)

                    {


                    }

                });*/
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public static File getPublicAlbumStorageDir() {
        // Get the directory for the user's public pictures directory.

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator +
                "VINCLES" + File.separator);
        /*File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "VINCLES");
*/
        /*File file = new File(Environment.getExternalStoragePublicDirectory(
                null), "VINCLES");*/
        if (!file.mkdirs()) {
            Log.e("image_utils", "Directory not created, path:"+file.getPath());
        }
        return file;
    }

    private static File storeFileExternalMemory(File src, File dst, String dstFilename) throws IOException {
        //If destination is null cannot save
        if (dst == null)return null;
        //if folder does not exist
        if (!dst.exists()) {
            if (!dst.mkdir()) {
                Log.e("image_utils", "storeFileExternalMemory cannit mkdir");
                return null;
            }
        }

        File expFile = new File(dst.getPath() + File.separator + dstFilename);
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(expFile).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            if (inChannel != null) {
                inChannel.transferTo(0, inChannel.size(), outChannel);
            }
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }

        return expFile;
    }

    public static void deleteFileIfExists(String path) {
        File myFile = new File(path);
        if(myFile.exists())
            myFile.delete();
    }

    public static void safeCopyFileToExternalStorage(Context context, String internalPath, String fileName) {
        String[] pathParts = internalPath.split("\\.");
        if (pathParts.length <=0) {
            Log.w("ImageUtils", "Error copying file due to missing extension. Path:"
                    + internalPath);
            return;
        }
        String extension = pathParts[pathParts.length-1];
        String externalPath = ImageUtils.getPublicAlbumStorageDir().getPath();
        String externalFilename = fileName + "." + extension;

        ImageUtils.deleteFileIfExists(externalPath + File.separator + externalFilename);

        if(context != null){
            ImageUtils.saveMediaExternalMemory(context, externalFilename,
                    internalPath);
        }

    }


    public static void setImageToImageView(Object image, ImageView imageView, Context context, Boolean centerCrop) {
        if(context == null){
            return;
        }
        if (!centerCrop){
            Glide.with(context.getApplicationContext())
                    .load(image)
                    .apply(RequestOptions.overrideOf(imageView.getWidth(), imageView.getHeight()))
                    .into(imageView);
        }
        else{
            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.override(imageView.getWidth(),imageView.getHeight());

            Glide.with(context.getApplicationContext())
                    .load(image)
                    .apply(options)
                    .into(imageView);

        }

        //                                     .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
    }

    public static void setImageToImageViewFromPath(String imagePath, ImageView imageView, Context context, Boolean centerCrop) {
        if(context == null){
            return;
        }
        if (!centerCrop){
            Glide.with(context.getApplicationContext())
                    .load(imagePath)
                    .apply(RequestOptions.overrideOf(imageView.getWidth(), imageView.getHeight()))
                    .into(imageView);
        }
        else{
            RequestOptions options = new RequestOptions();
            options.centerCrop();
            options.override(imageView.getWidth(),imageView.getHeight());

            Glide.with(context.getApplicationContext())
                    .load(imagePath)
                    .apply(options)
                    .into(imageView);

        }

        //                                     .centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
    }


    public static void setImageToImageViewWithCallbacks(final Object image, final ImageView imageView, final Context context, final int errorResource) {

        ((Activity)context).runOnUiThread(new Runnable() {

            @Override
            public void run() {

                Glide.with(context.getApplicationContext())
                        .load(image)
                        .apply(RequestOptions.overrideOf(imageView.getWidth(), imageView.getHeight()))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, final Target<Drawable> target, boolean isFirstResource) {
                                imageView.setImageResource(errorResource);
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(imageView);

            }
        });


    }

    public static String generateVideoThumbnail(Context context, String path) {
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(path,
                MediaStore.Images.Thumbnails.MINI_KIND);

        if (thumbnail == null){
            return "";
        }
        return saveFile(context, thumbnail).getPath();
    }

    public static boolean checkWriteExternalStoragePermission(Context context) {
        if (context == null) return false;
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }
}
