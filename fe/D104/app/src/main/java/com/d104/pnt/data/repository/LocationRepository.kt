package com.d104.pnt.data.repository

import android.location.Location
import com.d104.pnt.domain.model.DraggableLatLng
import com.d104.pnt.domain.model.GeoLocationInfo
import com.d104.pnt.domain.model.PlayerData
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.StateFlow

interface LocationRepository {
    val currentLocation: StateFlow<Location?>
    val polygonPoints: StateFlow<List<LatLng>>
    val prisonLocation: StateFlow<LatLng?>
    val playerLocations: StateFlow<List<PlayerData>>

    /**
     * 최근 위치 업데이트
     */
    fun updateCurrentLocation(location: Location)

    /**
     * 플레이어 위치 업데이트
     */
    fun updatePlayerLocation(locations: List<PlayerData>)

    /**
     * 폴리곤 포인트 저장
     */
    fun setPolygonPoints(points: List<LatLng>)

    /**
     * 폴리곤 포인트 삭제
     */
    fun deletePolygonPoint(targetList: MutableList<DraggableLatLng>, index: Int): Boolean

    /**
     * 리스트에 포인트 추가
     */
    fun addPointToList(targetList: MutableList<DraggableLatLng>, newPoint: LatLng)

    /**
     * 기준 위치를 받아 기본 사각형 생성 후 저장
     */
    fun createDefaultPolygon(center: Location)

    /**
     * 감옥 위치 지정
     */
    fun setPrisonLocation(location: LatLng?)

    /**
     * 방 생성 취소 시 정보 초기화
     */
    fun dismissCreateGame()

    /**
     * 경도 위도 -> 주소 정보 반환
     */
    suspend fun getAddressFromLatLng(latitude: Double, longitude: Double): GeoLocationInfo

    /**
     * 행정 지역명을 지역 코드로 변환
     */
    fun getRegionCode(major: String, middle: String): Int
}