package io.truereactive.core.reactiveui

import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber

fun <T> Observable<T>.logLifecycle(tag: String): Observable<T> = this.logSubscription(tag)
    .logEmissions(tag)
    .logCompletion(tag)
    .logDisposal(tag)
    .logError(tag)

fun <T> Single<T>.logLifecycle(tag: String): Single<T> = this.logSubscription(tag)
    .logEmissions(tag)
    .logDisposal(tag)
    .logError(tag)

fun <T> Maybe<T>.logLifecycle(tag: String): Maybe<T> = this.logSubscription(tag)
    .logEmissions(tag)
    .logCompletion(tag)
    .logDisposal(tag)
    .logError(tag)

fun <T> Observable<T>.logEmissions(tag: String): Observable<T> = this.doOnNext { printEmit(tag, it) }
fun <T> Single<T>.logEmissions(tag: String) = this.doOnSuccess { printEmit(tag, it) }
fun <T> Maybe<T>.logEmissions(tag: String) = this.doOnSuccess { printEmit(tag, it) }

fun <T> Observable<T>.logSubscription(tag: String): Observable<T> = this.doOnSubscribe { printSubscription(tag) }
fun <T> Single<T>.logSubscription(tag: String) = this.doOnSubscribe { printSubscription(tag) }
fun <T> Maybe<T>.logSubscription(tag: String) = this.doOnSubscribe { printSubscription(tag) }

fun <T> Observable<T>.logDisposal(tag: String): Observable<T> = this.doOnDispose { printDisposal(tag) }
fun <T> Single<T>.logDisposal(tag: String) = this.doOnDispose { printDisposal(tag) }
fun <T> Maybe<T>.logDisposal(tag: String) = this.doOnDispose { printDisposal(tag) }

fun <T> Observable<T>.logCompletion(tag: String): Observable<T> = this.doOnComplete { printComplete(tag) }
fun <T> Maybe<T>.logCompletion(tag: String) = this.doOnComplete { printComplete(tag) }

fun <T> Observable<T>.logError(tag: String): Observable<T> = this.doOnError { printError(tag, it) }
fun <T> Single<T>.logError(tag: String) = this.doOnError { printError(tag, it) }
fun <T> Maybe<T>.logError(tag: String) = this.doOnError { printError(tag, it) }

fun <T> printEmit(tag: String, value: T) {
    Timber.i("$tag emit: $value")
}

fun printSubscription(tag: String) {
    Timber.i("$tag subscribed")
}

fun printDisposal(tag: String) {
    Timber.i("$tag disposed")
}

fun printComplete(tag: String) {
    Timber.i("$tag completed")
}

fun printError(tag: String, err: Throwable) {
    Timber.e("$tag error: $err")
}