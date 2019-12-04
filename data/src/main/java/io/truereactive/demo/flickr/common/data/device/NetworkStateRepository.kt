package io.truereactive.demo.flickr.common.data.device

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest.Builder
import android.os.Build
import android.os.HandlerThread
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class NetworkStateRepository @Inject constructor(
    private val context: Context
) {

    private val looperScheduler by lazy {
        AndroidSchedulers.from(connectivityThread.looper)
    }

    private val connectivityThread by lazy {
        HandlerThread(CONNECTIVITY_THREAD).apply { start() }
    }

    val observable by lazy {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        Observable.create<Boolean> { emitter ->

            val callback = object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    emitter.onNext(true)
                }

                override fun onLost(network: Network) {
                    emitter.onNext(false)
                }

                override fun onUnavailable() {
                    emitter.onNext(false)
                }
            }

            emitter.setCancellable {
                cm.unregisterNetworkCallback(callback)
            }

            val networkRequest = Builder().build()
            cm.registerNetworkCallback(networkRequest, callback)

        }.startWith(getCurrentStategetCurrentState(cm))
            .distinctUntilChanged()
            .replay(1)
            .refCount()
            .subscribeOn(looperScheduler)
            .observeOn(Schedulers.computation())
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