package com.angcyo.uiview.resources;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.View;

import com.angcyo.uiview.R;
import com.angcyo.uiview.RApplication;

import static android.view.View.LAYER_TYPE_HARDWARE;
import static android.view.View.LAYER_TYPE_NONE;


/**
 * Created by angcyo on 15-12-31 031 10:52 上午.
 */
public class ResUtil {

    /**
     * Dp to px float.
     *
     * @param res the res
     * @param dp  the dp
     * @return the float
     */
    public static float dpToPx(Resources res, float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics());
        return px;
    }

    public static float spToPx(Resources res, float sp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, res.getDisplayMetrics());
        return px;
    }

    public static float dpToPx(Context context, float dp) {
        return dpToPx(context.getResources(), dp);
    }

    public static float spToPx(Context context, float sp) {
        return spToPx(context.getResources(), sp);
    }

    /**
     * 颜色过滤
     */
    public static void colorFilter(Drawable drawable, int color) {
        drawable.mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
    }

    /**
     * tint颜色
     */
    public static Drawable tintDrawable(Drawable drawable, int color) {
        Drawable wrappedDrawable = DrawableCompat.wrap(drawable).mutate();
        DrawableCompat.setTint(wrappedDrawable, color);
        return wrappedDrawable;
    }

    /**
     * Generate text color color state list.
     *
     * @param pressColor   the press color
     * @param defaultColor the default color
     * @return the color state list
     */
    public static ColorStateList generateTextColor(int pressColor, int defaultColor) {
        ColorStateList stateList = new ColorStateList(new int[][]{{android.R.attr.state_pressed}, {}},
                new int[]{pressColor, defaultColor});
        return stateList;
    }

    public static ColorStateList generateTextColor(int pressColor, int checkColor, int defaultColor) {
        ColorStateList stateList = new ColorStateList(new int[][]{{android.R.attr.state_pressed}, {android.R.attr.state_checked}, {}},
                new int[]{pressColor, checkColor, defaultColor});
        return stateList;
    }

    public static ColorStateList generateTextColor(int pressColor, int checkColor, int disableColor, int defaultColor) {
        ColorStateList stateList = new ColorStateList(new int[][]{{-android.R.attr.state_enabled}, {android.R.attr.state_pressed}, {android.R.attr.state_checked}, {}},
                new int[]{disableColor, pressColor, checkColor, defaultColor});
        return stateList;
    }

    /**
     * Generate bg drawable drawable.
     *
     * @param pressColor   the press color
     * @param defaultColor the default color
     * @return the drawable
     */
    public static Drawable generateRoundDrawable(float radii, int pressColor, int defaultColor) {
        //圆角
        Shape roundRectShape = new RoundRectShape(new float[]{radii, radii, radii, radii, radii, radii, radii, radii}, null, null);//圆角背景

        //按下状态
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShape);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, shopDrawablePress);
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    public static Drawable generateRoundDrawable(float radii, int pressColor, int disableColor, int defaultColor) {
        //圆角
        Shape roundRectShape = new RoundRectShape(new float[]{radii, radii, radii, radii, radii, radii, radii, radii}, null, null);//圆角背景

        //按下状态
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShape);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        //禁用状态
        ShapeDrawable disableDrawableNormal = new ShapeDrawable(roundRectShape);
        disableDrawableNormal.getPaint().setColor(disableColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, disableDrawableNormal);//
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    /**
     * Generate bg drawable drawable.
     *
     * @param radii        the radii
     * @param pressColor   the press color
     * @param defaultColor the default color
     * @return the drawable
     */
    public static Drawable generateRoundDrawable(Resources res, float radii, int pressColor, int defaultColor) {

        radii = dpToPx(res, radii);

        //外环的圆角矩形
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, null, null);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, null, null);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }


    /**
     * Generate bg drawable drawable.
     *
     * @param radii        the radii
     * @param borderWidth  the border width
     * @param pressColor   the press color
     * @param defaultColor the default color
     * @return the drawable
     */
    public static Drawable generateRoundBorderDrawable(Resources res, float radii, float borderWidth, int pressColor, int defaultColor) {

        radii = dpToPx(res, radii);
        borderWidth = dpToPx(res, borderWidth);

        //外环的圆角矩形
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个

        //与内环的距离
        RectF inset = new RectF(borderWidth, borderWidth, borderWidth, borderWidth);

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, inset, null);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, inset, null);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    public static Drawable generateRoundBorderDrawable(float radii, float borderWidth,
                                                       int pressColor, int disableColor, int defaultColor) {

        //外环的圆角矩形,
        // 左上 右上   左下 右下
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个

        //与内环的距离
        RectF inset = new RectF(borderWidth, borderWidth, borderWidth, borderWidth);

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, inset, outRadii);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        ShapeDrawable shopDrawableEnable = new ShapeDrawable(roundRectShape);
        shopDrawableEnable.getPaint().setColor(disableColor);

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, inset, outRadii);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, shopDrawableEnable);//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_checked}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_focused}, shopDrawablePress);//焦点状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    public static Drawable generateStrokeDrawable(float radii, float borderWidth, int pressColor, int defaultColor, int disableColor) {
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个

        //与内环的距离
        RectF inset = new RectF(borderWidth, borderWidth, borderWidth, borderWidth);

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, inset, outRadii);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        ShapeDrawable shopDrawableEnable = new ShapeDrawable(roundRectShape);
        shopDrawableEnable.getPaint().setColor(disableColor);

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, inset, outRadii);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, shopDrawableEnable);//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_checked}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_focused}, shopDrawablePress);//焦点状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    public static Drawable generateRoundBorderDrawable(float radii, float borderWidth,
                                                       int pressColor, int defaultColor) {

        return generateRoundBorderDrawable(radii, borderWidth, pressColor,
                ContextCompat.getColor(RApplication.getApp(), R.color.default_base_bg_disable2), defaultColor);
    }


    public static Drawable generateRoundDrawable(float rL1, float rL2, float rT1, float rT2,
                                                 float rR1, float rR2, float rB1, float rB2,
                                                 int pressColor, int defaultColor) {
        //外环的圆角矩形
        float[] outRadii = new float[]{rL1, rL2, rT1, rT2, rR1, rR2, rB1, rB2};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个

        //与内环的距离
        RectF inset = new RectF(0, 0, 0, 0);

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, inset, null);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, inset, null);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    public static Drawable generateRoundDrawable(float radiiL, float radiiR, int pressColor, int defaultColor) {
        //外环的圆角矩形
        float[] outRadii = new float[]{radiiL, radiiL, radiiR, radiiR, radiiR, radiiR, radiiL, radiiL};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个

        //与内环的距离
        RectF inset = new RectF(0, 0, 0, 0);

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, inset, null);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, inset, null);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }


    /**
     * 正常 圆角边框;
     * 按下 圆角色块
     */
    public static Drawable generateBorderDrawable(float radii, float borderWidth, int pressColor, int defaultColor) {

        //外环的圆角矩形
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个
        RectF inset = new RectF(borderWidth, borderWidth, borderWidth, borderWidth);

        //按下状态
        Shape roundRectShape = new RoundRectShape(outRadii, null, null);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(pressColor);//设置颜色

        //正常状态
        Shape roundRectShapeNormal = new RoundRectShape(outRadii, inset, outRadii);
        ShapeDrawable shopDrawableNormal = new ShapeDrawable(roundRectShapeNormal);
        shopDrawableNormal.getPaint().setColor(defaultColor);

        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, shopDrawablePress);//按下状态
        bgStateDrawable.addState(new int[]{}, shopDrawableNormal);//其他状态

        return bgStateDrawable;
    }

    /**
     * Generate bg drawable drawable.
     *
     * @param radii       圆角角度
     * @param borderWidth 厚度
     * @param color       颜色
     * @return the drawable
     */
    public static Drawable generateRoundBorderDrawable(float radii, float borderWidth, int color) {

        float[] radiiF = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个
        RectF rectF = new RectF(borderWidth, borderWidth, borderWidth, borderWidth);

        Shape roundRectShape = new RoundRectShape(radiiF, rectF, radiiF);//圆角背景
        ShapeDrawable shopDrawablePress = new ShapeDrawable(roundRectShape);//圆角shape
        shopDrawablePress.getPaint().setColor(color);//设置颜色

        return shopDrawablePress;
    }

    public static Drawable generateCircleBgDrawable(float width, int color) {
        Shape arcShape = new ArcShape(0, 360);
        ShapeDrawable shopDrawablePress = new ShapeDrawable(arcShape);//圆形shape
        shopDrawablePress.getPaint().setColor(color);//设置颜色
        shopDrawablePress.getPaint().setStyle(Paint.Style.STROKE);//设置颜色
        shopDrawablePress.getPaint().setStrokeWidth(width);//设置颜色
        return shopDrawablePress;
    }

    public static Drawable generateBgDrawable(int pressColor, int defaultColor) {
        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressColor));//按下状态
        bgStateDrawable.addState(new int[]{}, new ColorDrawable(defaultColor));//其他状态

        return bgStateDrawable;
    }

    public static Drawable generateBgDrawable(int pressColor, int disableColor, int defaultColor) {
        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{-android.R.attr.state_enabled}, new ColorDrawable(disableColor));//
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, new ColorDrawable(pressColor));//按下状态
        bgStateDrawable.addState(new int[]{}, new ColorDrawable(defaultColor));//其他状态

        return bgStateDrawable;
    }

    public static void setBgDrawable(View view, Drawable drawable) {
        ViewCompat.setBackground(view, drawable);
    }

    /**
     * 获取状态栏的高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取ActionBar的高度
     */
    public static float getActionBarHeight(Context context) {
        TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        float h = actionbarSizeTypedArray.getDimension(0,
                context.getResources().getDimension(R.dimen.abc_action_bar_default_height_material));
        return h;
    }

    /**
     * 获取 colorAccent 颜色
     */
    public static int getThemeColorAccent(Context context) {
        return getThemeColor(context, "colorAccent");
    }

    /**
     * 获取主题声明的颜色
     */
    public static int getThemeColor(Context context, String name) {
//        TypedArray array = getTheme().obtainStyledAttributes(new int[] {
//                R.color.colorAccent,
//                android.R.attr.textColorPrimary,
//        });
//        int backgroundColor = array.getColor(0, 0xFF00FF);
//        int textColor = array.getColor(1, 0xFF00FF);
//        array.recycle();
        return ContextCompat.getColor(context, getThemeIdentifier(context, name, "color"));
    }

    public static float getThemeDimen(Context context, String name) {
        int id = getThemeIdentifier(context, name, "dimen");
        return context.getResources().getDimension(id);
    }

    public static Drawable getThemeDrawable(Context context, String name) {
        int id = getThemeIdentifier(context, name, "drawable");
        return ContextCompat.getDrawable(context, id);
    }

    /**
     * 根据name, 在主题中 寻找资源id
     */
    public static int getThemeIdentifier(Context context, String name, String type) {
        //L.i("call: getThemeIdentifier([context, name, type])-> type:" + type +" name:"+name );
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }

    /**
     * 返回屏幕宽度(像素)
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 返回屏幕高度(像素, 包含了状态栏的高度)
     */
    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 判断是否包含属性
     *
     * @see View#SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
     */
    public static boolean isLayoutFullscreen(Activity activity) {
        final int visibility = activity.getWindow().getDecorView().getSystemUiVisibility();
        return ((visibility & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN) == View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * 边界无限制的Ripple, 默认状态是透明
     */
    public static Drawable generateRippleDrawable(int rippleColor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        RippleDrawable drawable = new RippleDrawable(ColorStateList.valueOf(rippleColor), null, null);
        return drawable;
    }

    /**
     * 边界被限制的Ripple, 默认状态是透明
     */
    public static Drawable generateRippleMaskDrawable(int rippleColor) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        RippleDrawable drawable = new RippleDrawable(ColorStateList.valueOf(rippleColor), null, new ColorDrawable(rippleColor));
        return drawable;
    }

    /**
     * 边界被限制的Ripple, 默认状态是参数content对应的
     */
    public static Drawable generateRippleMaskDrawable(int rippleColor, Drawable content) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        RippleDrawable drawable = new RippleDrawable(ColorStateList.valueOf(rippleColor), content, new ColorDrawable(rippleColor));
        if (content != null) {
            drawable.setDrawableByLayerId(android.R.id.mask, content);
        }
        return drawable;
    }

    public static Drawable generateRippleRoundMaskDrawable(float radius, int rippleColor, int pressColor, int defaultColor) {
        Drawable drawable = ResUtil.generateRoundDrawable(radius, pressColor, defaultColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ResUtil.generateRippleMaskDrawable(rippleColor, drawable);
        } else {
            return drawable;
        }
    }

    public static Drawable generateRippleRoundMaskDrawable(float radius, int rippleColor, int pressColor, int disableColor, int defaultColor) {
        Drawable drawable = ResUtil.generateRoundDrawable(radius, pressColor, disableColor, defaultColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ResUtil.generateRippleMaskDrawable(rippleColor, drawable);
        } else {
            return drawable;
        }
    }

    public static Drawable generateRippleMaskDrawable(int rippleColor, int pressColor, int disableColor, int defaultColor) {
        Drawable drawable = ResUtil.generateBgDrawable(pressColor, disableColor, defaultColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ResUtil.generateRippleMaskDrawable(rippleColor, drawable);
        } else {
            return drawable;
        }
    }

    public static Drawable generateRippleMaskDrawable(int rippleColor, int pressColor, int defaultColor) {
        Drawable drawable = ResUtil.generateBgDrawable(pressColor, defaultColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ResUtil.generateRippleMaskDrawable(rippleColor, drawable);
        } else {
            return drawable;
        }
    }

    public static Drawable generateClickDrawable(Context context, @DrawableRes int defaultRes, @DrawableRes int pressRes) {
        StateListDrawable bgStateDrawable = new StateListDrawable();//状态shape
        bgStateDrawable.addState(new int[]{android.R.attr.state_pressed}, ContextCompat.getDrawable(context, pressRes));//按下状态
        bgStateDrawable.addState(new int[]{android.R.attr.state_checked}, ContextCompat.getDrawable(context, pressRes));//按下状态
        bgStateDrawable.addState(new int[]{}, ContextCompat.getDrawable(context, defaultRes));//其他状态
        return bgStateDrawable;
    }

    /**
     * 创建一个框框的Drawable
     */
    public static Drawable createStrokeDrawable(int color, float radii, float borderWidth) {
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个
        RectF inset = new RectF(borderWidth, borderWidth, borderWidth, borderWidth);
        Shape roundRectShape = new RoundRectShape(outRadii, inset, outRadii);//圆角背景
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);//圆角shape
        shapeDrawable.getPaint().setColor(color);//设置颜色
        return shapeDrawable;
    }

    public static Drawable createSolidDrawable(int color, float radii) {
        float[] outRadii = new float[]{radii, radii, radii, radii, radii, radii, radii, radii};//四个角的 圆角幅度,8个可以设置的值,每个角都有2个边 2*4=8个
        Shape roundRectShape = new RoundRectShape(outRadii, null, null);//圆角背景
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);//圆角shape
        shapeDrawable.getPaint().setColor(color);//设置颜色
        return shapeDrawable;
    }

    /**
     * 系统xml翻译过来的就是GradientDrawable
     */
    public static Drawable createDrawable(int strokeColor, int solidColor, int strokeWidth, float radii) {
        GradientDrawable drawable = (GradientDrawable) createDrawable(strokeColor, strokeWidth, radii);
        drawable.setColor(solidColor);//请不要用透明颜色试图隐藏solid
        return drawable;
    }

    /**
     * 效果比createSolidDrawable好
     */
    public static Drawable createDrawable(int solidColor, float radii) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);//设置形状
        drawable.setCornerRadius(radii);
        drawable.setColor(solidColor);//请不要用透明颜色试图隐藏solid
        return drawable;
    }

    /**
     * 创建一个框框的Drawable, 效果比 createStrokeDrawable 方法好.
     */
    public static Drawable createDrawable(int strokeColor, int strokeWidth, float radii) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);//设置形状
        drawable.setStroke(strokeWidth, strokeColor);
        drawable.setCornerRadius(radii);
        return drawable;
    }

    public static Drawable createColorDrawable(int color) {
        return new ColorDrawable(color);
    }

    /**
     * 创建Drawable选择器
     */
    public static Drawable selector(Drawable normalDrawable, Drawable pressDrawable) {
        StateListDrawable listDrawable = new StateListDrawable();//状态shape
        listDrawable.addState(new int[]{android.R.attr.state_pressed}, pressDrawable);//按下状态
        listDrawable.addState(new int[]{}, normalDrawable);//其他状态
        return listDrawable;
    }

    public static Drawable selector(Drawable normalDrawable, Drawable pressDrawable, Drawable disableDrawable) {
        StateListDrawable listDrawable = new StateListDrawable();//状态shape
        listDrawable.addState(new int[]{-android.R.attr.state_enabled}, disableDrawable);//禁止状态
        listDrawable.addState(new int[]{android.R.attr.state_pressed}, pressDrawable);//按下状态
        listDrawable.addState(new int[]{}, normalDrawable);//其他状态
        return listDrawable;
    }

    public static Drawable selector(Drawable normalDrawable, Drawable pressDrawable, Drawable checkedDrawable, Drawable disableDrawable) {
        StateListDrawable listDrawable = new StateListDrawable();//状态shape
        listDrawable.addState(new int[]{-android.R.attr.state_enabled}, disableDrawable);//禁止状态
        listDrawable.addState(new int[]{android.R.attr.state_checked}, checkedDrawable);
        listDrawable.addState(new int[]{android.R.attr.state_pressed}, pressDrawable);//按下状态
        listDrawable.addState(new int[]{}, normalDrawable);//其他状态
        return listDrawable;
    }

    public static Drawable selector(int[] states, Drawable[] drawables, Drawable normalDrawable) {
        StateListDrawable listDrawable = new StateListDrawable();//状态shape
        for (int i = 0; i < states.length; i++) {
            listDrawable.addState(new int[]{states[i]}, drawables[i]);
        }
        listDrawable.addState(new int[]{}, normalDrawable);
        return listDrawable;
    }

    public static Drawable selectorChecked(Drawable normalDrawable, Drawable pressDrawable) {
        StateListDrawable listDrawable = new StateListDrawable();//状态shape
        listDrawable.addState(new int[]{android.R.attr.state_pressed}, pressDrawable);//按下状态
        listDrawable.addState(new int[]{android.R.attr.state_checked}, pressDrawable);//按下状态
        listDrawable.addState(new int[]{}, normalDrawable);//其他状态
        return listDrawable;
    }

    /**
     * 创建Ripple
     */
    public static Drawable ripple(int rippleColor, Drawable contentDrawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new RippleDrawable(ColorStateList.valueOf(rippleColor), contentDrawable, contentDrawable);
        } else {
            return contentDrawable;
        }
    }

    /**
     * 设置View的灰度
     */
    public static void setGreyscale(View v, boolean greyscale) {
        if (greyscale) {
            // Create a paint object with 0 saturation (black and white)
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0);
            Paint greyscalePaint = new Paint();
            greyscalePaint.setColorFilter(new ColorMatrixColorFilter(cm));
            // Create a hardware layer with the greyscale paint
            v.setLayerType(LAYER_TYPE_HARDWARE, greyscalePaint);
        } else {
            // Remove the hardware layer
            v.setLayerType(LAYER_TYPE_NONE, null);
        }
    }

    public static Bitmap getBitmap(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
