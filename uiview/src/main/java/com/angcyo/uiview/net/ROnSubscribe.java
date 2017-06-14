package com.angcyo.uiview.net;

import com.angcyo.library.utils.L;

import rx.Observable;
import rx.Observer;
import rx.observables.SyncOnSubscribe;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * {@link Observable#create(SyncOnSubscribe)}
 * 创建人员：Robi
 * 创建时间：2017/06/14 13:44
 * 修改人员：Robi
 * 修改时间：2017/06/14 13:44
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class ROnSubscribe<T> extends SyncOnSubscribe<String, T> {

    public static final String STATE_ASYNC = "state_async";

    /**
     * 第一次创建的时候, 检查状态, 然后返回状态
     */
    @Override
    protected String generateState() {
        L.d("ROnSubscribe:generateState-> ");
        return "ok";
    }

    /**
     * 创建状态之后, 决定是否需要继续执行, 返回一个新的状态, 在下次next执行的时候, 传入.
     */
    @Override
    protected String next(String state, Observer<? super T> observer) {
        boolean async = isAsync();
        //L.d("ROnSubscribe: next-> " + state + " isAsync:" + async);
        try {
            if (!STATE_ASYNC.equalsIgnoreCase(state)) {
                next(observer);
            }
        } catch (Exception e) {
            e.printStackTrace();
            observer.onError(e);
        }
        if (!async) {
            observer.onCompleted();
        }
        return async ? STATE_ASYNC : "ok_next";
    }

    protected abstract void next(Observer<? super T> observer);

    /**
     * 是否是异步监听, 如果是异步, 则需要手动调用 onCompleted 和 onError方法, 否则 next 方法会一直循环调用.
     */
    protected boolean isAsync() {
        return false;
    }
}
