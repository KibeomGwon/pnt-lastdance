package com.d104.pnt.service.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.d104.pnt.data.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var LocationRepository: LocationRepository

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    LocationRepository.updateCurrentLocation(location)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "location_channel")
            .setContentTitle("위치 추적 중")
            .setContentText("백그라운드에서 위치 정보를 수집하고 있습니다.")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(100, notification)

        val inGameMode: Boolean = intent?.getBooleanExtra(EXTRA_GAME_MODE, false) ?: false

        startLocationUpdates(inGameMode)

        return START_STICKY
    }

    private fun startLocationUpdates(shortInterval: Boolean) {
        val intervalMillis = if (shortInterval) 1000L else 30000L

        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis).apply {
                setMinUpdateIntervalMillis(intervalMillis)
                setMinUpdateDistanceMeters(if (shortInterval) 1.0f else 10.0f)
                setWaitForAccurateLocation(false)
            }.build()

        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location_channel",
                "위치 서비스 채널",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val EXTRA_GAME_MODE = "com.d104.pnt.extra_game_mode"
    }
}