package io.truereactive.library.core

import java.util.*

interface ViewEvents

interface Renderer<M> {
    fun render(model: M)
}

sealed class ViewState(val name: String) {
    object Created : ViewState("Created")
    object Started : ViewState("Started")
    object Resumed : ViewState("Resumed")
    object Paused : ViewState("Paused")
    object Stopped : ViewState("Stopped")
    object SavingState : ViewState("SavingState")
    object Destroyed : ViewState("Destroyed")
    object Dead : ViewState("Dead")
}

val ViewState.isAlive: Boolean
    get() = when (this) {
        ViewState.Created,
        ViewState.Started,
        ViewState.Resumed -> true

        ViewState.Paused,
        ViewState.Stopped,
        ViewState.SavingState,
        ViewState.Destroyed,
        ViewState.Dead -> false
    }

fun sameAliveState(first: ViewState, second: ViewState): Boolean =
    !(first.isAlive xor second.isAlive)

data class AndroidModel<M, R>(
    val data: M,
    val restored: R
)

data class Optional<T>(
    val value: T?,
    val logKey: String = UUID.randomUUID().toString()
)