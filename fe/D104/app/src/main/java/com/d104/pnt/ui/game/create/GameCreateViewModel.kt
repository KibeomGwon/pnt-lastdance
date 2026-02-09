package com.d104.pnt.ui.game.create

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.data.remote.model.request.SaveMapRequest
import com.d104.pnt.data.remote.model.response.CreateGameRoomResponse
import com.d104.pnt.data.remote.model.response.MapData
import com.d104.pnt.data.repository.GameRoomRepository
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.util.getSingleLocation
import com.d104.pnt.util.socket.RoomSocketManager
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameCreateViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val gameRoomRepository: GameRoomRepository,
    private val roomSocketManager: RoomSocketManager,
) : ViewModel() {
    val userLocation = locationRepository.currentLocation
    val polygonPoints = locationRepository.polygonPoints
    val prisonLocation = locationRepository.prisonLocation

    private val _gameName = MutableStateFlow("")
    private val _totalPlayers = MutableStateFlow(25)
    private val _gameTime = MutableStateFlow(20)
    private val _missionCount = MutableStateFlow(5)
    private val _cctvCycle = MutableStateFlow(10)
    private val _policeCount = MutableStateFlow(9)
    private val _thiefCount = MutableStateFlow(_totalPlayers.value - _policeCount.value)

    val gameName: StateFlow<String> = _gameName.asStateFlow()
    val totalPlayers: StateFlow<Int> = _totalPlayers.asStateFlow()
    val gameTime: StateFlow<Int> = _gameTime.asStateFlow()
    val missionCount: StateFlow<Int> = _missionCount.asStateFlow()
    val cctvCycle: StateFlow<Int> = _cctvCycle.asStateFlow()
    val policeCount: StateFlow<Int> = _policeCount.asStateFlow()
    val thiefCount: StateFlow<Int> = _thiefCount.asStateFlow()

    private val _myMaps = MutableStateFlow<UiState<List<MapData>>>(UiState.Idle)
    val myMaps: StateFlow<UiState<List<MapData>>> = _myMaps.asStateFlow()

    private val _selectedMapId = MutableStateFlow<Long?>(null)
    val selectedMapId = _selectedMapId.asStateFlow()

    private val _mapDetailState = MutableStateFlow<UiState<MapData>>(UiState.Idle)
    val mapDetailState = _mapDetailState.asStateFlow()

    private val _saveMap = MutableStateFlow(false)
    val saveMap = _saveMap.asStateFlow()

    private val _mapName = MutableStateFlow("")
    val mapName = _mapName.asStateFlow()


    fun setSaveMap(value: Boolean) {
        _saveMap.value = value
    }

    fun setMapName(value: String) {
        _mapName.value = value
    }

    fun updateGameName(newName: String) {
        _gameName.value = newName
    }

    fun updateTotalPlayers(plus: Boolean) {
        if (plus && _totalPlayers.value < 30) {
            _totalPlayers.value += 1
        } else if (!plus && _totalPlayers.value > 5) {
            _totalPlayers.value -= 1
        } else {
            return
        }

        if (_policeCount.value >= _totalPlayers.value) {
            _policeCount.value = _totalPlayers.value - 1
        }

        _thiefCount.value = _totalPlayers.value - _policeCount.value

        if (_missionCount.value > _thiefCount.value) {
            _missionCount.value = _thiefCount.value
        }
    }

    fun updateGameTime(plus: Boolean) {
        if (plus && _gameTime.value < 60) _gameTime.value += 5
        else if (!plus && _gameTime.value > 5) {
            _gameTime.value -= 5
            if (_cctvCycle.value >= _gameTime.value) {
                _cctvCycle.value = (_gameTime.value - 1).coerceAtLeast(0)
            }
        }
    }

    fun updateMissionCount(plus: Boolean) {
        if (plus && _missionCount.value < _thiefCount.value) {
            _missionCount.value += 1
        } else if (!plus && _missionCount.value > 0) {
            _missionCount.value -= 1
        }
    }

    fun updateCctvCycle(plus: Boolean) {
        if (plus && _cctvCycle.value < _gameTime.value - 1) {
            _cctvCycle.value += 1
        } else if (!plus && _cctvCycle.value > 0) {
            _cctvCycle.value -= 1
        }
    }

    fun updatePoliceCount(newCount: Int) {
        _policeCount.value = newCount
        _thiefCount.value = _totalPlayers.value - _policeCount.value
        if (_thiefCount.value < _missionCount.value) _missionCount.value = _thiefCount.value
    }

    fun dismissCreateGame() {
        locationRepository.dismissCreateGame()
    }

    fun setDefaultSettings(context: Context) {
        viewModelScope.launch {
            val location = context.getSingleLocation()
            if (location != null) {
                locationRepository.updateCurrentLocation(location)
                locationRepository.createDefaultPolygon(location)
                locationRepository.setPrisonLocation(
                    LatLng(
                        userLocation.value!!.latitude,
                        userLocation.value!!.longitude
                    )
                )
            } else {

            }
        }
    }

    private val _gameRoomState = MutableStateFlow<UiState<CreateGameRoomResponse>>(UiState.Idle)
    val gameRoomState: StateFlow<UiState<CreateGameRoomResponse>> = _gameRoomState.asStateFlow()

    fun createGameRoom(
        playerCount: Int,
        timeLimit: Int,
        cctvInterval: Int,
        missionCount: Int,
        policeCount: Int,
        thiefCount: Int,
        prison: Location,
        polygon: List<Location>
    ) {
        if (_gameRoomState.value is UiState.Loading) return

        viewModelScope.launch {
            _gameRoomState.value = UiState.Loading

            if (_saveMap.value) {
                val saveResult = gameRoomRepository.saveMap(
                    SaveMapRequest(
                        name = _mapName.value.ifEmpty { "내 커스텀 맵" },
                        description = "",
                        prison = prison,
                        polygon = polygon
                    )
                )

                if (saveResult is BaseResult.Error) {

                }
            }

            roomSocketManager.disconnect()
            delay(100)

            when (val result = gameRoomRepository.createGameRoom(
                playerCount,
                timeLimit,
                cctvInterval,
                missionCount,
                policeCount,
                thiefCount,
                prison,
                polygon
            )) {
                is BaseResult.Success -> {
                    _gameRoomState.value = UiState.Success(result.data)
                }

                is BaseResult.Error -> {
                    _gameRoomState.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    fun isValid(
        playerCount: Int,
        timeLimit: Int,
        policeCount: Int,
        thiefCount: Int,
        polygon: List<Location>
    ): Boolean {
        if (playerCount < 2 || playerCount > 30) return false
        if (timeLimit < 5 || timeLimit > 60) return false
        if (policeCount < 1 || policeCount >= playerCount) return false
        if (thiefCount < 1 || thiefCount >= playerCount) return false
        if (polygon.size < 3) return false
        return true
    }

    // 내 맵 목록 불러오기
    fun fetchMyMaps() {
        viewModelScope.launch {
            _myMaps.value = UiState.Loading
            when (val result = gameRoomRepository.getMyMaps()) {
                is BaseResult.Success -> {
                    _myMaps.value = UiState.Success(result.data)
                }

                is BaseResult.Error -> {
                    _myMaps.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    fun selectMap(mapId: Long) {
        _selectedMapId.value = mapId
        viewModelScope.launch {
            _mapDetailState.value = UiState.Loading
            when (val result = gameRoomRepository.getMap(mapId)) {
                is BaseResult.Success -> {
                    _mapDetailState.value = UiState.Success(result.data)
                }

                is BaseResult.Error -> {
                    _mapDetailState.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    // 선택한 맵 적용 확인
    fun applySelectedMap() {
        val uiState = _mapDetailState.value
        if (uiState !is UiState.Success) {
            return
        }

        val mapDetail = uiState.data

        mapDetail.prison?.let { p ->
            locationRepository.setPrisonLocation(LatLng(p.lat, p.lng))
        }

        mapDetail.polygon?.let { points ->
            val latLngList = points.map { LatLng(it.lat, it.lng) }
            locationRepository.setPolygonPoints(latLngList)
        }

        _selectedMapId.value = null
        _mapDetailState.value = UiState.Idle
    }

    // 맵 선택 취소
    fun clearSelectedMap() {
        _selectedMapId.value = null
        _mapDetailState.value = UiState.Idle
    }
}