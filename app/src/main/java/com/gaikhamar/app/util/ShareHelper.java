package com.gaikhamar.app.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

public final class ShareHelper {
    private ShareHelper() {}

    /** Implicit intent: opens the Android share sheet for a file (PDF, CSV...). */
    public static void shareFile(Activity a, File f, String mime, String title) {
        Uri uri = FileProvider.getUriForFile(a, a.getPackageName() + ".fileprovider", f);
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(mime);
        i.putExtra(Intent.EXTRA_STREAM, uri);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        a.startActivity(Intent.createChooser(i, title));
    }

    public static void shareText(Activity a, String text, String title) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, text);
        a.startActivity(Intent.createChooser(i, title));
    }
}