package com.d104.pnt.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.TextPrimary
import com.d104.pnt.util.generateQrCode

@Composable
fun QRcodeContainer(
    modifier: Modifier,
    data: String,
) {
    val qrCode = remember(data) {
        generateQrCode(data, size = 256)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TextPrimary)
            .border(4.dp, BorderDefault)
            .padding(5.dp),
    ) {
        if (qrCode != null) {
            Image(
                bitmap = qrCode.asImageBitmap(),
                contentDescription = "Thief QR Code",
                modifier = modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            )
        } else {
            CircularProgressIndicator(color = BorderDefault)
        }
    }
}