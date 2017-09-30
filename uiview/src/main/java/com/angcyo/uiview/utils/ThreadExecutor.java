package com.angcyo.uiview.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 主线程调度器
 * Created by robi on 2016-06-02 20:47.
 */
public class ThreadExecutor {
    static private ThreadExecutor instance;
    private final Executor mCallbackPoster;
    private final Executor mCallbackPosterDelay;
    private final ExecutorService mExecutorService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int delayTime = 0;

    private ThreadExecutor() {
        mCallbackPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
        mCallbackPosterDelay = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.postDelayed(command, delayTime);
            }
        };

        mExecutorService = Executors.newCachedThreadPool();
    }

    public static ThreadExecutor instance() {
        return instance == null ? instance = new ThreadExecutor() : instance;
    }

    public void onMain(Runnable runnable) {
        onMain(0, runnable);
    }

    public void onThread(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    public void onMain(int delayTime, Runnable runnable) {
        this.delayTime = delayTime;
        if (delayTime > 0) {
            mCallbackPosterDelay.execute(runnable);
        } else {
            if (RUtils.isMainThread()) {
                if (runnable != null) {
                    runnable.run();
                }
            } else {
                mCallbackPoster.execute(runnable);
            }
        }
    }
}
