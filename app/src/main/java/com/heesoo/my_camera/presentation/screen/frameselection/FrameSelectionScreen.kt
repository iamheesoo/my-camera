package com.heesoo.my_camera.presentation.screen.frameselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heesoo.my_camera.R
import com.heesoo.my_camera.domain.model.PhotoFrame
import com.heesoo.my_camera.presentation.composable.frame.FrameLayout
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.Effect
import com.heesoo.my_camera.presentation.screen.frameselection.FrameSelectionContract.Event

@Composable
fun FrameSelectionScreen(
    viewModel: FrameSelectionViewModel,
    listener: FrameSelectionListener,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentListener by rememberUpdatedState(listener)

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.GoToCapture -> currentListener.goToCapture(effect.frame)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        val onClickFrame: (PhotoFrame) -> Unit = { frame ->
            viewModel.sendEvent(Event.ClickFrame(frame = frame))
        }
        val startButton: @Composable () -> Unit = {
            Button(onClick = { viewModel.sendEvent(Event.ClickStartCapture) }) {
                Text(stringResource(R.string.start_capture))
            }
        }
        val title: @Composable () -> Unit = {
            Text(stringResource(R.string.select_frame), style = MaterialTheme.typography.headlineMedium)
        }

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BoxWithConstraints(
                    modifier = Modifier.weight(3f).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    FrameOptions(
                        modifier = Modifier.widthIn(max = minOf(maxWidth, maxHeight * 0.86f)),
                        selectedFrame = state.selectedFrame,
                        onClickFrame = onClickFrame,
                    )
                }
                Column(
                    modifier = Modifier.weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    title()
                    startButton()
                }
            }
        } else {
            Column(
                modifier = Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                title()
                FrameOptions(
                    modifier = Modifier.padding(vertical = 24.dp).widthIn(max = 440.dp),
                    selectedFrame = state.selectedFrame,
                    onClickFrame = onClickFrame,
                )
                startButton()
            }
        }
    }
}

@Composable
private fun FrameOptions(
    modifier: Modifier = Modifier,
    selectedFrame: PhotoFrame,
    onClickFrame: (PhotoFrame) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PhotoFrame.entries.forEach { frame ->
            FrameOption(
                modifier = Modifier.weight(1f),
                frame = frame,
                isSelected = frame == selectedFrame,
                onClick = { onClickFrame(frame) },
            )
        }
    }
}

@Composable
private fun FrameOption(
    modifier: Modifier = Modifier,
    frame: PhotoFrame,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val label = when (frame) {
        PhotoFrame.STRIP -> R.string.frame_strip
        PhotoFrame.GRID -> R.string.frame_grid
    }
    Surface(
        modifier = modifier.selectable(
            selected = isSelected,
            role = Role.RadioButton,
            onClick = onClick,
        ),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(text = stringResource(label), style = MaterialTheme.typography.titleSmall)
            FrameLayout(frame = frame, slot = {})
        }
    }
}
