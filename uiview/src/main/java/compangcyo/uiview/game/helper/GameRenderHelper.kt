package com.angcyo.uiview.game.helper

import com.angcyo.uiview.game.GameRenderView
import com.angcyo.uiview.game.layer.BaseLayer

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/12/15 11:15
 * 修改人员：Robi
 * 修改时间：2017/12/15 11:15
 * 修改备注：
 * Version: 1.0.0
 */

class GameRenderHelper(val gameRenderView: GameRenderView) {
    /**会按顺序绘制*/
    fun addLayer(layer: BaseLayer) {
        gameRenderView.layerList.add(layer)
    }
}