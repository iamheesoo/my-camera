package com.heesoo.my_camera.core.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

abstract class BaseMviViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>(
    initialState: State,
) : ViewModel() {
    private val mutableState = MutableStateFlow(initialState)
    private val effectChannel = Channel<Effect>(Channel.UNLIMITED)

    val state = mutableState.asStateFlow()
    val effect = effectChannel.receiveAsFlow()

    fun sendEvent(event: Event) = handleEvent(event)

    protected abstract fun handleEvent(event: Event)

    protected fun setState(update: State.() -> State) {
        mutableState.update(update)
    }

    protected fun sendEffect(effect: Effect) {
        effectChannel.trySend(effect)
    }
}
