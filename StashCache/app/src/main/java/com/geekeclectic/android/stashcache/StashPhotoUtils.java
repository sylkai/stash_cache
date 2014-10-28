package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;

/**
 * Created by sylk on 10/27/2014.
 */
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
                inSampleSize = Math.round(srcHeight / displaySize.y);
            } else {
                inSampleSize = Math.round(srcWidth / displaySize.x);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return new BitmapDrawable(a.getResources(), bitmap);
    }

    public static void cleanImageView(ImageView imageView) {
        if (!(imageView.getDrawable() instanceof BitmapDrawable)) {
            return;
        }

        BitmapDrawable b = (BitmapDrawable)imageView.getDrawable();
        b.getBitmap().recycle();
        imageView.setImageDrawable(null);
    }

}
