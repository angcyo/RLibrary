package com.angcyo.uiview.kotlin

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/07/07 16:41
 * 修改人员：Robi
 * 修改时间：2017/07/07 16:41
 * 修改备注：
 * Version: 1.0.0
 */

/**整型数中, 是否包含另一个整数*/
public fun Int.have(value: Int): Boolean = value != 0 && this and value == value