package com.d104.pnt.ui.game.end.news

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.R
import com.d104.pnt.data.repository.GameRepository
import com.d104.pnt.data.repository.GameSessionEvent
import com.d104.pnt.data.repository.GameSessionRepository
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.navigation.NavArgs
import com.d104.pnt.util.SoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsLoadingViewModel @Inject constructor(
    private val gameSessionRepository: GameSessionRepository,
    private val gameRepository: GameRepository,
    val soundPlayer: SoundPlayer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val gameId: Long = savedStateHandle.get<Long>(NavArgs.GAME_ID) ?: 0L
    private val _uiEvent = MutableSharedFlow<NewsLoadingUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private var newsReceived = false

    private val screenStartTime = System.currentTimeMillis()
    private val minimumDisplayTime = 7000L

    init {
        observeNewsSignal()
        startFallbackTimer()
    }

    private fun observeNewsSignal() {
        viewModelScope.launch {
            gameSessionRepository.eventFlow.collect { event ->
                if (event is GameSessionEvent.NavigateToNews) {
                    val targetGameId = if (event.gameId != 0L) event.gameId else gameId
                    fetchNewsContent(targetGameId, event.newsId)
                }
            }
        }
    }

    private fun startFallbackTimer() {
        viewModelScope.launch {
            delay(10000)
            if (!newsReceived) {
                newsReceived = true
                fetchNewsContent(gameId, 0L)
            }
        }
    }

    private suspend fun fetchNewsContent(gId: Long, nId: Long) {
        repeat(5) { attempt ->
            when (val result = gameRepository.getGameNews(gId)) {
                is BaseResult.Success -> {
                    ensureMinimumDisplayTime()

                    _uiEvent.emit(NewsLoadingUiEvent.NavigateToActualNews(gId, nId))
                    return@fetchNewsContent
                }

                is BaseResult.Error -> {
                    delay(2000)
                }
            }
        }
        ensureMinimumDisplayTime()
        _uiEvent.emit(NewsLoadingUiEvent.NavigateToActualNews(gId, nId))
    }

    private suspend fun ensureMinimumDisplayTime() {
        val elapsedTime = System.currentTimeMillis() - screenStartTime
        val remainingTime = minimumDisplayTime - elapsedTime
        if (remainingTime > 0) {
            delay(remainingTime)
        }
    }

    fun playNewsLoadingSound() {
        soundPlayer.playBgm(R.raw.news, tag = "news_loading", isLooping = false)
    }

    override fun onCleared() {
        super.onCleared()
    }
}

sealed class NewsLoadingUiEvent {
    data class NavigateToActualNews(val gameId: Long, val newsId: Long) : NewsLoadingUiEvent()
}
