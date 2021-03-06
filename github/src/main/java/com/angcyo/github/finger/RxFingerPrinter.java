package com.angcyo.github.finger;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.HashMap;

import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static com.angcyo.github.finger.CodeException.FINGERPRINTERS_FAILED_ERROR;
import static com.angcyo.github.finger.CodeException.HARDWARE_MISSIING_ERROR;
import static com.angcyo.github.finger.CodeException.KEYGUARDSECURE_MISSIING_ERROR;
import static com.angcyo.github.finger.CodeException.NO_FINGERPRINTERS_ENROOLED_ERROR;
import static com.angcyo.github.finger.CodeException.PERMISSION_DENIED_ERROE;
import static com.angcyo.github.finger.CodeException.SYSTEM_API_ERROR;


/**
 * 指纹识别
 * Created by Administrator on 2016/12/31.
 * https://github.com/Zweihui/RxFingerPrinter
 */

public class RxFingerPrinter {
    static final String TAG = "RxFingerPrinter";
    PublishSubject<Boolean> publishSubject;
    @SuppressLint("NewApi")
    CancellationSignal mCancellationSignal;
    @SuppressLint("NewApi")
    FingerprintManager.AuthenticationCallback mSelfCancelled;
    private FingerprintManager manager;
    private KeyguardManager mKeyManager;
    private Context context;
    private HashMap<String, CompositeSubscription> mSubscriptionMap;

    public RxFingerPrinter(@NonNull Context context) {
        this.context = context;
    }

    /**
     * 判断指纹是否可用, 1:有指纹模块, 2:录入了指纹
     */
    public static boolean isFingerAvailable(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }

        FingerprintManager manager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        KeyguardManager mKeyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

        //权限检查
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        //判断硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            return false;
        }
        //判断 是否开启锁屏密码
        if (!mKeyManager.isKeyguardSecure()) {
            return false;
        }
        //判断是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            return false;
        }

        return true;
    }

    public PublishSubject<Boolean> begin() {

        if (publishSubject == null) {
            publishSubject = PublishSubject.create();
        }
        if (Build.VERSION.SDK_INT < 23) {
            publishSubject.onError(new FPerException(SYSTEM_API_ERROR));
        } else {
            initManager();
            if (confirmFinger()) {
                startListening(null);
            }
        }
        return publishSubject;

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            throw new FPerException(PERMISSION_DENIED_ERROE);
        }
        manager.authenticate(cryptoObject, null, 0, mSelfCancelled, null);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initManager() {
        mCancellationSignal = new CancellationSignal();
        manager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        mKeyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        mSelfCancelled = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                //多次指纹密码验证错误后，进入此方法；并且，不能短时间内调用指纹验证
                publishSubject.onError(new FPerException(FINGERPRINTERS_FAILED_ERROR));
                mCancellationSignal.cancel();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                publishSubject.onNext(true);
            }

            @Override
            public void onAuthenticationFailed() {
                publishSubject.onNext(false);
            }
        };
    }

    /**
     * 再次认证
     */
    public void authAgain() {
        startListening(null);
    }

    @SuppressLint("NewApi")
    @TargetApi(23)
    public boolean confirmFinger() {

        //android studio 上，没有这个会报错
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT)
                != PackageManager.PERMISSION_GRANTED) {
            publishSubject.onError(new FPerException(PERMISSION_DENIED_ERROE));
            return false;
        }
        //判断硬件是否支持指纹识别
        if (!manager.isHardwareDetected()) {
            publishSubject.onError(new FPerException(HARDWARE_MISSIING_ERROR));
            return false;
        }
        //判断 是否开启锁屏密码

        if (!mKeyManager.isKeyguardSecure()) {
            publishSubject.onError(new FPerException(KEYGUARDSECURE_MISSIING_ERROR));
            return false;
        }
        //判断是否有指纹录入
        if (!manager.hasEnrolledFingerprints()) {
            publishSubject.onError(new FPerException(NO_FINGERPRINTERS_ENROOLED_ERROR));
            return false;
        }

        return true;
    }

    public Observable.Transformer<Object, Boolean> ensure() {
        return new Observable.Transformer<Object, Boolean>() {

            @Override
            public Observable<Boolean> call(Observable<Object> o) {
                return null;
            }
        };
    }


    /**
     * 保存订阅后的subscription
     *
     * @param o
     * @param subscription
     */
    public void addSubscription(Object o, Subscription subscription) {
        if (mSubscriptionMap == null) {
            mSubscriptionMap = new HashMap<>();
        }
        String key = o.getClass().getName();
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).add(subscription);
        } else {
            CompositeSubscription compositeSubscription = new CompositeSubscription();
            compositeSubscription.add(subscription);
            mSubscriptionMap.put(key, compositeSubscription);
        }
    }


    /**
     * 取消订阅
     *
     * @param o
     */
    public void unSubscribe(Object o) {
        if (mSubscriptionMap == null) {
            return;
        }

        String key = o.getClass().getName();
        if (!mSubscriptionMap.containsKey(key)) {
            return;
        }
        if (mSubscriptionMap.get(key) != null) {
            mSubscriptionMap.get(key).unsubscribe();
        }

        mSubscriptionMap.remove(key);
    }

}
