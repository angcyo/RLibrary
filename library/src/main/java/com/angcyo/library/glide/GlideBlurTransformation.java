package com.angcyo.library.glide;

import android.content.Context;
import android.graphics.Bitmap;

import com.angcyo.library.utils.FastBlurUtil;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

import java.security.MessageDigest;

public class GlideBlurTransformation extends BitmapTransformation {

    private int mRadius = 20;

    public GlideBlurTransformation(Context context) {
        super(context);
    }

    public String getId() {
        return "GlideBlurTransformation(radius=" + mRadius + ")";
    }

    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
        return FastBlurUtil.doBlur(toTransform, mRadius, true);
    }

    @Override
    public void updateDiskCacheKey(MessageDigest messageDigest) {
        messageDigest.update(getId().getBytes());
    }
}