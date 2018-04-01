package com.angcyo.uiview;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.angcyo.github.utilcode.utils.AppUtils;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.resources.ResUtil;
import com.angcyo.uiview.utils.ScreenUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;

import kotlin.jvm.functions.Function2;

/**
 * Created by angcyo on 2016-11-05.
 */

public class Root {
    public static String APP_FOLDER = RApplication.getApp().getPackageName();

    public static String device_info(Activity activity) {
        StringBuilder builder = new StringBuilder();
        builder.append(AppUtils.getAppVersionName(RApplication.getApp()));
        builder.append(" by ");
        builder.append(ResUtil.getThemeString(RApplication.getApp(), "build_time"));
        builder.append(" on ");
        builder.append(ResUtil.getThemeString(RApplication.getApp(), "os_name"));
        builder.append(" ");
        builder.append(ScreenUtil.screenWidth);
        builder.append("×");
        builder.append(ScreenUtil.screenHeight);
        builder.append(" ");
        builder.append(activity.getWindow().getDecorView().getMeasuredHeight());
        builder.append(" ");
        builder.append(ScreenUtil.densityDpi);
        builder.append(" ");
        builder.append(ScreenUtil.density);
        builder.append(" \n");
        builder.append("v:");
        builder.append(Build.MANUFACTURER);
        builder.append(" ");
        builder.append("m:");
        builder.append(Build.MODEL);
        builder.append(" ");
        builder.append("d:");
        builder.append(Build.DEVICE);
        builder.append(" ");
        builder.append("h:");
        builder.append(Build.HARDWARE);
        builder.append(" ");
        return builder.toString();
    }

    public static String getAppExternalFolder() {
        return getAppExternalFolder("");
    }

    public static String externalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String sd() {
        return externalStorageDirectory();
    }

    /**
     * @param type The type of storage directory to return. Should be one of
     *             {@link Environment#DIRECTORY_MUSIC}, {@link Environment#DIRECTORY_PODCASTS},
     *             {@link Environment#DIRECTORY_RINGTONES}, {@link Environment#DIRECTORY_ALARMS},
     *             {@link Environment#DIRECTORY_NOTIFICATIONS}, {@link Environment#DIRECTORY_PICTURES},
     *             {@link Environment#DIRECTORY_MOVIES}, {@link Environment#DIRECTORY_DOWNLOADS},
     *             {@link Environment#DIRECTORY_DCIM}, or {@link Environment#DIRECTORY_DOCUMENTS}. May not be null.
     */
    public static String getExternalStoragePublicDirectory(String type) {
        return Environment.getExternalStoragePublicDirectory(type).getAbsolutePath();
    }

    /**
     * 获取录屏路径
     */
    public static String getScreenshotsDirectory() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Screenshots";
    }

    public static void ensureFolder(String folder) {
        File file = new File(folder);
        if (!file.exists()) {
            file.mkdirs();
        }
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

    /**
     * /data/user/0/com.angcyo.mainhost/files
     */
    public static String getAppInternalFolder(String folder) {
        if (folder == null) {
            folder = "";
        }
        File file = new File(RApplication.getApp().getFilesDir()/*getCacheDir()*/, folder);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    /**
     * /data/user/0/com.angcyo.mainhost/app_dddddddddddd
     */
    public static String getAppInternalDir(String folder) {
        if (folder == null) {
            folder = "";
        }
        File file = RApplication.getApp().getDir(folder, Context.MODE_PRIVATE);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file.getAbsolutePath();
    }

    private static Properties loadProperties(Function2<Properties, String, Void> function) {
        Reader reader = null;

        try {
            File file = new File(getAppExternalFolder("prop") + File.separator + "account.prop");
            if (file.exists() && file.isDirectory()) {
                file.delete();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            reader = new FileReader(file.getAbsolutePath());

            Properties pro = new Properties();
            pro.load(reader);
            if (function != null) {
                function.invoke(pro, file.getAbsolutePath());
            }
            return pro;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 写入键值对到properties
     */
    public static void storeProperties(final String key, final String value) {
        L.d("root storeProperties key : " + key + " value : " + value);
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
            return;
        }
        loadProperties(new Function2<Properties, String, Void>() {
            @Override
            public Void invoke(Properties properties, String path) {
                for (Enumeration e = properties.propertyNames(); e.hasMoreElements(); ) {
                    String s = (String) e.nextElement(); // 遍历所有元素
                    if (s.equals(key)) {
                        properties.setProperty(key, value);
                    } else {
                        properties.setProperty(s, properties.getProperty(s));
                    }
                }
                properties.setProperty(key, value);
                Writer writer = null;
                try {
                    writer = new FileWriter(path);
                    properties.store(writer, new Date().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
                return null;
            }
        });
    }

    public static String loadProperties(String key) {
        L.d("root before loadProperties key : " + key);
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        String value = null;
        try {
            value = loadProperties(new Function2<Properties, String, Void>() {
                @Override
                public Void invoke(Properties properties, String writer) {
                    return null;
                }
            }).getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
            value = "";
        }
        L.d("root loadProperties key : " + key + " value : " + value);

        if (TextUtils.isEmpty(value)) {
            return "";
        } else {
            return value;
        }
    }

    /**
     * 创建时间文件名
     */
    public static String createTimeFileName() {
        return createTimeFileName("yyyy-MM-dd_HH-mm-ss-SSS");
    }

    public static String createTimeFileName(String format) {
        String dataTime = RCrashHandler.getDataTime(format);
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

    /**
     * 在SD APP_FOLDER根目录下, 随机创建一个文件名路径
     */
    public static String createFilePath() {
        return createExternalFilePath();
    }

    public static String createExternalFilePath() {
        return createExternalFilePath("");
    }

    public static String createExternalFilePath(String folder) {
        return createExternalFilePath(folder, createFileName());
    }

    /**
     * 在SD卡的程序指定根目录下, 创建文件路径
     */
    public static String createExternalFilePath(String folder, String fileName) {
        return getAppExternalFolder(folder) + File.separator + fileName;
    }
}
