package org.hyperskill.photoeditor

import android.graphics.drawable.BitmapDrawable
import org.hyperskill.photoeditor.internals.PhotoEditorUnitTest
import org.junit.FixMethodOrder
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner



// version 2.0
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner::class)
class Stage5UnitTestB : PhotoEditorUnitTest<MainActivity>(MainActivity::class.java) {

    companion object {
        const val messageNullAfterFilters = "Image was null after filters been applied"
        const val messageWrongValues = "Wrong values after filters been applied."
        const val marginError = 3
        const val calculationWaitTime = 600L
    }

    @Test
    @Ignore
    fun test01_checkSliderSaturation() {
        testActivity {
            slSaturation
        }
    }

    @Test
    @Ignore
    fun test02_checkSliderGamma() {
        testActivity {
            slGamma
        }
    }

    @Test
    @Ignore
    fun test03_checkSliderSaturationNotCrashingByDefault() {
        testActivity {
            ivPhoto
            slSaturation.value += slSaturation.stepSize
            slSaturation.value -= slSaturation.stepSize
            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()
            (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
        }
    }

    @Test
    @Ignore
    fun test04_checkSliderGammaNotCrashingByDefault() {
        testActivity {
            ivPhoto
            slGamma.value += slGamma.stepSize
            slGamma.value -= slGamma.stepSize
            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()
            (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
        }
    }

    @Test
    @Ignore
    fun test05_checkSaturationOnlyWithHints() {
        testActivity {
            slSaturation
            ivPhoto
            val expectedRgb = Triple(114, 138, 146)

            slSaturation.value -=  3 * slSaturation.stepSize

            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            val actualRgb = singleColor(actualImage, 70, 60)
            assertColorsValues("$messageWrongValues For x=70, y=60, avgBrightness=129, cAlpha=1.0, rgbAvg=133, sAlpha=0.7894",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    fun test06_checkSaturationOnlyWithoutHints() {
        testActivity {
            slSaturation
            ivPhoto
            val expectedRgb = Triple(88, 146, 165)

            slSaturation.value += 8 * slSaturation.stepSize

            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            val actualRgb = singleColor(actualImage, 70, 60)
            assertColorsValues("$messageWrongValues For x=70, y=60",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    @Ignore
    fun test07_checkGammaOnlyWithHints() {
        testActivity {
            slGamma
            ivPhoto
            val expectedRgb = Triple(215, 226, 229)

            slGamma.value -= 4 * slGamma.stepSize

            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            val actualRgb = singleColor(actualImage, 70, 60)
            assertColorsValues("$messageWrongValues For x=70, y=60, avgBrightness=129, cAlpha=1.0, rgbAvg=133, sAlpha=1.0",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    fun test08_checkGammaOnlyWithoutHints() {
        testActivity {
            slGamma
            ivPhoto
            val expectedRgb = Triple(56, 86, 98)

            slGamma.value += 4 * slGamma.stepSize

            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            val actualRgb = singleColor(actualImage, 70, 60)
            assertColorsValues("$messageWrongValues For x=70, y=60",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    fun test09_checkFiltersAppliedInOrder() {
        testActivity {
            slBrightness
            slContrast
            slSaturation
            slGamma
            ivPhoto
            val expectedRgb = Triple(36, 208, 246)


            slBrightness.value += slBrightness.stepSize
            slContrast.value += slContrast.stepSize * 4
            slContrast.value += slContrast.stepSize
            slSaturation.value += slSaturation.stepSize * 10
            slSaturation.value += slSaturation.stepSize * 5
            slGamma.value -= slGamma.stepSize * 2

            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            val actualRgb = singleColor(actualImage, 70, 60)
            assertColorsValues("$messageWrongValues For x=70, y=60",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    fun test10_checkFiltersAppliedInReverseOrder() {
        testActivity {
            slBrightness
            slContrast
            slSaturation
            slGamma
            ivPhoto
            val expectedRgb = Triple(71, 122, 186)

            slGamma.value += slGamma.stepSize * 5
            slSaturation.value += slSaturation.stepSize * 5
            slContrast.value -= slContrast.stepSize
            slBrightness.value += slBrightness.stepSize
            slBrightness.value += slBrightness.stepSize

            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            val actualRgb = singleColor(actualImage, 90, 80)
            assertColorsValues("$messageWrongValues For x=90, y=80",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    fun test11_checkFiltersAppliedInReverseOrderWiderSample() {
        testActivity {
            slBrightness
            slContrast
            slSaturation
            slGamma
            ivPhoto

            val sample = listOf(
                15 to 41,
                30 to 98,
                150 to 42,
                97 to 20,
                11 to 50,
                191 to 4,
                5 to 62,
                73 to 90,
                1 to 0,
                39 to 80
            )
            val expected = listOf(
                Triple(0, 180, 255),
                Triple(0, 255, 242),
                Triple(0, 99, 255),
                Triple(223, 0, 223),
                Triple(0, 206, 255),
                Triple(143, 0, 255),
                Triple(0, 240, 255),
                Triple(0, 255, 255),
                Triple(0, 110, 255),
                Triple(0, 255, 225)
            )


            slContrast.value += slContrast.stepSize * 5
            slBrightness.value += slBrightness.stepSize
            slGamma.value -= slGamma.stepSize * 2
            slSaturation.value += slSaturation.stepSize * 15


            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                messageNullAfterFilters)
            for (i in sample.indices) {
                val point = sample[i]
                val actual = singleColor(actualImage, point.first, point.second)
                assertColorsValues("$messageWrongValues For x=${point.first}, y=${point.second}",
                    expected[i], actual, marginError
                )
            }
        }
    }
}