package org.hyperskill.photoeditor

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import org.hyperskill.photoeditor.internals.PhotoEditorUnitTest
import org.junit.Assert.*
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

// version 2.0
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner::class)
class Stage1UnitTest : PhotoEditorUnitTest<MainActivity>(MainActivity::class.java) {

    companion object {
        const val messageIntentNotFound = "No intent was found by tests. Have you launched an intent?"
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
    fun test03_checkButtonOpensGallery() {
        testActivity {
            btnGallery.clickAndRun()

            val expectedIntent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )

            val actualIntent = shadowActivity.nextStartedActivity
                ?: throw AssertionError(messageIntentNotFound)

            assertTrue(
                "Intent found was different from expected." +
                        " expected <$expectedIntent> actual <$actualIntent>",
                actualIntent.filterEquals(expectedIntent)
            )
        }
    }

    @Test
    fun test04_checkButtonLoadsImage() {
        testActivity {
            ivPhoto
            btnGallery.clickAndRun()

            val activityResult = createGalleryPickActivityResultStub(activity)
            val intent = shadowActivity.peekNextStartedActivityForResult()?.intent
                ?: throw AssertionError(messageIntentNotFound)

            shadowActivity.receiveResult(
                intent, Activity.RESULT_OK, activityResult
            )
            shadowLooper.runToEndOfTasks()

            val messageNullAfterLoading = "Image was null after loading from gallery"
            assertNotNull(messageNullAfterLoading, ivPhoto.drawable)

            val actualDrawableId: Int = try {
                // shadowOf(ivPhoto.drawable) can throw NullPointer if .setImageBitmap(null)
                shadowOf(ivPhoto.drawable).createdFromResId
            } catch (ex: NullPointerException) {
                throw AssertionError(messageNullAfterLoading)
            }

            assertEquals("Drawable loaded is different from expected.",
                R.drawable.myexample, actualDrawableId
            )
        }
    }
}