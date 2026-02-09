package com.d104.pnt.ui.game.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun CounterControl(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    value: String,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onHelpClick: (() -> Unit)? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))

            Text(text = label, color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.weight(1f))

            if (onHelpClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.HelpOutline,
                    contentDescription = "도움말",
                    tint = Color(0xFFC4C4C4),
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onHelpClick() }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(Color(0xFF3B4049)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // - 버튼
            RepeatingIconButton(
                icon = Icons.Default.Remove,
                onClick = onDecrease
            )

            Text(
                text = "$value$unit",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )

            // + 버튼
            RepeatingIconButton(
                icon = Icons.Default.Add,
                onClick = onIncrease
            )
        }
    }
}

@Composable
fun RepeatingIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
    tint: Color = Color.Gray
) {
    val currentOnClick by rememberUpdatedState(onClick)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(500)

            while (isPressed) {
                currentOnClick()
                delay(100)
            }
        }
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(bounded = false, radius = 16.dp),
                enabled = enabled,

                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) tint else Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}