package com.heesoo.my_camera.presentation.screen.tutorial

import com.heesoo.my_camera.core.mvi.BaseMviViewModel
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.Effect
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.Event
import com.heesoo.my_camera.presentation.screen.tutorial.TutorialContract.State
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class TutorialViewModel @Inject constructor() :
    BaseMviViewModel<State, Event, Effect>(initialState = State) {

    override fun handleEvent(event: Event) {
        when (event) {
            Event.ClickStart -> goToFrameSelection()
        }
    }

    private fun goToFrameSelection() = sendEffect(Effect.GoToFrameSelection)
}
