package com.d104.pnt.ui.game.create

import androidx.lifecycle.ViewModel
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.domain.model.DraggableLatLng
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapSettingViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {
    val userLocation = locationRepository.currentLocation
    val polygonPoints = locationRepository.polygonPoints
    val prisonLocation = locationRepository.prisonLocation

    fun setPolygonPoints(points: List<LatLng>) {
        locationRepository.setPolygonPoints(points)
    }

    fun setPrisonLocation(location: LatLng?) {
        locationRepository.setPrisonLocation(location)
    }

    fun deletePolygonPoint(targetList: MutableList<DraggableLatLng>, index: Int): Boolean {
        return locationRepository.deletePolygonPoint(targetList, index)
    }

    fun addPointToList(targetList: MutableList<DraggableLatLng>, newPoint: LatLng) {
        locationRepository.addPointToList(targetList, newPoint)
    }
}