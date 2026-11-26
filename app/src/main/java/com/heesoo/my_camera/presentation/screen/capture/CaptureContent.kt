package com.heesoo.my_camera.presentation.screen.capture

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heesoo.my_camera.R
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Effect
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Phase
import com.heesoo.my_camera.presentation.screen.capture.composable.CaptureResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val FLASH_PEAK_ALPHA = 0.65f
private const val FLASH_HOLD_MILLIS = 75L
private const val FLASH_FADE_MILLIS = 180

@Composable
internal fun CaptureContent(
    viewModel: CaptureViewModel,
    listener: CaptureListener,
    deviceActions: CaptureDeviceActions,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentContext by rememberUpdatedState(LocalContext.current)
    val currentListener by rememberUpdatedState(listener)
    val currentDeviceActions by rememberUpdatedState(deviceActions)
    val retakeSlotIndex = state.retakeSlotIndex
    val flashAlpha = remember(viewModel) { Animatable(0f) }

    BackHandler(enabled = state.isSaving) {}

    BackHandler(enabled = retakeSlotIndex != null) {
        viewModel.sendEvent(Event.ClickCancelRetake)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.RequestCameraPermission -> currentDeviceActions.requestCameraPermission()
                is Effect.TakePhoto -> currentDeviceActions.takePhoto(effect.requestId)
                Effect.CheckSavePermission -> currentDeviceActions.checkSavePermission()
                is Effect.GoToNextGuest -> currentListener.goToNextGuest(effect.frame)
                is Effect.ShowToast -> Toast.makeText(currentContext, effect.messageResId, Toast.LENGTH_SHORT).show()
                Effect.ShowCaptureFlash -> launch {
                    flashAlpha.snapTo(FLASH_PEAK_ALPHA)
                    delay(FLASH_HOLD_MILLIS)
                    flashAlpha.animateTo(targetValue = 0f, animationSpec = tween(FLASH_FADE_MILLIS))
                }
                Effect.GoToFrames -> currentListener.goToFrames()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().drawWithContent {
            drawContent()
            val alpha = flashAlpha.value
            if (alpha > 0f) {
                drawRect(color = Color.White, alpha = alpha)
            }
        },
    ) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.phase == Phase.COMPLETE -> {
                    val title = if (state.isSaved) R.string.save_photo_complete else R.string.capture_complete
                    Text(stringResource(title), style = MaterialTheme.typography.headlineMedium)
                    CaptureResult(
                        modifier = Modifier.weight(1f, fill = false),
                        frame = state.frame,
                        photoFileList = state.photoFileList,
                        selectedSlotIndex = state.selectedSlotIndex,
                        isSlotSelectionEnabled = !state.isSaving && !state.isSaved && state.retakesRemaining > 0,
                        onClickSlot = { slotIndex -> viewModel.sendEvent(Event.ClickSlot(slotIndex)) },
                        loadThumbnail = viewModel::loadThumbnail,
                    )
                    if (state.isSaved) {
                        Button(onClick = { viewModel.sendEvent(Event.ClickNextGuest) }) {
                            Text(stringResource(R.string.next_guest))
                        }
                    } else {
                        Text(stringResource(R.string.retakes_remaining, state.retakesRemaining))
                        state.messageResId?.let { messageResId -> Text(stringResource(messageResId)) }
                        Button(
                            onClick = { viewModel.sendEvent(Event.ClickSave) },
                            enabled = !state.isSaving,
                        ) {
                            Text(stringResource(if (state.isSaving) R.string.saving_photo else R.string.save_photo))
                        }
                        OutlinedButton(
                            onClick = { viewModel.sendEvent(Event.ClickStartRetake) },
                            enabled = !state.isSaving && state.retakesRemaining > 0,
                        ) {
                            Text(stringResource(R.string.start_retake))
                        }
                    }
                    OutlinedButton(
                        onClick = { viewModel.sendEvent(Event.ClickBackToFrames) },
                        enabled = !state.isSaving,
                    ) {
                        Text(stringResource(if (state.isSaved) R.string.change_frame else R.string.back_to_frames))
                    }
                }
                deviceActions.isPermissionGranted -> {
                    val progress = if (retakeSlotIndex != null) {
                        stringResource(R.string.retake_progress, retakeSlotIndex + 1)
                    } else {
                        stringResource(R.string.capture_progress, state.photoFileList.size + 1, state.frame.slotCount)
                    }
                    Text(
                        text = progress,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AndroidView(
                            factory = { deviceActions.preview },
                            modifier = Modifier.widthIn(max = 560.dp).aspectRatio(PhotoFrame.SLOT_ASPECT_RATIO),
                        )
                        CountdownOverlay(secondsFlow = viewModel.countdownSeconds)
                    }
                    state.messageResId?.let { messageResId -> Text(stringResource(messageResId)) }
                    if (state.phase == Phase.PAUSED && state.isCameraReady) {
                        Button(onClick = { viewModel.sendEvent(Event.ClickRetryCapture) }) {
                            Text(stringResource(R.string.retry_current_slot))
                        }
                    }
                }
                else -> {
                    Text(stringResource(R.string.camera_permission_needed))
                    Button(onClick = { viewModel.sendEvent(Event.ClickAllowCamera) }) {
                        Text(stringResource(R.string.allow_camera))
                    }
                }
            }

            if (retakeSlotIndex != null) {
                OutlinedButton(onClick = { viewModel.sendEvent(Event.ClickCancelRetake) }) {
                    Text(stringResource(R.string.cancel_retake))
                }
            }
        }
    }
}

@Composable
private fun CountdownOverlay(secondsFlow: StateFlow<Int?>) {
    val secondsRemaining by secondsFlow.collectAsStateWithLifecycle()
    secondsRemaining?.let { seconds ->
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
        ) {
            Text(
                text = stringResource(R.string.capture_countdown, seconds),
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                style = MaterialTheme.typography.displayLarge,
            )
        }
    }
}
