package com.lzy.imagepicker;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.angcyo.github.utilcode.utils.ImageUtils;
import com.angcyo.library.okhttp.Ok;
import com.angcyo.library.utils.L;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.lzy.imagepicker.loader.ImageLoader;

import java.io.File;

import pl.droidsonroids.gif.GifDrawableBuilder;

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
                RequestBuilder<File> gifRequestBuilder = Glide.with(imageView.getContext())
                        .asFile()
                        .load(file)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA).placeholder(placeholderDrawable));
                gifRequestBuilder.into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, Transition<? super File> transition) {
                        try {
                            imageView.setImageDrawable(new GifDrawableBuilder().from(resource).build());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Glide.with(imageView.getContext())
                        .load(file)
                        .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL).placeholder(placeholderDrawable))
                        .into(imageView);
            }
        } else {
            Ok.instance().type(url, new Ok.OnImageTypeListener() {
                @Override
                public void onImageType(String imageUrl, Ok.ImageType imageType) {
                    L.v("call: onImageType([imageType])-> " + imageUrl + " : " + imageType);

                    if (imageType != Ok.ImageType.UNKNOWN) {
                        if (imageType == Ok.ImageType.GIF) {
                            RequestBuilder<File> gifRequestBuilder = Glide.with(imageView.getContext())
                                    .asFile()
                                    .load(imageUrl)
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA).placeholder(placeholderDrawable));
                            gifRequestBuilder.into(new SimpleTarget<File>() {
                                @Override
                                public void onResourceReady(File resource, Transition<? super File> transition) {
                                    try {
                                        imageView.setImageDrawable(new GifDrawableBuilder().from(resource).build());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } else {
                            Glide.with(imageView.getContext())
                                    .load(imageUrl)
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL).placeholder(placeholderDrawable))
                                    .into(imageView);
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
                             String url, final ImageView imageView, int width, int height) {
        if (imageView == null) {
            return;
        }

        if (TextUtils.isEmpty(path)) {
            loadUrlImage(activity, thumbPath, url, imageView);
        } else {
            File file = new File(path);
            if ("GIF".equalsIgnoreCase(ImageUtils.getImageType(file))) {
                RequestBuilder<File> gifRequestBuilder = Glide.with(imageView.getContext())
                        .asFile()
                        .load(file);
                RequestOptions requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA);

                if (TextUtils.isEmpty(thumbPath)) {
                    requestOptions.placeholder(R.mipmap.base_zhanweitu_klg);     //设置占位图片
                } else if ("no".equalsIgnoreCase(thumbPath)) {

                } else {
                    requestOptions.placeholder(new BitmapDrawable(activity.getResources(), thumbPath));
                }
                gifRequestBuilder.apply(requestOptions);
                gifRequestBuilder.into(new SimpleTarget<File>() {
                    @Override
                    public void onResourceReady(File resource, Transition<? super File> transition) {
                        try {
                            imageView.setImageDrawable(new GifDrawableBuilder().from(resource).build());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else {
                RequestOptions requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL);

                RequestBuilder<Drawable> builder = Glide.with(imageView.getContext())
                        .load(file);

                if (TextUtils.isEmpty(thumbPath)) {
                    requestOptions.placeholder(R.mipmap.base_zhanweitu_klg);     //设置占位图片

                } else if ("no".equalsIgnoreCase(thumbPath)) {
                } else {
                    requestOptions.placeholder(new BitmapDrawable(activity.getResources(), thumbPath));
                }
                builder.apply(requestOptions);
                builder.into(imageView);
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
                public void onImageType(String imageUrl, Ok.ImageType imageType) {
                    L.d("call: onImageType([imageType])-> " + imageUrl + " : " + imageType);

                    if (imageType != Ok.ImageType.UNKNOWN) {
                        if (imageType == Ok.ImageType.GIF) {
                            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                            RequestBuilder<File> gifRequestBuilder = Glide.with(imageView.getContext())
                                    .asFile()
                                    .load(imageUrl)
                                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA).placeholder(R.mipmap.base_zhanweitu_klg));
                            gifRequestBuilder.into(new SimpleTarget<File>() {
                                @Override
                                public void onResourceReady(File resource, Transition<? super File> transition) {
                                    try {
                                        imageView.setImageDrawable(new GifDrawableBuilder().from(resource).build());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        } else {
                            RequestOptions requestOptions = RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.ALL);

                            RequestBuilder<Drawable> builder = Glide.with(imageView.getContext())
                                    .load(imageUrl);

                            if (TextUtils.isEmpty(thumbPath)) {
                                requestOptions.placeholder(R.mipmap.base_zhanweitu_klg);     //设置占位图片

                            } else if ("no".equalsIgnoreCase(thumbPath)) {
                            } else {
                                requestOptions.placeholder(new BitmapDrawable(activity.getResources(), thumbPath));
                            }
                            builder.apply(requestOptions);
                            builder.into(imageView);
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
