package com.d104.pnt.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d104.pnt.R
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState

@Composable
fun PixelMarker(
    location: LatLng,
    position: String,
) {
    MarkerComposable(
        state = MarkerState(position = location),
        onClick = { true }
    ) {
        when (position) {
            "ME" -> Image(
                painter = painterResource(id = R.drawable.map_marker_green),
                contentDescription = "픽셀 커스텀 마커",
                modifier = Modifier.size(20.dp)
            )

            "POLICE" -> Image(
                painter = painterResource(id = R.drawable.map_marker_blue),
                contentDescription = "픽셀 커스텀 마커",
                modifier = Modifier.size(20.dp)
            )

            "CCTV" -> Image(
                painter = painterResource(id = R.drawable.map_marker_red),
                contentDescription = "픽셀 커스텀 마커",
                modifier = Modifier.size(20.dp)
            )

            "TRANSFER" -> Image(
                painter = painterResource(id = R.drawable.map_marker_gray),
                contentDescription = "픽셀 커스텀 마커",
                modifier = Modifier.size(20.dp)
            )

            "PRISON" -> Image(
                painter = painterResource(id = R.drawable.map_marker_gray),
                contentDescription = "픽셀 커스텀 마커",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}