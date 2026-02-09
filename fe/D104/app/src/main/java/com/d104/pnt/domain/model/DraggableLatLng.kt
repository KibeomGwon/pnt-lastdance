package com.d104.pnt.domain.model

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class DraggableLatLng(
    val id: String = UUID.randomUUID().toString(),
    val position: LatLng
)
