package com.angcyo.uiview.base

import com.angcyo.uiview.R
import com.angcyo.uiview.RApplication
import com.angcyo.uiview.skin.SkinHelper

/**页面*/
data class PageBean(
        val iview: UIBaseView,
        val textNormal: String? = null,
        val textSelected: String? = null,
        val textColorNormal: Int? = null,
        val textColorSelected: Int? = null,
        val icoResNormal: Int? = null,
        val icoResSelected: Int? = null,
        val icoSubResNormal: Int? = null,
        val icoSubResSelected: Int? = null
) {
    constructor(iview: UIBaseView,
                textNormal: String? = null,
                icoResNormal: Int? = null
    ) : this(iview, textNormal, icoResNormal, icoResNormal)

    constructor(iview: UIBaseView,
                textNormal: String? = null,
                icoResNormal: Int? = null,
                icoResSelected: Int? = null
    ) : this(iview, textNormal, textNormal,
            RApplication.getApp().resources.getColor(R.color.base_text_color),
            SkinHelper.getSkin().themeSubColor,
            icoResNormal, icoResSelected,
            null, null)

    constructor(iview: UIBaseView,
                textNormal: String? = null,
                textColorNormal: Int? = null,
                textColorSelected: Int? = null,
                icoResNormal: Int? = null
    ) : this(iview, textNormal, textNormal,
            textColorNormal, textColorSelected,
            icoResNormal, icoResNormal,
            null, null)

    constructor(iview: UIBaseView,
                textNormal: String? = null,
                textColorNormal: Int? = null,
                textColorSelected: Int? = null,
                icoResNormal: Int? = null,
                icoResSelected: Int? = null
    ) : this(iview, textNormal, textNormal,
            textColorNormal, textColorSelected,
            icoResNormal, icoResSelected,
            null, null)
}