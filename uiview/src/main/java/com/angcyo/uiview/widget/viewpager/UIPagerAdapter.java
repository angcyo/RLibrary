package com.angcyo.uiview.widget.viewpager;

import android.content.Context;
import android.view.View;

import com.angcyo.uiview.container.UILayoutImpl;
import com.angcyo.uiview.view.IView;
import com.angcyo.uiview.view.UIIViewImpl;

/**
 * Created by angcyo on 2016-11-26.
 */
public abstract class UIPagerAdapter extends RPagerAdapter {

    @Override
    protected View createView(Context context, int position, int itemType) {
        IView iView = getIView(position, itemType);
        if (iView instanceof UIIViewImpl) {
            ((UIIViewImpl) iView).setShowInViewPager(true);
        }
        final UILayoutImpl layout = new UILayoutImpl(context, iView);
        return layout;
    }

    @Deprecated
    protected IView getIView(int position) {
        return null;
    }

    protected IView getIView(int position, int itemType) {
        return getIView(position);
    }

}

/*
public abstract class UIPagerAdapter extends PagerAdapter {

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        IView iView = getIView(position);
        if (iView instanceof UIIViewImpl) {
            ((UIIViewImpl) iView).setShowInViewPager(true);
        }

        final UILayoutImpl layout = new UILayoutImpl(container.getContext(), iView);
        container.addView(layout);
        return layout;
    }

    protected abstract IView getIView(int position);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        final UILayoutImpl layout = (UILayoutImpl) object;
        container.removeView(layout);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}*/
