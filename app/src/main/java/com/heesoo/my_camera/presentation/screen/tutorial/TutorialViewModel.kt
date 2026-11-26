package com.heesoo.my_camera.presentation.screen.tutorial

import androidx.lifecycle.viewModelScope
import com.heesoo.my_camera.core.mvi.BaseMviViewModel
import com.heesoo.my_camera.domain.usecase.CompleteTutorialUseCase
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.Effect
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.Event
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
internal class TutorialViewModel @Inject constructor(
    private val completeTutorialUseCase: CompleteTutorialUseCase,
) : BaseMviViewModel<State, Event, Effect>(
    initialState = State(isCompleting = false, hasCompletionError = false),
) {
    override fun handleEvent(event: Event) {
        when (event) {
            Event.ClickStart -> completeTutorial()
        }
    }

    private fun completeTutorial() {
        if (!state.value.isCompleting) {
            setState { copy(isCompleting = true, hasCompletionError = false) }
            viewModelScope.launch {
                try {
                    completeTutorialUseCase()
                    sendEffect(Effect.GoToFrameSelection)
                } catch (_: IOException) {
                    setState { copy(isCompleting = false, hasCompletionError = true) }
                }
            }
        }
    }
}
