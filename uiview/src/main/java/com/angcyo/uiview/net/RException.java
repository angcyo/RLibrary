package com.angcyo.uiview.net;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2016/12/26 18:23
 * 修改人员：Robi
 * 修改时间：2016/12/26 18:23
 * 修改备注：
 * Version: 1.0.0
 */
public class RException extends RuntimeException {


    Throwable throwable;
    /**
     * code : 1039
     * msg : 验证码未发送或者已过期,请重新发送
     * more :
     */

    private int code;
    private String msg;
    private String more;

    public RException(int code, String msg, String more) {
        this.code = code;
        this.msg = msg;
        this.more = more;
    }

    public RException(int code, String msg) {
        this(code, msg, "no more");
    }

    @Override
    public String getMessage() {
        return "[" + code + "]" + msg + ":" + throwable;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public RException setThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public String getMore() {
        return more;
    }

    public int getCode() {
        return code;
    }
}
