package com.angcyo.uiview;

import android.os.Environment;
import android.text.TextUtils;

import com.angcyo.library.utils.L;

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
    public static String APP_FOLDER = "A_APP";

    public static String getAppExternalFolder() {
        return getAppExternalFolder("");
    }

    public static String externalStorageDirectory() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
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

    private static Properties loadProperties(Function2<Properties, Writer, Void> function) {
        Writer writer = null;
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
            writer = new FileWriter(file.getAbsolutePath());

            Properties pro = new Properties();
            pro.load(reader);
            if (function != null) {
                function.invoke(pro, writer);
            }
            return pro;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
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
        loadProperties(new Function2<Properties, Writer, Void>() {
            @Override
            public Void invoke(Properties properties, Writer writer) {
                for (Enumeration e = properties.propertyNames(); e.hasMoreElements(); ) {
                    String s = (String) e.nextElement(); // 遍历所有元素
                    if (s.equals(key)) {
                        properties.setProperty(key, value);
                    } else {
                        properties.setProperty(s, properties.getProperty(s));
                    }
                }
                properties.setProperty(key, value);
                try {
                    properties.store(writer, new Date().toString());
                } catch (IOException e) {
                    e.printStackTrace();
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
            value = loadProperties(new Function2<Properties, Writer, Void>() {
                @Override
                public Void invoke(Properties properties, Writer writer) {
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

    /**
     * 在SD APP_FOLDER根目录下, 随机创建一个文件名路径
     */
    public static String createFilePath() {
        return createExternalFilePath();
    }

    public static String createExternalFilePath() {
        return getAppExternalFolder("") + File.separator + createFileName();
    }

    public static String createInternalFilePath(String folder) {
        return getAppExternalFolder(folder) + File.separator + createFileName();
    }
}
