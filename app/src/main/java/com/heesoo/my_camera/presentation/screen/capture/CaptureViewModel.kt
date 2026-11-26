package com.heesoo.my_camera.presentation.screen.capture

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.heesoo.my_camera.R
import com.heesoo.my_camera.core.mvi.BaseMviViewModel
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.route.Route
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Effect
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Phase
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
internal class CaptureViewModel(
    frame: PhotoFrame,
    private val thumbnailDecoder: CaptureThumbnailDecoder,
) : BaseMviViewModel<State, Event, Effect>(initialState = State(frame = frame)) {

    @Inject
    constructor(
        savedStateHandle: SavedStateHandle,
        thumbnailDecoder: CaptureThumbnailDecoder,
    ) : this(
        frame = savedStateHandle.toRoute<Route.Capture>().frame,
        thumbnailDecoder = thumbnailDecoder,
    )

    private var countdownJob: Job? = null
    private var nextRequestId = 0L
    private var activeRequestId: Long? = null

    override fun handleEvent(event: Event) {
        when (event) {
            Event.ClickAllowCamera -> sendEffect(Effect.RequestCameraPermission)
            is Event.ClickSlot -> selectSlot(event.slotIndex)
            Event.ClickStartRetake -> startRetake()
            Event.ClickCancelRetake -> cancelRetake()
            Event.ClickRetryCapture -> retryCapture()
            Event.ClickBackToFrames -> sendEffect(Effect.GoToFrames)
            Event.CameraUnavailable -> showCameraUnavailable()
            is Event.CameraReadyChanged -> updateCameraReadiness(event.isReady)
            is Event.CaptureCompleted -> completeCapture(event)
        }
    }

    suspend fun loadThumbnail(photoFile: File): Bitmap? = thumbnailDecoder.decode(photoFile)

    fun isActiveRequest(requestId: Long): Boolean = activeRequestId == requestId

    private fun updateCameraReadiness(isReady: Boolean) {
        setState { copy(isCameraReady = isReady) }
        val phase = state.value.phase
        if (isReady && (phase == Phase.WAITING || phase == Phase.RETAKE_WAITING)) {
            startCountdown()
        } else if (!isReady && (phase == Phase.COUNTDOWN || phase == Phase.TAKING_PHOTO)) {
            handleCaptureFailure(R.string.capture_interrupted)
        }
    }

    private fun showCameraUnavailable() {
        setState { copy(isCameraReady = false) }
        if (state.value.phase != Phase.COMPLETE) {
            handleCaptureFailure(R.string.camera_unavailable)
        }
    }

    private fun selectSlot(slotIndex: Int) {
        val currentState = state.value
        if (currentState.phase == Phase.COMPLETE &&
            currentState.retakesRemaining > 0 &&
            slotIndex in currentState.photoFileList.indices
        ) {
            setState { copy(selectedSlotIndex = slotIndex) }
        }
    }

    private fun startRetake() {
        val currentState = state.value
        val slotIndex = currentState.selectedSlotIndex
        if (currentState.phase == Phase.COMPLETE &&
            currentState.retakesRemaining > 0 &&
            slotIndex != null &&
            slotIndex in currentState.photoFileList.indices
        ) {
            setState {
                copy(
                    phase = Phase.RETAKE_WAITING,
                    retakeSlotIndex = slotIndex,
                    isCameraReady = false,
                    messageResId = null,
                )
            }
        }
    }

    private fun cancelRetake() {
        if (state.value.retakeSlotIndex != null) {
            returnToResult()
        }
    }

    private fun retryCapture() {
        val currentState = state.value
        if (currentState.isCameraReady && currentState.phase == Phase.PAUSED) {
            startCountdown()
        }
    }

    private fun startCountdown() {
        setState { copy(phase = Phase.COUNTDOWN, secondsRemaining = COUNTDOWN_SECONDS, messageResId = null) }
        countdownJob = viewModelScope.launch {
            for (remaining in COUNTDOWN_SECONDS downTo 1) {
                setState { copy(secondsRemaining = remaining) }
                delay(ONE_SECOND_MILLIS)
            }
            val requestId = ++nextRequestId
            activeRequestId = requestId
            setState { copy(phase = Phase.TAKING_PHOTO, secondsRemaining = 0) }
            sendEffect(Effect.TakePhoto(requestId = requestId))
        }
    }

    private fun completeCapture(event: Event.CaptureCompleted) {
        val photoFile = event.photoFile
        if (event.requestId == activeRequestId) {
            activeRequestId = null
            val retakeSlotIndex = state.value.retakeSlotIndex
            when {
                photoFile == null -> handleCaptureFailure(R.string.capture_failed)
                retakeSlotIndex != null -> replacePhoto(slotIndex = retakeSlotIndex, photoFile = photoFile)
                else -> appendPhoto(photoFile)
            }
        } else {
            photoFile?.delete()
        }
    }

    private fun appendPhoto(photoFile: File) {
        setState { copy(photoFileList = photoFileList + photoFile) }
        val currentState = state.value
        if (currentState.photoFileList.size == currentState.frame.slotCount) {
            setState { copy(phase = Phase.COMPLETE) }
        } else {
            startCountdown()
        }
    }

    private fun replacePhoto(slotIndex: Int, photoFile: File) {
        val previousPhoto = state.value.photoFileList[slotIndex]
        setState {
            val updatedPhotoFileList = photoFileList.toMutableList().apply { this[slotIndex] = photoFile }
            copy(
                photoFileList = updatedPhotoFileList,
                phase = Phase.COMPLETE,
                retakeSlotIndex = null,
                selectedSlotIndex = null,
                retakesRemaining = retakesRemaining - 1,
                isCameraReady = false,
            )
        }
        previousPhoto.delete()
    }

    private fun handleCaptureFailure(messageResId: Int) {
        if (state.value.retakeSlotIndex != null) {
            returnToResult(messageResId)
        } else {
            cancelPendingCapture()
            setState { copy(phase = Phase.PAUSED, secondsRemaining = 0, messageResId = messageResId) }
        }
    }

    private fun returnToResult(messageResId: Int? = null) {
        cancelPendingCapture()
        setState {
            copy(
                phase = Phase.COMPLETE,
                retakeSlotIndex = null,
                isCameraReady = false,
                secondsRemaining = 0,
                messageResId = messageResId,
            )
        }
    }

    private fun cancelPendingCapture() {
        countdownJob?.cancel()
        activeRequestId = null
    }

    override fun onCleared() {
        activeRequestId = null
        state.value.photoFileList.forEach { photoFile -> photoFile.delete() }
        super.onCleared()
    }

    private companion object {
        const val COUNTDOWN_SECONDS = 5
        const val ONE_SECOND_MILLIS = 1_000L
    }
}
