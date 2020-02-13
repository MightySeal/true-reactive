package io.truereactive.demo.core

class Data<T>(
    val value: T,
    val state: State
)

sealed class State {
    object Loading : State()
    object Success : State()
    data class Error(
        val error: Throwable
    ) : State()
}