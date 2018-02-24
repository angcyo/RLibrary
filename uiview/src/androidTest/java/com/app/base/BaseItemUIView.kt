package com.app.base

import android.graphics.Color
import com.angcyo.uiview.base.SingleItem
import com.angcyo.uiview.base.UIItemUIView

/**
 * Created by angcyo on 2018/02/13 23:09
 */
abstract class BaseItemUIView : UIItemUIView<SingleItem>() {
    override fun getDefaultBackgroundColor(): Int {
        return Color.WHITE
    }
}