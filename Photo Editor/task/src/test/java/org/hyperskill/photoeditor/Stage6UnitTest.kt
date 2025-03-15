package org.hyperskill.photoeditor

import android.graphics.drawable.BitmapDrawable
import org.hyperskill.photoeditor.internals.PhotoEditorUnitTest
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner


/*
* Message for maintainers:
* Tests are testing use of asynchronous code by assuming asynchronous code will run only after
* calls to shadowLooper.runToEndOfTasks().
* So first assertion is done on values stored before calling runToEndOfTasks, which should capture
* solutions that are not using asynchronous code. These values should have before filter applied values.
* Later assertions are assertion with filters already applied intended to capture incorrect calculations.
* */
// version 2.1
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner::class)
class Stage6UnitTest : PhotoEditorUnitTest<MainActivity>(MainActivity::class.java) {

    companion object {
        const val messageNullAfterFilters = "Image was null after filters been applied"
        const val messageWrongValues = "Wrong values after filters been applied."
        const val messageSynchronousCode = "Are your filters being applied asynchronously?"
        const val marginError = 3
        const val calculationWaitTime = 600L
    }


    @Test
    fun test01_checkHighBrightnessValue() {
        testActivity {
            slBrightness
            val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
            val initialRgb = singleColor(initialImage, 70, 60)

            slBrightness.value += 120
            Thread.sleep(calculationWaitTime)
            val actualImage0 =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualRgb0 = singleColor(actualImage0, 70, 60)

            shadowLooper.runToEndOfTasks()
            assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)


            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val expectedRgb = Triple(230, 255, 255)
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
    fun test02_checkSomeContrastValue() {
        testActivity {
            slContrast
            val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
            val initialRgb = singleColor(initialImage, 70, 60)

            slContrast.value += 100
            Thread.sleep(calculationWaitTime)
            val actualImage0 =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualRgb0 = singleColor(actualImage0, 70, 60)

            shadowLooper.runToEndOfTasks()
            assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val expectedRgb = Triple(85, 154, 177)
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
    fun test03_checkSomeSaturationValue() {
        testActivity {
            slSaturation
            val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
            val initialRgb = singleColor(initialImage, 70, 60)

            slSaturation.value += 80
            Thread.sleep(calculationWaitTime)
            val actualImage0 =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualRgb0 = singleColor(actualImage0, 70, 60)

            shadowLooper.runToEndOfTasks()
            assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val expectedRgb = Triple(88, 146, 165)
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
    fun test04_checkSomeGammaValue() {
        testActivity {
            slGamma
            val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
            val initialRgb = singleColor(initialImage, 70, 60)


            slGamma.value += 4 * slGamma.stepSize

            Thread.sleep(calculationWaitTime)
            val actualImage0 =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualRgb0 = singleColor(actualImage0, 70, 60)

            shadowLooper.runToEndOfTasks()
            assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val expectedRgb = Triple(56, 86, 98)
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
    fun test05_checkDefaultBitmapEdit() {
        testActivity {
            slBrightness
            slContrast
            slSaturation
            slGamma
            val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
            val initialRgb = singleColor(initialImage, 70, 60)

            slBrightness.value += slBrightness.stepSize
            slContrast.value += slContrast.stepSize * 4
            slContrast.value += slContrast.stepSize
            slSaturation.value += slSaturation.stepSize * 10
            slSaturation.value += slSaturation.stepSize * 5
            slGamma.value -= slGamma.stepSize * 2

            Thread.sleep(calculationWaitTime)
            val actualImage0 =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualRgb0 = singleColor(actualImage0, 70, 60)

            shadowLooper.runToEndOfTasks()
            assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val expectedRgb = Triple(36, 208, 246)
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
    fun test06_checkDefaultBitmapEdit2() {
        testActivity {
            slBrightness
            slContrast
            slSaturation
            slGamma
            val initialImage = (ivPhoto.drawable as BitmapDrawable).bitmap
            val initialRgb = singleColor(initialImage, 70, 60)

            slGamma.value += slGamma.stepSize * 5
            slSaturation.value += slSaturation.stepSize * 5
            slBrightness.value += slBrightness.stepSize
            slContrast.value -= slContrast.stepSize
            slBrightness.value += slBrightness.stepSize

            Thread.sleep(calculationWaitTime)
            val actualImage0 =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualRgb0 = singleColor(actualImage0, 70, 60)

            shadowLooper.runToEndOfTasks()
            assertColorsValues(messageSynchronousCode, initialRgb, actualRgb0, marginError)

            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val expectedRgb = Triple(71, 122, 186)
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
    fun test07_checkDefaultBitmapEditWiderSample() {
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
            val expectedBefore = listOf(
                Triple(55, 121, 176),
                Triple(70, 178, 148),
                Triple(90, 122, 212),
                Triple(137, 100, 137),
                Triple(51, 130, 181),
                Triple(131, 84, 215),
                Triple(45, 142, 187),
                Triple(113, 170, 183),
                Triple(41, 80, 121),
                Triple(79, 160, 139)
            )

            val expectedAfter = listOf(
                Triple(0, 180, 255),
                Triple(0, 255, 242),
                Triple(0, 99, 255),
                Triple(223, 0, 223),
                Triple(0, 206, 255),
                Triple(143, 0, 255),
                Triple(0, 240, 255),
                Triple(0, 255, 255),
                Triple(0, 110, 255),
                Triple(0, 255, 225),
            )

            slBrightness.value += slBrightness.stepSize
            slContrast.value += slContrast.stepSize * 4
            slContrast.value += slContrast.stepSize
            slSaturation.value += slSaturation.stepSize * 10
            slSaturation.value += slSaturation.stepSize * 5
            slGamma.value -= slGamma.stepSize * 2
            Thread.sleep(calculationWaitTime)

            val actualBeforeImage =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            val actualBeforeImageImmutable = actualBeforeImage.copy(actualBeforeImage.config!!, false)
            shadowLooper.runToEndOfTasks()
            for (i in sample.indices) {
                val point = sample[i]
                val actual = singleColor(actualBeforeImageImmutable, point.first, point.second)
                assertColorsValues("$messageSynchronousCode For x=${point.first}, y=${point.second}",
                    expectedBefore[i], actual, marginError
                )
            }

            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualAfterImage =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterFilters)
            for (i in sample.indices) {
                val point = sample[i]
                val actual = singleColor(actualAfterImage, point.first, point.second)
                assertColorsValues("$messageWrongValues For x=${point.first}, y=${point.second}",
                    expectedAfter[i], actual, marginError
                )
            }
        }
    }
}