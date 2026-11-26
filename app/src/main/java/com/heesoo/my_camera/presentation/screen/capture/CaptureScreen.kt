package com.heesoo.my_camera.presentation.screen.capture

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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heesoo.my_camera.R
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Effect
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Event
import com.heesoo.my_camera.presentation.screen.capture.CaptureContract.Phase

@Composable
internal fun CaptureScreen(viewModel: CaptureViewModel, listener: CaptureListener) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect, listener) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.RequestCameraPermission -> listener.requestCameraPermission()
                is Effect.TakePhoto -> listener.takePhoto(effect.requestId)
                Effect.GoToFrames -> listener.goToFrames()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.safeDrawingPadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                state.phase == Phase.COMPLETE -> {
                    Text(stringResource(R.string.capture_complete), style = MaterialTheme.typography.headlineMedium)
                    Button(onClick = { viewModel.sendEvent(Event.ClickBackToFrames) }) {
                        Text(stringResource(R.string.back_to_frames))
                    }
                }
                listener.isPermissionGranted -> {
                    val currentSlot = state.photoFileList.size + 1
                    Text(
                        text = stringResource(R.string.capture_progress, currentSlot, state.frame.slotCount),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AndroidView(
                            factory = { listener.preview },
                            modifier = Modifier.widthIn(max = 560.dp).aspectRatio(4f / 3f),
                        )
                        if (state.phase == Phase.COUNTDOWN) {
                            Surface(
                                shape = MaterialTheme.shapes.large,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                            ) {
                                Text(
                                    text = stringResource(R.string.capture_countdown, state.secondsRemaining),
                                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.displayLarge,
                                )
                            }
                        }
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
        }
    }
}
