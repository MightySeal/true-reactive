package io.truereactive.core.abstraction

import timber.log.Timber

object PresenterCache {

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

}