package org.hyperskill.photoeditor

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import org.hyperskill.photoeditor.internals.PhotoEditorUnitTest
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import kotlin.math.max
import kotlin.math.min

// version 2.0.1
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner::class)
class Stage2UnitTest : PhotoEditorUnitTest<MainActivity>(MainActivity::class.java) {

    companion object {
        const val messageNullAfterSlBrightness = "Image was null after slBrightness triggered"
        const val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"
        const val messageWrongValues = "Wrong values after brightness applied."
        const val marginError = 1
        const val calculationWaitTime = 200L
    }

    @Test
    fun test01_checkImageView() {
        testActivity {
            ivPhoto
        }
    }

    @Test
    fun test02_checkButtonGallery() {
        testActivity {
            btnGallery
        }
    }

    @Test
    fun test03_checkSliderBrightness() {
        testActivity {
            slBrightness
        }
    }

    @Test
    fun test04_checkSliderNotCrashing() {
        testActivity {
            ivPhoto
            slBrightness.value += slBrightness.stepSize
            slBrightness.value -= slBrightness.stepSize
            shadowLooper.runToEndOfTasks()
            val bitmap = (ivPhoto.drawable as BitmapDrawable?)?.bitmap
            assertNotNull(messageNullAfterSlBrightness, bitmap)
        }
    }

    @Test
    fun test05_checkDefaultBitmapEdit() {
        testActivity {
            slBrightness
            val initialImage =
                (ivPhoto.drawable as BitmapDrawable).bitmap // null checked on initialization
            val (initialRed, initialGreen, initialBlue) = singleColor(initialImage)

            val expectedRgb1 =
                Triple(initialRed + 110, initialGreen + 110, initialBlue + 105)
            val expectedRgb2 =
                Triple(initialRed - 110, initialGreen - 120, initialBlue - 120)

            slBrightness.value += slBrightness.stepSize * 5
            slBrightness.value += slBrightness.stepSize * 6
            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(
                messageNullAfterSlBrightness)
            val actualRgb1 = singleColor(actualImage1)
            assertColorsValues("$messageWrongValues For x=70, y=60",
                expectedRgb1,
                actualRgb1,
                marginError)

            slBrightness.value -= slBrightness.stepSize * 10
            slBrightness.value -= slBrightness.stepSize * 13
            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage2 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(
                messageNullAfterSlBrightness)
            val actualRgb2 = singleColor(actualImage2)
            assertColorsValues("$messageWrongValues For x=70, y=60",
                expectedRgb2,
                actualRgb2,
                marginError)
        }
    }

    @Test
    fun test06_checkLoadedBitmapEdit() {
        testActivity {
            ivPhoto
            slBrightness
            btnGallery.clickAndRun()
            shadowLooper.runToEndOfTasks()

            val activityStubResult: Intent = createGalleryPickActivityResultStub(activity)
            val actualIntent = shadowActivity.peekNextStartedActivityForResult()?.intent
                ?: throw AssertionError(messageIntentNotFound)

            val expectedIntent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            assertTrue(
                "Intent found was different from expected." +
                        " expected <$expectedIntent> actual <$actualIntent>",
                actualIntent.filterEquals(expectedIntent)
            )

            val messageNullAfterLoading = "Image was null after loading from gallery"
            shadowActivity.receiveResult(
                actualIntent,
                Activity.RESULT_OK,
                activityStubResult
            )
            shadowLooper.runToEndOfTasks()

            val initialImage =
                (ivPhoto.drawable as BitmapDrawable?)?.bitmap ?: throw AssertionError(
                    messageNullAfterLoading)
            val (initialRed, initialGreen, initialBlue) = singleColor(initialImage, 80, 90)
            val expectedRgb = Triple(initialRed + 50, initialGreen + 50, initialBlue + 50)

            slBrightness.value += slBrightness.stepSize * 3
            slBrightness.value += slBrightness.stepSize * 2
            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(
                messageNullAfterSlBrightness)
            val actualRgb = singleColor(actualImage, 80, 90)
            assertColorsValues("$messageWrongValues For x=80, y=90",
                expectedRgb,
                actualRgb,
                marginError)
        }
    }

    @Test
    fun test07_checkDefaultBitmapEditExhaustive() {
        testActivity {
            slBrightness
            val initialBitmap = (ivPhoto.drawable as BitmapDrawable).bitmap // null checked on initialization
            val initialImageImmutable = initialBitmap.copy(initialBitmap.config!!, false)

            slBrightness.value += slBrightness.stepSize * 15
            slBrightness.value -= slBrightness.stepSize * 4
            shadowLooper.runToEndOfTasks()
            Thread.sleep(calculationWaitTime)
            shadowLooper.runToEndOfTasks()

            val actualImage1 = (ivPhoto.drawable as BitmapDrawable).bitmap ?: throw AssertionError(
                messageNullAfterSlBrightness)

            for (x in 0 until initialImageImmutable.width) {
                for (y in 0 until initialImageImmutable.height) {
                    val (initialRed, initialGreen, initialBlue) = singleColor(initialImageImmutable, x, y)
                    val expectedRed = max(0, min(initialRed + 110, 255))
                    val expectedGreen = max(0, min(initialGreen + 110, 255))
                    val expectedBlue = max(0, min(initialBlue + 110, 255))
                    val expectedRgb1 = Triple(expectedRed, expectedGreen, expectedBlue)
                    val actualRgb1 = singleColor(actualImage1, x, y)
                    assertColorsValues("$messageWrongValues For x=$x, y=$y",
                        expectedRgb1,
                        actualRgb1,
                        marginError)
                }
            }
        }
    }
}