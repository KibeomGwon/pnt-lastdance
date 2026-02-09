package com.d104.pnt.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameFeedbackManager @Inject constructor(
    @ApplicationContext context: Context
) {
    private val appContext = context.applicationContext

    // VIBRATOR
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    // TONE
    private val toneGenerator: ToneGenerator by lazy {
        ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    }

    // SOUND POOL
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundMap = mutableMapOf<SoundType, Int>()

    enum class SoundType {
        BEEP_NEAR_POLICE,
    }

    init {
        loadSounds()
    }

    // 내부 코루틴 / 동시성 제어
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val playMutex = Mutex()
    private var playJob: Job? = null

    private var lastPlayAtMs: Long = 0L
    private val minIntervalMs = 450L

    // PUBLIC API

    /**
     * 경찰 근접 경보
     */
    fun playBeepAlert(distance: Double?) {
        scope.launch {
            playEmergencyBeepAlertInternal(distance)
        }
    }

    fun playEmergencyTestAlert() {
        playBeepAlert(distance = 10.0)
    }

    /**
     * 앱 종료 시 리소스 정리
     */
    fun release() {
        try {
            playJob?.cancel()
            soundPool.release()
            toneGenerator.release()
            scope.cancel()
        } catch (e: Exception) {

        }
    }

    // INTERNAL

    private suspend fun playEmergencyBeepAlertInternal(distance: Double?) {
        val now = System.currentTimeMillis()
        if (now - lastPlayAtMs < minIntervalMs) {
            return
        }
        lastPlayAtMs = now

        playMutex.withLock {
            playJob?.cancel()

            val pattern = buildPattern(distance)

            playJob = scope.launch {
                try {
                    playVibration(pattern.vibrationType)

                    repeat(pattern.beepCount) { idx ->
                        playBeepOnce(pattern.beepDurationMs)
                        if (idx != pattern.beepCount - 1) {
                            delay(pattern.beepGapMs)
                        }
                    }

                    if (pattern.longToneMs > 0) {
                        delay(pattern.longToneDelayMs)
                        playBeepOnce(pattern.longToneMs)
                    }
                } catch (_: Exception) {

                }
            }
        }
    }

    /**
     * distance에 따른 강도 패턴 설계
     */
    private fun buildPattern(distance: Double?): BeepPattern {
        val d = distance ?: 9999.0

        return when {
            d <= 10.0 -> {
                // 매우 가까움
                BeepPattern(
                    beepCount = 4,
                    beepDurationMs = 120,
                    beepGapMs = 90,
                    longToneMs = 450,
                    longToneDelayMs = 120,
                    vibrationType = VibrationType.STRONG
                )
            }

            d <= 20.0 -> {
                // 가까움
                BeepPattern(
                    beepCount = 3,
                    beepDurationMs = 110,
                    beepGapMs = 120,
                    longToneMs = 220,
                    longToneDelayMs = 120,
                    vibrationType = VibrationType.MEDIUM
                )
            }

            d <= 35.0 -> {
                // 중간
                BeepPattern(
                    beepCount = 2,
                    beepDurationMs = 110,
                    beepGapMs = 160,
                    longToneMs = 0,
                    longToneDelayMs = 0,
                    vibrationType = VibrationType.LIGHT
                )
            }

            else -> {
                // 멀다
                BeepPattern(
                    beepCount = 1,
                    beepDurationMs = 120,
                    beepGapMs = 0,
                    longToneMs = 0,
                    longToneDelayMs = 0,
                    vibrationType = VibrationType.LIGHT
                )
            }
        }
    }

    private fun playBeepOnce(durationMs: Int) {
        val soundId = soundMap[SoundType.BEEP_NEAR_POLICE]
        if (soundId != null) {
            soundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
            return
        }

        try {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, durationMs)
        } catch (e: Exception) {

        }
    }

    private fun playVibration(type: VibrationType) {
        if (!vibrator.hasVibrator()) return

        try {
            when (type) {
                VibrationType.LIGHT -> vibrateOneShot(40L, 90)
                VibrationType.MEDIUM -> vibrateOneShot(70L, 140)
                VibrationType.STRONG -> {
                    vibrateOneShot(70L, 200)
                    scope.launch {
                        delay(90)
                        vibrateOneShot(60L, 200)
                    }
                }

                VibrationType.NONE -> Unit
            }
        } catch (e: Exception) {
        }
    }

    private fun vibrateOneShot(durationMillis: Long, amplitude: Int) {
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(durationMillis, amplitude)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(durationMillis)
        }
    }

    private fun loadSounds() {
        try {

        } catch (e: Exception) {

        }
    }

    // DATA

    private data class BeepPattern(
        val beepCount: Int,
        val beepDurationMs: Int,
        val beepGapMs: Long,
        val longToneMs: Int,
        val longToneDelayMs: Long,
        val vibrationType: VibrationType
    )

    private enum class VibrationType {
        NONE, LIGHT, MEDIUM, STRONG
    }
}
