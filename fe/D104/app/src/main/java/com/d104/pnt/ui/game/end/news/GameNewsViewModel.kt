package com.d104.pnt.ui.game.end.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.response.GameNewsResponse
import com.d104.pnt.data.repository.GameRepository
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.navigation.NavArgs
import com.d104.pnt.util.SoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameNewsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    val soundPlayer: SoundPlayer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val gameId: Long = savedStateHandle.get<Long>(NavArgs.GAME_ID) ?: 0L

    private val _newsState = MutableStateFlow<UiState<GameNewsResponse>>(UiState.Loading)
    val newsState: StateFlow<UiState<GameNewsResponse>> = _newsState.asStateFlow()

    init {
        fetchGameNews()
    }

    private fun fetchGameNews() {
        if (gameId == 0L) return

        viewModelScope.launch {
            _newsState.value = UiState.Loading

            // 뉴스 API 호출
            when (val result = gameRepository.getGameNews(gameId)) {
                is BaseResult.Success -> {
                    _newsState.value = UiState.Success(result.data)
                }

                is BaseResult.Error -> {
                    _newsState.value = UiState.Error(result.error.message ?: "뉴스 로딩 실패")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.stopBgm("news_speaking")
    }
}