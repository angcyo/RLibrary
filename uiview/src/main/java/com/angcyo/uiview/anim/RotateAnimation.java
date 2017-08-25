package com.angcyo.uiview.anim;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.os.Build;
import android.text.TextUtils;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Y轴旋转动画
 * Created by robi on 2016-05-25 15:25.
 */
public class RotateAnimation extends Animation {
    /**
     * 值为true时可明确查看动画的旋转方向。
     */
    public static final boolean DEBUG = false;
    /**
     * 沿Y轴正方向看，数值减1时动画逆时针旋转。
     */
    public static final int ROTATE_DECREASE = 1;
    /**
     * 沿Y轴正方向看，数值减1时动画顺时针旋转。
     */
    public static final int ROTATE_INCREASE = 2;
    /**
     * Z轴上最大深度。
     */
    public static final float DEPTH_Z = 310.0f;
    /**
     * 动画显示时长。
     */
    public static final long DURATION = 800l;
    private final float centerX;
    private final float centerY;
    /**
     * 图片翻转类型。
     */
    private int type = ROTATE_DECREASE;
    private Camera camera;
    /**
     * 用于监听动画进度。当值过半时需更新txtNumber的内容。
     */
    private InterpolatedTimeListener listener;

    public RotateAnimation(float cX, float cY, int type) {
        centerX = cX;
        centerY = cY;
        this.type = type;
        setDuration(DURATION);
    }

    public RotateAnimation(float cX, float cY) {
        this(cX, cY, ROTATE_DECREASE);
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        // 在构造函数之后、getTransformation()之前调用本方法。
        super.initialize(width, height, parentWidth, parentHeight);
        camera = new Camera();
    }

    public void setInterpolatedTimeListener(InterpolatedTimeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation transformation) {
        // interpolatedTime:动画进度值，范围为[0.0f,10.f]
        //L.e("call: applyTransformation-> interpolatedTime:" + interpolatedTime + "\n" + transformation);
        if (listener != null) {
            listener.interpolatedTime(interpolatedTime);
        }
        float from = 0.0f, to = 0.0f;
        if (type == ROTATE_DECREASE) {
            from = 0.0f;
            to = 180.0f;
        } else if (type == ROTATE_INCREASE) {
            from = 360.0f;
            to = 180.0f;
        }
        float degree = from + (to - from) * interpolatedTime;
        boolean overHalf = (interpolatedTime > 0.5f);
        if (overHalf) {
            // 翻转过半的情况下，为保证数字仍为可读的文字而非镜面效果的文字，需翻转180度。
            degree = degree - 180;
        }
        // float depth = 0.0f;
        float depth = (0.5f - Math.abs(interpolatedTime - 0.5f)) * DEPTH_Z;
        //L.e("call: applyTransformation depth:-> " + depth);
        final Matrix matrix = transformation.getMatrix();
        camera.save();
        camera.translate(0.0f, 0.0f, depth);
        //L.e("call: applyTransformation([interpolatedTime, transformation])-> x:" + camera.getLocationX() + " y:" + camera.getLocationY() + " z:" + camera.getLocationZ());\
        if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP /*华为手机处理圆角, 偶尔会失败.*/) {
            camera.setLocation(0, 0, -160);
        }
        camera.rotateY(degree);
        camera.getMatrix(matrix);
        camera.restore();
        if (DEBUG) {
            if (overHalf) {
                matrix.preTranslate(-centerX * 2, -centerY);
                matrix.postTranslate(centerX * 2, centerY);
            }
        } else {
            //确保图片的翻转过程一直处于组件的中心点位置
            matrix.preTranslate(-centerX, -centerY);
            matrix.postTranslate(centerX, centerY);
        }
    }

    /**
     * 动画进度监听器。
     */
    public static interface InterpolatedTimeListener {
        void interpolatedTime(float interpolatedTime);
    }
}
