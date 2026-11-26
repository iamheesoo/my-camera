package com.heesoo.my_camera.presentation.screen.frameselection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.heesoo.my_camera.R
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.Effect
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.Event

@Composable
fun FrameSelectionScreen(
    viewModel: FrameSelectionViewModel,
    listener: FrameSelectionListener,
) {
    LaunchedEffect(viewModel.effect, listener) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.GoToCapture -> listener.goToCapture()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { viewModel.sendEvent(Event.ClickStartCapture) }) {
            Text(stringResource(R.string.start_capture))
        }
    }
}
