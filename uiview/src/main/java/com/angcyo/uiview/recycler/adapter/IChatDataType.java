package com.angcyo.uiview.recycler.adapter;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/06/07 10:18
 * 修改人员：Robi
 * 修改时间：2018/06/07 10:18
 * 修改备注：
 * Version: 1.0.0
 */
public interface IChatDataType extends IExBaseDataType {
    /**
     * 获取消息的创建时间, 毫秒.
     */
    long getChatTime();

    /**
     * true, 表示一定不显示时间
     * 否则, 一分钟以上时间间隔的消息就会显示时间
     */
    boolean ignoreChatTime();
}
