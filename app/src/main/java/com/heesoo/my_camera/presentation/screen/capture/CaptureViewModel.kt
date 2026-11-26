package com.heesoo.my_camera.presentation.screen.capture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.heesoo.my_camera.R
import com.heesoo.my_camera.core.mvi.BaseMviViewModel
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
class CaptureViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
    BaseMviViewModel<State, Event, Effect>(
        initialState = State(frame = savedStateHandle.toRoute<Route.Capture>().frame),
    ) {
    private var countdownJob: Job? = null
    private var nextRequestId = 0L
    private var activeRequestId: Long? = null

    override fun handleEvent(event: Event) {
        when (event) {
            Event.ClickAllowCamera -> sendEffect(Effect.RequestCameraPermission)
            Event.ClickRetryCapture -> retryCapture()
            Event.ClickBackToFrames -> sendEffect(Effect.GoToFrames)
            Event.CameraUnavailable -> showCameraUnavailable()
            is Event.CameraReadyChanged -> updateCameraReadiness(event.isReady)
            is Event.CaptureCompleted -> completeCapture(event)
        }
    }

    internal fun isActiveRequest(requestId: Long): Boolean = activeRequestId == requestId

    private fun updateCameraReadiness(isReady: Boolean) {
        setState { copy(isCameraReady = isReady) }
        val phase = state.value.phase
        if (isReady && phase == Phase.WAITING) {
            startCountdown()
        } else if (!isReady && (phase == Phase.COUNTDOWN || phase == Phase.TAKING_PHOTO)) {
            pauseCapture(R.string.capture_interrupted)
        }
    }

    private fun showCameraUnavailable() {
        setState { copy(isCameraReady = false) }
        pauseCapture(R.string.camera_unavailable)
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
            if (photoFile != null) {
                appendPhoto(photoFile)
            } else {
                pauseCapture(R.string.capture_failed)
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

    private fun pauseCapture(messageResId: Int) {
        countdownJob?.cancel()
        activeRequestId = null
        setState { copy(phase = Phase.PAUSED, secondsRemaining = 0, messageResId = messageResId) }
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
