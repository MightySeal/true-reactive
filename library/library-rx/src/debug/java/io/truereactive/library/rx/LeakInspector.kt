package io.truereactive.library.rx

import io.truereactive.library.core.Optional
import shark.ObjectInspector

val optionalReporter = ObjectInspector { reporter ->
    reporter.whenInstanceOf(Optional::class) { instance ->

        val label = instance[Optional::class, "logKey"]!!
        reporter.labels.add("${label.name}: ${label.value.readAsJavaString()}")
    }
}