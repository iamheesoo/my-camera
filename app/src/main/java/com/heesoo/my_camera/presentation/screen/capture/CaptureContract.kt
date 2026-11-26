package com.heesoo.my_camera.presentation.screen.capture

import androidx.annotation.StringRes
import com.heesoo.my_camera.core.mvi.UiEffect
import com.heesoo.my_camera.core.mvi.UiEvent
import com.heesoo.my_camera.core.mvi.UiState
import com.heesoo.my_camera.domain.model.PhotoFrame
import java.io.File

object CaptureContract {
    enum class Phase { WAITING, COUNTDOWN, TAKING_PHOTO, PAUSED, COMPLETE, RETAKE_WAITING }

    data class State(
        val frame: PhotoFrame,
        val photoFileList: List<File>,
        val phase: Phase,
        val retakeSlotIndex: Int?,
        val selectedSlotIndex: Int?,
        val retakesRemaining: Int,
        val isSaving: Boolean,
        val isSaved: Boolean,
        val isCameraReady: Boolean,
        @param:StringRes val messageResId: Int?,
    ) : UiState

    sealed interface Event : UiEvent {
        data object ClickAllowCamera : Event
        data class ClickSlot(val slotIndex: Int) : Event
        data object ClickStartRetake : Event
        data object ClickCancelRetake : Event
        data object ClickRetryCapture : Event
        data object ClickBackToFrames : Event
        data object ClickNextGuest : Event
        data object ClickSave : Event
        data class SavePermissionResult(val isGranted: Boolean) : Event
        data object CameraUnavailable : Event
        data class CameraReadyChanged(val isReady: Boolean) : Event
        data class CaptureCompleted(val requestId: Long, val photoFile: File?) : Event
    }

    sealed interface Effect : UiEffect {
        data object RequestCameraPermission : Effect
        data class TakePhoto(val requestId: Long) : Effect
        data object GoToFrames : Effect
        data class GoToNextGuest(val frame: PhotoFrame) : Effect
        data object CheckSavePermission : Effect
        data object ShowCaptureFlash : Effect
        data class ShowToast(@param:StringRes val messageResId: Int) : Effect
    }
}
