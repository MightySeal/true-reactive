package com.github.rbusarow

/*
 * Copyright 2016-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

// package kotlinx.coroutines.flow.internal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.broadcast
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

internal fun <T> Flow<T>.asCachedFlow(
    cacheHistory: Int
): Flow<T> {

    require(cacheHistory > 0) { "cacheHistory parameter must be greater than 0, but was $cacheHistory" }

    val cache = CircularArray<T>(cacheHistory)

    return onEach { value ->
        // While flowing, also record all values in the cache.
        cache.add(value)
    }.onStart {
        // Before emitting any values in sourceFlow,
        // emit any cached values starting with the oldest.
        cache.forEach { emit(it) }
    }
}

internal fun <T> Flow<T>.asSharedFlow(
    scope: CoroutineScope, cacheHistory: Int, tag: String? = null
): Flow<T> = SharedFlow(this, scope, cacheHistory, tag)

/**
 * An auto-resetting [broadcast] flow.  It tracks the number of active collectors, and automatically resets when
 * the number reaches 0.
 *
 * `SharedFlow` has an optional [cache], where the latest _n_ elements emitted by the source Flow will be replayed to
 * late collectors.
 *
 * ### Upon reset
 * 1) The underlying [BroadcastChannel] is closed. A new BroadcastChannel will be created when a new collector starts.
 * 2) The cache is reset.  New collectors will not receive values from before the reset, but will generate a new cache.
 */
internal class SharedFlow<T>(
    private val sourceFlow: Flow<T>,
    private val scope: CoroutineScope,
    private val cacheHistory: Int,
    private val tag: String? = null
) : AbstractFlow<T>() {
// ) : Flow<T> {

    private var refCount = 0
    private var cache = CircularArray<T>(cacheHistory)
    private val mutex = Mutex(false)

    init {
        require(cacheHistory >= 0) { "cacheHistory parameter must be at least 0, but was $cacheHistory" }
    }

    /*override suspend fun collectSafely(collector: FlowCollector<T>) {
        TODO("Not yet implemented")
    }*/

    // public override suspend fun collect(
    public override suspend fun collectSafely(
        collector: FlowCollector<T>
    ) = collector.emitAll(createFlow()).also {
        Timber.i("++++++++++ Start collecting $tag, $refCount")
    }

    // Replay happens per new collector, if cacheHistory > 0.
    private suspend fun createFlow(): Flow<T> = getChannel()
        .asFlow()
        .replayIfNeeded()
        .onCompletion { onCollectEnd() }

    private suspend fun getChannel(): BroadcastChannel<T> = mutex.withLock {
        refCount++
        lazyChannelRef.value
    }

    // lazy holder for the BroadcastChannel, which is reset whenever all collection ends
    private var lazyChannelRef = createLazyChannel()

    // must be lazy so that the broadcast doesn't begin immediately after a reset
    private fun createLazyChannel() = lazy(LazyThreadSafetyMode.NONE) {
        sourceFlow
            .cacheIfNeeded()
            .broadcastIn(scope)

        // scope.broadcast()
    }

    private fun Flow<T>.replayIfNeeded(): Flow<T> = if (cacheHistory > 0) {
        onStart {
            cache.forEach {
                emit(it)
            }
        }
    } else this

    private fun Flow<T>.cacheIfNeeded(): Flow<T> = if (cacheHistory > 0) {
        onEach { value ->
            // While flowing, also record all values in the cache.
            cache.add(value)
        }
    } else this

    private suspend fun onCollectEnd() = mutex.withLock { if (--refCount == 0) reset() }.also {
        Timber.i("++++++++++ Stop collecting $tag, $refCount")
    }

    private fun reset() {
        cache = CircularArray(cacheHistory)

        Timber.i("++++++++++ Reset $tag")

        lazyChannelRef.value.cancel()
        lazyChannelRef = createLazyChannel()
    }
}