package com.angcyo.uiview.net;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * 通知观察器, 用来接收事件改变的回调
 *
 * @param <T>
 */
public interface RObserver<T> extends Serializable {

    /**
     * 通知产生后的回调函数
     *
     * @param event 事件参数
     * @param data  附加数据, 可以存放状态码之类的数据
     */
    public void onEvent(@NonNull String data, T event);
}
