package io.truereactive.demo.core

data class Data<T>(
    val value: T,
    val state: State
)

sealed class State {
    object Default : State()
    object Loading : State()
    object Success : State()
    data class Error(
        val error: Throwable
    ) : State()
}

fun <T> T.toData(state: State = State.Default): Data<T> = Data(this, state)