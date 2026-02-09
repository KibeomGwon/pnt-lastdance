package com.d104.pnt.ui.game.play.mission

import androidx.lifecycle.ViewModel
import com.d104.pnt.data.repository.GameSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val gameSessionRepository: GameSessionRepository,
) : ViewModel() {
    fun uploadImage(image: File, missionId: Long) {
        gameSessionRepository.uploadMissionImage(image, missionId)
    }
}