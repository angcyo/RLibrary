package com.angcyo.uiview.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.View;
import android.view.WindowManager;

import com.angcyo.github.utilcode.utils.CmdUtil;
import com.angcyo.github.utilcode.utils.ImageUtils;
import com.angcyo.github.utilcode.utils.PhoneUtils;
import com.angcyo.library.utils.L;
import com.angcyo.library.utils.RIo;
import com.angcyo.uiview.RApplication;
import com.angcyo.uiview.RCrashHandler;
import com.angcyo.uiview.Root;
import com.angcyo.uiview.accessibility.permission.SettingsCompat;
import com.angcyo.uiview.widget.RExTextView;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.angcyo.uiview.utils.RUtils.ImageType.BMP;
import static com.angcyo.uiview.utils.RUtils.ImageType.GIF;
import static com.angcyo.uiview.utils.RUtils.ImageType.JPEG;
import static com.angcyo.uiview.utils.RUtils.ImageType.PNG;
import static com.angcyo.uiview.utils.RUtils.ImageType.UNKNOWN;

/**
 * Created by angcyo on 15-12-16 016 15:41 下午.
 */
public class RUtils {

    /**
     * 人民币符号
     */
    public static final String RMB = "￥";

    public static final HashMap<String, String> mFileTypes = new HashMap<String, String>();
    private static final String[][] MIME_MapTable = {
            //{后缀名，MIME类型}
            {".3gp", "video/3gpp"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".avi", "video/x-msvideo"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".exe", "application/octet-stream"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "application/x-javascript"},
            {".log", "text/plain"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".ogg", "audio/ogg"},
            {".pdf", "application/pdf"},
            {".png", "image/png"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "text/plain"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".txt", "text/plain"},
            {".wav", "audio/x-wav"},
            {".wma", "audio/x-ms-wma"},
            {".wmv", "audio/x-ms-wmv"},
            {".wps", "application/vnd.ms-works"},
            {".xml", "text/plain"},
            {".z", "application/x-compress"},
            {".zip", "application/x-zip-compressed"},
            {"", "*/*"}
    };

    static {
        //images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("49492A00", "tif");
        mFileTypes.put("424D", "bmp");
        //
        mFileTypes.put("41433130", "dwg"); //CAD
        mFileTypes.put("38425053", "psd");
        mFileTypes.put("7B5C727466", "rtf"); //日记本
        mFileTypes.put("3C3F786D6C", "xml");
        mFileTypes.put("68746D6C3E", "html");
        mFileTypes.put("44656C69766572792D646174653A", "eml"); //邮件
        mFileTypes.put("D0CF11E0", "doc");
        mFileTypes.put("5374616E64617264204A", "mdb");
        mFileTypes.put("252150532D41646F6265", "ps");
        mFileTypes.put("255044462D312E", "pdf");
        mFileTypes.put("504B0304", "zip");
        mFileTypes.put("52617221", "rar");
        mFileTypes.put("57415645", "wav");
        mFileTypes.put("41564920", "avi");
        mFileTypes.put("2E524D46", "rm");
        mFileTypes.put("000001BA", "mpg");
        mFileTypes.put("000001B3", "mpg");
        mFileTypes.put("6D6F6F76", "mov");
        mFileTypes.put("3026B2758E66CF11", "asf");
        mFileTypes.put("4D546864", "mid");
        mFileTypes.put("1F8B08", "gz");
        mFileTypes.put("", "");
    }

    /**
     * 获取文件类型
     */
    public static String getFileType(String filePath) {
        String type = mFileTypes.get(getFileHeader(filePath));
        if (TextUtils.isEmpty(type)) {
            int indexOf = filePath.lastIndexOf('/');
            if (indexOf != -1) {
                return filePath.substring(indexOf + 1);
            } else {
                return ".unknown";
            }
        }

        return type;
    }

    //获取文件头信息
    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    /**
     * 跳至拨号界面
     *
     * @param phoneNumber 电话号码
     */
    public static void callTo(String phoneNumber) {
        PhoneUtils.dial(phoneNumber);
    }

    public static void emailTo(Activity activity, String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + email));
//        intent.putExtra(Intent.EXTRA_CC, new String[]
//                {"1032694760@qq.com"});
        intent.putExtra(Intent.EXTRA_EMAIL, "");
        intent.putExtra(Intent.EXTRA_TEXT, "欢迎提供意您的见或建议");
        activity.startActivity(Intent.createChooser(intent, "选择邮件客户端"));
    }

    /**
     * 快速加群
     */
    public static boolean joinQQGroup(Context context, String key) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            T_.error("您没有安装腾讯QQ");
            return false;
        }
    }

    /**
     * qq咨询
     */
    public static boolean chatQQ(Context context, String qq) {
        try {
            if (CmdUtil.checkApkExist(context, "com.tencent.mobileqq")) {
                String url = "mqqwpa://im/chat?chat_type=wpa&uin=" + qq;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } else {
                T_.error("您没有安装腾讯QQ");
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            T_.error("您没有安装腾讯QQ");
        }
        return false;
    }

    /**
     * 去除字符串左右的字符
     *
     * @param des the des
     * @return the string
     */
    public static String trimMarks(String des) {
        return trimMarks(des, 1);
    }

    /**
     * 去除字符串左右指定个数的字符
     *
     * @param des   the des
     * @param count the count
     * @return the string
     */
    public static String trimMarks(String des, int count) {
        if (des == null || count < 0 || des.length() < count + 1) {
            return des;
        }
        return des.substring(count, des.length() - count);
    }

    /**
     * 返回现在的时间,不包含日期
     *
     * @return the now time
     */
    public static String getNowTime() {
        return getNowTime("HH:mm:ss");
    }

    /**
     * Gets now time.
     *
     * @param pattern the patternUrl
     * @return the now time
     */
    public static String getNowTime(String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date());
    }

    /**
     * 判断字符串是否为空
     *
     * @param str the str
     * @return the boolean
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.trim().length() < 1);
    }

    /**
     * Gets time.
     *
     * @return 按照HH :mm:ss 返回时间
     */
    public static String getTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * Gets date.
     *
     * @return 按照yyyy -MM-dd 格式返回日期
     */
    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(new Date());
    }

    /**
     * Gets date and time.
     *
     * @return 按照yyyy -MM-dd HH:mm:ss 的格式返回日期和时间
     */
    public static String getDateAndTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * Gets date.
     *
     * @param pattern 格式
     * @return 返回日期 date
     */
    public static String getDate(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * Gets time.
     *
     * @param pattern 格式
     * @return 按照指定格式返回时间 time
     */
    public static String getTime(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

//

    /**
     * Gets date and time.
     *
     * @param pattern 指定的格式
     * @return 按照指定格式返回日期和时间 date and time
     */
    public static String getDateAndTime(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(new Date());
    }

    /**
     * @param context 上下文
     * @return 返回手机号码 tel number
     */
    @SuppressLint("MissingPermission")
    public static String getTelNumber(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }

    /**
     * Gets os version.
     *
     * @return 获取device的os version
     */
    public static String getOsVersion() {
        String string = Build.VERSION.RELEASE;
        return string;
    }

    /**
     * Gets os sdk.
     *
     * @return 返回设备sdk版本 os sdk
     */
    public static String getOsSdk() {
        int sdk = Build.VERSION.SDK_INT;
        return String.valueOf(sdk);
    }

    /**
     * Gets random.
     *
     * @return the random
     */
    public static int getRandom() {
        Random random = new Random();
        return random.nextInt();
    }

    /**
     * 获取随机数
     *
     * @param n 最大范围
     * @return random
     */
    public static int getRandom(int n) {
        Random random = new Random();
        return random.nextInt(n);
    }

    /**
     * 获取字符数组中随机的字符串
     *
     * @param context the context
     * @param resId   the res id
     * @return random string
     */
    public static String getRandomString(Context context, int resId) {
        String[] strings;
        strings = context.getResources().getStringArray(resId);

        return strings[getRandom(strings.length)];
    }

    /**
     * 从资源id获取字符串
     *
     * @param context 上下文
     * @param id      资源id
     * @return 字符串 string for res
     */
    public static String getStringForRes(Context context, int id) {
        if (context == null) {
            return "";
        }
        return context.getResources().getString(id);
    }

    /**
     * 获取APP的名字
     */
    public static String getAppName() {
        String appName = RApplication.getApp().getPackageName();
        PackageManager packageManager = RApplication.getApp().getPackageManager();
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(appName, 0);
            appName = packInfo.applicationInfo.loadLabel(RApplication.getApp().getPackageManager()).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return appName;
    }

    /**
     * 返回app的版本名称.
     *
     * @param context the context
     * @return app version name
     */
    public static String getAppVersionName(Context context) {
        String version = "unknown";
// 获取package manager的实例
        PackageManager packageManager = context.getPackageManager();
// getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
// Log.i("版本名称:", version);
        return version;
    }

    public static String getAppVersionName() {
        return getAppVersionName(RApplication.getApp());
    }

    public static int getAppVersionCode() {
        return getAppVersionCode(RApplication.getApp());
    }

    /**
     * 返回app的版本代码.
     *
     * @param context the context
     * @return app version code
     */
    public static int getAppVersionCode(Context context) {
// 获取package manager的实例
        PackageManager packageManager = context.getPackageManager();
// getPackageName()是你当前类的包名，0代表是获取版本信息
        int code = 1;
        PackageInfo packInfo;
        try {
            packInfo = packageManager.getPackageInfo(context.getPackageName(),
                    0);
            code = packInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
// Log.i("版本代码:", version);
        return code;
    }

    /**
     * 获取屏幕的宽度高度
     *
     * @param context the context
     * @param size    the size
     * @return the display
     */
    public static DisplayMetrics getDisplay(Context context, Point size) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getSize(size);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics;
    }

    /**
     * 返回调用行的方法 所在行号
     *
     * @return the string
     */
    public static String callMethodAndLine() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName() + ".");
        result.append(thisMethodStack.getMethodName());
        result.append("(" + thisMethodStack.getFileName());
        result.append(":" + thisMethodStack.getLineNumber() + ")");
        return result.toString();
    }

    /**
     * 返回调用行的类名
     *
     * @return the string
     */
    public static String callClassName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName());
        return result.toString();
    }

    /**
     * 返回调用行的方法名
     *
     * @return the string
     */
    public static String callMethodName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getMethodName());
        return result.toString();
    }

    /**
     * 返回调用行的 类名 和 方法名
     *
     * @return the string
     */
    public static String callClassMethodName() {
        StringBuilder result = new StringBuilder();
        StackTraceElement thisMethodStack = (new Exception()).getStackTrace()[1];
        result.append(thisMethodStack.getClassName() + ".");
        result.append(thisMethodStack.getMethodName());
        return result.toString();
    }

    /**
     * 打开网页,调用系统应用
     *
     * @param context the context
     * @param url     the url
     */
    public static void openUrl(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        if (!url.toLowerCase().startsWith("http:") && !url.toLowerCase().startsWith("https:")) {
            url = "http:".concat(url);
        }

        Uri webPage = Uri.parse(url);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, webPage);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(webIntent);
    }

    /**
     * 通过Url打开指定app
     */
    public static void openAppFromUrl(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(url));

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
        int size = resolveInfos.size();
        if (size > 0) {
            if (size == 1) {
                ActivityInfo activityInfo = resolveInfos.get(0).activityInfo;
                //T_.show("正在打开:" + activityInfo.applicationInfo.loadLabel(packageManager));

                intent.setComponent(new ComponentName(activityInfo.packageName, activityInfo.name));
                context.startActivity(intent);
            } else {
                context.startActivity(intent);
            }
        }
    }

    /**
     * 打开文件
     *
     * @param file
     */
    public static void openFile(Context context, File file) {

        if (file == null || !file.exists()) {
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(/*uri*/getFileUri(context, file), type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //跳转
        try {
            context.startActivity(intent);     //这里最好try一下，有可能会报错。 //比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据文件后缀名获得对应的MIME类型。
     *
     * @param file
     */
    private static String getMIMEType(File file) {

        String type = "*/*";
        String fName = file.getName();
        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }
    /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex, fName.length()).toLowerCase();
        if (end == "") return type;
        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (int i = 0; i < MIME_MapTable.length; i++) { //MIME_MapTable??在这里你一定有疑问，这个MIME_MapTable是什么？
            if (end.equals(MIME_MapTable[i][0]))
                type = MIME_MapTable[i][1];
        }
        return type;
    }

    /**
     * 跳转到应用市场
     */
    public static void jumpToMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void sendSMS(Activity ctx, String message, String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        ctx.startActivity(intent);
    }


    /**
     * 使用,分割string, 并返回一个列表
     */
    public static ArrayList<String> split(String string) {
        return split(string, ",");
    }

    public static ArrayList<String> split(String string, String regex) {
        final ArrayList<String> list = new ArrayList<>();
        if (!"null".equalsIgnoreCase(string) && !TextUtils.isEmpty(string)) {
            final String[] split = string.split(regex);
            for (String s : split) {
                if (!TextUtils.isEmpty(s)) {
                    list.add(s);
                }
            }
        }
        return list;
    }

    /**
     * 安全的去掉字符串的最后一个字符
     */
    public static String safe(StringBuilder stringBuilder) {
        return stringBuilder.substring(0, Math.max(0, stringBuilder.length() - 1));
    }

    public static <T> String connect(List<T> list) {
        if (list == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (T bean : list) {
            builder.append(bean.toString());
            builder.append(",");
        }

        return safe(builder);
    }

    /**
     * 组装参数
     */
    public static Map<String, String> map(String... args) {
        final Map<String, String> map = new HashMap<>();
        foreach(new OnPutValue() {
            @Override
            public void onValue(String key, String value) {
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    map.put(key, value);
                }
            }
        }, args);
        return map;
    }

    private static void foreach(OnPutValue onPutValue, String... args) {
        if (onPutValue == null || args == null) {
            return;
        }
        for (String str : args) {
            String[] split = str.split(":");
            if (split.length >= 2) {
                String first = split[0];
                onPutValue.onValue(first, str.substring(first.length() + 1));
            }
        }
    }

    /**
     * 填充两个字段相同的数据对象
     */
    public static void fill(Object from, Object to) {
        Field[] fromFields = from.getClass().getDeclaredFields();
        Field[] toFields = to.getClass().getDeclaredFields();
        for (Field f : fromFields) {
            String name = f.getName();
            for (Field t : toFields) {
                String tName = t.getName();
                if (name.equalsIgnoreCase(tName)) {
                    try {
                        f.setAccessible(true);
                        t.setAccessible(true);
                        t.set(to, f.get(from));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }

        }
    }

    /**
     * 判断是否是网址
     */
    public static boolean isHttpUrl(String url) {
//        return RegexUtils.isMatch(
//                "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?",
//                url);
//        Matcher matcher = RExTextView.patternUrl.matcher(url);
//        return matcher.find();
        return RExTextView.isWebUrlSys(url, true);
    }

    public static boolean isLast(List data, int position) {
        if (data == null || data.isEmpty()) {
            return true;
        }
        return data.size() - 1 == position;
    }

    public static String getLang() {
        Resources resources = RApplication.getApp().getResources();
        Configuration config = resources.getConfiguration();
        // 应用用户选择语言
        String language = config.locale.getLanguage();
        return language;
    }

    public static void changeLang(Locale locale) {
        Resources resources = RApplication.getApp().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, dm);
    }

    /**
     * 缩短很大的数字
     */
    public static String getShortString(long number) {
        return getShortString(String.valueOf(number), "");
    }

    public static String getShortString(String number) {
        return getShortString(number, "");
    }

    public static String getShortString(String number, boolean needDecimal, boolean isEnglish) {
        return getShortString(number, "", needDecimal, isEnglish);
    }

    public static String getShortString(String number, String suffix) {
        return getShortString(number, suffix, false);
    }

    public static String getShortString(String number, String suffix, boolean needDecimal) {
        return getShortString(number, suffix, needDecimal, false);
    }

    public static String getShortString(String number, String suffix, boolean needDecimal, boolean isEnglish) {
        if (TextUtils.isEmpty(number)) {
            return "";
        }

        String unit;
        String num;
        int decimal = 0;
        if (number.length() > 9) {
            if (isEnglish) {
                return "9kw+";
            }
            return "9千万+";
        } else if (number.length() > 7) {
            if (isEnglish) {
                unit = "kw";
            } else {
                unit = "千万";
            }

            int end = number.length() - 7;
            num = number.substring(0, end);

            if (num.length() >= 4) {
                decimal = Integer.parseInt(number.substring(end, end + 1));
            } else {
                decimal = Integer.parseInt(number.substring(end, end + 2));
            }
        } else if (number.length() > 4) {
            if (isEnglish) {
                unit = "w";
            } else {
                unit = "万";
            }

            int end = number.length() - 4;
            num = number.substring(0, end);
            if (num.length() >= 4) {
                decimal = Integer.parseInt(number.substring(end, end + 1));
            } else {
                decimal = Integer.parseInt(number.substring(end, end + 2));
            }
        } else {
            unit = "";
            num = number;
        }

//        if (number.length() >= 8) {
//            unit = "kk";
//            int end = number.length() - 7;
//            num = number.substring(0, end);
//
//            if (num.length() >= 2) {
//                decimal = Integer.parseInt(number.substring(end, end + 1));
//            } else {
//                decimal = Integer.parseInt(number.substring(end, end + 2));
//            }
//        } else if (number.length() >= 4) {
//            unit = "k";
//            int end = number.length() - 3;
//            num = number.substring(0, end);
//            if (num.length() >= 2) {
//                decimal = Integer.parseInt(number.substring(end, end + 1));
//            } else {
//                decimal = Integer.parseInt(number.substring(end, end + 2));
//            }
//
//        } else {
//            unit = "";
//            num = number;
//        }
//
//        if (decimal > 0) {
//            num = num + "." + decimal;
//        }

        StringBuilder builder = new StringBuilder();
        if (needDecimal && decimal > 0) {
            num = num + "." + decimal;
        }
        builder.append(num);
        builder.append(unit);
        builder.append(suffix);
        return builder.toString();
    }

    /**
     * 该方法是调用了系统的下载管理器
     */
    public static long downLoadFile(Context context, String url) {
        if (TextUtils.isEmpty(url)) {
            return -1;
        }
        /**
         * 在这里返回的 reference 变量是系统为当前的下载请求分配的一个唯一的ID，
         * 我们可以通过这个ID重新获得这个下载任务，进行一些自己想要进行的操作
         * 或者查询下载的状态以及取消下载等等
         */
        Uri uri = Uri.parse(url);        //下载连接
        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);  //得到系统的下载管理
        DownloadManager.Request request = new DownloadManager.Request(uri);  //得到连接请求对象
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);   //指定在什么网络下进行下载，这里我指定了WIFI网络

        String fileName = getFileNameFromUrl(url);

        request.setDestinationInExternalPublicDir(context.getPackageName() + "/download", fileName);  //制定下载文件的保存路径，我这里保存到根目录
        request.setVisibleInDownloadsUi(true);  //设置是否显示在系统的下载界面
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);//下载的时候, 完成的时候都显示通知
        request.allowScanningByMediaScanner();  //表示允许MediaScanner扫描到这个文件，默认不允许。

        request.setTitle(fileName);      //设置下载中通知栏的提示消息, 并且也是系统下载界面的显示名
        request.setDescription(fileName + "文件正在下载...");//设置设置下载中通知栏提示的介绍, 部分机型无效

        long downLoadId = manager.enqueue(request);   //启动下载,该方法返回系统为当前下载请求分配的一个唯一的ID

        return downLoadId;
    }

    /**
     * 获取文件名, 在url中
     */
    public static String getFileNameFromUrl(String url) {
        String fileName = "unknown";
        try {
            int indexOf = url.lastIndexOf('/');
            if (indexOf != -1) {
                fileName = url.substring(indexOf + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileName;
    }

    /**
     * 删除特殊字符
     */
    public static String fixName(String name) {
        String result = "-";
        if (TextUtils.isEmpty(name)) {
            return result;
        }
        String all = name.replaceAll("\\+", "").replaceAll("=", "").replaceAll("/", "").replaceAll("&", "");
        if (TextUtils.isEmpty(all)) {
            return result;
        }
        return all;
    }

    /**
     * 打印Map
     */
    public static void logMap(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            L.e("map is null or empty. ");
            return;
        }

        StringBuilder builder = new StringBuilder("\n");
        for (Map.Entry<String, String> p : map.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();
            builder.append(key);
            builder.append(":");
            builder.append(value);
            builder.append("\n");
        }
        L.e(builder.toString());
    }

    public static Uri getFileUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName(), file);
        } else {
            uri = Uri.fromFile(file);
        }
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return uri;
    }

    /**
     * 判断是否是主线程
     */
    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static ImageType getImageType(File file) {
        if (file == null) return UNKNOWN;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return getImageType(is);
        } catch (IOException e) {
            e.printStackTrace();
            return UNKNOWN;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static ImageType getImageType(InputStream is) {
        if (is == null) return UNKNOWN;
        try {
            byte[] bytes = new byte[8];
            return is.read(bytes, 0, 8) != -1 ? getImageType(bytes) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return UNKNOWN;
        }
    }

    private static ImageType getImageType(byte[] bytes) {
        if (isJPEG(bytes)) return JPEG;
        if (isGIF(bytes)) return GIF;
        if (isPNG(bytes)) return PNG;
        if (isBMP(bytes)) return BMP;
        return ImageType.UNKNOWN;
    }

    private static boolean isJPEG(byte[] b) {
        return b.length >= 2
                && (b[0] == (byte) 0xFF) && (b[1] == (byte) 0xD8);
    }

    private static boolean isGIF(byte[] b) {
        return b.length >= 6
                && b[0] == 'G' && b[1] == 'I'
                && b[2] == 'F' && b[3] == '8'
                && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
    }

    private static boolean isPNG(byte[] b) {
        return b.length >= 8
                && (b[0] == (byte) 137 && b[1] == (byte) 80
                && b[2] == (byte) 78 && b[3] == (byte) 71
                && b[4] == (byte) 13 && b[5] == (byte) 10
                && b[6] == (byte) 26 && b[7] == (byte) 10);
    }

    private static boolean isBMP(byte[] b) {
        return b.length >= 2
                && (b[0] == 0x42) && (b[1] == 0x4d);
    }

    /**
     * 00:00的格式输出, 如果有小时,01:00:00
     */
    public static String formatTime(long millisecond /*毫秒*/) {
        long mill = millisecond / 1000;

        long min = mill / 60;
        long hour = min / 60;
        long second = mill % 60;

        min %= 60;
        hour %= 24;

        StringBuilder builder = new StringBuilder();
        if (hour > 0) {
            builder.append(hour >= 10 ? hour : ("0" + hour));
            builder.append(":");
        }
        builder.append(min >= 10 ? min : ("0" + min));
        builder.append(":");
        builder.append(second >= 10 ? second : ("0" + second));

        return builder.toString();
    }

    /**
     * 分享文件
     */
    public static void shareFile(Activity activity, String filePath) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_STREAM,
                uriFromFile(new File(filePath)));
        share.setType("*/*");//此处可发送多种文件
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(share, "发送给..."));
    }

    public static void shareImage(Activity activity, String imgFilePath, String content) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, content);
        share.putExtra(Intent.EXTRA_STREAM,
                uriFromFile(new File(imgFilePath)));
        share.setType("image/*");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(share, "发送给..."));
    }

    public static void shareVideo(Activity activity, String videoFilePath, String content) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.putExtra(Intent.EXTRA_TEXT, content);
        share.putExtra(Intent.EXTRA_STREAM,
                uriFromFile(new File(videoFilePath)));
        share.setType("video/*");
        share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(share, "发送给..."));
    }

    public static void shareText(Activity activity, final String title, final String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享：" + title);
        intent.putExtra(Intent.EXTRA_TEXT, title + " " + text);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activity.startActivity(Intent.createChooser(intent, "选择分享"));
    }

    /**
     * 摄像头是否可用
     */
    public static boolean isCameraUseable() {
        Camera mCamera = null;
        try {
            mCamera = Camera.open();
            // setParameters 是针对魅族MX5。MX5通过Camera.open()拿到的Camera对象不为null
            Camera.Parameters mParameters = mCamera.getParameters();
            mCamera.setParameters(mParameters);
        } catch (Exception e) {
            return false;
        } finally {
            if (mCamera != null) {
                try {
                    mCamera.release();
                    Thread.sleep(100);
                } catch (Exception e) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 麦克风是否可用
     */
    public static boolean isAudioRecordable() {
        AudioRecord record = null;
        try {
            record = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT));
            record.startRecording();

            int recordingState = record.getRecordingState();

            if (recordingState == AudioRecord.RECORDSTATE_STOPPED) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (record != null) {
                try {
                    record.release();
                } catch (Exception e) {
                    return false;
                }
            }
        }
    }

    /**
     * 一个矩形(from), 在另一个矩形(to),居中显示时的宽高度
     */
    public static int[] getCenterRectWidthHeight(RectF from, RectF to) {
        int[] result = new int[2];

        Matrix matrix = new Matrix();
        matrix.setRectToRect(from, to, Matrix.ScaleToFit.CENTER);
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);

        result[0] = (int) (matrixValues[Matrix.MSCALE_X] * from.width());//缩放之后的宽度
        result[1] = (int) (matrixValues[Matrix.MSCALE_Y] * from.height());//缩放之后的高度

        return result;
    }

    /**
     * 判断是否为平板
     *
     * @return
     */
    public static boolean isPad(Context context) {
        // 大于6尺寸则为Pad
        if (getScreenInches(context) >= 6.0) {
            return true;
        }
        return false;
    }

    /**
     * 多少寸的屏幕
     */
    public static double getScreenInches(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        double x = Math.pow(displayMetrics.widthPixels / displayMetrics.xdpi, 2);
        double y = Math.pow(displayMetrics.heightPixels / displayMetrics.ydpi, 2);
        // 屏幕尺寸
        double screenInches = Math.sqrt(x + y);
        // 大于6尺寸则为Pad
        return screenInches;
    }

    /**
     * 写入数据到 SD卡  Root.APP_FOLDER/log 目录下
     *
     * @param fileName 文件名
     * @param data     需要写入的数据
     */
    public static void saveToSDCard(String fileName, String data) {
        saveToSDCard("log", fileName, data);
    }

    public static void saveToSDCard(String folderName, String fileName, String data) {
        try {
            String saveFolder = Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    File.separator + Root.APP_FOLDER + File.separator + folderName;
            File folder = new File(saveFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return;
                }
            }
            String dataTime = RCrashHandler.getDataTime("yyyy-MM-dd_HH-mm-ss-SSS");
            File file = new File(saveFolder, fileName);
            boolean append = true;
            if (file.length() > 1024 * 1024 * 1 /*大于10MB重写*/) {
                append = false;
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
            pw.println(dataTime);
            pw.println(data);
            //换行
            pw.println();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveToSDCard(String folderName, String fileName, Throwable data) {
        try {
            String saveFolder = Environment.getExternalStorageDirectory().getAbsoluteFile() +
                    File.separator + Root.APP_FOLDER + File.separator + folderName;
            File folder = new File(saveFolder);
            if (!folder.exists()) {
                if (!folder.mkdirs()) {
                    return;
                }
            }
            String dataTime = RCrashHandler.getDataTime("yyyy-MM-dd_HH-mm-ss-SSS");
            File file = new File(saveFolder, fileName);
            boolean append = true;
            if (file.length() > 1024 * 1024 * 10 /*大于10MB重写*/) {
                append = false;
            }
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, append)));
            pw.println(dataTime);
            data.printStackTrace(pw);
            //换行
            pw.println();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存View的截图
     */
    public static Bitmap saveView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap = drawingCache.copy(drawingCache.getConfig(), false);
        view.destroyDrawingCache();
        view.setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static Bitmap saveRecyclerViewBitmap(String path, RecyclerView view, int bgColor) {
        return saveRecyclerViewBitmap(path, view, bgColor, Integer.MAX_VALUE);
    }

    public static Bitmap saveRecyclerViewBitmap(String path, RecyclerView view, int bgColor, int itemCount/**需要截取的前几个item*/) {
        Bitmap bitmap = shotRecyclerView(view, bgColor, itemCount);
        ImageUtils.save(bitmap, path, Bitmap.CompressFormat.PNG);
        return bitmap;
    }

    /**
     * RecyclerView截图
     */
    public static Bitmap shotRecyclerView(RecyclerView view, int bgColor, int count) {
        RecyclerView.Adapter adapter = view.getAdapter();
        Bitmap bigBitmap = null;
        if (adapter != null) {
            int size = Math.min(count, adapter.getItemCount());
            int height = 0;
            Paint paint = new Paint();
            int iHeight = 0;
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

            // Use 1/8th of the available memory for this memory cache.
            final int cacheSize = maxMemory / 8;
            LruCache<String, Bitmap> bitmapCache = new LruCache<>(cacheSize);
            for (int i = 0; i < size; i++) {
                RecyclerView.ViewHolder holder = adapter.createViewHolder(view, adapter.getItemViewType(i));
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(
                        View.MeasureSpec.makeMeasureSpec(view.getWidth(), View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                holder.itemView.layout(0, 0, holder.itemView.getMeasuredWidth(),
                        holder.itemView.getMeasuredHeight());
                holder.itemView.setDrawingCacheEnabled(true);
                holder.itemView.buildDrawingCache();
                Bitmap drawingCache = holder.itemView.getDrawingCache();
                if (drawingCache != null) {

                    bitmapCache.put(String.valueOf(i), drawingCache);
                }
                height += holder.itemView.getMeasuredHeight();
            }

            bigBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas bigCanvas = new Canvas(bigBitmap);

            bigCanvas.drawColor(bgColor);

            Drawable lBackground = view.getBackground();
            if (lBackground instanceof ColorDrawable) {
                ColorDrawable lColorDrawable = (ColorDrawable) lBackground;
                int lColor = lColorDrawable.getColor();
                bigCanvas.drawColor(lColor);
            }

            for (int i = 0; i < size; i++) {
                Bitmap bitmap = bitmapCache.get(String.valueOf(i));
                bigCanvas.drawBitmap(bitmap, 0f, iHeight, paint);
                iHeight += bitmap.getHeight();
                bitmap.recycle();
            }
        }
        return bigBitmap;
    }

    /**
     * 钱
     * 将分转换成元
     */
    public static String yuan(int value) {
        return decimal(value * 1f / 100f, 2, false) + "元";
    }

    /**
     * 保留小数点后几位
     */
    public static String decimal(float value, int bitNum, boolean halfUp) {
//        BigDecimal bigDecimal = new BigDecimal(value);
//
//        /*
//        *         L.e("call:  -> ")
//        L.e("call: 测试小数点0 -> ${bigDecimal.toFloat()}")
//        L.e("call: 测试小数点2 -> ${bigDecimal.setScale(bitNum, BigDecimal.ROUND_HALF_DOWN).toFloat()}")
//        L.e("call: 测试小数点1 -> ${bigDecimal.setScale(bitNum, BigDecimal.ROUND_CEILING).toFloat()}")
//        L.e("call: 测试小数点1 -> ${bigDecimal.setScale(bitNum, BigDecimal.ROUND_DOWN).toFloat()}")
//        L.e("call: 测试小数点1 -> ${bigDecimal.setScale(bitNum, BigDecimal.ROUND_FLOOR).toFloat()}")
//        L.e("call: 测试小数点1 -> ${bigDecimal.setScale(bitNum, BigDecimal.ROUND_UP).toFloat()}")
//        L.e("call: 测试小数点1 -> ${bigDecimal.setScale(bitNum, BigDecimal.ROUND_HALF_EVEN).toFloat()}")
//
//        val myformat = java.text.DecimalFormat("0.000000")
//        val str = myformat.format(value)
//        L.e("call 1:  -> $str")
//        L.e("call 2:  -> ${String.format(Locale.CHINA, "%.6f", value)}")
//        */
//        if (halfUp) {
//            return bigDecimal.setScale(bitNum, BigDecimal.ROUND_HALF_UP).floatValue();
//        } else {
//            return bigDecimal.setScale(bitNum, BigDecimal.ROUND_DOWN).floatValue();
//        }

        if (halfUp) {
            //四舍五入
            StringBuilder parrern = new StringBuilder("0");
            if (bitNum > 0) {
                parrern.append(".");
            }
            for (int i = 0; i < bitNum; i++) {
                parrern.append("0");
            }
            DecimalFormat format = new DecimalFormat(parrern.toString());
            return format.format(value);
        } else {
            StringBuilder parrern = new StringBuilder("%");
            if (bitNum > 0) {
                parrern.append(".");
                parrern.append(bitNum);
                parrern.append("f");
                int pow = (int) Math.pow(10, bitNum);
                return String.format(Locale.CHINA, parrern.toString(), ((int) (pow * value)) / (pow * 1f));
//                return String.valueOf(((int) (Math.pow(10, bitNum) * value)) / Math.pow(10, bitNum));
            } else if (bitNum == 0) {
                String valueString = String.valueOf(value);
                int lastIndexOf = valueString.lastIndexOf(".");
                if (lastIndexOf == -1) {
                    return valueString;
                } else {
                    return valueString.subSequence(0, lastIndexOf).toString();
                }
            } else {
                return String.format(Locale.CHINA, "%s", value);
            }
        }
    }

    public static String getDataTime(String pattern, long date) {
        DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.CHINA);
        return dateFormat.format(new Date(date));
    }

    public static String getDataTime(String pattern) {
        return getDataTime(pattern, System.currentTimeMillis());
    }

    public static String getDataTime() {
        return getDataTime("yyyy-MM-dd HH:mm", System.currentTimeMillis());
    }

    /**
     * 从url中,  返回文件后缀, 也可以从文件路径中返回, 不包含.字符
     */
    public static String getFileSuffix(String url, String defaultSuffix) {
        String fileSuffix = defaultSuffix;
        try {
            String[] split = url.split("\\?")[0].split("\\.");
            if (split.length > 1) {
                fileSuffix = split[split.length - 1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileSuffix;
    }

    public static String getFileSuffix(String url) {
        return getFileSuffix(url, "");
    }

    /**
     * 获取文件名, 包含后缀
     */
    public static String getFileName(String url) {
        return getFileName(url, "/");
    }

    public static String getFileName(String url, String regex) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String[] split = url.split(regex);

        return split[split.length - 1];
    }

    /**
     * 获取bitmap对象的大小, 字节
     */
    public static long getBitmapSize(Bitmap bitmap) {
        if (bitmap.isRecycled()) {
            return 0;
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * 打开应用详情界面
     */
    public static void openAppDetailView(Activity activity, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + packageName));
        activity.startActivity(intent);
    }

    public static void openAppDetailView(Activity activity) {
        openAppDetailView(activity, activity.getPackageName());
    }

    /**
     * 扫描文件, 到系统相册/视频文件夹
     */
    public static void scanFile(Context context, String filePath) {
        File file = new File(filePath);
        if (file.exists() && context != null) {
            /*需要android.intent.action.MEDIA_MOUNTED系统权限，但是再Android 4.4系统以后，限制了只有系统应用才有使用广播通知系统扫描的权限，否则会抛出异常信息*/
//            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//            Uri uri = uriFromFile(file);
//            intent.setData(uri);
//            context.sendBroadcast(intent);

            MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{filePath}, null,
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                            L.e("call: onMediaScannerConnected([])-> ");
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            L.e("call: onScanCompleted([path, uri])-> " + path + " ->" + uri);
                        }
                    });
        }
    }

    public static void scanVideo(Context context, String videoPath) {
        File file = new File(videoPath);
        if (file.exists() && context != null) {
            MediaScannerConnection.scanFile(context, new String[]{videoPath}, new String[]{"video/mp4"},
                    new MediaScannerConnection.MediaScannerConnectionClient() {
                        @Override
                        public void onMediaScannerConnected() {
                            L.e("call: onMediaScannerConnected([])-> ");
                        }

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            L.e("call: onScanCompleted([path, uri])-> " + path + " ->" + uri);
                        }
                    });
        }
    }

    /**
     * 将文件复制到相册目录, 系统相册就会自动扫描
     *
     * @param filePath 需要复制的文件全路径
     * @param newName  新的文件名, 如果为空, 则使用源文件名
     */
    public static boolean copyToDCIM(String filePath, String newName) {
        //相册文件夹
        try {
            File dcimFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File srcFile = new File(filePath);
            File targetFile;
            if (TextUtils.isEmpty(newName)) {
                targetFile = new File(dcimFolder.getPath(), srcFile.getName());
            } else {
                targetFile = new File(dcimFolder.getPath(), newName);
            }
            long l = RIo.copyFile(srcFile, targetFile);
            boolean result = l != -1;
            if (result) {
                scanFile(RApplication.getApp(), targetFile.getAbsolutePath());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将视频插入图库
     *
     * @param videoPath 视频路径地址
     */
    public static boolean updateVideo(Context context, String videoPath, long videoDuration /*毫秒*/) {
        File file = new File(videoPath);
        if (!file.exists()) {
            return false;
        }
        //获取ContentResolve对象，来操作插入视频
        ContentResolver localContentResolver = context.getContentResolver();
        //ContentValues：用于储存一些基本类型的键值对
        ContentValues localContentValues = getVideoContentValues(context, file, System.currentTimeMillis(), videoDuration);
        //insert语句负责插入一条新的纪录，如果插入成功则会返回这条记录的id，如果插入失败会返回-1。
        Uri localUri = localContentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
        L.e("call: updateVideo([context, videoPath])-> " + localUri);
        return localUri != null;
    }

    //再往数据库中插入数据的时候将，将要插入的值都放到一个ContentValues的实例当中
    public static ContentValues getVideoContentValues(Context paramContext, File paramFile, long addTime, long videoDuration) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Video.Media.TITLE, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, paramFile.getName());
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        localContentValues.put(MediaStore.Video.Media.DURATION, videoDuration);
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, Long.valueOf(addTime));
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, Long.valueOf(addTime));
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, Long.valueOf(addTime));
        localContentValues.put(MediaStore.Video.Media.DATA, paramFile.getAbsolutePath());
        localContentValues.put(MediaStore.Video.Media.SIZE, Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    //android把图片文件添加到相册
    public static void udpateMedia(Context context, String url) {
        //图片路径
        File file = new File(url);
        ContentResolver localContentResolver = context.getContentResolver();
        ContentValues localContentValues = getImageContentValues(context, file, System.currentTimeMillis());
        localContentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
        Intent localIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        final Uri localUri = uriFromFile(file);
        localIntent.setData(localUri);
        //发送广播即时更新图库
        context.sendBroadcast(localIntent);
    }

    //再往数据库中插入数据的时候将，将要插入的值都放到一个ContentValues的实例当中
    public static ContentValues getImageContentValues(Context paramContext, File paramFile, long paramLong) {
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("title", paramFile.getName());
        localContentValues.put("_display_name", paramFile.getName());
        localContentValues.put("mime_type", "image/jpeg");
        localContentValues.put("datetaken", Long.valueOf(paramLong));
        localContentValues.put("date_modified", Long.valueOf(paramLong));
        localContentValues.put("date_added", Long.valueOf(paramLong));
        localContentValues.put("orientation", Integer.valueOf(0));
        localContentValues.put("_data", paramFile.getAbsolutePath());
        localContentValues.put("_size", Long.valueOf(paramFile.length()));
        return localContentValues;
    }

    /**
     * 请求拿到音频焦点
     */
    public static void requestAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);//请求焦点
    }

    /**
     * 释放音频焦点
     */
    public static void abandonAudioFocus(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);//放弃焦点
    }

    /**
     * 简单的压缩图片
     * 图片对象有可能会很大, 但是转成成 bytes之后, 会很小, 此方法会判断不准确
     */
    @Deprecated
    public static Bitmap compressBitmap(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;

        while (baos.toByteArray().length / 1024 > 200) { // 循环判断如果压缩后图片是否大于200kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }

    public final static Bitmap compressBitmap(Bitmap image, int inSampleSize) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bts = baos.toByteArray();

            final BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
            options.inSampleSize = inSampleSize;
//            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeByteArray(bts, 0, bts.length, options);
        } catch (Exception e) {
            e.printStackTrace();
            return image;
        }
    }

    /**
     * 获取图片的宽高
     */
    public final static int[] getBitmapSize(String filePath) {
        int[] size = new int[]{-1, -1};
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            size[0] = options.outWidth;
            size[1] = options.outHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    public static void logProcessInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        /**只能拿到自己程序的进程列表*/
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            L.e("logProcessInfo([context])-> " + info.processName + " " + info.pid);
//            TaskInfo taskInfo = new TaskInfo();
//            //进程名称
//            String packageName = info.processName;
//            taskInfo.setPackageName(packageName);
//            try {
//                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);
//                //图标
//                Drawable task_icon = applicationInfo.loadIcon(pm);
//                if (task_icon == null) {
//                    taskInfo.setTask_icon(context.getResources().getDrawable(R.drawable.ic_launcher));
//                } else {
//                    taskInfo.setTask_icon(task_icon);
//                }
//                //名称
//                String task_name = applicationInfo.loadLabel(pm).toString();
//                taskInfo.setTask_name(task_name);
//            } catch (PackageManager.NameNotFoundException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//            //进程id
//            int pid = info.pid;
//            taskInfo.setPid(pid);
//            //获取进程占用的内存
//            android.os.Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(new int[]{pid});
//            android.os.Debug.MemoryInfo memoryInfo = processMemoryInfo[0];
//            long totalPrivateDirty = memoryInfo.getTotalPrivateDirty(); //KB
//            taskInfo.setTask_memory(totalPrivateDirty);
//            taskInfos.add(taskInfo);
        }
    }

    public static void killMyAllProcess(Context context) {
        //PackageManager pm = context.getPackageManager();
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        /**只能拿到自己程序的进程列表*/
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            //L.e("logProcessInfo([context])-> " + info.processName + " " + info.pid);
            if (!TextUtils.equals(info.processName, context.getPackageName()) && info.processName.contains(context.getPackageName())) {
                Process.killProcess(info.pid);
            }
        }
        Process.killProcess(Process.myPid());
    }

    public static String getShotTimeString(long milliseconds) {
        return getShotTimeString(milliseconds, true);
    }

    public static String getShotTimeString(long milliseconds, boolean abbreviate) {
        return TimeUtil.getTimeShowString(milliseconds, abbreviate);
    }

    /**
     * 获取视频时长 毫秒
     */
    public static long getVideoDuration(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        long time = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        retriever.release();
        return time;
    }

    public static int randomColor(Random random) {
        return randomColor(random, 120, 250);
    }

    /**
     * 随机颜色, 设置一个最小值, 设置一个最大值, 第三个值在这2者之间随机改变
     */
    public static int randomColor(Random random, int minValue, int maxValue) {
        int a = minValue + random.nextInt(maxValue - minValue);
        int b = minValue;
        int c = maxValue;
        List<Integer> list1 = new ArrayList<>();
        List<Integer> list2 = new ArrayList<>();
        list1.add(a);
        list1.add(b);
        list1.add(c);

        while (list2.size() != 3) {
            int i = random.nextInt(list1.size());
            list2.add(list1.remove(i));
        }

        return Color.rgb(list2.get(0), list2.get(1), list2.get(2));
    }

    /**
     * 根据包名 启动其他应用
     */
    public static String startApp(String packageName) {
        String error = "";
        try {
            CmdUtil.startApp(RApplication.getApp(), packageName);
        } catch (Exception e) {
            e.printStackTrace();
            error = e.getMessage();
        }

        return error;
    }

    /**
     * 是否有浮窗权限 xml需要  Manifest.permission.SYSTEM_ALERT_WINDOW
     */
    public static boolean canDrawOverlays() {
        return SettingsCompat.canDrawOverlays(RApplication.getApp());
    }

    public static String getClassSimpleName(Class<?> cls) {
        if (cls == null) {
            return "NoClassName";
        }
        if (cls.isAnonymousClass()) {
            //匿名类
            return getClassSimpleName(cls.getSuperclass());
        }
        return cls.getSimpleName();
    }

    public static boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public static Uri uriFromFile(File file) {
        return getFileUri(RApplication.getApp(), file);
    }

    public enum ImageType {
        JPEG, GIF, PNG, BMP, UNKNOWN
    }

    interface OnPutValue {
        void onValue(String key, String value);
    }
}
