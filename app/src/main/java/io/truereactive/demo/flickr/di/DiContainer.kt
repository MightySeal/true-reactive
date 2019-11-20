package io.truereactive.demo.flickr.di

import kotlin.reflect.KClass

interface DiContainer {
}

class AppDiContainer : DiContainer {

    private val map = mutableMapOf<KClass<*>, Any>()


}