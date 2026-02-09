package com.d104.pnt.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import com.d104.pnt.service.location.LocationService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

fun Context.changeLocationMode(isGameMode: Boolean) {
    val intent = Intent(this, LocationService::class.java).apply {
        putExtra(LocationService.EXTRA_GAME_MODE, isGameMode)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.startForegroundService(intent)
    } else {
        this.startService(intent)
    }
}

@SuppressLint("MissingPermission")
fun Context.stopLocationService() {
    val intent = Intent(this, LocationService::class.java)
    this.stopService(intent)
}

@SuppressLint("MissingPermission")
suspend fun Context.getSingleLocation(): Location? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    return try {
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            CancellationTokenSource().token
        ).await()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}