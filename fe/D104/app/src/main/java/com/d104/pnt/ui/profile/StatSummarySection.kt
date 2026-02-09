package com.d104.pnt.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.PixelFont
import kotlin.math.roundToInt

@Composable
fun StatSummarySection(
    wins: Int,
    totalGames: Int,
    modifier: Modifier = Modifier
) {
    // 승률 계산
    val rawWinRate = if (totalGames > 0) (wins.toDouble() / totalGames * 100) else 0.0
    val winRate = rawWinRate.roundToInt()

    PixelContainer(
        modifier = modifier,
        backgroundColor = Color(0xFF3F3F68).copy(alpha = 0.5f),
        borderColor = Color(0xFF8D90B3),
        borderWidth = 6f,
        cornerSize = 20f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // 타이틀
            Text(
                text = "전적 요약",
                fontFamily = PixelFont,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 승률 게이지
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..10) {
                    val blockMax = i * 10
                    val blockMin = (i - 1) * 10

                    val fillFraction = when {
                        winRate >= blockMax -> 1f
                        winRate <= blockMin -> 0f
                        else -> (winRate - blockMin) / 10f
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1.5f)
                            .padding(horizontal = 2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF5A5A82))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fillFraction)
                                .background(Color(0xFFD9D9D9))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 승률 텍스트
                Text(
                    text = "승률 ($winRate%)",
                    fontFamily = PixelFont,
                    color = Color(0xFFC4C4C4),
                    fontSize = 14.sp
                )

                // 플레이 횟수
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFFC4C4C4))) {
                            append("플레이 횟수  ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                baselineShift = BaselineShift(-0.2f)
                            )
                        ) {
                            append("$totalGames")
                        }
                    },
                    fontFamily = PixelFont,
                    fontSize = 14.sp
                )
            }
        }
    }
}