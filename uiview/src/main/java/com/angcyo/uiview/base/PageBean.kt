package com.angcyo.uiview.base

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
    ) : this(iview, textNormal, textNormal, null, null, icoResNormal, icoResNormal, null, null)

    constructor(iview: UIBaseView,
                textNormal: String? = null,
                textColorNormal: Int? = null,
                textColorSelected: Int? = null,
                icoResNormal: Int? = null
    ) : this(iview, textNormal, textNormal, textColorNormal, textColorSelected, icoResNormal, icoResNormal, null, null)
}