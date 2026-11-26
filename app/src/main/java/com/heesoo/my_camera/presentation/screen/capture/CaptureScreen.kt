package com.heesoo.my_camera.presentation.screen.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
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

@Composable
internal fun CaptureScreen(viewModel: CaptureViewModel, listener: CaptureListener) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect, listener) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.RequestCameraPermission -> listener.requestCameraPermission()
                Effect.TakePhoto -> listener.takePhoto()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (listener.isPermissionGranted) {
            AndroidView(factory = { listener.preview }, modifier = Modifier.weight(1f))
            state.messageResId?.let { messageResId ->
                Text(stringResource(messageResId))
            }
            Button(
                onClick = { viewModel.sendEvent(Event.ClickTakePhoto) },
                enabled = state.isCameraReady && !state.isCapturing,
            ) {
                Text(stringResource(R.string.take_test_photo))
            }
        } else {
            Text(stringResource(R.string.camera_permission_needed))
            Button(onClick = { viewModel.sendEvent(Event.ClickAllowCamera) }) {
                Text(stringResource(R.string.allow_camera))
            }
        }
    }
}
