package com.angcyo.uiview;

import android.os.Environment;

import java.io.File;
import java.util.UUID;

/**
 * Created by angcyo on 2016-11-05.
 */

public class Root {
    public static String APP_FOLDER = "DValley";

    public static String getAppExternalFolder() {
        return getAppExternalFolder("");
    }

    public static String getAppExternalFolder(String folder) {
        if (folder == null) {
            folder = "";
        }
        return Environment.getExternalStorageDirectory().getAbsoluteFile() +
                File.separator + Root.APP_FOLDER + File.separator + folder;
    }

    public static String createFileName(String suffix) {
        if (suffix == null) {
            suffix = "";
        }
        return UUID.randomUUID().toString() + suffix;
    }

    public static String createFileName() {

        return createFileName("");
    }
}
