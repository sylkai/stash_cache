/*
package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.view.Display;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

*/
/**
 * Class to hold utility methods relating to the StashPhoto object and photo cleanup.  All photo
 * items intended for release 1.1, not initial.
 *//*


public class StashPhotoUtils {

    public static BitmapDrawable getScaledDrawable(Activity a, String path) {
        Display display = a.getWindowManager().getDefaultDisplay();
        Point displaySize = new Point();
        display.getSize(displaySize);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        int inSampleSize = 1;
        if (srcHeight > displaySize.y || srcWidth > displaySize.x) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round((float)srcHeight / displaySize.y);
            } else {
                inSampleSize = Math.round((float)srcWidth / displaySize.x);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        options.inPurgeable = true;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return new BitmapDrawable(a.getResources(), bitmap);
    }*//*


    // recycles bitmaps when no longer displayed to clean memory
    public static void cleanImageView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return;
        }

        BitmapDrawable b = (BitmapDrawable)imageView.getDrawable();
        if (b.getBitmap() == null) {
            return;
        }

        b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }

    // create a temporary file to transfer to the user's chosen camera app for the photo
    public static File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }

}
*/
