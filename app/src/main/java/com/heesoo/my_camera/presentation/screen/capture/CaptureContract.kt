package com.heesoo.my_camera.presentation.screen.capture

import androidx.annotation.StringRes
import com.heesoo.my_camera.core.mvi.UiEffect
import com.heesoo.my_camera.core.mvi.UiEvent
import com.heesoo.my_camera.core.mvi.UiState

object CaptureContract {
    data class State(
        val isCameraReady: Boolean = false,
        val isCapturing: Boolean = false,
        @param:StringRes val messageResId: Int? = null,
    ) : UiState

    sealed interface Event : UiEvent {
        data object ClickAllowCamera : Event
        data object ClickTakePhoto : Event
        data class CameraAvailabilityChanged(val isAvailable: Boolean) : Event
        data class CaptureCompleted(val isSuccessful: Boolean) : Event
    }

    sealed interface Effect : UiEffect {
        data object RequestCameraPermission : Effect
        data object TakePhoto : Effect
    }
}
