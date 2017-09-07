package com.lzy.imagepicker.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.angcyo.library.okhttp.Ok;
import com.angcyo.library.utils.L;
import com.angcyo.library.widget.DragPhotoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.lzy.imagepicker.ImageDataSource;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.ImagePickerHelper;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.Utils;
import com.lzy.imagepicker.YImageControl;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.loader.ImageLoader;
import com.lzy.imagepicker.ui.VideoPlayActivity;
import com.lzy.imagepicker.view.ImagePickerImageView;
import com.lzy.imagepicker.view.SimpleCircleProgressBar;

import java.io.File;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawableBuilder;


/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：2016-12-13
 * ================================================
 */
public class ImagePageAdapter extends PagerAdapter {

    public PhotoViewClickListener listener;
    DragPhotoView.OnExitListener mOnExitListener;
    PhotoViewLongClickListener mPhotoViewLongClickListener;
    private int screenWidth;
    private int screenHeight;
    private ImagePicker imagePicker;
    private ArrayList<ImageItem> images = new ArrayList<>();
    private Activity mActivity;
    private boolean enableMoveExit = true;

    //当前是缩略图在显示图片, 还是大图在显示图片
    private View currentImageView;

    public ImagePageAdapter(Activity activity, ArrayList<ImageItem> images) {
        this.mActivity = activity;
        this.images = images;

        DisplayMetrics dm = Utils.getScreenPix(activity);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        imagePicker = ImagePicker.getInstance();
    }

    /**
     * @param path      本地大图路径
     * @param thumbPath 本地小图路径
     * @param url       图片网络路径
     */
    public static void displayImage(Activity activity, String path, String thumbPath, String url, ImageView imageView, int width, int height) {
//        if (TextUtils.isEmpty(path)) {
//            final DrawableRequestBuilder<String> drawableRequestBuilder = Glide.with(activity)                             //配置上下文
//                    .load(url)                                       //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
//                    //.error(R.mipmap.default_image)                    //设置错误图片
//                    //.fitCenter()
//                    //.centerCrop()
//                    //.diskCacheStrategy(DiskCacheStrategy.ALL)
//                    ;
//
//            if (TextUtils.isEmpty(thumbPath)) {
//                drawableRequestBuilder.placeholder(R.mipmap.default_image)     //设置占位图片
//                        .into(imageView);
//            } else {
//                drawableRequestBuilder.placeholder(new BitmapDrawable(activity.getResources(), thumbPath))
//                        .into(imageView);
//            }
//        } else {
//            DrawableRequestBuilder<File> fileDrawableRequestBuilder = Glide.with(activity)
//                    .load(new File(path));//.error(R.mipmap.default_image);
//
//            if (TextUtils.isEmpty(thumbPath)) {
//                fileDrawableRequestBuilder.placeholder(R.mipmap.default_image)     //设置占位图片
//                        .into(imageView);
//            } else {
//                fileDrawableRequestBuilder.placeholder(new BitmapDrawable(activity.getResources(), thumbPath))
//                        .into(imageView);
//            }
//        }
        ImageLoader imageLoader = ImagePicker.getInstance().getImageLoader();
        if (imageLoader == null) {
            ImagePickerHelper.init();
        }
        imageLoader.displayImage(activity, path, thumbPath, url, imageView, width, height);
    }

    public void setData(ArrayList<ImageItem> images) {
        this.images = images;
    }

    public void resetData(ArrayList<ImageItem> images) {
        this.images = images;
        notifyDataSetChanged();
    }

    public void setPhotoViewClickListener(PhotoViewClickListener listener) {
        this.listener = listener;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        FrameLayout itemLayout = new FrameLayout(mActivity);

        //支持手势的图片
        final DragPhotoView photoView = new DragPhotoView(mActivity);
        photoView.setEnableMoveExit(enableMoveExit);
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        //缩略图显示
        final ImagePickerImageView thumbImageView = new ImagePickerImageView(mActivity);
        thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        currentImageView = thumbImageView;

        //进度条显示
        //final MaterialProgressView progressView = new MaterialProgressView(mActivity);
        final SimpleCircleProgressBar progressView = new SimpleCircleProgressBar(mActivity);
        progressView.setVisibility(View.GONE);

        //item data
        final ImageItem imageItem = images.get(position);
        /*imagePicker.getImageLoader().*/

        //显示视频播放按钮
        if (imageItem.loadType == ImageDataSource.VIDEO) {
            thumbImageView.setPlayDrawable(R.drawable.image_picker_play);
        } else {
            thumbImageView.setPlayDrawable(null);
        }

        String thumbPath = imageItem.thumbPath;
        //显示缩略图
        if (imageItem.placeholderDrawable != null) {
            //thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbImageView.setImageDrawable(imageItem.placeholderDrawable);
        } else {

            if (!TextUtils.isEmpty(thumbPath)) {
                File thumbFile = new File(thumbPath);
                if (thumbFile.exists()) {
                    Glide.with(mActivity).load(thumbFile).apply(RequestOptions.noAnimation()).into(thumbImageView);
                }
            }
        }

        //显示真正的图片
        if (imageItem.loadType == ImageDataSource.VIDEO) {
            if (!TextUtils.isEmpty(imageItem.videoThumbPath)) {
                File videoThumbFile = new File(imageItem.videoThumbPath);
                if (videoThumbFile.exists()) {
                    thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    Glide.with(mActivity).load(videoThumbFile).apply(RequestOptions.noAnimation()).into(thumbImageView);
                }
            }
            thumbImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VideoPlayActivity.launcher(mActivity, imageItem.path);
                }
            });
        } else {
            if (!TextUtils.isEmpty(imageItem.path) && new File(imageItem.path).exists()) {
                //加载本地图片
                displayImage(mActivity, imageItem.path, "no",
                        imageItem.url, photoView, screenWidth, screenHeight);
                thumbImageView.setVisibility(View.GONE);
                currentImageView = photoView;
            } else {
                //加载网络图片
                thumbImageView.setVisibility(View.VISIBLE);
                progressView.setVisibility(View.VISIBLE);

                if (YImageControl.isYellowImage(imageItem.url)) {
                    YImageControl.showYellowImageXiao(photoView);
                    photoView.setScaleType(ImageView.ScaleType.CENTER);
                } else {
                    progressView.start();
                    if (imageItem.placeholderDrawable instanceof TransitionDrawable) {
                        TransitionDrawable transitionDrawable = (TransitionDrawable) imageItem.placeholderDrawable;
                        thumbImageView.setImageDrawable(transitionDrawable.getDrawable(transitionDrawable.getNumberOfLayers() - 1));
                    } else {
                        thumbImageView.setImageDrawable(imageItem.placeholderDrawable);
                    }

                    if (!TextUtils.isEmpty(imageItem.path)) {
                        File file = new File(imageItem.path);
                        if (file.exists()) {
                            Glide.with(mActivity)
                                    .load(file)
                                    .into(photoView);
                            thumbImageView.setVisibility(View.GONE);
                            progressView.setVisibility(View.GONE);
                            progressView.stop();

                            currentImageView = photoView;
                        } else {
                            loadImage(photoView, thumbImageView, progressView, imageItem);
                        }
                    } else {
                        loadImage(photoView, thumbImageView, progressView, imageItem);
                    }
                }
            }
        }

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.OnPhotoTapListener(v, 0, 0);
            }
        });

        photoView.setOnTapListener(new DragPhotoView.OnTapListener() {
            @Override
            public void onTap(DragPhotoView view, float x, float y) {
                if (listener != null) listener.OnPhotoTapListener(view, x, y);
            }
        });
        photoView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if (listener != null) listener.OnPhotoTapListener(view, x, y);
            }
        });
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mPhotoViewLongClickListener == null || photoView.isMove()) {
                    return false;
                } else {
                    mPhotoViewLongClickListener.onLongClickListener(photoView, position, imageItem);
                    return false;
                }
            }
        });
        photoView.setOnExitListener(mOnExitListener);

        itemLayout.addView(thumbImageView, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        itemLayout.addView(photoView);
        itemLayout.addView(progressView, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        container.addView(itemLayout);
        return itemLayout;
    }

    public void setEnableMoveExit(boolean enableMoveExit) {
        this.enableMoveExit = enableMoveExit;
    }

    protected void loadImage(final PhotoView photoView, final ImageView imageView,
                             final SimpleCircleProgressBar progressView, final ImageItem imageItem) {

        Ok.instance().type(imageItem.url, new Ok.OnImageTypeListener() {
            @Override
            public void onImageType(String imageUrl, Ok.ImageType imageType) {
                L.e("call: onImageType([imageType])-> " + imageItem.url + " : " + imageType);

//                if (!imageUrl.contains(imageView.getTag())) {
//                }

                if (imageType != Ok.ImageType.UNKNOWN) {
                    if (imageType == Ok.ImageType.GIF) {
                        Glide.with(imageView.getContext())
                                .downloadOnly()
                                .load(imageItem.url)
                                .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.DATA))
                                .into(new SimpleTarget<File>() {
                                    @Override
                                    public void onResourceReady(File resource, Transition<? super File> transition) {
                                        if (progressView == null || photoView == null) {
                                            return;
                                        }
                                        imageView.setVisibility(View.GONE);
                                        progressView.setVisibility(View.GONE);
                                        progressView.stop();

                                        try {
                                            photoView.setImageDrawable(new GifDrawableBuilder().from(resource).build());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        currentImageView = photoView;
                                    }
                                });
                    } else {
                        Glide.with(mActivity).asBitmap().load(imageItem.url).into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                if (progressView == null || photoView == null) {
                                    return;
                                }
                                imageView.setVisibility(View.GONE);
                                progressView.setVisibility(View.GONE);
                                progressView.stop();
                                photoView.setImageBitmap(resource);

                                currentImageView = photoView;
                            }
                        });
                    }
                }
            }

            @Override
            public void onLoadStart() {
            }
        });
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public void setOnExitListener(DragPhotoView.OnExitListener onExitListener) {
        mOnExitListener = onExitListener;
    }

    public void setPhotoViewLongClickListener(PhotoViewLongClickListener photoViewLongClickListener) {
        mPhotoViewLongClickListener = photoViewLongClickListener;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public View getCurrentImageView() {
        return currentImageView;
    }

    public interface PhotoViewClickListener {
        void OnPhotoTapListener(View view, float v, float v1);
    }

    public interface PhotoViewLongClickListener {
        void onLongClickListener(PhotoView photoView, int position, ImageItem item);
    }
}
