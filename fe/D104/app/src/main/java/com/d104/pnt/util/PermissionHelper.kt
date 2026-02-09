package com.d104.pnt.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.d104.pnt.base.BaseApplication

/**
 * 앱에서 필요한 모든 권한 관리
 */
object PermissionHelper {
    /**
     * 권한 타입 정의
     */
    enum class PermissionType(
        val permissions: Array<String>,
        val title: String,
        val description: String,
    ) {
        LOCATION(
            permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            title = "위치 권한",
            description = "게임 진행을 위해 위치 권한이 필요합니다.",
        ),

        CAMERA(
            permissions = arrayOf(Manifest.permission.CAMERA),
            title = "카메라 권한",
            description = "미션 수행 및 QR 코드 스캔을 위해 카메라 권한이 필요합니다.",
        ),

        AUDIO(
            permissions = arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.MODIFY_AUDIO_SETTINGS
            ),
            title = "오디오 권한",
            description = "무전기 기능 사용을 위해 오디오 권한이 필요합니다.",
        ),

        NOTIFICATION(
            permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                emptyArray()
            },
            title = "알림 권한",
            description = "게임 중요 이벤트 알림을 위해 알림 권한이 필요합니다.",
        ),

        @RequiresApi(Build.VERSION_CODES.P)
        FOREGROUND_SERVICE(
            permissions = arrayOf(
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.FOREGROUND_SERVICE_LOCATION
            ),
            title = "백그라운드 위치 추적",
            description = "게임 중 지속적인 위치 추적을 위해 필요합니다.",
        ),

        @RequiresApi(Build.VERSION_CODES.Q)
        STEP_SENSOR(
            permissions = arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
            title = "걸음 수 측정",
            description = "게임 중 걸음 수를 측정하기 위해 필요합니다."
        );

        /**
         * 권한이 필요한지 확인
         */
        fun isRequired(): Boolean {
            return permissions.isNotEmpty()
        }
    }

    /**
     * 특정 권한이 승인되었는지 확인
     */
    fun isPermissionGranted(
        context: Context = BaseApplication.getContext(),
        permissionType: PermissionType
    ): Boolean {
        if (!permissionType.isRequired()) {
            return true
        }

        return permissionType.permissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) ==
                    PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 여러 권한이 모두 승인되었는지 확인
     */
    fun arePermissionsGranted(
        context: Context = BaseApplication.getContext(),
        vararg permissionTypes: PermissionType
    ): Boolean {
        return permissionTypes.all { isPermissionGranted(context, it) }
    }

    /**
     * 특정 권한이 거부되었고 "다시 묻지 않음"이 선택되었는지 확인
     */
    fun shouldShowRationale(
        activity: Activity,
        permissionType: PermissionType
    ): Boolean {
        return permissionType.permissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }

    /**
     * 앱 설정 화면으로 이동
     */
    fun openAppSettings(context: Context = BaseApplication.getContext()) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * 위치 설정 화면으로 이동
     */
    fun openLocationSettings(context: Context = BaseApplication.getContext()) {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * 게임 플레이를 위한 필수 권한들이 모두 승인되었는지 확인
     */
    fun areEssentialPermissionsGranted(
        context: Context = BaseApplication.getContext()
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return arePermissionsGranted(
                context,
                PermissionType.LOCATION,
                PermissionType.CAMERA,
                PermissionType.AUDIO,
                PermissionType.NOTIFICATION,
                PermissionType.STEP_SENSOR
            )
        } else {
            return arePermissionsGranted(
                context,
                PermissionType.LOCATION,
                PermissionType.CAMERA,
                PermissionType.AUDIO,
                PermissionType.NOTIFICATION,
            )
        }
    }

    /**
     * 거부된 권한 목록 반환
     */
    fun getDeniedPermissions(
        context: Context = BaseApplication.getContext()
    ): List<PermissionType> {
        return PermissionType.values().filter { type ->
            type.isRequired() && !isPermissionGranted(context, type)
        }
    }

    /**
     * 권한 상태를 문자열로 반환
     */
    fun getPermissionStatusText(
        context: Context = BaseApplication.getContext(),
        permissionType: PermissionType
    ): String {
        return if (isPermissionGranted(context, permissionType)) {
            "허용됨"
        } else {
            "거부됨"
        }
    }
}