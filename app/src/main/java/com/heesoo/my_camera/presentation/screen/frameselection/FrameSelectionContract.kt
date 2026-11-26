package com.heesoo.my_camera.presentation.screen.frameselection

import com.heesoo.my_camera.core.mvi.UiEffect
import com.heesoo.my_camera.core.mvi.UiEvent
import com.heesoo.my_camera.core.mvi.UiState

import com.heesoo.my_camera.domain.model.PhotoFrame

object FrameSelectionContract {
    data class State(val selectedFrame: PhotoFrame = PhotoFrame.STRIP) : UiState

    sealed interface Event : UiEvent {
        data class ClickFrame(val frame: PhotoFrame) : Event
        data object ClickStartCapture : Event
    }

    sealed interface Effect : UiEffect {
        data class GoToCapture(val frame: PhotoFrame) : Effect
    }
}
