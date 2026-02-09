package com.d104.pnt.util

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var sfxPlayer: MediaPlayer? = null
    private var bgmPlayer: MediaPlayer? = null
    private var currentBgmTag: String? = null

    // 효과음 재생
    fun playSound(@RawRes soundResId: Int) {
        try {
            sfxPlayer?.release()

            sfxPlayer = MediaPlayer.create(context, soundResId).apply {
                setOnCompletionListener {
                    it.release()
                    sfxPlayer = null
                }
                start()
            }
        } catch (e: Exception) {

        }
    }

    // BGM 재생
    fun playBgm(@RawRes soundResId: Int, tag: String, isLooping: Boolean = true) {
        try {

            if (currentBgmTag == tag && bgmPlayer?.isPlaying == true) {
                return
            }

            // 기존 BGM 정리
            bgmPlayer?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }

            bgmPlayer = MediaPlayer.create(context, soundResId)

            if (bgmPlayer == null) {
                return
            }

            currentBgmTag = tag

            bgmPlayer?.apply {
                this.isLooping = isLooping
                setOnCompletionListener {
                    if (!isLooping) {
                        it.release()
                        bgmPlayer = null
                        currentBgmTag = null
                    }
                }
                setOnErrorListener { mp, what, extra ->
                    mp.release()
                    bgmPlayer = null
                    currentBgmTag = null
                    true
                }
                start()
            }
        } catch (e: Exception) {
            currentBgmTag = null
        }
    }

    // 반복 효과음
    fun playSoundWithRepeat(@RawRes soundResId: Int, count: Int) {
        var remaining = count

        try {
            sfxPlayer?.release()

            sfxPlayer = MediaPlayer.create(context, soundResId).apply {
                setOnCompletionListener {
                    remaining--
                    if (remaining > 0) {
                        it.start()
                    } else {
                        it.release()
                        sfxPlayer = null
                    }
                }
                start()
            }
        } catch (e: Exception) {

        }
    }

    // 특정 태그의 BGM만 정지
    fun stopBgm(tag: String? = null) {
        if (tag != null && currentBgmTag != tag) {
            return
        }

        bgmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgmPlayer = null
        currentBgmTag = null
    }

    // 모든 사운드 정지
    fun release() {

        sfxPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        sfxPlayer = null

        bgmPlayer?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
        bgmPlayer = null
        currentBgmTag = null
    }
}