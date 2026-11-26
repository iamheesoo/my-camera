package com.heesoo.my_camera.presentation.screen.tutorial

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heesoo.my_camera.R
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.Effect
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.Event

@Composable
internal fun TutorialScreen(viewModel: TutorialViewModel, listener: TutorialListener) {
    val currentListener by rememberUpdatedState(listener)
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.isCompleting) {}

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                Effect.GoToFrameSelection -> currentListener.goToFrameSelection()
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        val configuration = LocalConfiguration.current
        val screenModifier = Modifier.fillMaxSize().safeDrawingPadding().padding(
            horizontal = 24.dp,
            vertical = 16.dp,
        )
        if (configuration.screenWidthDp > configuration.screenHeightDp) {
            Row(
                modifier = screenModifier,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TutorialGuide(modifier = Modifier.weight(3f).fillMaxHeight())
                TutorialActions(
                    modifier = Modifier.weight(2f),
                    state = state,
                    onClickStart = { viewModel.sendEvent(Event.ClickStart) },
                )
            }
        } else {
            Column(
                modifier = screenModifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                TutorialGuide(
                    modifier = Modifier.weight(1f).widthIn(max = 480.dp).fillMaxWidth(),
                )
                TutorialActions(
                    modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth(),
                    state = state,
                    onClickStart = { viewModel.sendEvent(Event.ClickStart) },
                )
            }
        }
    }
}

@Composable
private fun TutorialGuide(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.tutorial_title),
            modifier = Modifier.padding(bottom = 8.dp).semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
        )
        TutorialStep(
            titleResId = R.string.tutorial_capture_title,
            descriptionResId = R.string.tutorial_capture_description,
        )
        TutorialStep(
            titleResId = R.string.tutorial_retake_title,
            descriptionResId = R.string.tutorial_retake_description,
        )
        TutorialStep(
            titleResId = R.string.tutorial_save_title,
            descriptionResId = R.string.tutorial_save_description,
        )
    }
}

@Composable
private fun TutorialActions(
    modifier: Modifier = Modifier,
    state: TutorialContract.State,
    onClickStart: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (state.hasCompletionError) {
            Text(
                text = stringResource(R.string.tutorial_completion_failed),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Button(
            onClick = onClickStart,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isCompleting,
        ) {
            Text(stringResource(if (state.isCompleting) R.string.tutorial_completing else R.string.tutorial_start))
        }
    }
}

@Composable
private fun TutorialStep(
    modifier: Modifier = Modifier,
    @StringRes titleResId: Int,
    @StringRes descriptionResId: Int,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = stringResource(titleResId), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(descriptionResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
