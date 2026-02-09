package com.d104.pnt.ui.game.play.mission

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptureReady: (ImageCapture) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    // 카메라 프리뷰 설정
    val preview = remember {
        Preview.Builder().build()
    }

    // 후면 카메라 선택
    val cameraSelector = remember {
        CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }

    // 카메라 바인딩
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        try {
            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            onImageCaptureReady(imageCapture)

        } catch (e: Exception) {

        }
    }

    // 프리뷰 화면
    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                preview.surfaceProvider = surfaceProvider
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier
    )
}

fun takePicture(
    imageCapture: ImageCapture,
    outputDirectory: File,
    executor: Executor,
    onImageCaptured: (File) -> Unit,
    onError: (ImageCaptureException) -> Unit
) {
    // 파일 이름 생성
    val photoFile = File(
        outputDirectory,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.KOREA)
            .format(System.currentTimeMillis()) + ".jpg"
    )

    // 저장 옵션 설정
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    // 사진 촬영
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(photoFile)
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception)
            }
        }
    )
}