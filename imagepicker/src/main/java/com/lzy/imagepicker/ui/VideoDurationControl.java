package com.lzy.imagepicker.ui;

import com.angcyo.uiview.utils.Tip;

import static com.lzy.imagepicker.ImageDataSource.IMAGE_AND_VIDEO;
import static com.lzy.imagepicker.ImageDataSource.VIDEO;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2018/05/16 11:11
 * 修改人员：Robi
 * 修改时间：2018/05/16 11:11
 * 修改备注：
 * Version: 1.0.0
 */
public class VideoDurationControl {
    public static boolean isVideoDurationLong(long maxVideoDuration, long videoDuration, int loadType, int itemLoadType) {
        if (maxVideoDuration > -1 && (loadType == IMAGE_AND_VIDEO || loadType == VIDEO)) {
            if (itemLoadType == VIDEO) {
                if (videoDuration > maxVideoDuration) {
                    String tip;
                    if (maxVideoDuration >= 60 * 1000) {
                        tip = maxVideoDuration / 1000 / 60 + "分钟";
                    } else {
                        tip = maxVideoDuration / 1000 + "秒";
                    }
                    Tip.tip("时长不能大于" + tip);
                    return true;
                }
            }
        }
        return false;
    }
}
