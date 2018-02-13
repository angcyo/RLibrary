package com.angcyo.uiview.kotlin

import com.angcyo.uiview.utils.RUtils

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/09/12 10:04
 * 修改人员：Robi
 * 修改时间：2017/09/12 10:04
 * 修改备注：
 * Version: 1.0.0
 */

/**保留小数点后几位*/
public fun Float.decimal(bitNum: Int = 2 /*小数点后几位*/, halfUp: Boolean = true /*四舍五入*/): String {
    return RUtils.decimal(this, bitNum, halfUp)
}