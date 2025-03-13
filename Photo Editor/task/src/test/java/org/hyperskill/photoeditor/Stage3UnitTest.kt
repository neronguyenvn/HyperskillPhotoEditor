package org.hyperskill.photoeditor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import org.hyperskill.photoeditor.internals.CustomShadowBitmap
import org.hyperskill.photoeditor.internals.PhotoEditorUnitTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// version 2.0
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(RobolectricTestRunner::class)
class Stage3UnitTest : PhotoEditorUnitTest<MainActivity>(MainActivity::class.java) {

    @Test
    fun test01_checkButtonSave() {
        testActivity {
            btnSave
        }
    }

    @Test
    fun test02_checkPermissionWasAsked () {
        testActivity {
            checkPermissionWasAsked()
        }
    }

    @Test
    @Config(shadows = [CustomShadowBitmap::class])
    fun test03_checkBitmapIsSaved() {
        testActivity {
            CustomShadowBitmap.LastCompressed.init()
            val expectedUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
            val expectedFormat = Bitmap.CompressFormat.JPEG
            val expectedQuality = 100
            val expectedBitmap =
                (ivPhoto.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.RGB_565, false)
            val shadowContentResolver = shadowOf(activity.contentResolver)

            shadowActivity.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            btnSave.clickAndRun()

            val messageWrongUri = "The uri for saving the image is wrong"
            val actualUri = shadowContentResolver.insertStatements.last().uri
            assertEquals(messageWrongUri, expectedUri, actualUri)

            val messageWrongFormat = "The image saved had wrong format"
            val actualFormat = CustomShadowBitmap.LastCompressed.compressedFormat
            assertEquals(messageWrongFormat, expectedFormat, actualFormat)

            val messageWrongQuality = "The image saved had wrong quality"
            val actualQuality = CustomShadowBitmap.LastCompressed.compressedQuality
            assertEquals(messageWrongQuality, expectedQuality, actualQuality)

            val messageWrongBitmap =
                "Image saved is not the same as the image that was displaying before the click"
            val actualBitmap = CustomShadowBitmap.LastCompressed.compressedBitmap
            assertTrue(messageWrongBitmap, expectedBitmap.sameAs(actualBitmap))
        }
    }

    @Test
    @Config(shadows = [CustomShadowBitmap::class])
    fun test04_checkBitmapIsSavedAfterPermissionIsGranted() {
        testActivity {
            CustomShadowBitmap.LastCompressed.init()
            checkPermissionWasAsked()

            val expectedUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
            val expectedFormat = Bitmap.CompressFormat.JPEG
            val expectedQuality = 100
            val expectedBitmap =
                (ivPhoto.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.RGB_565, false)
            val shadowContentResolver = shadowOf(activity.contentResolver)

            shadowActivity.grantPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            activity.onRequestPermissionsResult(
                0,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), arrayOf(
                    PackageManager.PERMISSION_GRANTED).toIntArray()
            )
            shadowLooper.runToEndOfTasks()

            val messageAfterPermissionShouldSaveImage =
                "After the permission is granted the image should be saved without " +
                        "requiring additional clicks"
            val messageWrongUri = "The uri for saving the image is wrong"
            val actualUri = shadowContentResolver.insertStatements.lastOrNull()?.uri
                ?: throw AssertionError(messageAfterPermissionShouldSaveImage)
            assertEquals(messageWrongUri, expectedUri, actualUri)

            val messageWrongFormat = "The image saved had wrong format"
            val actualFormat = CustomShadowBitmap.LastCompressed.compressedFormat
            assertEquals(messageWrongFormat, expectedFormat, actualFormat)

            val messageWrongQuality = "The image saved had wrong quality"
            val actualQuality = CustomShadowBitmap.LastCompressed.compressedQuality
            assertEquals(messageWrongQuality, expectedQuality, actualQuality)

            val messageWrongBitmap =
                "Bitmap saved is not the same as the bitmap that was displaying before the click"
            val actualBitmap = CustomShadowBitmap.LastCompressed.compressedBitmap
            assertTrue(messageWrongBitmap, expectedBitmap.sameAs(actualBitmap))
        }
    }
}