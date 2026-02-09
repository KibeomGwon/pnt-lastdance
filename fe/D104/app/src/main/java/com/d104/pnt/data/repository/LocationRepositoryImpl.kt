package com.d104.pnt.data.repository

import android.content.Context
import android.location.Location
import com.d104.pnt.BuildConfig
import com.d104.pnt.data.remote.api.NaverApiService
import com.d104.pnt.data.source.local.RegionCodeManager
import com.d104.pnt.domain.model.DraggableLatLng
import com.d104.pnt.domain.model.GeoLocationInfo
import com.d104.pnt.domain.model.PlayerData
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val naverApiService: NaverApiService,
    private val regionCodeManager: RegionCodeManager
) : LocationRepository {
    private val _currentLocation = MutableStateFlow<Location?>(null)
    private val _polygonPoints = MutableStateFlow<List<LatLng>>(emptyList())
    private val _prisonLocation = MutableStateFlow<LatLng?>(null)
    private val _playerLocations = MutableStateFlow<List<PlayerData>>(emptyList())

    override val currentLocation = _currentLocation.asStateFlow()
    override val polygonPoints = _polygonPoints.asStateFlow()
    override val prisonLocation = _prisonLocation.asStateFlow()
    override val playerLocations = _playerLocations.asStateFlow()


    override fun updateCurrentLocation(location: Location) {
        _currentLocation.value = location
    }

    override fun updatePlayerLocation(locations: List<PlayerData>) {
        _playerLocations.value = locations
    }

    override fun setPolygonPoints(points: List<LatLng>) {
        _polygonPoints.value = points
    }

    override fun deletePolygonPoint(targetList: MutableList<DraggableLatLng>, index: Int): Boolean {
        if (targetList.size > 3 && index in targetList.indices) {
            targetList.removeAt(index)
            return true
        }
        return false
    }

    override fun addPointToList(targetList: MutableList<DraggableLatLng>, newPoint: LatLng) {
        val insertIndex = getInsertionIndex(newPoint, targetList.map { it.position })
        targetList.add(insertIndex, DraggableLatLng(position = newPoint))
    }

    private fun getInsertionIndex(point: LatLng, points: List<LatLng>): Int {
        var minDistance = Double.MAX_VALUE
        var insertIndex = points.size

        for (i in points.indices) {
            val p1 = points[i]
            val p2 = points[(i + 1) % points.size]
            val distance = PolyUtil.distanceToLine(point, p1, p2)

            if (distance < minDistance) {
                minDistance = distance
                insertIndex = i + 1
            }
        }
        return insertIndex
    }

    override fun createDefaultPolygon(center: Location) {
        val lat = center.latitude
        val lng = center.longitude
        val offset = 0.001

        val defaultPoints = listOf(
            LatLng(lat + offset, lng - offset),
            LatLng(lat + offset, lng + offset),
            LatLng(lat - offset, lng + offset),
            LatLng(lat - offset, lng - offset)
        )
        _polygonPoints.value = defaultPoints
    }

    override fun setPrisonLocation(location: LatLng?) {
        _prisonLocation.value = location
    }

    override fun dismissCreateGame() {
        _polygonPoints.value = emptyList()
        _prisonLocation.value = null
    }

    override suspend fun getAddressFromLatLng(
        latitude: Double,
        longitude: Double
    ): GeoLocationInfo {
        return withContext(Dispatchers.IO) {
            try {
                val clientId = BuildConfig.CLIENT_ID
                val clientSecret = BuildConfig.CLIENT_SECRET

                val coords = "$longitude,$latitude"

                val response = naverApiService.getAddress(
                    clientId = clientId,
                    clientSecret = clientSecret,
                    coords = coords
                )

                if (response.results.isNotEmpty()) {
                    val region = response.results[0].region
                    val fullCode = response.results[0].code.id
                    val code = fullCode.take(8).toInt()

                    return@withContext GeoLocationInfo(
                        major = region.area1.name, // 서울특별시
                        middle = region.area2.name, // 강남구
                        code = code // 행정동 코드
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext GeoLocationInfo()
        }
    }

    override fun getRegionCode(major: String, middle: String): Int {
        val normalizedMajor = when (major) {
            "서울" -> "서울특별시"
            "인천" -> "인천광역시"
            "강원도" -> "강원특별자치도"
            "경북" -> "경상북도"
            "경남" -> "경상남도"
            "전북" -> "전북특별자치도"
            "전남" -> "전라남도"
            "충북" -> "충청북도"
            "충남" -> "충청남도"
            "제주도" -> "제주특별자치도"
            "대구" -> "대구광역시"
            "부산" -> "부산광역시"
            "광주" -> "광주광역시"
            "대전" -> "대전광역시"
            "울산" -> "울산광역시"
            "세종시" -> "세종특별자치시"
            else -> major
        }
        val code = regionCodeManager.getRegionCode(normalizedMajor, middle)
        return code?.toInt() ?: 99999999
    }
}