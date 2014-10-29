package com.geekeclectic.android.stashcache;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Created by sylk on 10/29/2014.
 */
public class StashPhotoTask extends AsyncTask<Integer, Void, BitmapDrawable> {
    private final WeakReference<ImageView> imageViewReference;
    private Activity mActivity;
    private int mWidth;
    private int mHeight;
    private final String mPath;

    public StashPhotoTask(Activity activity, ImageView imageView, String path) {
        mPath = path;
        mActivity = activity;

        mWidth = imageView.getWidth();
        mHeight = imageView.getHeight();

        // using a WeakReference to ensure the ImageView can be garbage collected
        imageViewReference = new WeakReference<ImageView>(imageView);
    }

    // decode image in the background
    @Override
    protected BitmapDrawable doInBackground(Integer... params) {
        return getScaledDrawable(mActivity, mPath, mWidth, mHeight);
    }

    // when complete, check to make sure ImageView is still around before setting drawable
    @Override
    protected void onPostExecute(BitmapDrawable bitmap) {
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = imageViewReference.get();
            if (imageView != null) {
                imageView.setImageDrawable(bitmap);
            }
        }
    }

    private static BitmapDrawable getScaledDrawable(Activity a, String path, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int inSampleSize = calculateInSampleSize(options, width, height);

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        options.inPurgeable = true;
        options.inJustDecodeBounds = false;

        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        return new BitmapDrawable(a.getResources(), bitmap);
    }

    // from Android example @ http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
