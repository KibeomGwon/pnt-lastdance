package com.d104.pnt.ui.auth

import android.os.Build
import android.widget.NumberPicker
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelInputField
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun BirthDatePicker(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val initialDate = remember(value) {
        try {
            if (value.isNotEmpty()) {
                val parts = value.split(".")
                Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
            } else {
                Triple(2000, 1, 1)
            }
        } catch (e: Exception) {
            Triple(2000, 1, 1)
        }
    }

    var selectedYear by remember { mutableIntStateOf(initialDate.first) }
    var selectedMonth by remember { mutableIntStateOf(initialDate.second) }
    var selectedDay by remember { mutableIntStateOf(initialDate.third) }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 10.dp, bottom = 4.dp),
            color = Color.White
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    selectedYear = initialDate.first
                    selectedMonth = initialDate.second
                    selectedDay = initialDate.third
                    showDatePicker = true
                }
        ) {
            PixelInputField(
                value = value,
                onValueChange = { },
                placeholder = "YYYY.MM.DD",
                borderColor = BorderDefault,
                backgroundColor = backgroundColor,
                modifier = Modifier.fillMaxWidth(),
                enabled = false
            )
        }
    }

    if (showDatePicker) {
        Dialog(onDismissRequest = { showDatePicker = false }) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFAFAFA),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "생년월일 선택",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NumberPickerWrapper(
                            value = selectedYear,
                            minValue = 1900,
                            maxValue = LocalDate.now().year,
                            onValueChange = { selectedYear = it },
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "년",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        NumberPickerWrapper(
                            value = selectedMonth,
                            minValue = 1,
                            maxValue = 12,
                            onValueChange = {
                                selectedMonth = it
                                val maxDay = getMaxDayOfMonth(selectedYear, it)
                                if (selectedDay > maxDay) {
                                    selectedDay = maxDay
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "월",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        NumberPickerWrapper(
                            value = selectedDay,
                            minValue = 1,
                            maxValue = getMaxDayOfMonth(selectedYear, selectedMonth),
                            onValueChange = { selectedDay = it },
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "일",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        PixelButtonCode(
                            text = "취소",
                            onClick = { showDatePicker = false },
                            mainColor = Color.Gray,
                            borderColor = BorderDefault,
                            textColor = Color.White,
                            fontSize = 15,
                            blockHeight = 14,
                            pixelSize = 2.8.dp,
                            modifier = Modifier.weight(1f)
                        )

                        PixelButtonCode(
                            text = "확인",
                            onClick = {
                                val formatted = String.format(
                                    "%04d.%02d.%02d",
                                    selectedYear,
                                    selectedMonth,
                                    selectedDay
                                )
                                onValueChange(formatted)
                                showDatePicker = false
                            },
                            mainColor = CustomBlue,
                            borderColor = BorderDefault,
                            textColor = Color.White,
                            fontSize = 15,
                            blockHeight = 14,
                            pixelSize = 2.8.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NumberPickerWrapper(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                this.minValue = minValue
                this.maxValue = maxValue
                this.value = value.coerceIn(minValue, maxValue)
                this.wrapSelectorWheel = false

                setOnValueChangedListener { _, _, newVal ->
                    onValueChange(newVal)
                }
            }
        },
        update = { picker ->
            picker.minValue = minValue
            picker.maxValue = maxValue
            val safeValue = value.coerceIn(minValue, maxValue)
            if (picker.value != safeValue) {
                picker.value = safeValue
            }
        },
        modifier = modifier.height(150.dp)
    )
}

@RequiresApi(Build.VERSION_CODES.O)
private fun getMaxDayOfMonth(year: Int, month: Int): Int {
    return try {
        LocalDate.of(year, month, 1).lengthOfMonth()
    } catch (e: Exception) {
        31
    }
}