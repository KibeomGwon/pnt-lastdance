package com.d104.pnt.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.d104.pnt.R

@Composable
fun UserProfileCard(
    nickname: String,
    avatarUrl: String?,
    policeGrade: String,
    thiefGrade: String,
    modifier: Modifier = Modifier
) {
    PixelContainer(
        modifier = modifier,
        backgroundColor = Color(0xFF3F3F68),
        borderColor = Color(0xFF8D90B3),
        borderWidth = 8f,
        cornerSize = 30f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 아바타
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color.Black, CircleShape)
                        .background(Color.White)
                ) {
                    val painter = when (avatarUrl) {
                        "POLICE_1" -> painterResource(id = R.drawable.profile_img_police_1)
                        "POLICE_2" -> painterResource(id = R.drawable.profile_img_police_2)
                        "THIEF_1" -> painterResource(id = R.drawable.profile_img_thief_1)
                        "THIEF_2" -> painterResource(id = R.drawable.profile_img_thief_2)
                        "DEFAULT", null, "" -> painterResource(id = R.drawable.profile_img_default)
                        else -> rememberAsyncImagePainter(
                            model = avatarUrl,
                            error = painterResource(id = R.drawable.profile_img_default),
                            placeholder = painterResource(id = R.drawable.profile_img_default)
                        )
                    }

                    Image(
                        painter = painter,
                        contentDescription = "유저 아바타",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 닉네임
                Text(
                    text = nickname,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp),
                thickness = 1.dp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 티어 이미지
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val policeRes = getInfoPoliceTierImage(policeGrade)
                InfoGradeItem(
                    title = "경찰",
                    iconRes = policeRes,
                    modifier = Modifier.weight(1f)
                )

                // 중앙 점선
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(130.dp)
                ) {
                    drawLine(
                        color = Color(0xFF8D90B3),
                        start = Offset(0f, 0f),
                        end = Offset(0f, size.height),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                val thiefRes = getInfoThiefTierImage(thiefGrade)
                InfoGradeItem(
                    title = "도둑",
                    iconRes = thiefRes,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun InfoGradeItem(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = title,
            color = Color(0xFFC4C4C4),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(androidx.compose.ui.graphics.RectangleShape)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "$title 티어",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer(
                        scaleX = 2f,
                        scaleY = 2f,
                        translationY = 0f
                    )
            )
        }
    }
}

private fun getInfoPoliceTierImage(grade: String): Int {
    return when (grade) {
        "순경" -> R.drawable.police_lv1
        "경장" -> R.drawable.police_lv2
        "경사" -> R.drawable.police_lv3
        "경위" -> R.drawable.police_lv4
        "경감" -> R.drawable.police_lv5
        "경정" -> R.drawable.police_lv6
        "총경" -> R.drawable.police_lv7
        "경무관" -> R.drawable.police_lv8
        "치안감" -> R.drawable.police_lv9
        "치안정감" -> R.drawable.police_lv10
        "치안총감" -> R.drawable.police_lv11
        else -> R.drawable.police_lv1
    }
}

private fun getInfoThiefTierImage(grade: String): Int {
    return when (grade) {
        "바늘도둑", "바늘 도둑" -> R.drawable.thief_lv1
        "좀도둑", "좀 도둑" -> R.drawable.thief_lv2
        "소매치기" -> R.drawable.thief_lv3
        "빈집털이" -> R.drawable.thief_lv4
        "소도둑", "소 도둑" -> R.drawable.thief_lv5
        "금고털이" -> R.drawable.thief_lv6
        "은행털이" -> R.drawable.thief_lv7
        "홍길동" -> R.drawable.thief_lv8
        "인비저블" -> R.drawable.thief_lv9
        "괴도" -> R.drawable.thief_lv10
        "대도" -> R.drawable.thief_lv11
        else -> R.drawable.thief_lv1
    }
}