package com.lzy.imagepicker;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.angcyo.library.okhttp.Ok;
import com.angcyo.library.utils.L;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.GifRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.lzy.imagepicker.loader.ImageLoader;

import java.io.File;

public class GlideImageLoader implements ImageLoader {

    /**
     * 最简单的显示图片(自动判断是否是GIF)
     */
    public static void displayImage(final ImageView imageView, final String url) {
        displayImage(imageView, url, -1);
    }

    /**
     * 可以设置占位图的
     */
    public static void displayImage(final ImageView imageView, final String url, final int placeholderRes) {
        if (imageView == null) {
            return;
        }
        displayImage(imageView, url, placeholderRes == -1 ? null : ContextCompat.getDrawable(imageView.getContext(), placeholderRes));
    }

    public static void displayImage(final ImageView imageView, final String url, final Drawable placeholderDrawable) {
        if (imageView == null) {
            return;
        }
        File file = new File(url);
        if (file.exists()) {
            if ("GIF".equalsIgnoreCase(ImageUtils.getImageType(file))) {
                GifRequestBuilder<File> gifRequestBuilder = Glide.with(imageView.getContext())
                        .load(file)
                        .asGif()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE);
                gifRequestBuilder.placeholder(placeholderDrawable);
                gifRequestBuilder.into(imageView);
            } else {
                final DrawableRequestBuilder<File> requestBuilder = Glide.with(imageView.getContext())
                        .load(file)
                        .diskCacheStrategy(DiskCacheStrategy.ALL);
                requestBuilder.placeholder(placeholderDrawable);
                requestBuilder.into(imageView);
            }
        } else {
            Ok.instance().type(url, new Ok.OnImageTypeListener() {
                @Override
                public void onImageType(Ok.ImageType imageType) {
                    L.v("call: onImageType([imageType])-> " + url + " : " + imageType);

                    if (imageType != Ok.ImageType.UNKNOWN) {
                        if (imageType == Ok.ImageType.GIF) {
                            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            GifRequestBuilder<String> requestBuilder = Glide.with(imageView.getContext())
                                    .load(url)
                                    .asGif()
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE);
                            requestBuilder.placeholder(placeholderDrawable);
                            requestBuilder.into(imageView);
                        } else {
                            final DrawableRequestBuilder<String> drawableRequestBuilder = Glide.with(imageView.getContext())
                                    .load(url)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL);

                            drawableRequestBuilder.placeholder(placeholderDrawable);
                            drawableRequestBuilder.into(imageView);
                        }
                    }
                }

                @Override
                public void onLoadStart() {
                }
            });

        }
    }

    @Override
    public void displayImage(Activity activity, String path, String thumbPath,
                             String url, ImageView imageView, int width, int height) {
        if (imageView == null) {
            return;
        }

        if (TextUtils.isEmpty(path)) {
            loadUrlImage(activity, thumbPath, url, imageView);
        } else {
            File file = new File(path);
            if ("GIF".equalsIgnoreCase(ImageUtils.getImageType(file))) {
                GifRequestBuilder<File> gifRequestBuilder = Glide.with(activity)                             //配置上下文
                        .load(file)      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                        //.error(R.mipmap.default_image)           //设置错误图片
                        //.fitCenter()
                        .asGif()
                        //.centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE);

                if (TextUtils.isEmpty(thumbPath)) {
                    gifRequestBuilder.placeholder(R.mipmap.base_zhanweitu_klg)     //设置占位图片
                            .into(imageView);
                } else if ("no".equalsIgnoreCase(thumbPath)) {
                    gifRequestBuilder.into(imageView);
                } else {
                    gifRequestBuilder.placeholder(new BitmapDrawable(activity.getResources(), thumbPath))
                            .into(imageView);
                }
            } else {
                final DrawableRequestBuilder<File> requestBuilder = Glide.with(activity)                             //配置上下文
                        .load(file)      //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                        //.error(R.mipmap.default_image)           //设置错误图片
                        //.fitCenter()
                        //.centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);

                if (TextUtils.isEmpty(thumbPath)) {
                    requestBuilder.placeholder(R.mipmap.base_zhanweitu_klg)     //设置占位图片
                            .into(imageView);
                } else if ("no".equalsIgnoreCase(thumbPath)) {
                    requestBuilder.into(imageView);
                } else {
                    requestBuilder.placeholder(new BitmapDrawable(activity.getResources(), thumbPath))
                            .into(imageView);
                }
            }
        }


    }

    /**
     * 可以自定义占位图的
     */
    protected void loadUrlImage(final Activity activity, final String thumbPath, final String url, final ImageView imageView) {
        if (YImageControl.isYellowImage(url)) {
            YImageControl.showYellowImageXiao(imageView);
        } else {
            Ok.instance().type(url, new Ok.OnImageTypeListener() {
                @Override
                public void onImageType(Ok.ImageType imageType) {
                    L.d("call: onImageType([imageType])-> " + url + " : " + imageType);

                    if (imageType != Ok.ImageType.UNKNOWN) {
                        if (imageType == Ok.ImageType.GIF) {
                            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            Glide.with(imageView.getContext())
                                    .load(url)
                                    .asGif()
                                    .placeholder(R.mipmap.base_zhanweitu_klg)
                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                    .into(imageView);
                        } else {
                            final DrawableRequestBuilder<String> drawableRequestBuilder = Glide.with(activity)                             //配置上下文
                                    .load(url)                                       //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                                    //.error(R.mipmap.default_image)                    //设置错误图片
                                    //.fitCenter()
                                    //.centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL);

                            if (TextUtils.isEmpty(thumbPath)) {
                                drawableRequestBuilder.placeholder(R.mipmap.base_zhanweitu_klg)     //设置占位图片
                                        .into(imageView);
                            } else {
                                drawableRequestBuilder.placeholder(new BitmapDrawable(activity.getResources(), thumbPath))
                                        .into(imageView);
                            }
                        }
                    }
                }

                @Override
                public void onLoadStart() {
                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    imageView.setImageResource(R.mipmap.base_zhanweitu_klg);
                }
            });
        }
    }

    @Override
    public void clearMemoryCache() {
        //这里是清除缓存的方法,根据需要自己实现
    }
}
