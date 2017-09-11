package com.angcyo.uiview.net;

import com.angcyo.library.utils.L;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import retrofit2.HttpException;
import rx.Subscriber;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/12/15 11:53
 * 修改人员：Robi
 * 修改时间：2016/12/15 11:53
 * 修改备注：
 * Version: 1.0.0
 */
public abstract class RSubscriber<T> extends Subscriber<T> {

    public static final int NO_NETWORK = -50000;

    @Override
    public void onStart() {
        super.onStart();
        L.d("开始订阅->" + this.getClass().getSimpleName());
    }

    @Override
    public void onCompleted() {
        L.d("订阅完成->" + this.getClass().getSimpleName());
        try {
            onEnd(false, false, null);
        } catch (Exception e) {
        }
    }

    @Override
    final public void onNext(T bean) {
        L.d("订阅onNext->" + this.getClass().getSimpleName());
        try {
            onSucceed(bean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSucceed(T bean) {

    }

    @Override
    final public void onError(Throwable e) {
        L.e("----------------------------------------异常处理----------------------------------------");
        int errorCode;
        String errorMsg;
        String errorMore = "no more";
        boolean error = true, nonet = false;

        if (e instanceof UnknownHostException ||
                e instanceof SocketTimeoutException ||
                e instanceof SocketException /*无网络*/ ||
                e instanceof NonetException ||
                e instanceof HttpException) {
            L.e(e.getMessage());
            errorMsg = "请检查网络连接!";
            errorCode = NO_NETWORK;
        } else if (e instanceof JsonParseException || e instanceof JsonMappingException) {
            errorMsg = "恐龙君打了个盹，请稍后再试!"; //   "数据解析错误:" + e.getMessage();
            errorCode = -40001;
        } else if (e instanceof RException) {
            errorMsg = ((RException) e).getMsg();
            errorCode = ((RException) e).getCode();
            errorMore = ((RException) e).getMore();
        } else {
            errorMsg = "未知错误:" + e.getMessage();
            errorCode = -40000;
        }

        e.printStackTrace();

        L.d("订阅异常->" + this.getClass().getSimpleName() + " [" + errorCode + "]" + errorMsg);
        L.e("-----------------------------------------End-------------------------------------------");

        if (errorCode == RSubscriber.NO_NETWORK) {
            try {
                onNoNetwork();
            } catch (Exception e1) {

            }
            nonet = true;
        }

        RException exception = new RException(errorCode, errorMsg, errorMore).setThrowable(e);

        try {
            onError(errorCode, errorMsg);
        } catch (Exception e1) {

        }

        try {
            onEnd(error, nonet, exception);
        } catch (Exception e1) {
        }
//        if (L.LOG_DEBUG) {
//            T_.error("[" + errorCode + "]" + errorMsg);
//        }
    }

    /**
     * 统一错误处理, 多用来弹出Toast
     */
    public void onError(int code, String msg) {
        //L.w("call: onError([code, msg])-> " + code + " " + msg);
    }

    /**
     * 统一错误处理
     * 不管是成功订阅,还是异常,都会执行的方法
     */
    public void onEnd(boolean isError, boolean isNoNetwork, RException e) {
        L.e("订阅结束-> isError:" + isError + " isNoNetwork:" + isNoNetwork + " Throwable:" + e);
    }

//    public void onEnd(boolean isError, int errorCode, boolean isNoNetwork, RException e) {
//        L.e("订阅结束-> isError:" + isError + " errorCode:" + errorCode + " isNoNetwork:" + isNoNetwork + " Throwable:" + e);
//    }

    public void onNoNetwork() {
        //L.w("call: onNoNetwork([])-> ");
    }
}
