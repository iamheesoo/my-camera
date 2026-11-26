package com.heesoo.my_camera.presentation.screen.capture

import com.heesoo.my_camera.R
import com.heesoo.my_camera.core.mvi.BaseMviViewModel
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Effect
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor() :
    BaseMviViewModel<State, Event, Effect>(initialState = State()) {

    override fun handleEvent(event: Event) {
        when (event) {
            Event.ClickAllowCamera -> sendEffect(Effect.RequestCameraPermission)
            Event.ClickTakePhoto -> takePhoto()
            is Event.CameraAvailabilityChanged -> updateCameraAvailability(event.isAvailable)
            is Event.CaptureCompleted -> completeCapture(event.isSuccessful)
        }
    }

    private fun takePhoto() {
        val currentState = state.value
        if (currentState.isCameraReady && !currentState.isCapturing) {
            setState { copy(isCapturing = true, messageResId = null) }
            sendEffect(Effect.TakePhoto)
        }
    }

    private fun updateCameraAvailability(isAvailable: Boolean) {
        setState {
            copy(
                isCameraReady = isAvailable,
                isCapturing = false,
                messageResId = if (isAvailable) null else R.string.camera_unavailable,
            )
        }
    }

    private fun completeCapture(isSuccessful: Boolean) {
        setState {
            copy(
                isCapturing = false,
                messageResId = if (isSuccessful) R.string.test_photo_saved else R.string.test_photo_failed,
            )
        }
    }
}
