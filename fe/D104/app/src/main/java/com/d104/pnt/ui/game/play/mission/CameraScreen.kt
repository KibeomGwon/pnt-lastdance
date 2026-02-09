package com.d104.pnt.ui.game.play.mission

import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onPhotoConfirmed: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    compressionQuality: Int = 80,
    maxWidth: Int = 1280,
    maxHeight: Int = 720,
    missionId: Long,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraExecutor = remember { ContextCompat.getMainExecutor(context) }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }

    // 촬영된 사진 파일들
    var originalPhotoFile by remember { mutableStateOf<File?>(null) }
    var compressedPhotoFile by remember { mutableStateOf<File?>(null) }

    // 사진 저장 디렉토리
    val outputDirectory = remember {
        File(context.cacheDir, "mission_photos").apply {
            if (!exists()) mkdirs()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (compressedPhotoFile == null) {
            // 카메라 미리보기
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onImageCaptureReady = { capture ->
                    imageCapture = capture
                }
            )

            // 닫기 버튼
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .systemBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 촬영 버튼
            FloatingActionButton(
                onClick = {
                    imageCapture?.let { capture ->
                        takePicture(
                            imageCapture = capture,
                            outputDirectory = outputDirectory,
                            executor = cameraExecutor,
                            onImageCaptured = { photoFile ->
                                originalPhotoFile = photoFile

                                coroutineScope.launch {
                                    try {
                                        val compressed = withContext(Dispatchers.IO) {
                                            compressImage(
                                                originalFile = photoFile,
                                                maxWidth = maxWidth,
                                                maxHeight = maxHeight,
                                                quality = compressionQuality
                                            )
                                        }

                                        compressedPhotoFile = compressed
                                        originalPhotoFile?.delete()

                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "이미지 처리 실패: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        photoFile.delete()
                                    }
                                }
                            },
                            onError = { exception ->
                                Toast.makeText(
                                    context,
                                    "촬영 실패: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .size(72.dp),
                shape = CircleShape,
                containerColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "사진 촬영",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }
        } else {
            // 미리보기 모드
            PhotoPreviewScreen(
                photoFile = compressedPhotoFile!!,
                onConfirm = {
                    viewModel.uploadImage(compressedPhotoFile!!, missionId)
                    onPhotoConfirmed()
                },
                onCancel = {
                    compressedPhotoFile?.delete()
                    originalPhotoFile?.delete()
                    compressedPhotoFile = null
                    originalPhotoFile = null
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
