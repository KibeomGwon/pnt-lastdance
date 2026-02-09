package com.d104.pnt.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RoundedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color,
    textColor: Color = Color.Black,
    enabled: Boolean = true,
    cornerRadius: Dp = 12.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = textColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = textColor.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = 10.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleSmall,
            color = textColor
        )
    }
}
