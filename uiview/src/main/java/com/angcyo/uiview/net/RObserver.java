package com.angcyo.uiview.net;

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
     * @param t 事件参数
     */
    public void onEvent(T t);
}
