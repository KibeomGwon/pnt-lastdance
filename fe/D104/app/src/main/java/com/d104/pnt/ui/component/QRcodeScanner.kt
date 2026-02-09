package com.d104.pnt.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.CompoundBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

@Composable
fun QRcodeScanner(
    modifier: Modifier,
    onScan: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 바코드 뷰 생성
    val scannerView = remember {
        CompoundBarcodeView(context).apply {
            val formats = listOf(BarcodeFormat.QR_CODE)
            setStatusText("도둑의 QR 코드를 비춰주세요")
            decoderFactory = DefaultDecoderFactory(formats)
            val callback = object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult?) {
                    result?.text?.let {
                        onScan(it)
                    }
                }

                override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
            }
            decodeSingle(callback)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> scannerView.resume()
                Lifecycle.Event.ON_PAUSE -> scannerView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            scannerView.pause()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { scannerView }
    )
}