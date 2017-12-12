package com.neovisionaries.ws.client.angcyo;

import android.text.TextUtils;
import android.util.Log;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：compile 'com.neovisionaries:nv-websocket-client:2.3'  2017-12-12
 * 类的描述：https://github.com/TakahikoKawasaki/nv-websocket-client
 * 创建人员：Robi
 * 创建时间：2017/12/12 14:01
 * 修改人员：Robi
 * 修改时间：2017/12/12 14:01
 * 修改备注：
 * Version: 1.0.0
 */
public class RWebSocket extends WebSocketAdapter {

    /**
     * webSocket关闭
     */
    public final static int WEB_SOCKET_CLOSE = 0x10;
    /**
     * websocket正在连接
     */
    public final static int WEB_SOCKET_CONNECTING = 0x11;
    /**
     * webSocket连接成功
     */
    public final static int WEB_SOCKET_OPEN = 0x12;
    final String TAG = "RWebSocket";
    final int TIMEOUT = 10_000;
    String mWebSocketUrl;
    /**
     * 连接状态标识
     */
    private int webSocketState = WEB_SOCKET_CLOSE;
    /**
     * 定时监测websocket状态  实现重连
     */
    private Subscription observable;

    private WebSocket mWebSocket;

    private RWebSocketListener listener;

    private RWebSocket() {
    }

    public static RWebSocket create() {
        return new RWebSocket();
    }

    public RWebSocket setListener(RWebSocketListener listener) {
        this.listener = listener;
        return this;
    }

    public void connect(String wsUrl) {
        if (TextUtils.isEmpty(wsUrl)) {
            return;
        }
        //连接相同的Url (会自动重连, 不需要手动重连) 
        if (TextUtils.equals(wsUrl, mWebSocketUrl)) {
            return;
        }
        //释放之前的资源
        if (mWebSocket != null) {
            closeWebSocket();
        }

        this.mWebSocketUrl = wsUrl;

        //注意  websocket的连接需要在异步线程
        Observable.just(mWebSocketUrl)
                .map(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        try {
                            webSocketState = WEB_SOCKET_CONNECTING;
                            WebSocketFactory mWebSocketFactory = new WebSocketFactory();
                            WebSocket webSocket = mWebSocketFactory.createSocket(mWebSocketUrl, TIMEOUT);
                            webSocket.addListener(RWebSocket.this);
                            webSocket.connect();
                        } catch (Exception e) {
                            Log.e(TAG, "连接出错:" + e.getMessage());
                            webSocketState = WEB_SOCKET_CLOSE;

                            throw new IllegalStateException(e);
                        }
                        return true;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        checkWebSocketState();
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }
                });
    }

    /**
     * webSocket的连接状态检查
     */
    private void checkWebSocketState() {
        Log.i(TAG, "开始检测webSocket的状态");
        if (observable == null || observable.isUnsubscribed()) {
            observable = Observable.interval(10 * 1000, 10 * 1000, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<Long>() {
                        @Override
                        public void onCompleted() {
                            if (mWebSocket != null) {
                                Log.i(TAG, "wenSocket连接是否成功:" + mWebSocket.isOpen());
                            }
                            if (!TextUtils.isEmpty(mWebSocketUrl) && webSocketState == WEB_SOCKET_CLOSE) {
                                Log.i(TAG, "检测到webSocket已断开,重连中...");
                                reconnecntWebsocket();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Long aLong) {

                        }
                    });
        }
    }

    /**
     * 关闭webSocket
     */
    public void closeWebSocket() {
        mWebSocketUrl = "";
        if (observable != null) {
            observable.unsubscribe();
        }
        if (mWebSocket != null) {
            mWebSocket.disconnect();
        }
        webSocketState = WEB_SOCKET_CLOSE;
        Log.i(TAG, "释放wensocket资源");
    }


    /**
     * 重新连接
     */
    public void reconnecntWebsocket() {
        if (mWebSocket != null) {
            try {
                webSocketState = WEB_SOCKET_CONNECTING;
                mWebSocket = mWebSocket.recreate();
                mWebSocket.connect();
            } catch (Exception e) {
                Log.e(TAG, "重连失败:" + e.getMessage());
                webSocketState = WEB_SOCKET_CLOSE;
                mWebSocket.disconnect();
                mWebSocket = null;
            }
        } else {
            connect(mWebSocketUrl);
        }
    }


    //*****************************WebSocketAdapter  start***********************************************************************/

    /**
     * 连接中途失败回调
     *
     * @param websocket
     * @param cause
     * @throws Exception
     */
    @Override
    public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
        super.onError(websocket, cause);
        Log.e(TAG, "webSocket连接失败onError：" + cause.getMessage());
        webSocketState = WEB_SOCKET_CLOSE;
        if (listener != null) {
            listener.disConnectWebsocket(2, cause.getMessage());
        }
    }

    /**
     * 连接中途失败回调
     *
     * @param websocket
     * @param exception
     * @throws Exception
     */
    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        super.onConnectError(websocket, exception);
        Log.e(TAG, "webSocket连接失败onConnectError：" + exception.getMessage());
        webSocketState = WEB_SOCKET_CLOSE;
        if (listener != null) {
            listener.disConnectWebsocket(3, exception.getMessage());
        }
    }

    /**
     * 连接断开  主动关闭webSocket时回调
     *
     * @param websocket
     * @param serverCloseFrame
     * @param clientCloseFrame
     * @param closedByServer
     * @throws Exception
     */
    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        super.onDisconnected(websocket, serverCloseFrame, clientCloseFrame, closedByServer);
        Log.e(TAG, "webSocket断开" + mWebSocket);
        webSocketState = WEB_SOCKET_CLOSE;
        if (listener != null) {
            listener.disConnectWebsocket(serverCloseFrame.getCloseCode(), serverCloseFrame.getCloseReason());
        }
    }

    /**
     * 连接成功
     *
     * @param websocket
     * @param headers
     * @throws Exception
     */
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        super.onConnected(websocket, headers);
        Log.i(TAG, "webSocket连接成功");
        if (websocket != null) {
            this.mWebSocket = websocket;
            webSocketState = WEB_SOCKET_OPEN;
            if (listener != null) {
                listener.connectSuccessWebsocket(websocket);
            }

        }
    }

    /**
     * 字节数据  请根据需要去处理
     *
     * @param websocket
     * @param binary
     * @throws Exception
     */
    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] binary) throws Exception {
        super.onBinaryMessage(websocket, binary);
    }

    /**
     * 获取数据
     *
     * @param websocket
     * @param text
     * @throws Exception
     */
    @Override
    public void onTextMessage(final WebSocket websocket, final String text) throws Exception {
        super.onTextMessage(websocket, text);
        Observable.just("1")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        dealData(websocket, text);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(String s) {

                    }
                });
    }


    /**
     * 因websocket接收数据的线程是子线程，外围接收者接受数据时也处于子线程，无法进行ui更新，因此通过rx将其放到主线程处理
     *
     * @param data
     */
    private void dealData(WebSocket websocket, String data) {
        if (listener != null) {
            listener.onTextMessage(websocket, data);
        }
    }


    //*****************************WebSocketAdapter  end***********************************************************************/
}
