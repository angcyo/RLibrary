package com.angcyo.uiview;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
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

        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() +
                File.separator + Root.APP_FOLDER + File.separator + folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    public static String getAppInternalFolder(String folder) {
        if (folder == null) {
            folder = "";
        }
        File file = new File(RApplication.getApp().getCacheDir(), folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * 写入键值对到properties
     */
    public static void storeProperties(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        Writer writer = null;
        try {
            File file = new File(getAppExternalFolder("prop") + File.separator + "account.prop");
            if (!file.exists()) {
                file.createNewFile();
            }
            writer = new FileWriter(getAppExternalFolder("prop") + File.separator + "account.prop");
            Properties pro = new Properties();
            pro.setProperty(key, value);
            pro.store(writer, "");
        } catch (IOException e) {
            e.printStackTrace();
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static String loadProperties(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        Reader reader = null;
        try {
            File file = new File(getAppExternalFolder("prop" + File.separator + "account.prop"));
            if (!file.exists()) {
                file.createNewFile();
            }
            reader = new FileReader(getAppExternalFolder("prop" + File.separator + "account.prop"));
            Properties pro = new Properties();
            pro.load(reader);
            String value = pro.getProperty(key);
            if (TextUtils.isEmpty(value)) {
                return "";
            } else {
                return value;
            }
        } catch (IOException e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            e.printStackTrace();
            return "";
        }

    }

    /**
     * 创建时间文件名
     */
    public static String createTimeFileName() {
        String dataTime = RCrashHandler.getDataTime("yyyy-MM-dd_HH-mm-ss-SSS");
        return dataTime;
    }

    /**
     * 创建随机文本名
     *
     * @param suffix 后缀
     */
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
