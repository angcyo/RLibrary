package com.angcyo.uiview.helper

import com.angcyo.uiview.RApplication
import com.angcyo.uiview.helper.sound.*
import java.io.File


/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：用来播放音效
 * 创建人员：Robi
 * 创建时间：2017/12/22 09:09
 * 修改人员：Robi
 * 修改时间：2017/12/22 09:09
 * 修改备注：
 * Version: 1.0.0
 */
class SoundHelper {

    /*使用MediaPlayer播放*/
    private val musicManager: MusicManager by lazy {
        MusicManager()
    }

    /*使用SoundPool播放*/
    private val soundManager: SoundManager by lazy {
        SoundManager()
    }

    fun playMusiuc(pAssetPath: String): Music? {
        var music: Music? = null
        try {
            music = MusicFactory.createMusicFromAsset(musicManager,
                    RApplication.getApp(),
                    pAssetPath)
            music.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return music
    }

    fun playMusiuc(file: File): Music? {
        var music: Music? = null
        try {
            music = MusicFactory.createMusicFromFile(musicManager,
                    file)
            music.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return music
    }

    fun playMusiuc(resId: Int): Music? {
        var music: Music? = null
        try {
            music = MusicFactory.createMusicFromResource(musicManager,
                    RApplication.getApp(),
                    resId)
            music.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return music
    }

    fun releaseMusicAll() {
        musicManager.releaseAll()
    }

    fun pauseMusicAll() {
        musicManager.pause()
    }

    fun resumeMusicAll() {
        musicManager.resume()
    }

    fun playSound(pAssetPath: String): Sound? {
        var sound: Sound? = null
        try {
            sound = SoundFactory.createSoundFromAsset(soundManager,
                    RApplication.getApp(),
                    pAssetPath)
            sound.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sound
    }

    fun playSound(file: File): Sound? {
        var sound: Sound? = null
        try {
            sound = SoundFactory.createSoundFromFile(soundManager,
                    file)
            sound.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sound
    }

    fun playSound(resId: Int): Sound? {
        var sound: Sound? = null
        try {
            sound = SoundFactory.createSoundFromResource(soundManager,
                    RApplication.getApp(),
                    resId)
            sound.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sound
    }

    fun releaseSoundAll() {
        soundManager.releaseAll()
    }
}