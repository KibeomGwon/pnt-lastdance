package com.d104.pnt.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.ButtonPrimary
import com.d104.pnt.ui.theme.TextPrimary

@Composable
fun PixelDropdown(
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    label: String,
    enabled: Boolean = true,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val gapPx = with(density) { 4.dp.roundToPx() }
    var buttonHeight by remember { mutableIntStateOf(0) }
    var buttonWidth by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                buttonWidth = coordinates.size.width
                buttonHeight = coordinates.size.height
            }
            .alpha(if (enabled) 1f else 0.3f)
    ) {
        PixelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { isExpanded = !isExpanded },
            backgroundColor = if (highlighted) ButtonPrimary else TextPrimary,
            borderColor = BorderDefault,
            cornerSize = 10f,
            innerVerticalPadding = 10,
            innerHorizontalPadding = 10
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val displayText = if (selectedItem.isEmpty()) label else selectedItem
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (highlighted) TextPrimary else BorderDefault

                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "드롭다운",
                    tint = BorderDefault,
                    modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                )
            }
        }

        if (isExpanded && enabled) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, buttonHeight + gapPx),
                onDismissRequest = { isExpanded = false }
            ) {
                val popupWidthDp = with(density) { buttonWidth.toDp() }
                PixelContainer(
                    modifier = Modifier
                        .width(popupWidthDp)
                        .heightIn(max = 200.dp),
                    backgroundColor = if (highlighted) ButtonPrimary else TextPrimary,
                    borderColor = BorderDefault,
                    cornerSize = 10f,
                    innerHorizontalPadding = 4
                ) {
                    LazyColumn(
                        modifier = Modifier
                    ) {
                        itemsIndexed(items) { index, item ->
                            PixelDropdownItem(
                                text = item,
                                highlighted = highlighted,
                                isSelected = item == selectedItem,
                                onClick = {
                                    onItemSelected(item)
                                    isExpanded = false
                                }
                            )
                            if (index < items.lastIndex) {
                                PixelDashedDivider(
                                    color = BorderDefault.copy(alpha = 0.5f),
                                    thickness = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}