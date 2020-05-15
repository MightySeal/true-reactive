package io.truereactive.library.flow

import com.github.rbusarow.shareIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {

    runBlocking {

        val flow = flow<Int> {
            repeat(10) {
                emit(it)
                delay(500)
            }
        }.shareIn(this, 1)

        launch {
            println("---------- Launch 1")
            flow.collect {
                println("1: $it")
            }
        }

        delay(3000)

        launch {
            println("---------- Launch 2")
            flow.collect {
                println("2: $it")
            }
        }

    }
}