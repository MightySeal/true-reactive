package io.truereactive.library.core

import timber.log.Timber

object CustomCache {

    private val map = mutableMapOf<String, Any>()

    fun put(key: String, value: Any) {
        map[key] = value
        logState()
    }

    fun hasKey(key: String): Boolean = map.containsKey(key)

    fun get(key: String): Any {
        return map.getValue(key).also {
            logState()
        }
    }

    fun remove(key: String): Any? {
        return map.remove(key)?.also {
            logState()
        }
    }

    private fun logState() {
        Timber.d("Custom cache size: ${map.size}")
        if (map.isNotEmpty()) {
            Timber.d("Custom cache: ${map.values.joinToString { it::class.simpleName.toString() }}")
        }
    }
}