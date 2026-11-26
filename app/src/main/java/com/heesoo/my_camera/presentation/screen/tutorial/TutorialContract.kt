package com.heesoo.my_camera.presentation.screen.tutorial

import com.heesoo.my_camera.core.mvi.UiEffect
import com.heesoo.my_camera.core.mvi.UiEvent
import com.heesoo.my_camera.core.mvi.UiState

internal object TutorialContract {
    data class State(
        val isCompleting: Boolean,
        val hasCompletionError: Boolean,
    ) : UiState

    sealed interface Event : UiEvent {
        data object ClickStart : Event
    }

    sealed interface Effect : UiEffect {
        data object GoToFrameSelection : Effect
    }
}
