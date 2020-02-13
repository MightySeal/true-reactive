package io.truereactive.demo.flickr.common.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest.Builder
import android.os.Build
import android.os.HandlerThread
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkStateRepository @Inject constructor(
    private val context: Context
) {

    private val connectivityThread by lazy {
        HandlerThread(CONNECTIVITY_THREAD).apply { start() }
    }

    val observable by lazy {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        callbackFlow<Boolean> {
            val callback = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    offer(true)
                }

                override fun onLost(network: Network) {
                    offer(false)
                }

                override fun onUnavailable() {
                    offer(false)
                }
            }

            awaitClose {
                cm.unregisterNetworkCallback(callback)
            }

            val networkRequest = Builder().build()
            cm.registerNetworkCallback(networkRequest, callback)

        }.onStart { emit(getCurrentStategetCurrentState(cm)) }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Main)
        // .replay(1)
        // .refCount()
        // .subscribeOn(looperScheduler)
        // .observeOn(Schedulers.computation())
    }

    private fun getCurrentStategetCurrentState(connectivityManager: ConnectivityManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                ?.let {
                    it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                            || it.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                }

        } else {
            connectivityManager.activeNetworkInfo?.isConnected
        } ?: false
    }

    companion object {
        private const val CONNECTIVITY_THREAD = "connectivity_thread"
    }
}