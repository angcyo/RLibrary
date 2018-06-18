package com.angcyo.uiview.widget.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by angcyo on 2016-11-26.
 */

public abstract class RPagerAdapter extends PagerAdapter {

    SparseArray<WeakReference<View>> mViewCache = new SparseArray<>();

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();
        View view = getCacheView(position);

        if (view == null) {
            int layoutId = getLayoutId(position);
            if (layoutId != -1) {
                view = LayoutInflater.from(context).inflate(layoutId, container, false);
            } else {
                view = createView(context, position);
            }
        }

        if (view.getLayoutParams() == null) {
            container.addView(view, -1, -1);
        } else {
            container.addView(view);
        }

        initItemView(view, position);
        return view;
    }

    public View getCacheView(int position) {
        WeakReference<View> viewWeakReference = mViewCache.get(position);
        View view = null;
        if (viewWeakReference != null) {
            view = viewWeakReference.get();
        }
        return view;
    }

    protected int getLayoutId(int position) {
        return -1;
    }

    protected View createView(Context context, int position) {
        return null;
    }

    protected void initItemView(@NonNull View rootView, int position) {

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        mViewCache.put(position, new WeakReference<>((View) object));
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
