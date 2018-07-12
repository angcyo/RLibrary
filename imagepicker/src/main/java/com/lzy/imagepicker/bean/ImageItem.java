package com.lzy.imagepicker.bean;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.lzy.imagepicker.ImageDataSource;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：图片信息
 * 修订历史：
 * ================================================
 */
public class ImageItem implements Parcelable {

    public String name;       //图片的名字
    public String path;       //路径
    public String thumbPath;  //图片的占位路径
    public String url;        //图片的网络地址
    public Drawable placeholderDrawable; //第一次需要显示的占位图
    public long size;         //大小
    public int width;         //宽度
    public int height;        //高度
    public String mimeType;   //类型
    public long addTime = -1;      //创建时间 秒

    //星期二 2017-6-13 支持扫描视频文件
    public long videoDuration = -1;//视频时长, 毫秒
    public String videoThumbPath = "";//
    public String resolution;// width x height or height x width
    public int videoRotation = 0;//视频的宽高,已通过旋转方向自动调整了

    //@ImageDataSource.LoaderType
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

    public ImageItem(String path) {
        this.url = path;
        this.path = path;
        this.thumbPath = path;
    }

    /**
     * 图片的路径和创建时间相同就认为是同一张图片
     */
    @Override
    public boolean equals(Object o) {
        try {
            ImageItem other = (ImageItem) o;
            return this.path.equalsIgnoreCase(other.path)/* &&
                    (this.addTime != -1 &&
                            other.addTime != -1 &&
                            this.addTime == other.addTime)*/;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeString(this.thumbPath);
        dest.writeString(this.url);
        dest.writeLong(this.size);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.mimeType);
        dest.writeLong(this.addTime);
        dest.writeLong(this.videoDuration);
        dest.writeString(this.videoThumbPath);
        dest.writeString(this.resolution);
        dest.writeInt(this.videoRotation);
        dest.writeInt(this.loadType);
        dest.writeByte(this.canSave ? (byte) 1 : (byte) 0);
        dest.writeParcelable(this.mViewLocation, flags);
    }

    protected ImageItem(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.thumbPath = in.readString();
        this.url = in.readString();
        this.size = in.readLong();
        this.width = in.readInt();
        this.height = in.readInt();
        this.mimeType = in.readString();
        this.addTime = in.readLong();
        this.videoDuration = in.readLong();
        this.videoThumbPath = in.readString();
        this.resolution = in.readString();
        this.videoRotation = in.readInt();
        this.loadType = in.readInt();
        this.canSave = in.readByte() != 0;
        this.mViewLocation = in.readParcelable(Rect.class.getClassLoader());
    }

    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };
}
