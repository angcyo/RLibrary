package com.lzy.imagepicker.adapter;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.imagepicker.GlideImageLoader;
import com.lzy.imagepicker.ImageDataSource;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.Utils;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageBaseActivity;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.ImagePickerImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.lzy.imagepicker.ImageDataSource.VIDEO;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧 Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：星期二 2017-2-21
 * 修订历史：2016-12-13
 * ================================================
 */
public class ImageGridAdapter2 extends RecyclerView.Adapter<ImageViewHolder> {

    private static final int ITEM_TYPE_CAMERA = 0;  //第一个条目是相机
    private static final int ITEM_TYPE_NORMAL = 1;  //第一个条目不是相机
    TextureView mTextureView;
    private ImagePicker imagePicker;
    private Activity mActivity;
    private ArrayList<ImageItem> images;       //当前需要显示的所有的图片数据
    private ArrayList<ImageItem> mSelectedImages; //全局保存的已经选中的图片数据
    private boolean isShowCamera;         //是否显示拍照按钮
    private int mImageSize;               //每个条目的大小
    private OnImageItemClickListener listener;   //图片被点击的监听
    private int loadType = ImageDataSource.IMAGE;

    public ImageGridAdapter2(Activity activity, ArrayList<ImageItem> images, int loadType) {
        this.mActivity = activity;
        this.loadType = loadType;
        if (images == null || images.size() == 0) this.images = new ArrayList<>();
        else this.images = images;

        mImageSize = Utils.getImageItemWidth(mActivity);
        imagePicker = ImagePicker.getInstance();
        isShowCamera = imagePicker.isShowCamera();
        mSelectedImages = imagePicker.getSelectedImages();
    }

    static String getVideoTime(long time) {
        final long videoTime = time;
        long min = videoTime / 60;
        long second = videoTime % 60;

        StringBuilder builder = new StringBuilder();
        builder.append(min >= 10 ? min : ("0" + min));
        builder.append(":");
        builder.append(second >= 10 ? second : ("0" + second));

        return builder.toString();
    }


    public void refreshData(ArrayList<ImageItem> images) {
        if (images == null || images.size() == 0) this.images = new ArrayList<>();
        else this.images = images;
        notifyDataSetChanged();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView;
        if (viewType == ITEM_TYPE_CAMERA) {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.adapter_camera_item, parent, false);
        } else {
            convertView = LayoutInflater.from(mActivity).inflate(R.layout.adapter_image_list_item, parent, false);
        }
        convertView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize)); //让图片是个正方形
        return new ImageViewHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final ImageViewHolder holder, final int position) {
        View convertView = holder.itemView;
        if (holder.getItemViewType() == ITEM_TYPE_CAMERA) {
            convertView.setTag(null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!((ImageBaseActivity) mActivity).checkPermission(Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CAMERA}, ImageGridActivity.REQUEST_PERMISSION_CAMERA);
                    } else {
                        if (loadType == VIDEO) {
                            imagePicker.recordVideo(mActivity, ImagePicker.REQUEST_CODE_RECORD_VIDEO);
                        } else {
                            imagePicker.takePicture(mActivity, ImagePicker.REQUEST_CODE_TAKE);
                        }
                    }
                }
            });
            mTextureView = (TextureView) convertView.findViewById(R.id.texture_view);
            TextView tipView = (TextView) convertView.findViewById(R.id.tip_view);

            if (loadType == VIDEO) {
                tipView.setText("录制视频");
            } else {
                tipView.setText("拍摄照片");
            }
        } else {
            final ImageItem imageItem = getItem(position);
            final CheckBox checkBox = holder.v(R.id.cb_check);
            final View maskView = holder.v(R.id.mask);
            final ImagePickerImageView thumbImageView = holder.v(R.id.iv_thumb);

            thumbImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (loadType == VIDEO) {
                //视频类型处理
                thumbImageView.setPlayDrawable(R.drawable.image_picker_play);
                TextView textView = holder.v(R.id.video_duration_view);
                textView.setText(getVideoTime(imageItem.videoDuration / 1000));

                //创建视频缩略图
                ThumbLoad.createThumbFile(new WeakReference<>(mActivity), new WeakReference<RecyclerView.Adapter>(this)
                        , imageItem, position);

                GlideImageLoader.displayImage(thumbImageView, imageItem.videoThumbPath, R.drawable.image_placeholder_shape); //显示图片
                //imagePicker.getImageLoader().displayImage(mActivity, imageItem.videoThumbPath, "no", "", thumbImageView, mImageSize, mImageSize);
            } else {
                GlideImageLoader.displayImage(thumbImageView, imageItem.path, R.drawable.image_placeholder_shape); //显示图片
            }

            thumbImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onImageItemClick(holder.itemView, imageItem, position);
                }
            });
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectLimit = imagePicker.getSelectLimit();
                    if (checkBox.isChecked() && mSelectedImages.size() >= selectLimit) {
                        Toast.makeText(mActivity.getApplicationContext(),
                                mActivity.getString(loadType == VIDEO ? R.string.select_video_limit : R.string.select_limit, selectLimit + ""),
                                Toast.LENGTH_SHORT).show();
                        checkBox.setChecked(false);
                        maskView.setVisibility(View.GONE);
                    } else {
                        imagePicker.addSelectedImageItem(position, imageItem, checkBox.isChecked());
                        maskView.setVisibility(View.VISIBLE);
                    }
                }
            });
            //根据是否多选，显示或隐藏checkbox
            if (imagePicker.isMultiMode()) {
                checkBox.setVisibility(View.VISIBLE);
                boolean checked = mSelectedImages.contains(imageItem);
                if (checked) {
                    maskView.setVisibility(View.VISIBLE);
                    checkBox.setChecked(true);
                } else {
                    maskView.setVisibility(View.GONE);
                    checkBox.setChecked(false);
                }
            } else {
                checkBox.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) return position == 0 ? ITEM_TYPE_CAMERA : ITEM_TYPE_NORMAL;
        return ITEM_TYPE_NORMAL;
    }

    public ImageItem getItem(int position) {
        if (isShowCamera) {
            if (position == 0) return null;
            return images.get(position - 1);
        } else {
            return images.get(position);
        }
    }

    @Override
    public int getItemCount() {
        return isShowCamera ? images.size() + 1 : images.size();
    }

    public void setOnImageItemClickListener(OnImageItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnImageItemClickListener {
        void onImageItemClick(View view, ImageItem imageItem, int position);
    }

}
