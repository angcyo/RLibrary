package com.angcyo.picker.media;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/06/19 16:33
 * 修改人员：Robi
 * 修改时间：2018/06/19 16:33
 * 修改备注：
 * Version: 1.0.0
 */
public class TakeUtils {
    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    public static File createFile(File folder, String prefix, String suffix) {
        if (!folder.exists() || !folder.isDirectory()) folder.mkdirs();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA);
        String filename = prefix + dateFormat.format(new Date(System.currentTimeMillis())) + suffix;
        return new File(folder, filename);
    }

    /**
     * 拍照的方法
     */
    public static File takePicture(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        File takeImageFile = null;
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/camera/");
            takeImageFile = createFile(takeImageFile, "IMG_", ".jpg");
            Uri fileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(activity, activity.getPackageName(), takeImageFile);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                fileUri = Uri.fromFile(takeImageFile);
            }
            // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
            // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
            // 如果没有指定uri，则data就返回有数据！
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        }
        activity.startActivityForResult(takePictureIntent, requestCode);

        return takeImageFile;
    }

    /**
     * 录制视频
     */
    public static File recordVideo(Activity activity, int requestCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        takePictureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        File takeImageFile = null;

        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            takeImageFile = new File(Environment.getExternalStorageDirectory(), "/DCIM/Video/");
            takeImageFile = createFile(takeImageFile, "Video_", ".mp4");
            Uri fileUri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fileUri = FileProvider.getUriForFile(activity, activity.getPackageName(), takeImageFile);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            } else {
                fileUri = Uri.fromFile(takeImageFile);
            }
            // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
            // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
            // 如果没有指定uri，则data就返回有数据！
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        }
        activity.startActivityForResult(takePictureIntent, requestCode);
        return takeImageFile;
    }
}
