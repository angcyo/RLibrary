package com.angcyo.uiview.recycler;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.angcyo.library.utils.L;
import com.leochuan.ViewPagerLayoutManager;

/**
 * Created by angcyo on 2018-03-07.
 */

public class RExLoopRecyclerView extends RRecyclerView {

    private LoopLayoutManager mLoopLayoutManager;
    private boolean mInfinite = true;

    public RExLoopRecyclerView(Context context) {
        this(context, null);
    }

    public RExLoopRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RExLoopRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void initView(Context context) {
        super.initView(context);

        mLoopLayoutManager = new LoopLayoutManager(getContext(), ViewPagerLayoutManager.HORIZONTAL, false);
        setInfinite(true);
        setLayoutManager(mLoopLayoutManager);

        new RPagerSnapHelper().setOnPageListener(new RPagerSnapHelper.OnPageListener() {
            @Override
            public void onPageSelector(int fromPosition, int toPosition) {
                super.onPageSelector(fromPosition, toPosition);
                L.e("onPageSelector() -> " + fromPosition + " to " + toPosition);
            }
        }).attachToRecyclerView(this);

//        setLayoutManager(new CircleLayoutManager(context));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mLoopLayoutManager != null) {
            mLoopLayoutManager.setItemInterval(getMeasuredWidth());
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
//        mLoopLayoutManager = new LoopLayoutManager(getContext(), orientation, false);
//        mLoopLayoutManager.setInfinite(mInfinite);
//        //需要在adapter设置之后调用
//        setLayoutManager(mLoopLayoutManager);
    }

    /**
     * 打开无限循环
     */
    public void setInfinite(boolean enable) {
        mInfinite = enable;
        if (mLoopLayoutManager != null) {
            mLoopLayoutManager.setInfinite(enable);
        }
    }

    public static class LoopLayoutManager extends ViewPagerLayoutManager {

        private float itemInterval = -1;

        public LoopLayoutManager(Context context) {
            this(context, ViewPagerLayoutManager.HORIZONTAL, false);
        }

        public LoopLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
            setEnableBringCenterToFront(true);
        }

        public void setItemInterval(float itemInterval) {
            this.itemInterval = itemInterval;
            requestLayout();
        }

        /**
         * Item 之间间隔的大小
         * 默认情况下, Item之间是相互叠加显示的, 需要通过此方法, 设置间隔才能显示出线性的效果
         */
        @Override
        protected float setInterval() {
            return itemInterval;
        }

        /**
         * 用来控制item属性, 比如各种属性动画, 在滑动的时候出发
         */
        @Override
        protected void setItemViewProperty(View itemView, float targetOffset) {
            //targetOffset 和 itemInterval 密切广西
            //targetOffset 取值范围 -itemInterval/2  0  itemInterval/2
            //L.e("setItemViewProperty() -> " + targetOffset);
        }
    }
}
