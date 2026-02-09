package com.d104.pnt.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.d104.pnt.R
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelIconButton
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.PrimaryYellow

private val AvatarItemSize = 72.dp

sealed class AvatarImage {
    data class Resource(val resId: Int) : AvatarImage()
    data class Gallery(val uri: Uri) : AvatarImage()
}

@Composable
fun ProfileImageSelectionDialog(
    currentAvatarUrl: String?,
    onDismissRequest: () -> Unit,
    onImageSelected: (AvatarImage) -> Unit
) {
    val defaultImages = listOf(
        R.drawable.profile_img_default,
        R.drawable.profile_img_police_1,
        R.drawable.profile_img_police_2,
        R.drawable.profile_img_thief_1,
        R.drawable.profile_img_thief_2
    )

    val initialImage = remember(currentAvatarUrl) {
        when (currentAvatarUrl) {
            "POLICE_1" -> AvatarImage.Resource(R.drawable.profile_img_police_1)
            "POLICE_2" -> AvatarImage.Resource(R.drawable.profile_img_police_2)
            "THIEF_1" -> AvatarImage.Resource(R.drawable.profile_img_thief_1)
            "THIEF_2" -> AvatarImage.Resource(R.drawable.profile_img_thief_2)
            "DEFAULT", null, "" -> AvatarImage.Resource(R.drawable.profile_img_default)
            else -> {
                if (currentAvatarUrl.startsWith("content://") || currentAvatarUrl.startsWith("file://")) {
                    try {
                        AvatarImage.Gallery(Uri.parse(currentAvatarUrl))
                    } catch (e: Exception) {
                        AvatarImage.Resource(R.drawable.profile_img_default)
                    }
                } else {
                    AvatarImage.Resource(R.drawable.profile_img_default)
                }
            }
        }
    }

    var selectedImage by remember { mutableStateOf<AvatarImage>(initialImage) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImage = AvatarImage.Gallery(uri)
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        // 배경 컨테이너
        PixelContainer(
            modifier = Modifier.width(340.dp),
            backgroundColor = Color(0xFF3F3F68),
            borderColor = Color(0xFF8D90B3),
            borderWidth = 6f,
            cornerSize = 20f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 타이틀
                Text(
                    text = "이미지 선택",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // 이미지 그리드
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        defaultImages.take(3).forEach { resId ->
                            AvatarItem(
                                image = AvatarImage.Resource(resId),
                                isSelected = selectedImage is AvatarImage.Resource && (selectedImage as AvatarImage.Resource).resId == resId,
                                onClick = { selectedImage = AvatarImage.Resource(resId) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        defaultImages.drop(3).forEach { resId ->
                            AvatarItem(
                                image = AvatarImage.Resource(resId),
                                isSelected = selectedImage is AvatarImage.Resource && (selectedImage as AvatarImage.Resource).resId == resId,
                                onClick = { selectedImage = AvatarImage.Resource(resId) }
                            )
                        }

                        val isGallerySelected = selectedImage is AvatarImage.Gallery

                        if (isGallerySelected) {
                            AvatarItem(
                                image = selectedImage,
                                isSelected = true,
                                onClick = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        } else {
                            GallerySelectionItem(
                                isSelected = false,
                                onClick = {
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 선택 완료 버튼
                PixelIconButton(
                    onClick = {
                        onImageSelected(selectedImage)
                        onDismissRequest()
                    },
                    modifier = Modifier
                        .width(140.dp)
                        .height(48.dp),
                    mainColor = Color.White,
                    borderColor = BorderDefault,
                    blockHeight = 12
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "선택 완료",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

// 개별 아바타 아이템 UI
@Composable
fun AvatarItem(
    image: AvatarImage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryYellow else Color.Transparent
    val borderWidth = if (isSelected) 4.dp else 0.dp

    Box(
        modifier = Modifier
            .size(AvatarItemSize)
            .clip(RoundedCornerShape(12.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        val painter = when (image) {
            is AvatarImage.Resource -> painterResource(id = image.resId)
            is AvatarImage.Gallery -> rememberAsyncImagePainter(model = image.uri)
        }

        Image(
            painter = painter,
            contentDescription = "Avatar",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

// 갤러리 선택 버튼 UI
@Composable
fun GallerySelectionItem(
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryYellow else Color(0xFF8D90B3)
    val borderWidth = if (isSelected) 4.dp else 2.dp

    Box(
        modifier = Modifier
            .size(AvatarItemSize)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A4A))
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "갤러리에서\n사진선택",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}