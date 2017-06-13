package com.lzy.imagepicker.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.VideoView;

import com.lzy.imagepicker.R;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：
 * 创建人员：Robi
 * 创建时间：2017/06/13 15:19
 * 修改人员：Robi
 * 修改时间：2017/06/13 15:19
 * 修改备注：
 * Version: 1.0.0
 */
public class VideoPlayActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {

    public static final String KEY_VIDEO_PATH = "key_video_path";

    VideoView mVideoView;
    ImageView playView;

    public static void launcher(Activity activity, String videoPath) {
        Intent intent = new Intent(activity, VideoPlayActivity.class);
        intent.putExtra(KEY_VIDEO_PATH, videoPath);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);

        enableLayoutFullScreen();

        mVideoView = (VideoView) findViewById(R.id.video_view);
        playView = (ImageView) findViewById(R.id.play_view);

        playView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playView.setVisibility(View.GONE);
                mVideoView.start();
            }
        });

        String videoPath = getIntent().getStringExtra(KEY_VIDEO_PATH);

        mVideoView.setVideoPath(videoPath);
        mVideoView.start();

        mVideoView.setOnCompletionListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVideoView.stopPlayback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.setKeepScreenOn(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
        playView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void enableLayoutFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        playView.setVisibility(View.VISIBLE);
    }
}
