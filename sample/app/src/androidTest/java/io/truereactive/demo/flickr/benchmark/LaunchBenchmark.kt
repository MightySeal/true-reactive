package io.truereactive.demo.flickr.benchmark

import android.content.Context
import android.content.Intent
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private lateinit var context: Context
    private lateinit var device: UiDevice
    private lateinit var launchIntent: Intent

    @Before
    fun setup() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        context = ApplicationProvider.getApplicationContext<Context>()

        device = UiDevice.getInstance(instrumentation)
        device.pressHome()

        launchIntent = context.packageManager.getLaunchIntentForPackage(SAMPLE_PACKAGE)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }!!

        device.wait(Until.hasObject(By.pkg(SAMPLE_PACKAGE).depth(0)), LAUNCH_TIMEOUT)
    }

    @Test
    fun startActivityFromMainScreen() = benchmarkRule.measureRepeated {
        context.startActivity(launchIntent)
        device.wait<UiObject2>(Until.findObject(By.clazz(RecyclerView::class.java)), 3000)

    }

    companion object {
        private const val SAMPLE_PACKAGE = "io.truereactive.demo.flickr"
        private val LAUNCH_TIMEOUT = 5000L
    }
}
