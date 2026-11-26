package com.heesoo.my_camera.presentation.screen.frameselection

import com.heesoo.my_camera.core.mvi.UiEffect
import com.heesoo.my_camera.core.mvi.UiEvent
import com.heesoo.my_camera.core.mvi.UiState

object FrameSelectionContract {
    data object State : UiState

    sealed interface Event : UiEvent {
        data object ClickStartCapture : Event
    }

    sealed interface Effect : UiEffect {
        data object GoToCapture : Effect
    }
}
