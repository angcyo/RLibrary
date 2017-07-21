package com.lzy.imagepicker.bean;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.lzy.imagepicker.ImageDataSource;

import java.io.Serializable;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片信息
 * 修订历史：
 * ================================================
 */
public class ImageItem implements Serializable {

    public String name;       //图片的名字
    public String path;       //路径
    public String thumbPath;  //图片的占位路径
    public String url;        //图片的网络地址
    public Drawable placeholderDrawable; //第一次需要显示的占位图
    public long size;         //大小
    public int width;         //宽度
    public int height;        //高度
    public String mimeType;   //类型
    public long addTime;      //创建时间

    //星期二 2017-6-13 支持扫描视频文件
    public long videoDuration = -1;//视频时长
    public String videoThumbPath = "";//
    public String resolution;// width x height or height x width
    public int videoRotation = 0;//视频的宽高,已通过旋转方向自动调整了

    @ImageDataSource.LoaderType
    public int loadType;

    //是否可以保存到本地
    public boolean canSave = true;

    //保存对应视图的坐标位置信息
    public Rect mViewLocation = new Rect();

    public ImageItem(int loadType) {
        this.loadType = loadType;
    }

    public ImageItem() {
        this(ImageDataSource.IMAGE);
    }

    /**
     * 图片的路径和创建时间相同就认为是同一张图片
     */
    @Override
    public boolean equals(Object o) {
        try {
            ImageItem other = (ImageItem) o;
            return this.path.equalsIgnoreCase(other.path) && this.addTime == other.addTime;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }
}
