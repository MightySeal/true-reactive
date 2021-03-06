package io.truereactive.library.flow

import timber.log.Timber

internal object PresenterCache {

    private val map = mutableMapOf<String, BasePresenter>()

    fun putPresenter(key: String, presenter: BasePresenter) {
        map[key] = presenter
        logState()
    }

    fun hasPresenter(key: String): Boolean = map.containsKey(key)

    fun getPresenter(key: String): BasePresenter {
        return map.getValue(key).also {
            logState()
        }
    }

    fun remove(key: String): BasePresenter {
        return (map.remove(key) as BasePresenter).also {
            logState()
        }
    }

    private fun logState() {
        Timber.d("Cache size: ${map.size}")
        if (map.isNotEmpty()) {
            Timber.d("Cache: ${map.values.joinToString { it::class.simpleName.toString() }}")
        }
    }

    fun log(): String = "\n" + map.entries.joinToString(separator = "\n", prefix = "\t\t\t") {
        "${it.key}, ${it.value::class.simpleName} - ${it.value.hashCode()}"
    }

}