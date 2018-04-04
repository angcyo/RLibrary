package com.lzy.imagepicker.iview;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;

import com.angcyo.github.utilcode.utils.FileUtils;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.Root;
import com.angcyo.uiview.container.ILayout;
import com.angcyo.uiview.dialog.UIBottomItemDialog;
import com.angcyo.uiview.dialog.UIItemDialog;
import com.angcyo.uiview.net.RFunc;
import com.angcyo.uiview.net.RSubscriber;
import com.angcyo.uiview.net.Rx;
import com.angcyo.uiview.utils.T_;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.R;
import com.lzy.imagepicker.adapter.ImagePageAdapter;
import com.lzy.imagepicker.bean.ImageItem;

import java.io.File;

/**
 * 用来保存图片的监听事件
 * 2018-3-26
 */
public class SavePhotoLongClickListener implements ImagePageAdapter.PhotoViewLongClickListener {
    protected ILayout mILayout;

    public SavePhotoLongClickListener(ILayout ILayout) {
        mILayout = ILayout;
    }

    @Override
    public void onLongClickListener(PhotoView photoView, int position, final ImageItem item) {
        if (item != null && item.canSave) {
            final UIItemDialog bottomDialog = UIBottomItemDialog.build();
            if (item.canSave) {
                bottomDialog.addItem(mILayout.getLayout().getContext().getString(R.string.save_image), createSaveClickListener(item));
            }
            bottomDialog.showDialog(mILayout);

            Rx.base(new RFunc<String>() {
                @Override
                public String onFuncCall() {
                    if (!TextUtils.isEmpty(item.path) && new File(item.path).exists()) {
                        //需要扫描的文件存在
                        //return QRCodeDecoder.syncDecodeQRCode(item.path);
                        //需要依赖Rcode库
                        //return UIScanView2.Companion.scanPictureFun(RApplication.getApp(), item.path);
                    }
                    return "";
                }
            }, new RSubscriber<String>() {
                @Override
                public void onSucceed(final String bean) {
                    super.onSucceed(bean);
                    L.e("二维码识别结果 -> " + bean);
                    if (!TextUtils.isEmpty(bean)) {
                        bottomDialog.addItem("识别图片二维码", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                T_.show(bean);
                            }
                        });
                    }
                }

                @Override
                public void onError(int code, String msg) {
                    //super.onError(code, msg);
                }
            });
        }
    }

    @NonNull
    protected View.OnClickListener createSaveClickListener(final ImageItem item) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(item.path) && new File(item.path).exists()) {
                    saveImageFile(new File(item.path));
                } else {
                    Glide.with(mILayout.getLayout().getContext().getApplicationContext())
                            .downloadOnly()
                            .load(item.url)
                            .into(new SimpleTarget<File>() {

                                @Override
                                public void onResourceReady(File resource, Transition<? super File> transition) {
                                    saveImageFile(resource);
                                }
                            });
                }
            }
        };
    }

    protected void saveImageFile(File file) {
        File toFile = new File(Root.getAppExternalFolder("images"), Root.createFileName(".jpeg"));
        if (FileUtils.copyFile(file, toFile)) {
            ImagePicker.galleryAddPic(mILayout.getLayout().getContext(), toFile);
            T_.ok(mILayout.getLayout().getContext().getString(R.string.save_to_phone_format, toFile.getAbsolutePath()));
        } else {
            T_.error(mILayout.getLayout().getContext().getString(R.string.save_error));
        }
    }
}