package com.angcyo.uiview.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.angcyo.uiview.R
import com.angcyo.uiview.kotlin.*
import com.angcyo.uiview.resources.ResUtil
import com.angcyo.uiview.utils.ScreenUtil
import com.angcyo.uiview.utils.UI

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：数字支付密码输入布局
 * 创建人员：Robi
 * 创建时间：2018/07/20 09:30
 * 修改人员：Robi
 * 修改时间：2018/07/20 09:30
 * 修改备注：
 * Version: 1.0.0
 */
class KeyboardLayout(context: Context, attributeSet: AttributeSet? = null) :
        ViewGroup(context, attributeSet), View.OnClickListener {

    /*可以决定按键的显示位置, 个数必须对*/
    var keys = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "-1")

    /*单独按键的高度, wrap_content的时候使用*/
    var keyViewHeight: Int = (40 * density).toInt()
    var keyTextSize = 20 * density

    var keyViewBGDrawable: Drawable? = null

    /*横竖间隙*/
    var vSpace = 1 * density
    var hSpace = 1 * density

    /*使用图片键盘*/
    var useImageKey = false

    var onKeyboardInputListener: OnKeyboardInputListener? = null

    private var mBackgroundDrawable: Drawable? = null

    init {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.KeyboardLayout)
        keyViewHeight = typedArray.getDimensionPixelOffset(R.styleable.KeyboardLayout_r_key_height, keyViewHeight)
        //typedArray.getDimensionPixelOffset(R.styleable.KeyboardLayout_r_key_width, keyViewHeight)
        keyTextSize = typedArray.getDimension(R.styleable.KeyboardLayout_r_key_text_size, keyTextSize)
        useImageKey = typedArray.getBoolean(R.styleable.KeyboardLayout_r_use_image_key, useImageKey)

        keyViewBGDrawable = typedArray.getDrawable(R.styleable.KeyboardLayout_r_key_background)

        if (keyViewBGDrawable == null) {
            keyViewBGDrawable = getDrawable(R.drawable.base_white_bg_selector)
        }
        mBackgroundDrawable = typedArray.getDrawable(R.styleable.KeyboardLayout_r_background)
        if (mBackgroundDrawable == null) {
            mBackgroundDrawable = ColorDrawable(getColor(R.color.base_chat_bg_color))
        }

        setWillNotDraw(false)
        typedArray.recycle()

        keys.forEach {
            val keyView: View = when (it) {
                "-1" -> {
                    //删除
                    imageView(R.drawable.keyboard_del, R.drawable.keyboard_del_press).apply {
                        background = null
                        setBackgroundColor(Color.parseColor("#E2E7ED"))
                    }
                }
                "" -> {
                    //占位View
                    View(context).apply {
                        setBackgroundColor(Color.parseColor("#E2E7ED"))
                    }
                }
                else -> {
                    createKeyView(it)
                }
            }

            keyView.tag = it
            addView(keyView)
        }
    }

    private fun createKeyView(key: String): View {
        return if (useImageKey) {
            val keyRes = when (key) {
                "1" -> R.drawable.keyboard_1
                "2" -> R.drawable.keyboard_2
                "3" -> R.drawable.keyboard_3
                "4" -> R.drawable.keyboard_4
                "5" -> R.drawable.keyboard_5
                "6" -> R.drawable.keyboard_6
                "7" -> R.drawable.keyboard_7
                "8" -> R.drawable.keyboard_8
                "9" -> R.drawable.keyboard_9
                else -> R.drawable.keyboard_0
            }
            imageView(keyRes)
        } else {
            textView(key)
        }
    }

    private fun imageView(res: Int, pressRes: Int = -1): ImageView {
        return ImageView(context).apply {

            if (pressRes == -1) {
                setImageResource(res)
            } else {
                setImageDrawable(ResUtil.selector(getDrawable(res), getDrawable(pressRes)))
            }

            scaleType = ImageView.ScaleType.CENTER

            keyViewBGDrawable?.let {
                UI.setBackgroundDrawable(this, it.constantState.newDrawable())
            }

            setOnClickListener(this@KeyboardLayout)
        }
    }

    private fun textView(text: String): TextView {
        return TextView(context).apply {
            gravity = Gravity.CENTER
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_PX, keyTextSize)

            keyViewBGDrawable?.let {
                UI.setBackgroundDrawable(this, it.constantState.newDrawable())
            }
            setTextColor(Color.BLACK)

            setOnClickListener(this@KeyboardLayout)
        }
    }

    /*计算出来的值, 请勿设置*/
    private var childWidth = 0
    private var childHeight = 0

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = ScreenUtil.screenWidth
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = (4 * keyViewHeight + 3 * vSpace).toInt()
        }

        childWidth = ((widthSize - 2 * hSpace - paddingLeft - paddingRight) / 3).toInt()
        childHeight = ((heightSize - 3 * vSpace - paddingTop - paddingBottom) / 4).toInt()
        childs { _, view ->
            view.measure(exactlyMeasure(childWidth), exactlyMeasure(childHeight))
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        //一行一行布局, 共4行
        for (line in 0..3) {

            var top: Int = (paddingTop + line * (childHeight + vSpace)).toInt()

            //3列
            for (i in 0..2) {
                var left: Int = (paddingLeft + i * (childWidth + hSpace)).toInt()

                getChildAt(line * 3 + i).layout(left, top, left + childWidth, top + childHeight)
            }
        }
    }

    override fun onClick(v: View?) {
        if (onKeyboardInputListener == null) {
            return
        }

        v?.let { view ->
            val tag = view.tag
            if (tag is String) {
                val isDel = "-1" == tag
                onKeyboardInputListener?.onKeyboardInput(tag, isDel)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        mBackgroundDrawable?.bounds = canvas.clipBounds
        mBackgroundDrawable?.draw(canvas)
        super.draw(canvas)
    }

    fun setupEditText(editText: EditText) {
        onKeyboardInputListener = object : KeyboardLayout.OnKeyboardInputListener {
            override fun onKeyboardInput(key: String, isDel: Boolean) {
                if (isDel) {
                    if (editText is PasswordInputEditText) {
                        editText.delInput()
                    } else {
                        val newText = editText.string()
                        editText.setInputText(newText.subSequence(0, 0.minValue(newText.length - 1)).toString())
                    }
                } else {
                    if (editText is PasswordInputEditText) {
                        editText.insertInput(key)
                    } else {
                        val newText = editText.string() + key
                        //editText.setInputText(newText.subSequence(0, passwordCount.maxValue(newText.length)).toString())
                    }
                }
            }
        }
    }

    interface OnKeyboardInputListener {
        fun onKeyboardInput(key: String, isDel: Boolean)
    }
}