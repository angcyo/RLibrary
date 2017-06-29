package com.angcyo.library.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.github.chrisbanes.photoview.PhotoView;

/**
 * https://github.com/githubwing/DragPhotoView
 * Created by wing on 2016/12/22.   by 1.0.2
 */

public class DragPhotoView extends PhotoView {
    private final static int MAX_TRANSLATE_Y = 500;
    private final static int CANCEL_TRANSLATE_Y = 100;
    private final static long DURATION = 300;
    boolean isMove = false;
    private Paint mPaint;
    // downX
    private float mDownX;
    // down Y
    private float mDownY;
    private float mTranslateY;
    private float mTranslateX;
    private float mScale = 1;
    private int mWidth;
    private int mHeight;
    private float mMinScale = 0.5f;
    private int mAlpha = 255;
    private boolean canFinish = false;
    private boolean isAnimate = false;
    //is event on PhotoView
    private boolean isTouchEvent = false;
    private OnTapListener mTapListener;
    private OnExitListener mExitListener;
    private boolean enableMoveExit = true;
    private boolean cancel_click = false;
    private Runnable mCheckTapRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTranslateX == 0 && mTranslateY == 0 && canFinish) {

                if (mTapListener != null) {
                    mTapListener.onTap(DragPhotoView.this, mDownX, mDownY);
                }
            }
            canFinish = false;
        }
    };
    private boolean isLargeBitmap;

    public DragPhotoView(Context context) {
        this(context, null);
    }

    public DragPhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public DragPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isLargeBitmap || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setLayerType(LAYER_TYPE_SOFTWARE, mPaint);
        } else {
            setLayerType(LAYER_TYPE_HARDWARE, mPaint);
        }

        //mPaint.setAlpha(mAlpha);
        //canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
        canvas.translate(mTranslateX, mTranslateY);
        canvas.scale(mScale, mScale, mWidth / 2, mHeight / 2);
        if (isAnimate && mExitListener != null) {
            mExitListener.onMoveExitCancelTo(this, mWidth, mHeight, mTranslateX, mTranslateY);
        }
        super.onDraw(canvas);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        isLargeBitmap = false;
        if (drawable != null) {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();
            if (width > 2000 || height > 2000) {
                isLargeBitmap = true;

                setMaximumScale(8f);
                setMediumScale(6f);
            }
        }

        if (!isLargeBitmap) {
            setMaximumScale(3.0f);
            setMediumScale(1.75f);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //only scale == 1 can drag
        if (enableMoveExit && !isAnimate && getScale() == 1) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    removeCallbacks(mCheckTapRunnable);

                    onActionDown(event);

                    //change the canFinish flag
                    canFinish = !canFinish;

                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = event.getY();
                    float moveX = event.getX();
                    float dx = moveX - mDownX;
                    float dy = moveY - mDownY;

                    //in viewpager
                    if (mTranslateY == 0 && mTranslateX != 0) {

                        //如果不消费事件，则不作操作
                        if (!isTouchEvent) {
                            mScale = 1;
                            return super.dispatchTouchEvent(event);
                        }
                    }

                    //single finger drag  down
                    if (Math.abs(dy) > Math.abs(dx) &&
                            mTranslateY >= 0 &&
                            event.getPointerCount() == 1) {
                        onActionMove(event);

                        //如果有上下位移 则不交给viewpager
                        if (mTranslateY != 0) {
                            isTouchEvent = true;
                        }
                        return true;
                    }


                    //防止下拉的时候双手缩放
                    if (mTranslateY >= 0 && mScale < 0.95) {
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    //防止下拉的时候双手缩放
                    if (event.getPointerCount() == 1) {
                        onActionUp(event);

                        if (mTranslateX != 0 || mTranslateY != 0) {
                            event.setAction(MotionEvent.ACTION_CANCEL);
                        }

                        isTouchEvent = false;
                        //judge finish or not
                        //postDelayed(mCheckTapRunnable, 300);
                    }

                case MotionEvent.ACTION_CANCEL:
                    isMove = false;
                    break;
            }
        }

        return super.dispatchTouchEvent(event);
    }

    private void onActionUp(MotionEvent event) {
        isMove = false;
        if (mTranslateY > MAX_TRANSLATE_Y) {
            if (mExitListener != null) {
                mExitListener.onExit(this, mTranslateX, mTranslateY, mWidth, mHeight);
            } else {
                //throw new RuntimeException("DragPhotoView: onExitLister can't be null ! call setOnExitListener() ");
                performAnimation();
            }
        } else {
            performAnimation();
        }
    }

    /**
     * 是否正在拖动返回
     */
    public boolean isMove() {
        return isMove;
    }

    private void onActionMove(MotionEvent event) {
        float moveY = event.getY();
        float moveX = event.getX();
        mTranslateX = moveX - mDownX;
        mTranslateY = moveY - mDownY;
        isMove = true;
        //保证上划到到顶还可以继续滑动
        if (mTranslateY < 0) {
            mTranslateY = 0;
        }

        float percent = mTranslateY / MAX_TRANSLATE_Y;
        if (mScale >= mMinScale && mScale <= 1f) {
            mScale = 1 - percent;

            mAlpha = (int) (255 * (1 - percent));
            if (mAlpha > 255) {
                mAlpha = 255;
            } else if (mAlpha < 0) {
                mAlpha = 0;
            }
        }
        if (mScale < mMinScale) {
            mScale = mMinScale;
        } else if (mScale > 1f) {
            mScale = 1;
        }


        invalidate();

        if (mExitListener != null) {
            mExitListener.onMoveTo(this, mWidth, mHeight, mTranslateX, mTranslateY);
        }
    }

    private void performAnimation() {
        getScaleAnimation().start();
        getTranslateXAnimation().start();
        getTranslateYAnimation().start();
        getAlphaAnimation().start();
    }

    private ValueAnimator getAlphaAnimation() {
        final ValueAnimator animator = ValueAnimator.ofInt(mAlpha, 255);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mAlpha = (int) valueAnimator.getAnimatedValue();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimate = false;
                animator.removeAllListeners();

                if (mExitListener != null) {
                    mExitListener.onMoveExitCancel(DragPhotoView.this);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }

    private ValueAnimator getTranslateYAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateY, 0);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTranslateY = (float) valueAnimator.getAnimatedValue();
            }
        });

        return animator;
    }

    private ValueAnimator getTranslateXAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mTranslateX, 0);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mTranslateX = (float) valueAnimator.getAnimatedValue();
            }
        });

        return animator;
    }

    private ValueAnimator getScaleAnimation() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mScale, 1);
        animator.setDuration(DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mScale = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimate = true;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                isAnimate = false;
                animator.removeAllListeners();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        return animator;
    }

    private void onActionDown(MotionEvent event) {
        mDownX = event.getX();
        mDownY = event.getY();
    }

    public float getMinScale() {
        return mMinScale;
    }

    public void setMinScale(float minScale) {
        mMinScale = minScale;
    }

    public void setOnTapListener(OnTapListener listener) {
        mTapListener = listener;
    }

    public void setOnExitListener(OnExitListener listener) {
        mExitListener = listener;
    }

    public void finishAnimationCallBack() {
        mTranslateX = -mWidth / 2 + mWidth * mScale / 2;
        mTranslateY = -mHeight / 2 + mHeight * mScale / 2;
        invalidate();
    }

    public void setEnableMoveExit(boolean enableMoveExit) {
        this.enableMoveExit = enableMoveExit;
    }

    public interface OnTapListener {
        void onTap(DragPhotoView view, float x, float y);
    }

    public interface OnExitListener {
        void onMoveTo(DragPhotoView view, float w, float h, float translateX, float translateY);

        void onExit(DragPhotoView view, float translateX, float translateY, float w, float h);

        void onMoveExitCancel(DragPhotoView view);

        void onMoveExitCancelTo(DragPhotoView view, float w, float h, float translateX, float translateY);
    }
}
