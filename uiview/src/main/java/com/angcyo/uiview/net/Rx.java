package com.angcyo.uiview.net;

import android.text.TextUtils;

import com.angcyo.github.type.TypeBuilder;
import com.angcyo.library.utils.L;
import com.angcyo.uiview.utils.Json;
import com.angcyo.uiview.utils.T;
import com.angcyo.uiview.utils.ThreadExecutor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

/**
 * Created by robi on 2016-04-21 15:41.
 */
public class Rx<Rx> extends Observable<Rx> {

    /**
     * 网络请求错误, 重试的次数
     */
    public static final long RETRY_COUNT = 0;
    public static final long RETRY_ERROR_COUNT = 5;
    public static final Observable.Transformer<T, T> ioSchedulersTransformer = new Observable.Transformer<T, T>() {
        @Override
        public Observable<T> call(Observable<T> tObservable) {
            return tObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }
    };
    public static final Observable.Transformer<T, T> newThreadSchedulersTransformer = new Observable.Transformer<T, T>() {
        @Override
        public Observable<T> call(Observable<T> tObservable) {
            return tObservable.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread());
        }
    };

    static ErrorRunnable onErrorRetryRunnable;

    protected Rx(OnSubscribe<Rx> f) {
        super(f);
    }

    public static void setOnErrorRetryRunnable(ErrorRunnable errorRetryRunnable) {
        onErrorRetryRunnable = errorRetryRunnable;
    }

    public static void runErrorRetry(final int code) {
        if (onErrorRetryRunnable != null) {
            ThreadExecutor.instance().onThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        onErrorRetryRunnable.setCode(code);
                        onErrorRetryRunnable.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 是否需要重试, token过期或者无效
     */
    public static boolean needRetryOnError(RException throwable) {
        //1018	无效的token
        //1019	token未获取或已过期
        //3003	无效的参数
        return throwable.getCode() == 1018 ||
                throwable.getCode() == 1019 ||
                throwable.getCode() == 1066 /*||
                throwable.getCode() == 3003*/;
    }

    public static boolean needRetryOnError(Throwable throwable) {
        if (throwable instanceof RException) {
            return needRetryOnError((RException) throwable);
        }
        return false;
    }

    public static final <T> Observable.Transformer<T, T> applyNewThreadSchedulers() {
        return (Observable.Transformer<T, T>) newThreadSchedulersTransformer;
    }

    public static final <T> Observable.Transformer<T, T> applyIOSchedulers() {
        return (Observable.Transformer<T, T>) ioSchedulersTransformer;
    }

    public static <T> Observable.Transformer<T, T> normalSchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> source) {
                return source.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T, R> Subscription base(T t, Func1<? super T, ? extends R> func) {
        return Observable.just(t).map(func).compose(applyIOSchedulers()).subscribe(new RSubscriber<Object>() {

            @Override
            public void onError(int code, String msg) {
                //super.onError(code, msg);
            }
        });
    }

    /**
     * <pre>
     *
     * Rx.base(object : RFunc<HomeBean>() {
     *      override fun call(t: String?): HomeBean? {
     *      return null
     *  }
     * }, object : RSubscriber<HomeBean>() {
     *
     * })
     *
     * </pre>
     * 简单的子线程,转主线程调用
     */
    public static <R> Subscription base(RFunc<? extends R> onBack, RSubscriber<R> onMain) {
        return Observable
                .just("base")
                .map(onBack)
                .compose(new Transformer<R, R>() {
                    @Override
                    public Observable<R> call(Observable<R> rObservable) {
                        return rObservable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
                    }
                })
                .subscribe(onMain);
    }

    public static Subscription base(final Runnable onBack, final Runnable onMain) {
        return base(
                new RFunc<String>() {
                    @Override
                    public String onFuncCall() {
                        onBack.run();
                        return "";
                    }
                },
                new RSubscriber<String>() {
                    @Override
                    public void onSucceed(String bean) {
                        super.onSucceed(bean);
                        onMain.run();
                    }
                });
    }

    public static Subscription base(final Runnable onMain) {
        return main(onMain);
    }

    public static Subscription main(final Runnable onMain) {
        return base(
                new RFunc<String>() {
                    @Override
                    public String onFuncCall() {
                        return "";
                    }
                },
                new RSubscriber<String>() {
                    @Override
                    public void onSucceed(String bean) {
                        super.onSucceed(bean);
                        onMain.run();
                    }
                });
    }

    public static Subscription back(final Runnable onBack) {
        return base(
                new RFunc<String>() {
                    @Override
                    public String onFuncCall() {
                        onBack.run();
                        return "";
                    }
                },
                new RSubscriber<String>() {
                    @Override
                    public void onSucceed(String bean) {
                        super.onSucceed(bean);
                    }
                });
    }


    public static <T, R> Subscription base(T t, Func1<? super T, ? extends R> func, Scheduler scheduler) {
        return scheduler == Schedulers.newThread() ?
                Observable.just(t).map(func).compose(applyNewThreadSchedulers()).subscribe(new RSubscriber<Object>() {
                    @Override
                    public void onError(int code, String msg) {
                        //super.onError(code, msg);
                    }
                })
                :
                Observable.just(t).map(func).compose(applyIOSchedulers()).subscribe(new RSubscriber<Object>() {
                    @Override
                    public void onError(int code, String msg) {
                        //super.onError(code, msg);
                    }
                });
    }

    public static <R> Subscription base(Func1<String, ? extends R> func) {
        return base("-", func);
    }

    public static <R> Subscription base(Func1<String, ? extends R> func, Scheduler scheduler) {
        return base("-", func, scheduler);
    }

    public static <T> Observable.Transformer<T, T> transformer() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    private static Func2<Integer, Throwable, Boolean> createRetry() {
        return new Func2<Integer, Throwable, Boolean>() {
            @Override
            public Boolean call(Integer integer, Throwable throwable) {
                if (throwable instanceof SocketTimeoutException) {
                    L.e("retry ..." + integer + " 无网络");
                    throw new RException(RSubscriber.NO_NETWORK, "无网络");
                }
                if (throwable instanceof RException) {
                    if (needRetryOnError((RException) throwable)) {
//                                        if (integer < RETRY_ERROR_COUNT) {
                        runErrorRetry(((RException) throwable).getCode());
//                                            return true;
//                                        }
                    }
                }
                try {
                    Thread.sleep(500);
                } catch (Exception e) {

                }
                L.e("retry ..." + integer + " " + throwable);
                return false;
            }
        };
    }

    /**
     * 不检查数据返回, 直接转换成Bean
     */
    public static <T> Observable.Transformer<ResponseBody, T> transformerBean(final Class<T> type) {
        return new Observable.Transformer<ResponseBody, T>() {

            @Override
            public Observable<T> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, T>() {
                            @Override
                            public T call(ResponseBody stringResponse) {
                                T bean;
                                String body;
                                try {
                                    body = stringResponse.string();

                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                    bean = Json.from(body, type);
                                    return bean;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //throw new RException(-1000, "服务器数据异常.", e.getMessage());
                                }
                                //throw new NullPointerException("无数据.");
                                return null;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 不检查数据返回, 直接转换成列表
     */
    public static <T> Observable.Transformer<ResponseBody, List<T>> transformerListBean(final Class<T> type) {
        return new Observable.Transformer<ResponseBody, List<T>>() {

            @Override
            public Observable<List<T>> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, List<T>>() {
                            @Override
                            public List<T> call(ResponseBody stringResponse) {
                                List<T> list = new ArrayList<>();
                                String body;
                                try {
                                    body = stringResponse.string();

                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                    list = Json.from(body, TypeBuilder.newInstance(List.class).addTypeParam(type).build());
                                    return list;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    //throw new RException(-1000, "服务器数据异常.", e.getMessage());
                                }
                                //throw new NullPointerException("无数据.");
                                return list;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    /**
     * 拿到的原始数据, 直接返回
     */
    public static Observable.Transformer<ResponseBody, String> transformerRaw() {
        return new Observable.Transformer<ResponseBody, String>() {

            @Override
            public Observable<String> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, String>() {
                            @Override
                            public String call(ResponseBody stringResponse) {
                                String body;
                                try {
                                    body = stringResponse.string();
                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    body = e.getMessage();
                                }
                                return body;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable.Transformer<ResponseBody, T> transformer(final Class<T> type) {
        return new Observable.Transformer<ResponseBody, T>() {

            @Override
            public Observable<T> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, T>() {
                            @Override
                            public T call(ResponseBody stringResponse) {
                                T bean = null;
                                String body = null;
                                try {
                                    body = stringResponse.string();

                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                    JSONObject jsonObject = new JSONObject(body);
                                    int result = 0;
                                    try {
                                        result = jsonObject.getInt("result");
                                    } catch (Exception e) {
                                        //兼容资讯API
                                        result = jsonObject.getInt("code");
                                        if (result == 200 || result == 0) {
                                            result = 1;//资讯接口没有此字段
                                        }
                                    }

                                    if (result == 1) {
                                        //请求成功
                                        String data = null;
                                        try {
                                            data = jsonObject.getString("data");
                                        } catch (JSONException e) {
                                            //没有data字段
                                            //e.printStackTrace();
                                            data = "";
                                        }
                                        if (!TextUtils.isEmpty(data)) {
                                            if (type == String.class) {
                                                bean = (T) data;
                                                return bean;
                                            }
                                            bean = Json.from(data, type);
                                            return bean;
                                        }
                                    } else {
                                        //请求成功, 但是有错误
                                        JSONObject errorObject = null;

                                        int errorCode = result;
                                        String errorMsg = "";
                                        String errorMore = "no more";

                                        try {
                                            errorObject = jsonObject.getJSONObject("error");
                                            errorCode = errorObject.getInt("code");
                                            errorMsg = errorObject.getString("msg");
                                            errorMore = errorObject.getString("more");
                                        } catch (JSONException e) {
                                            //e.printStackTrace();
                                            errorMsg = jsonObject.getString("data");
                                        }

                                        throw new RException(errorCode, errorMsg, errorMore);
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                    //throw new RException(-1000, "服务器数据异常.", e.getMessage());
                                }
                                //throw new NullPointerException("无数据.");
                                if (bean instanceof String) {
                                    return (T) body;
                                }
                                return null;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable.Transformer<ResponseBody, T> transformRedPacket(final Class<T> type) {
        return new Observable.Transformer<ResponseBody, T>() {

            @Override
            public Observable<T> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, T>() {
                            @Override
                            public T call(ResponseBody stringResponse) {
                                T bean;
                                String body;
                                try {
                                    body = stringResponse.string();

                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                    JSONObject jsonObject = new JSONObject(body);
                                    int result = -1;
                                    try {
                                        result = jsonObject.getInt("code");
                                    } catch (Exception e) {

                                    }

                                    if (result == 0) {
                                        //请求成功
                                        String data = null;
                                        try {
                                            data = jsonObject.getString("data");
                                        } catch (JSONException e) {
                                            //没有data字段
                                            //e.printStackTrace();
                                            data = "";
                                        }
                                        if (!TextUtils.isEmpty(data)) {
                                            bean = Json.from(data, type);
                                            return bean;
                                        }
                                    } else {
                                        //请求成功, 但是有错误
                                        JSONObject errorObject = null;

                                        int errorCode = result;
                                        String errorMsg = "";
                                        String errorMore = "no more";

                                        try {
                                            errorObject = jsonObject.getJSONObject("error");
                                            errorCode = errorObject.getInt("code");
                                            errorMsg = errorObject.getString("msg");
                                            errorMore = errorObject.getString("more");
                                        } catch (JSONException e) {
                                            //e.printStackTrace();
                                            errorMsg = jsonObject.getString("data");
                                        }

                                        throw new RException(errorCode, errorMsg, errorMore);
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable.Transformer<ResponseBody, List<T>> transformerList(final Class<T> type) {
        return new Observable.Transformer<ResponseBody, List<T>>() {

            @Override
            public Observable<List<T>> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, List<T>>() {
                            @Override
                            public List<T> call(ResponseBody stringResponse) {
                                List<T> bean = new ArrayList<>();
                                String body;
                                try {
                                    body = stringResponse.string();

                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                    JSONObject jsonObject = new JSONObject(body);
                                    int result = 0;
                                    try {
                                        result = jsonObject.getInt("result");
                                    } catch (Exception e) {
                                        ////兼容资讯API
                                        result = jsonObject.getInt("code");
                                        if (result == 200 || result == 0) {
                                            result = 1;//资讯接口没有此字段
                                        }
                                    }
                                    if (result == 1) {
                                        //请求成功
                                        String data = null;
                                        try {
                                            data = jsonObject.getString("data");
                                        } catch (JSONException e) {
                                            //没有data字段
                                            //e.printStackTrace();
                                            data = "";
                                        }
                                        if (!TextUtils.isEmpty(data)) {
                                            bean = Json.from(data, TypeBuilder.newInstance(List.class).addTypeParam(type).build());
                                            return bean;
                                        }
                                    } else {
                                        //请求成功, 但是有错误
                                        JSONObject errorObject = null;

                                        int errorCode = result;
                                        String errorMsg = "";
                                        String errorMore = "no more";

                                        try {
                                            errorObject = jsonObject.getJSONObject("error");
                                            errorCode = errorObject.getInt("code");
                                            errorMsg = errorObject.getString("msg");
                                            errorMore = errorObject.getString("more");
                                        } catch (JSONException e) {
                                            //e.printStackTrace();
                                            errorMsg = jsonObject.getString("data");
                                        }

                                        throw new RException(errorCode, errorMsg, errorMore);
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                                return bean;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable.Transformer<ResponseBody, List<T>> transformerList(final Class<T> type, final int successCode) {
        return new Observable.Transformer<ResponseBody, List<T>>() {

            @Override
            public Observable<List<T>> call(Observable<ResponseBody> responseObservable) {
                return responseObservable.unsubscribeOn(Schedulers.io())
                        .subscribeOn(Schedulers.io())
                        .map(new Func1<ResponseBody, List<T>>() {
                            @Override
                            public List<T> call(ResponseBody stringResponse) {
                                List<T> bean = new ArrayList<>();
                                String body;
                                try {
                                    body = stringResponse.string();

                                    //"接口返回数据-->\n" +
                                    L.json(body);

                                    JSONObject jsonObject = new JSONObject(body);
                                    int result = 0;
                                    try {
                                        result = jsonObject.getInt("result");
                                    } catch (Exception e) {
                                        ////兼容资讯API
                                        result = jsonObject.getInt("code");
                                        if (result == 200 || result == 0) {
                                            result = successCode;//资讯接口没有此字段
                                        }
                                    }
                                    if (result == successCode) {
                                        //请求成功
                                        String data = null;
                                        try {
                                            data = jsonObject.getString("data");
                                        } catch (JSONException e) {
                                            //没有data字段
                                            //e.printStackTrace();
                                            data = "";
                                        }
                                        if (!TextUtils.isEmpty(data)) {
                                            bean = Json.from(data, TypeBuilder.newInstance(List.class).addTypeParam(type).build());
                                            return bean;
                                        }
                                    } else {
                                        //请求成功, 但是有错误
                                        JSONObject errorObject = null;

                                        int errorCode = result;
                                        String errorMsg = "";
                                        String errorMore = "no more";

                                        try {
                                            errorObject = jsonObject.getJSONObject("error");
                                            errorCode = errorObject.getInt("code");
                                            errorMsg = errorObject.getString("msg");
                                            errorMore = errorObject.getString("more");
                                        } catch (JSONException e) {
                                            //e.printStackTrace();
                                            errorMsg = jsonObject.getString("data");
                                        }

                                        throw new RException(errorCode, errorMsg, errorMore);
                                    }
                                } catch (JSONException | IOException e) {
                                    e.printStackTrace();
                                }
                                return bean;
                            }
                        })
                        .retry(createRetry())
                        .retry(RETRY_COUNT)
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Observable<T> create(final Func<T> doFunc) {
        return create(new SyncOnSubscribe<Integer, T>() {
            @Override
            protected Integer generateState() {
                return 1;
            }

            @Override
            protected Integer next(Integer state, Observer<? super T> observer) {
                //L.e("next-----() -> " + state);
                if (state <= 0) {
                    observer.onCompleted();
                } else {
                    observer.onNext(doFunc.call(observer));
                }
                return 0;
            }
        }).compose(com.angcyo.uiview.net.Rx.<T>transformer());
    }

    public static <T> Observable<T> get(final Func<T> doFunc) {
        return create(doFunc);
    }
}
