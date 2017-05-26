package com.lzy.imagepicker;

import android.app.Activity;
import android.widget.ImageView;

import com.lzy.imagepicker.loader.ImageLoader;

public class PicassoImageLoader implements ImageLoader {

    @Override
    public void displayImage(Activity activity, String path, String thumbPath, String url, ImageView imageView, int width, int height) {
//            Picasso.with(activity)//
//                    .load(new File(path))//
//                    .placeholder(R.mipmap.default_image)//
//                    .error(R.mipmap.default_image)//
//                    .resize(width, height)//
//                    .centerInside()//
//                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)//
//                    .into(imageView);
    }

    @Override
    public void clearMemoryCache() {
    }
}