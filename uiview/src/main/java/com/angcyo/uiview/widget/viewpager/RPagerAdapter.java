package com.angcyo.uiview.widget.viewpager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.angcyo.uiview.recycler.RBaseViewHolder;

/**
 * Created by angcyo on 2016-11-26.
 */

public abstract class RPagerAdapter extends PagerAdapter {

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Context context = container.getContext();

        int layoutId = getLayoutId(position);
        View view;
        if (layoutId != -1) {
            view = LayoutInflater.from(context).inflate(layoutId, container, false);
        } else {
            view = createView(context, position);
        }

        if (view.getLayoutParams() == null) {
            container.addView(view, -1, -1);
        } else {
            container.addView(view);
        }

        initItemView(new RBaseViewHolder(view), position);
        return view;
    }

    protected int getLayoutId(int position) {
        return -1;
    }

    protected View createView(Context context, int position) {
        return null;
    }

    protected void initItemView(@NonNull RBaseViewHolder viewHolder, int position) {

    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
