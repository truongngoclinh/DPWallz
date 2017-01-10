package com.dpanic.dpwallz.control;

import java.io.File;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Created by dpanic on 10/11/2016.
 * Project: DPWallz
 */

public class FileUtil {
    private static final String prefixLink = "https://static.pexels.com/photos";

    private static String getDownloadFolder() {
        String root = Environment.getExternalStorageDirectory().toString();
        File appFolder = new File(root + "/DPWallz");
        if (!appFolder.exists()) {
            if (!appFolder.mkdir()) {
                Log.e("thanh.dao", "getDownloadFolder: cannot create app folder!");
                return "";
            }
        }

        return root + "/DPWallz/";
    }

    public static boolean isFileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static void deleteFile(String path) {
        if (isFileExists(path)) {
            File file = new File(path);
            boolean deleted = file.delete();
            if (deleted) {
                Log.d("thanh.dao", "deleted file: " + path);
            } else {
                Log.d("thanh.dao", "deleted file: failed!");
            }
        }
    }

    public static boolean isFileDownloaded(String url) {
        return getLocalFile(url).exists();
    }

    public static String getLocalPath(String url) {
        return getDownloadFolder() + getFileName(url);
    }

    private static String getInternalDownload(Context context) {
        String internalFolder = context.getFilesDir() + "/Download/";
        File file = new File(internalFolder);
        if (!file.exists()) {
            if (file.mkdir()) {
                Log.d("thanh.dao", "getInternalDownload: created download folder.");
            }
        }
        return internalFolder;
    }

    public static String getInternalPath(Context context, String url) {
        return getInternalDownload(context) + getFileName(url);
    }


    public static String getFileName(String url) {
        return "photo-" + getPexelIdFromUrl(url) + getExtension(url);
    }

    private static File getLocalFile(String url) {
        return new File(getLocalPath(url));
    }

    private static String getPexelIdFromUrl(String url) {
        return url.substring(url.indexOf("/", prefixLink.length()) + 1, url.lastIndexOf("/"));
    }

    private static String getExtension(String url) {
        return url.substring(url.lastIndexOf("."));
    }

    static Uri getImageUri(String url) {
        return Uri.fromFile(getLocalFile(url));
    }
}
