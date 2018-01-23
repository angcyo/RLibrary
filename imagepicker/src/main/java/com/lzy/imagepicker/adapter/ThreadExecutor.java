package com.lzy.imagepicker.adapter;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutor {
    static private ThreadExecutor instance;
    private final Executor mCallbackPoster;
    private final ExecutorService mExecutorService;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private ThreadExecutor() {
        mCallbackPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };

        mExecutorService = Executors.newCachedThreadPool();
    }

    public static ThreadExecutor instance() {
        return instance == null ? instance = new ThreadExecutor() : instance;
    }

    public void onMain(Runnable runnable) {
        mCallbackPoster.execute(runnable);
    }

    public void onThread(Runnable runnable) {
        mExecutorService.execute(runnable);
    }
}