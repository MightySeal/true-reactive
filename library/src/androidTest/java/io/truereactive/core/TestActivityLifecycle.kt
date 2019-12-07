package io.truereactive.core

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import io.truereactive.core.setup.activity.TestActivity
import org.junit.Test
import java.lang.ref.WeakReference

// TODO: Not working
class TestActivityLifecycle {

    @Test
    fun simple_test() {
        val activityScenario: ActivityScenario<TestActivity> =
            ActivityScenario.launch(TestActivity::class.java)

        activityScenario.moveToState(Lifecycle.State.STARTED)

        var activityRef: WeakReference<TestActivity>? = null
        activityScenario.onActivity {
            activityRef = WeakReference(it)
        }

        activityScenario.moveToState(Lifecycle.State.RESUMED)

        activityScenario.moveToState(Lifecycle.State.DESTROYED)

        assert(activityRef != null && activityRef?.get() == null)
    }
}