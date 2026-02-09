package com.d104.pnt.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class StepSensorManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SensorEventListener {
    private val sensorManager = ContextCompat.getSystemService(context, SensorManager::class.java)
    private val stepSensor = sensorManager!!.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _stepCountFlow = MutableStateFlow<Int>(0)
    val stepCountFlow = _stepCountFlow.asStateFlow()

    private var initialSteps: Int = -1

    fun startListening() {
        if (stepSensor == null) {
            return
        }

        // 센서 리스너 등록
        sensorManager!!.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    fun resetGameSteps() {
        initialSteps = -1
        _stepCountFlow.value = 0
    }

    // 센서 값이 변할 때마다 호출
    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_STEP_COUNTER) {
                // 현재 디바이스 총 누적 걸음 수
                val currentTotalSteps = it.values[0].toInt()

                if (initialSteps == -1) {
                    initialSteps = currentTotalSteps
                }

                var stepsInGame = currentTotalSteps - initialSteps

                if (stepsInGame < 0) {
                    stepsInGame = 0
                    initialSteps = currentTotalSteps
                }

                _stepCountFlow.value = stepsInGame
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}