package com.d104.pnt.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.ButtonHighlight
import com.d104.pnt.ui.theme.NeutralColor
import com.d104.pnt.ui.theme.TextPrimary

@Composable
fun PixelDropdownItem(
    text: String,
    highlighted: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(
                if (isSelected) NeutralColor
                else {
                    if (highlighted) ButtonHighlight else TextPrimary
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) TextPrimary
            else {
                if (highlighted) TextPrimary else BorderDefault
            },
            fontSize = 14.sp,
        )
    }
}
