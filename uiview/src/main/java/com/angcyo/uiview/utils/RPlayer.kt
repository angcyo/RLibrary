package com.angcyo.uiview.utils

import android.media.AudioManager
import android.media.MediaPlayer
import java.util.concurrent.atomic.AtomicInteger

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：播放音乐工具类
 * 创建人员：Robi
 * 创建时间：2017/10/25 16:20
 * 修改人员：Robi
 * 修改时间：2017/10/25 16:20
 * 修改备注：
 * Version: 1.0.0
 */
class RPlayer {
    private var mediaPlay: MediaPlayer? = null

    /**是否循环播放*/
    var isLoop = false

    var onPlayListener: OnPlayerListener? = null

    var audioStreamType = AudioManager.STREAM_MUSIC

    var leftVolume: Float = 0.5f
    var rightVolume: Float = 0.5f

    /**当前播放的状态*/
    private var playState: AtomicInteger = AtomicInteger(STATE_NORMAL)

    companion object {
        /**正常情况*/
        const val STATE_NORMAL = 1
        /**播放中*/
        const val STATE_PLAYING = 2
        /**停止播放*/
        const val STATE_STOP = 3
        /**资源释放*/
        const val STATE_RELEASE = 4
        const val STATE_ERROR = -1
    }

    fun init() {
        if (mediaPlay == null) {
            mediaPlay = MediaPlayer()
        }
    }

    /**@param url 可是有效的网络, 和有效的本地地址*/
    fun startPlay(url: String) {
        stopPlay()
        mediaPlay?.let {
            it.isLooping = isLoop
            it.setAudioStreamType(audioStreamType)
            it.setVolume(leftVolume, rightVolume)

            it.setOnErrorListener { mp, what, extra ->
                //L.e("call: startPlay -> $what $extra")
                playState.set(STATE_ERROR)
                onPlayListener?.onPlayError(what, extra)

                false
            }
            it.setOnCompletionListener {
                playState.set(STATE_NORMAL)
                onPlayListener?.onPlayCompletion(it.duration)
                it.reset()
            }
            it.setOnPreparedListener {
                //L.e("call: startPlay -> onPrepared ${it.duration}")
                onPlayListener?.onPreparedCompletion(it.duration)
                if (playState.get() == STATE_NORMAL) {
                    playState.set(STATE_PLAYING)
                    startProgress()
                    it.start()
                }
            }
            it.setDataSource(url)

            playState.set(STATE_NORMAL)
            it.prepareAsync()
        }
    }

    /**停止播放, 不释放资源, 下次可以重新setDataSource*/
    fun stopPlay() {

        mediaPlay?.let {
            if (isPlaying()) {
                it.stop()
            }
            it.reset()
        }

        playState.set(STATE_STOP)
    }

    /**释放资源, 下次需要重新创建*/
    fun release() {
        playState.set(STATE_RELEASE)
        stopPlay()
        mediaPlay?.let {
            it.release()
        }
        mediaPlay = null
    }

    /**设置音量*/
    fun setVolume(value: Float) {
        leftVolume = value
        rightVolume = value
        mediaPlay?.let {
            it.setVolume(value, value)
        }
    }

    private fun isPlaying() = playState.get() == STATE_PLAYING

    /*开始进度读取*/
    private fun startProgress() {
        Thread(Runnable {
            while (isPlaying() &&
                    mediaPlay != null &&
                    onPlayListener != null) {
                ThreadExecutor.instance().onMain {
                    if (isPlaying() && mediaPlay != null && onPlayListener != null) {
                        onPlayListener?.onPlayProgress(mediaPlay!!.currentPosition, mediaPlay!!.duration)
                    }
                }
                try {
                    Thread.sleep(300)
                } catch (e: Exception) {
                }
            }
        }).apply {
            start()
        }
    }

    interface OnPlayerListener {
        /**@param duration 媒体总时长 毫秒*/
        fun onPreparedCompletion(duration: Int)

        /**播放进度回调*/
        fun onPlayProgress(progress: Int, duration: Int)

        /**播放完成*/
        fun onPlayCompletion(duration: Int)

        /**播放错误*/
        fun onPlayError(what: Int, extra: Int)
    }
}