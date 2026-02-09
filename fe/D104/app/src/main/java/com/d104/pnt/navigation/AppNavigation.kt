package com.d104.pnt.navigation

import android.app.Activity
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.IntroScreen
import com.d104.pnt.base.BaseApplication
import com.d104.pnt.permission.PermissionDeniedDialog
import com.d104.pnt.permission.PermissionDialog
import com.d104.pnt.permission.exitApp
import com.d104.pnt.ui.MainScreen
import com.d104.pnt.ui.MainViewModel
import com.d104.pnt.ui.auth.LoginScreen
import com.d104.pnt.ui.auth.SignupScreen
import com.d104.pnt.util.AuthEventBus
import com.d104.pnt.util.PermissionHelper
import com.d104.pnt.util.SoundPlayer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * 전체 앱 네비게이션
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppNavigation(
    startChatRoomId: Long? = null,
    viewModel: MainViewModel = hiltViewModel(),
    soundPlayer: SoundPlayer = hiltViewModel<MainViewModel>().let {
        viewModel.soundPlayer
    }
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    val authEventBus = remember {
        (context.applicationContext as BaseApplication).authEventBus
    }

    var currentScreen by remember { mutableStateOf(AppScreen.Intro) }
    var memberId by remember { mutableStateOf("") }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    val neededPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            listOf(
                PermissionHelper.PermissionType.CAMERA,
                PermissionHelper.PermissionType.LOCATION,
                PermissionHelper.PermissionType.AUDIO,
                PermissionHelper.PermissionType.NOTIFICATION,
                PermissionHelper.PermissionType.STEP_SENSOR
            ).filter { it.isRequired() }
        } else {
            listOf(
                PermissionHelper.PermissionType.CAMERA,
                PermissionHelper.PermissionType.LOCATION,
                PermissionHelper.PermissionType.AUDIO,
                PermissionHelper.PermissionType.NOTIFICATION
            ).filter { it.isRequired() }
        }
    }

    val allPermissions = remember {
        neededPermissions.flatMap { it.permissions.toList() }
    }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = allPermissions,
        onPermissionsResult = { results ->
            if (results.values.all { it }) {
                showPermissionDeniedDialog = false
                currentScreen = AppScreen.Main
            } else {
                showPermissionDeniedDialog = true
            }
        }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

                if (showPermissionDeniedDialog) {
                    if (PermissionHelper.areEssentialPermissionsGranted(context)) {
                        showPermissionDeniedDialog = false
                        currentScreen = AppScreen.Main
                    } else {

                    }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        authEventBus.events.collect { event ->
            when (event) {
                is AuthEventBus.AuthEvent.TokenExpired -> {
                    currentScreen = AppScreen.Intro
                    Toast.makeText(
                        context,
                        "세션이 만료되었습니다. 다시 로그인해주세요.",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AuthEventBus.AuthEvent.Unauthorized -> {
                    currentScreen = AppScreen.Intro
                }
            }
        }
    }

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            currentScreen = AppScreen.Main
        } else {
            currentScreen = AppScreen.Intro
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            AppScreen.Intro -> {
                IntroScreen(
                    onClick = {
                        currentScreen = AppScreen.Login
                    },
                    soundPlayer = soundPlayer
                )
            }

            AppScreen.Login -> {
                LoginScreen(
                    onLoginSuccess = { id ->
                        memberId = id

                        if (PermissionHelper.areEssentialPermissionsGranted(context)) {

                        } else {
                            showPermissionDialog = true
                        }
                    },
                    goToSignup = {
                        currentScreen = AppScreen.Signup
                    }
                )
            }

            // 회원가입 화면
            AppScreen.Signup -> {
                SignupScreen(
                    onSuccess = {
                        currentScreen = AppScreen.Login
                    },
                    onBack = {
                        currentScreen = AppScreen.Login
                    }
                )
            }

            // 메인 앱
            AppScreen.Main -> {
                MainScreen(
                    navigateToIntro = {
                    }
                )
            }
        }

        // 권한 설명 다이얼로그
        if (showPermissionDialog) {
            PermissionDialog(
                permissionTypes = neededPermissions,
                onConfirm = {
                    showPermissionDialog = false
                    permissionsState.launchMultiplePermissionRequest()
                },
                onDismiss = {
                    showPermissionDialog = false
                    activity?.let { exitApp(it) }
                }
            )
        }

        // 권한 거부 다이얼로그
        if (showPermissionDeniedDialog) {
            val deniedPermissions = PermissionHelper.getDeniedPermissions(context)

            if (deniedPermissions.isEmpty()) {
                showPermissionDeniedDialog = false
                currentScreen = AppScreen.Main
            } else {
                val isPermanentlyDenied = !permissionsState.shouldShowRationale

                PermissionDeniedDialog(
                    deniedPermissions = deniedPermissions,
                    isPermanentlyDenied = isPermanentlyDenied,
                    onGoToSettings = {
                        PermissionHelper.openAppSettings(context)
                    },
                    onExitApp = {
                        activity?.let { exitApp(it) }
                    }
                )
            }
        }
    }
}

/**
 * 앱 전체 화면 상태
 */
private enum class AppScreen {
    Intro,
    Login,
    Signup,
    Main
}