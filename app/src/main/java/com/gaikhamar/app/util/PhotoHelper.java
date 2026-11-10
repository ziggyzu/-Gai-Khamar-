package com.gaikhamar.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.FileProvider;

import java.io.File;

/** Animal photos are stored privately on the phone: files/photos/animal_<id>.jpg */
public final class PhotoHelper {
    private PhotoHelper() {}

    public static File file(Context c, long animalId) {
        File dir = new File(c.getFilesDir(), "photos");
        dir.mkdirs();
        return new File(dir, "animal_" + animalId + ".jpg");
    }

    public static Uri uri(Context c, long animalId) {
        return FileProvider.getUriForFile(c, c.getPackageName() + ".fileprovider", file(c, animalId));
    }

    /** Shows the photo if it exists. Returns true when a photo was shown. */
    public static boolean load(Context c, long animalId, ImageView iv) {
        File f = file(c, animalId);
        if (f.exists() && f.length() > 0) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inSampleSize = 4;   // smaller copy, saves memory
            Bitmap bmp = BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            if (bmp != null) {
                iv.setImageBitmap(bmp);
                iv.setVisibility(View.VISIBLE);
                return true;
            }
        }
        iv.setVisibility(View.GONE);
        return false;
    }
}